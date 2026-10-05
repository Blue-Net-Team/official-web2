# Tasks: unify-image-crop-upload

## 1. 共享 Hook 抽取

- [x] 1.1 新建 `src/hooks/useImageCropUpload.ts`：入参（crop 配置、fileType、upload 注入函数、可覆盖的校验规则），出参（selectFile、uploading、progress、fileId、error、reset、cropModal 渲染元素）
- [x] 1.2 实现文件校验：默认 jpeg/png/webp 白名单 + 5MB，不通过时 message 提示且不打开裁剪弹窗
- [x] 1.3 实现 objectURL 生命周期：选择时创建、重新选择前释放旧 URL、确认与取消两条路径均 revoke
- [x] 1.4 实现裁剪确认流程：getCroppedBlob → 包装为 File → 调用注入的 upload → 暴露 fileId；PNG 保留透明通道
- [x] 1.5 `pnpm lint` 与 `tsc --noEmit`（或等效 typecheck）通过

## 2. 报名页 AvatarUpload 接入裁剪

- [x] 2.1 重构 `src/components/Enroll/AvatarUpload.tsx`：内部调用 `useImageCropUpload`（round、aspect=1、outputSize=512、类型 AVATAR、upload 注入 usePresignedUpload），props 接口保持不变
- [x] 2.2 调整 `src/components/Enroll/hooks/useEnrollForm.ts`：avatarPreview 改为裁剪后 blob 的 objectURL，补充旧 preview URL 的 revoke
- [x] 2.3 回归报名页头像链路：选择 → 裁剪 → 进度 → 提交报名（含 GIF 被拦截、超 5MB 被拦截、取消裁剪不上传）

## 3. Profile 迁移共享 Hook + 预签名通道

- [x] 3.1 重构 `src/components/Profile/ProfileSidebar/index.tsx`：删除手写 crop 状态（cropModalOpen/cropImageSrc/handleCropCancel 等），改用 `useImageCropUpload`（round、AVATAR）
- [x] 3.2 upload 注入从 `fileService.upload` 切换为 `usePresignedUpload().upload`，确认后仍调用 `fileService.updateAvatar(fileId)`，上传中 UI 保持 loading 态
- [x] 3.3 回归个人主页头像链路：裁剪 → 上传 → 刷新生效；GIF 被拦截提示更新为 JPG/PNG/WEBP

## 4. 竞赛 Logo 迁移共享 Hook + 预签名通道

- [x] 4.1 重构 `src/app/admin/competition/CompetitionDrawer.tsx`：删除手写 logo 裁剪状态，改用 `useImageCropUpload`（rect、outputSize=256、NORMAL_IMG、标题「裁剪 Logo」）
- [x] 4.2 logo 上传通道从 `fileService.upload` 切换为 `usePresignedUpload().upload`；封面（cover）上传流程保持原样
- [x] 4.3 回归竞赛创建/编辑链路：logo 裁剪 → 回填 logoFileId → 提交；取消裁剪不回填；编辑存量竞赛不强制重裁

## 5. 端到端验证

- [x] 5.1 `pnpm lint`、`pnpm format:check`、typecheck 全绿
- [x] 5.2 Playwright 验证三处链路（报名页、个人主页、竞赛 Logo）的裁剪弹窗开合、确认上传、取消路径
