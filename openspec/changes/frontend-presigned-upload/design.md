## Context

后端在 `FileUploadController` 已完整实现预签名直传三段式接口（`POST /file/prepare-upload`、`POST /file/confirm-upload`），`FileDomainServiceImpl` 负责生成预签名 URL、回调 Token、以及确认时的 MD5/大小/魔数校验。但前端 `file.service.ts` 仅封装了传统 `POST /file/upload`，所有 4 个上传调用点（报名头像、Bug 截图、考题附件、考核作品）均直接经过应用服务器转存到 OSS，大文件上传体验差且浪费带宽。

当前前端上传状态管理分散：各组件自行维护 `uploading`、`uploadProgress` 等状态，没有统一的上传生命周期抽象。

## Goals / Non-Goals

**Goals:**
- 前端接入后端预签名直传能力，大文件不再经过应用服务器
- 统一封装上传生命周期 Hook，4 个调用点复用同一套状态机
- 上传进度可视化拆分为 4 阶段（准备中 / 上传中 / 检查中 / 完成），用户可感知当前步骤
- 后端 `confirm-upload` 增加对已 `ACTIVE` 文件的幂等返回，支持网络超时后安全重试

**Non-Goals:**
- `prepare-upload` 本身不做幂等（多次调用仍产生多条 `PENDING` 记录，orphaned 记录靠后续清理）
- 不改动现有文件下载流程（仍走 302 重定向）
- 不改动机考/算法题等非文件上传题型的作答逻辑
- 不引入新的对象存储后端或修改存储路径规则

## Decisions

### 1. 使用 `usePresignedUpload` Hook 统一封装直传流程
**Rationale**: 4 个上传调用点的状态需求高度重合（进度、错误、重试），抽出 Hook 避免重复实现。Hook 内部维护 `UploadPhase` 状态机，对外暴露 `upload(file, type)` 和 `cancel()`。

**Alternatives considered**:
- 每个调用点自行编排 `prepareUpload` → `PUT` → `confirmUpload`：状态逻辑重复，维护成本高。
- 封装成高阶组件：不如 Hook 灵活，AntD Upload 的 `customRequest` 和 `beforeUpload` 都需要接入。

### 2. 分段进度条映射为 4 个伪区间
**Rationale**: 保持现有 `FileUploadArea` 组件布局不变，只改进度条数值映射逻辑。
- 准备中：`0% → 15%`（ fake progress，快速动画）
- 上传中：`15% → 85%`（真实 PUT 进度线性映射）
- 检查中：`85%` 停住，增加脉动/闪烁动画
- 完成：`100%`

**Alternatives considered**:
- 用 AntD Steps 组件：视觉改动大，和现有深色主题融合成本高。
- 4 段等宽物理分段：CSS 改动更多，单段连续进度更贴合用户心智模型。

### 3. PUT 到 OSS 使用原生 `XMLHttpRequest` 而非 axios
**Rationale**: 需要监听 `xhr.upload.onprogress` 获取真实上传进度。axios 虽然支持 `onUploadProgress`，但用于 PUT 到外部域名时可能受拦截器/默认配置干扰，原生 XHR 更可控。

**Alternatives considered**:
- fetch + ReadableStream：进度获取需要手动计算已读取字节，代码更冗长。

### 4. 前端 MD5 使用 `spark-md5` 库分片计算
**Rationale**: `confirm-upload` 需要前端提供 MD5。原生 `crypto.subtle.digest` 对超大文件会阻塞主线程。`spark-md5` 支持分片增量计算，结合 `requestIdleCallback` 或简单的 `setTimeout` 切片可避免 UI 卡顿。

**Alternatives considered**:
- Web Worker 计算 MD5：体验最好，但当前项目未配置 Worker loader，引入构建改动。后续可作为优化。

### 5. 后端 confirm-upload 幂等：已 ACTIVE 直接返回成功
**Rationale**: 这是让前端"检查中"阶段可安全重试的最小改动。Token（JWT）没有消费标记，拦住重试的是 `file.status != PENDING` 的硬校验。改为 `status == ACTIVE` 时直接返回相同响应，不改变安全模型（Token 仍然要验）。

**Alternatives considered**:
- 引入 Token 黑名单/消费标记：需要改表或引入 Redis，改动过大。
- 前端在收到 403 后查询文件状态再决定：后端没有开放"按 fileId 查状态"的接口。

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| 预签名 URL 15 分钟过期，上传中断后重试可能 URL 失效 | Hook 内部捕获 403/ExpiredToken 错误，自动重新走 `prepare-upload` 生成新 URL |
| 大文件 MD5 计算阻塞 UI | 使用 `spark-md5` 分片计算，每计算 10MB yield 一次事件循环；后续迭代可迁 Web Worker |
| 浏览器切后台导致 XHR 中断 | `uploading` 阶段监听 `visibilitychange`，切回前台后检测状态，若未完成且进度停止则自动重试 |
| 匿名上传场景（报名/Bug）的 `prepare-upload` 限流 | 前端透传后端 429 错误提示，"请求过于频繁，请稍后再试" |
| CORS 配置遗漏导致 PUT 到 OSS 失败 | 部署文档中补充检查项；前端错误提示明确告知"上传服务配置异常，请联系管理员" |
| 分段进度条是"假进度"，用户感知不准确 | 准备/检查阶段增加文字标签（"正在准备..."/"正在校验..."），降低对进度数值的绝对预期 |

## Migration Plan

1. **后端先行**：部署 `FileDomainServiceImpl.confirmUpload()` 的幂等修改（1 行逻辑 + 1 行返回）
2. **前端并行**：
   - 新增 `file.service.ts` 方法 + `usePresignedUpload` Hook
   - 逐个替换 4 个调用点（报名头像 → Bug 截图 → 考题附件 → 考核作品）
   - 每替换一个调用点在测试环境验证上传/重试/取消流程
3. **回滚**：若直传出现问题，前端回滚到 `fileService.upload()` 调用即可恢复传统上传；后端幂等修改不影响原有逻辑

## Open Questions

- 是否需要后端定时任务清理长期 `PENDING` 的 orphaned 文件记录？（当前没有，不影响本次变更）
- 是否需要对 `prepare-upload` 也做幂等（同一文件重复 prepare 只返回同一个 URL）？（本次不做，后续视 orphaned 记录增长情况决定）
