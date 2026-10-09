## Context

考核答题界面 `src/frontend/src/components/Assessment/QuestionDetail/index.tsx` 的文件上传区当前存在三个问题：

1. **无格式限制**：`FileUploadContent.allowedExtensions` / `maxFileSize` 是前端预留字段（`assessment.dto.ts` 注释明确"后端未返回"），`accept` 为 `undefined`，`dropHintText` 回退为「所有文件格式」，实际任何文件可传。
2. **文件夹拖拽产生伪 0 字节文件**：Chromium 拖文件夹到 drop zone 时生成 name 无扩展名、size 为 0 的 File 对象，走完整上传流程后在 confirm 阶段失败，失败点不可预测。
3. **错误被吞**：`customRequest` 的 catch 统一弹「上传失败，请重试」，`usePresignedUpload` 产出的具体错误信息（`'文件校验未通过'`、`HTTP 409` 等）被丢弃。

本次为**纯前端修改**，后端不做格式校验，其他上传场景（BugReport、头像、管理端 Drawer）不在范围内。

## Goals / Non-Goals

**Goals:**
- 上传区仅接受常见压缩包格式，拦截发生在进入上传流程之前（`beforeUpload` / `onDrop`）
- 拖拽文件夹得到明确、可操作的提示（「不支持上传文件夹，请压缩后上传」）
- 上传失败时用户能看到具体原因而非笼统文案
- 提示文案与实际校验规则一致

**Non-Goals:**
- 后端扩展名/魔数层面的强制校验（恶意绕过前端不兜底，已接受此边界）
- `usePresignedUpload` hook 本身的重构（仅消费其 error.message）
- 其他页面的上传组件改造
- 题目级 allowedExtensions 的后端落地（仅在前端保留兼容逻辑）

## Decisions

### 决策 1：白名单常量定义在前端，包含常见压缩包类型

```ts
// QuestionDetail 模块内常量（或 utils.ts）
const ALLOWED_ARCHIVE_EXTENSIONS = ['zip', 'rar', '7z', 'tar', 'gz', 'tar.gz', 'bz2', 'xz']
```

- 匹配逻辑：先尝试整体后缀（处理 `tar.gz` 这类复合扩展名，取 `name.slice(name.lastIndexOf('.') + 1)` 之外，对 `.tar.gz` 特判），再回落到末段扩展名。
- 备选方案 A：复用现有 `fileContent.allowedExtensions` 字段——被否，因为后端从不返回该字段，会导致本次需求实际不生效；最终采用"题目配置优先、常量兜底"：若后端未来返回 `allowedExtensions` 则用之，否则用常量。这样既不破坏预留的接口契约，又让本次需求立即可用。
- 备选方案 B：白名单只放 `zip`、`rar`——被否，用户明确要求"包含常见的压缩包类型"。

### 决策 2：文件夹检测用 `webkitGetAsEntry`，放在 `onDrop`，带降级

```
onDrop(e):
  items = e.dataTransfer.items
  if (items && items[0].webkitGetAsEntry) {
    entry = items[0].webkitGetAsEntry()
    if (entry && entry.isDirectory) {
      e.preventDefault()
      message.error('不支持上传文件夹，请压缩后上传')
      return   // 阻止 antd 默认处理
    }
  }
  // 不支持该 API（Firefox）：走 beforeUpload 的扩展名 + 空文件兜底
```

- 放在 `onDrop` 而非 `beforeUpload`：`beforeUpload` 拿到的是浏览器生成的伪 File，无法区分"空文件"和"文件夹"；`webkitGetAsEntry` 只能访问原始 DataTransfer。
- 降级路径：Firefox 拖入文件夹 → 伪 File 无扩展名 → 白名单拦截（提示"仅支持压缩包格式"）；若用户拖入无扩展名的真实文件同理被拦，属可接受误伤。
- 补充：`beforeUpload` 增加 `file.size === 0` 的拒绝分支（提示文件内容为空），进一步缩小降级路径的漏网面。

### 决策 3：错误透传只改考题界面的 catch，不动 hook

```ts
// customRequest catch
} catch (error) {
  const msg = error instanceof Error && error.message ? error.message : '上传失败，请重试'
  message.error(msg)
  onError?.(error as Error)
}
```

- 被否方案：在 `usePresignedUpload` 内统一 message——hook 被多个页面共用，各页面提示风格不一，不适合在 hook 内弹窗；保持 hook 只抛错、调用方决定如何展示。
- 注意：hook 内部有 `cancel()` 时抛 `UPLOAD_ABORTED` 的分支，当前考题界面未暴露取消入口，但 catch 文案需排除这种内部错误码（若 message 为 `UPLOAD_ABORTED` 这类内部码则不弹出提示），避免未来加取消按钮后出现误报。

### 决策 4：`accept` 与提示文案由同一派生值生成

`accept` 属性、格式提示文本、`beforeUpload` 校验共用同一个 `effectiveAllowedExtensions`（题目配置 ?? 常量），避免多处硬编码不一致。

## Risks / Trade-offs

- [前端校验可被绕过（curl 直接调 prepareUpload）] → 已在提案中明确接受；后端 confirm 仍有 MD5/魔数/大小校验，风险可控，后续如需强制可在后端 WORK 类型加白名单（独立变更）。
- [`accept` 仅过滤文件选择器，不阻止拖拽] → 由 `onDrop` + `beforeUpload` 双重拦截覆盖拖拽路径。
- [Firefox 无法识别文件夹，提示为"仅支持压缩包格式"而非"不支持文件夹"] → 可接受：信息仍指明了正确做法（压缩后上传）；且现代 Firefox 对拖文件夹进 input 的行为依版本而异，伪文件路径本身有白名单兜底。
- [无扩展名真实文件被误拦] → 业务上考核作业本就要求压缩包提交，误伤面可忽略。
- [`UPLOAD_ABORTED` 等内部错误码透出] → catch 中对内部错误码做白名单过滤，不弹 message。

## Migration Plan

纯前端改动，随前端发版生效，无迁移与回滚成本（回滚即还原该组件 commit）。

## Open Questions

- 白名单是否需要包含 `tar.bz2`、`tar.xz` 等复合后缀？当前设计按末段扩展名匹配（`.tar.bz2` → `bz2`），天然覆盖，无需特判；若产品要求精确匹配再补。
