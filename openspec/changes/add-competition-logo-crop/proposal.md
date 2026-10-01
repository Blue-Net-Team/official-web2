# 提案：竞赛 Logo 上传前裁剪为正方形

ref #78

## Why

竞赛 logo 在上传时不限定尺寸，管理员可上传任意宽高比的图片，导致竞赛列表卡片中 logo 渲染为 `h-[28px] w-auto`，容器宽度随图片比例漂移，视觉不一致。需要在竞赛创建/编辑时，logo 上传前裁剪为正方形再上传。

## What Changes

- 将现有 `AvatarCropModal`（`react-easy-crop` + canvas 输出正方形 JPEG）通用化为共享组件 `ImageCropModal`：
  - 新增 props：`outputSize`（输出边长，默认 512）、`aspect`（裁剪宽高比，默认 1，即正方形）
  - 组件位置从 `components/Profile/` 迁移至共享目录，原有头像裁剪行为保持不变（ProfileSidebar 消费方适配）
- `CompetitionDrawer` 的 logo 上传流程改为：选择图片 → 弹出 `ImageCropModal` 裁剪 → canvas 生成正方形 blob → 走原有 `fileService.upload` 上传并回填 `logoFileId`
- 竞赛封面（cover）上传流程不变，不裁剪
- 存量已上传的非方形 logo 不做迁移处理，展示层维持现状

## Capabilities

### New Capabilities

- `competition-logo-crop`: 竞赛创建/编辑时 logo 图片的上传前裁剪（正方形、可配置输出尺寸），裁剪后 blob 经既有文件上传接口存储

### Modified Capabilities

（无 —— 头像裁剪对外行为不变，组件重构属实现细节，不改变 `frontend-profile-avatar-upload` 的需求）

## Impact

- **前端**：
  - 新增/迁移 `src/frontend/src/components/common/ImageCropModal/`（原 `components/Profile/AvatarCropModal/` 重构，依赖 `react-easy-crop` 已存在）
  - 修改 `src/frontend/src/app/admin/competition/CompetitionDrawer.tsx`（logo Upload 拦截改为先裁剪）
  - 修改 `src/frontend/src/components/Profile/ProfileSidebar/index.tsx`（组件引用路径/props 适配）
- **后端**：无改动，logo 对后端仍只是 `fileId`
- **数据**：不迁移存量 logo
