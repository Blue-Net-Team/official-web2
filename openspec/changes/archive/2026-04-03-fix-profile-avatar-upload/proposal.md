## Why

个人主页侧边栏的头像编辑按钮（`.avatarEdit`）是纯装饰元素，没有绑定任何点击事件或文件上传逻辑。用户点击后无任何反应，而报名页已有完整的头像上传实现。后端 `POST /api/v1/file/upload/avatar` 已支持已登录用户上传头像并自动更新 `tb_user.avatar_id`，前端 `fileService.uploadAvatar()` 也已封装，但 ProfileSidebar 组件未接入。

## What Changes

- **交互方式变更**：整个头像区域（圆形图片）变为可点击区域，hover 时显示半透明遮罩+编辑图标，点击后触发文件选择（类似 antd Upload 的体验）。原来的小圆形编辑按钮作为备选/降级方案。
- **新增头像裁剪**：选择图片后弹出裁剪弹窗（圆形裁剪框，1:1 比例），用户调整裁剪区域后确认，再上传裁剪后的图片。
- ProfileSidebar 添加 `'use client'` 指令（需要交互状态）
- 新增 `AvatarCropModal` 组件（基于 `react-easy-crop`）
- 父组件 page.tsx 传递 `onAvatarUpdate` 回调给 ProfileSidebar
- 上传过程中显示 loading 状态反馈

## Capabilities

### New Capabilities

- `frontend-profile-avatar-upload`: 个人主页头像上传交互功能，包含文件选择、图片裁剪、上传调用、状态反馈和头像刷新

### Modified Capabilities

- `frontend-user-profile`: 新增头像上传交互的需求场景，用户可在个人主页直接更换头像

## Impact

- **前端组件**: `ProfileSidebar/index.tsx`（主要修改）、新增 `AvatarCropModal` 组件、`page.tsx`（传递回调）
- **前端依赖**: 新增 `react-easy-crop` npm 包
- **前端 API**: 复用已有 `fileService.uploadAvatar()`，无需新增 API
- **后端**: 无变更，已完全支持
