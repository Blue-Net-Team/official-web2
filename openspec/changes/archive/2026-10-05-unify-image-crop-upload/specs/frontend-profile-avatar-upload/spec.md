# frontend-profile-avatar-upload

## MODIFIED Requirements

### Requirement: 头像裁剪弹窗
系统 SHALL 在用户选择图片后弹出裁剪弹窗，允许用户调整裁剪区域后再上传。裁剪状态管理（弹窗开关、objectURL 生命周期）SHALL 经共享 hook `useImageCropUpload` 处理，ProfileSidebar MUST NOT 再手写裁剪状态样板代码。

#### Scenario: 选择图片后弹出裁剪弹窗
- **WHEN** 用户选择了一张有效图片文件
- **THEN** 系统弹出裁剪弹窗（antd Modal）
- **AND** 弹窗中显示图片和圆形裁剪框（1:1 比例）
- **AND** 用户可拖拽和缩放调整裁剪区域

#### Scenario: 确认裁剪后上传
- **WHEN** 用户在裁剪弹窗中点击确认按钮
- **THEN** 系统使用 Canvas API 将裁剪区域输出为 Blob
- **AND** 通过预签名直传通道（`usePresignedUpload`）上传裁剪后的图片
- **AND** 上传成功后调用 `fileService.updateAvatar(fileId)` 绑定新头像
- **AND** 调用 `onAvatarUpdate` 回调刷新页面数据
- **AND** 新头像在页面上立即生效

#### Scenario: 取消裁剪
- **WHEN** 用户在裁剪弹窗中点击取消按钮或关闭弹窗
- **THEN** 弹窗关闭，不上传任何内容，头像保持不变
- **AND** objectURL 被释放

### Requirement: 文件前端校验
系统 SHALL 在前端对选择的文件进行类型和大小校验，不通过时不上传。允许类型为 jpg/png/webp（GIF MUST NOT 进入裁剪弹窗，避免裁剪后动画被静默静态化）。

#### Scenario: 文件类型校验
- **WHEN** 用户选择了非图片类型的文件或 GIF 图片
- **THEN** 系统显示错误提示"请选择图片文件（JPG/PNG/WEBP）"
- **AND** 不打开裁剪弹窗，不发起上传请求

#### Scenario: 文件大小校验
- **WHEN** 用户选择了超过 5MB 的图片文件
- **THEN** 系统显示错误提示"图片大小不能超过 5MB"
- **AND** 不打开裁剪弹窗，不发起上传请求
