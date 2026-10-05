# Design: migrate-remaining-uploads-to-presigned

## Context

前端上传通道现状（unify-image-crop-upload 归档后）：

```
预签名直传 ✔（8 处消费方）          旧通道 fileService.upload ✘（6 处）
─────────────────────────────       ─────────────────────────────────────
AvatarUpload(AVATAR)                VenueDrawer(NORMAL_IMG)
ProfileSidebar(AVATAR)              EquipmentDrawer(NORMAL_IMG)
CompetitionDrawer(Logo)             CompetitionDrawer(cover) ← 上次有意保留
EnrollForm(AVATAR, 原实现)          QrcodeDrawer(QRCODE)
QuestionDetail / QuestionDrawer     ProfileInfo(QRCODE)
BugReport                           AchievementDrawer(NORMAL_IMG)
admin/enroll-form
```

这 6 处的共同特征：纯通道替换，无裁剪需求，文件类型/校验逻辑不变。每处现有代码形状统一：`try { const res = await fileService.upload(file, TYPE); if (res.code===200 && res.data) { setFileId(res.data.id); form.setFieldValue(...) } } catch ... finally { setUploading(false) }`。

## Goals / Non-Goals

**Goals:**
- 6 处全部迁移到 `usePresignedUpload`，前端删除 `fileService.upload`
- 文件类型、表单回填、UI 行为（loading 态）逐项保持不变
- 迁移后有明确的失败提示与状态复位

**Non-Goals:**
- 后端 `POST /file/upload` 接口（保留，仅前端弃用；是否下线另案）
- 知识库文档上传（域内业务 API，需后端配合，另案）
- 任何 UI/交互/校验变更；不引入裁剪

## Decisions

### D1: 每处直接调用 usePresignedUpload，不再抽象第二层 hook

上次变更抽象的 `useImageCropUpload` 解决的是"裁剪样板重复"。本次 6 处没有任何重复状态逻辑（各只有 `uploading` + fileId 回填，且已在各自组件内），直接 `const { upload } = usePresignedUpload()` 即可。再包一层（如 `useFileUpload`）只会多一次 Props drilling，无收益。

**替代方案**：抽 `useFileUpload(type)` 薄 hook——YAGNI，等出现第二处共享需求再说。

### D2: 失败处理语义转换

旧通道：`{code, msg}` 返回式；预签名：`upload()` 失败时 throw。迁移模板：

```ts
// before
const res = await fileService.upload(file, TYPE)
if (res.code === 200 && res.data) { setFileId(res.data.id); ... }
else { messageApi.error(`上传失败: ${res.msg}`) }

// after
try {
  const id = await upload(file, TYPE)
  if (id != null) { setFileId(id); ... } else { messageApi.error('上传失败') }
} catch {
  messageApi.error('上传失败，请稍后重试')
} finally {
  setUploading(false)
}
```

注意点：6 处现有代码都已有 try/catch/finally 骨架（扫描确认），迁移主要是把响应式判断换成返回值判断 + catch 分支，机械且低风险。

### D3: 成就附件维持 NORMAL_IMG 类型

`AchievementDrawer` 的 `FILE_TYPE` 当前是 `'NORMAL_IMG'`。虽语义上更像 ASSESSMENT/成就附件类型，但**本次不改类型**——桶路径与权限语义变更超出通道替换范围，避免迁移引入行为差异。遗留观察记入 Risks。

### D4: 后端接口保留

`POST /file/upload` 保留（对应 `unified-file-upload` spec 仍有效）。理由：后端删除涉及权限注解、测试、可能的第三方/存量客户端，且不属于"前端全面预签名"的目标。前端删方法后该接口自然成为无人调用的死接口，未来可由专门的后端清理变更下线。

## Risks / Trade-offs

- **成就附件类型语义遗留**（NORMAL_IMG vs 专属附件类型）→ 本次不动；将来若后端引入成就附件专属 FileType 再统一调整
- **预签名通道的上传体积**对场地/设备大图无 5MB 前端限制（旧通道也无）→ 行为一致，无回归；MD5 计算大文件时略有前端 CPU 开销，可接受
- **失败提示文案微调**（`上传失败: xxx` → `上传失败，请稍后重试`）→ 预签名通道的错误细节在 hook 内部已 message，此处仅兜底；可接受
- **回归面**：6 处均为管理端 + 个人资料页上传，E2E 覆盖登录后逐处上传验证 fileId 回填

## Migration Plan

纯前端，逐文件独立提交粒度建议：一次 commit 完成全部 6 处（改动同构、回归一起跑）。无数据迁移，无部署顺序要求。

## Open Questions

无。范围（含竞赛封面、不含知识库、后端接口保留）已与用户确认。
