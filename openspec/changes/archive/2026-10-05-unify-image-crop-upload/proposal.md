# Proposal: unify-image-crop-upload

## Why

图片上传的三个消费方（报名页头像、个人主页头像、竞赛 Logo）目前状态参差不齐：报名页头像上传完全没有裁剪（原图直传，可能把大尺寸横图塞进圆形展示框），个人主页和竞赛虽然接入了 `ImageCropModal`，但各自手写了约 40 行重复的裁剪状态样板代码（cropModalOpen/cropImageSrc/revoke 生命周期）。此外个人主页和竞赛仍走旧的后端中转上传通道（`fileService.upload`），而项目规范要求预签名直传（`usePresignedUpload`）。报名页还存在 objectURL 不 revoke 的内存泄漏，三处文件校验口径不一致（2MB vs 5MB、白名单 vs `image/*`），GIF 在裁剪流程中会被 Canvas 静默静态化却未在前端拦截。

## What Changes

- 新增共享 hook `useImageCropUpload`：统一封装「选文件 → 类型/大小校验 → objectURL 生命周期（confirm/cancel 双路 revoke）→ ImageCropModal 裁剪 → 裁剪后 File 上传」全流程；上传通道通过函数注入，兼容预签名直传与旧通道。
- 报名页 `AvatarUpload` 重构为自包含组件：内部调用 `useImageCropUpload`（round 裁剪、AVATAR 类型），对用户暴露的接口保持不变（`onFileSelect` 语义变为"已裁剪的 File"）。
- 报名页头像上传接入裁剪弹窗；预览图改为裁剪后 blob；校验对齐为 jpeg/png/webp 白名单 + 5MB，**拒绝 GIF**。
- 个人主页 `ProfileSidebar` 删除手写裁剪样板，改用 `useImageCropUpload`；上传通道从 `fileService.upload` 迁移到 `usePresignedUpload` 预签名直传，裁剪确认后仍调用 `fileService.updateAvatar(fileId)`；GIF 从允许列表移除。
- 竞赛 `CompetitionDrawer` 的 Logo 上传删除手写裁剪样板，改用 `useImageCropUpload`（rect、outputSize=256、NORMAL_IMG）；上传通道从 `fileService.upload` 迁移到预签名直传。
- 前端无测试框架（无 vitest/jest），验证依赖 lint/typecheck + Playwright E2E 三处上传链路。

## Capabilities

### New Capabilities

- `frontend-crop-upload`: 共享图片裁剪上传 hook（useImageCropUpload）——统一的文件校验、objectURL 生命周期管理、裁剪弹窗编排与通道注入式上传，以及 AvatarUpload 自包含组件契约。

### Modified Capabilities

- `frontend-enroll-page`: 「头像上传功能」需求变更——上传前强制裁剪、预览为裁剪结果、校验口径改为 jpeg/png/webp + 5MB 并拒绝 GIF。
- `frontend-profile-avatar-upload`: 文件类型白名单移除 GIF；上传通道从后端中转改为预签名直传；裁剪流程改经共享 hook。
- `competition-logo-crop`: Logo 上传通道从 `fileService.upload` 改为预签名直传；裁剪状态管理改经共享 hook。

## Impact

- **前端代码**：
  - 新增 `src/hooks/useImageCropUpload.ts` 及其测试
  - 修改 `src/components/Enroll/AvatarUpload.tsx`（自包含化）
  - 修改 `src/components/Enroll/hooks/useEnrollForm.ts`（预览源、objectURL 清理）
  - 修改 `src/components/Profile/ProfileSidebar/index.tsx`（裁剪样板删除 + 通道迁移）
  - 修改 `src/app/admin/competition/CompetitionDrawer.tsx`（裁剪样板删除 + 通道迁移）
- **无后端改动**：`prepare-upload` / `PUT` / `confirm-upload` 接口与权限均已存在；AVATAR、NORMAL_IMG 文件类型已存在。
- **无新依赖**：`react-easy-crop`、`ImageCropModal`、`usePresignedUpload` 均已存在。
- **范围外**（已知遗留，后续独立变更）：VenueDrawer、EquipmentDrawer、QrcodeDrawer、AchievementDrawer、ProfileInfo（微信二维码）等仍在使用 `fileService.upload` 旧通道，本次不迁移。
