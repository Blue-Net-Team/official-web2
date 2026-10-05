# Design: unify-image-crop-upload

## Context

三个图片上传消费方现状：

| 消费方 | 裁剪 | 裁剪状态管理 | 上传通道 | 校验 |
|---|---|---|---|---|
| 报名页 `AvatarUpload` | ❌ 无，原图直传 | — | `usePresignedUpload`（预签名 ✔） | `image/*` + 2MB |
| 个人主页 `ProfileSidebar` | ✔ `ImageCropModal` round | 手写 ~40 行 | `fileService.upload`（旧通道 ✘） | jpg/png/gif/webp + 5MB |
| 竞赛 `CompetitionDrawer` logo | ✔ `ImageCropModal` rect | 手写 ~40 行 | `fileService.upload`（旧通道 ✘） | 组件内校验 |

底层组件已存在：`ImageCropModal`（react-easy-crop，支持 cropShape/aspect/outputSize，PNG 保留透明通道）、`usePresignedUpload`（MD5 + prepare-upload + PUT + confirm-upload，含重试/进度/取消）。约束：不改后端、不新增依赖。

## Goals / Non-Goals

**Goals:**
- 抽取 `useImageCropUpload` hook，消灭两处手写裁剪样板，让报名页零样板获得裁剪能力
- 三处文件校验口径统一：jpeg/png/webp 白名单 + 5MB，拒绝 GIF（裁剪会把 GIF 静默静态化）
- Profile 与竞赛 Logo 迁移到预签名直传通道
- 修复报名页 objectURL 不 revoke 的内存泄漏

**Non-Goals:**
- 其余旧通道消费方（VenueDrawer、EquipmentDrawer、QrcodeDrawer、AchievementDrawer、ProfileInfo 微信二维码）不迁移
- 不改动 `ImageCropModal`、`usePresignedUpload` 内部实现
- 前端无 vitest/jest 测试框架，hook 的验证依赖 lint/typecheck 与 Playwright E2E 三处链路回归，不新增测试基建
- 竞赛封面（cover）上传流程不变（spec 已有「竞赛封面上传不受影响」需求）

## Decisions

### D1: 复用单元是 hook 而非组件

**选择**：抽取 `useImageCropUpload`，而非一个包揽 UI 的"通用上传组件"。

**理由**：三个消费方的触发 UI 完全不同（报名页圆形虚线框、个人主页 hover 遮罩头像、竞赛表单 Logo 字段），无法共享 UI；但"选文件→校验→objectURL→裁剪→上传"这段状态逻辑完全一致，且是重复样板所在。hook 是这段逻辑的精确复用边界，`AvatarUpload` 自包含的目标通过"AvatarUpload 内部调用 hook"达成，两者不矛盾。

**替代方案**：通用 `CropUpload` 组件（props 配置触发 UI）——配置面爆炸，trigger 渲染函数反而比 hook 更难复用。

### D2: 上传通道函数注入

**选择**：hook 入参 `upload: (file: File, type: FileType) => Promise<number | null>`，由调用方注入。

**理由**：两个通道签名天然兼容——`usePresignedUpload().upload(file, type)` 直接满足；旧通道用 `(file, type) => fileService.upload(file, type).then(r => r.data?.id ?? null)` 一行适配。hook 因此不感知通道，本次迁移后调用方代码即删除适配层。上传进度（progress）不经 hook 中转，消费方直接 `usePresignedUpload` 拿 phase/progress 传入展示组件，避免 hook 成为状态二传手。

**替代方案**：hook 内部直接调 `usePresignedUpload`——把仍在用旧通道的消费方挡在门外，违背渐进迁移原则。

### D3: hook 职责边界：到"裁剪后 File"为止，上传由调用方发起

**选择**：hook 负责 校验 → objectURL → 裁剪弹窗 → 输出裁剪后的 File（经 `onCropped` 回调或直接内部调用注入的 upload）。为避免上传状态散落在两处，本设计采用：**hook 内部完成上传**（upload 注入式），出参暴露 `uploading/progress/fileId/error/reset`。

理由：调用方（ProfileSidebar）在确认后还有业务动作（`updateAvatar`），通过 hook 出参 `fileId` 变化或 `onUploaded(fileId)` 回调衔接。相比"hook 只产出 File、调用方自己上传"，内部上传能让 objectURL 的释放时机（确认后立即 revoke，与上传结果无关）收敛在 hook 内一处，不容易漏。

### D4: 校验默认口径收敛为 jpeg/png/webp + 5MB，禁 GIF

**选择**：hook 默认 `allowedTypes: ['image/jpeg','image/png','image/webp']`，`maxSize: 5MB`，允许调用方覆盖。

**理由**：与 Profile 现状的 5MB 对齐；白名单从四处不一致收敛为一处。GIF 被禁是因为 Canvas 裁剪输出会丢失动画，静默静态化违背用户预期；报名页原 `image/*` 放宽没有意义。

### D5: AvatarUpload 完全自包含，接口重构为 onUploaded + ref

`AvatarUpload` props 重构为 `(messageApi, onUploaded, onUploadingChange?)`，内部自持 `usePresignedUpload` + `useImageCropUpload`，自行渲染预览与环形进度；通过 `forwardRef` 暴露 `reset()` 供表单提交成功后清空。`useEnrollForm` 删除预览/进度/上传状态，仅保留 `avatarId`。

**替代方案**：保持原 props（previewUrl/uploading/uploadProgress 由外部注入）——上传移入组件内部后外部注入的进度永远停留在 idle，进度条必然失效，故此方案不可行。"接口不变"的原始意图（消费方样板最少化）由新接口以另一种形式达成。

## Risks / Trade-offs

- **Profile 通道迁移引入新上传行为**（MD5 计算、PUT 直传、confirm 重试）→ 预签名通道已在报名页、考核题目、BugReport 等 5 处生产使用，行为已验证；迁移后需回归"头像更新成功 + 刷新生效"链路
- **竞赛 confirm 上传的 md5/size 语义**：预签名 confirm 需要准确的 MD5 与 size，裁剪后 File 由 blob 构造，MD5 在 hook 内由上传通道计算，无额外风险
- **GIF 存量用户**：此前已上传的 GIF 头像/Logo 不受影响（只拦新上传）；展示层维持原样
- **hook 抽取过早抽象的风险**：三处用法已确认存在（本变更内），非臆测需求
- **报名页"删除按钮"**：现有 spec 描述与 AvatarUpload 代码不一致（代码无删除按钮），本次不处理该既有偏差，仅保持 spec 原状

## Migration Plan

纯前端变更，无数据迁移。按 tasks 阶段实施：hook + 测试先行 → 报名页接入 → Profile 迁移 → 竞赛迁移。每阶段独立可验证、可回滚（git revert 单个 commit）。部署顺序无要求（三处互相独立，仅共享新 hook）。

## Open Questions

无。GIF 策略、modal 归属、变更边界、竞赛通道迁移均已与用户确认。
