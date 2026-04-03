## ADDED Requirements

### Requirement: 头像区域点击触发文件选择
系统 SHALL 允许已登录用户点击头像圆形区域触发文件选择对话框，hover 时显示半透明遮罩与编辑图标作为视觉提示。

#### Scenario: hover 头像区域显示遮罩
- **WHEN** 用户鼠标悬停在头像圆形区域上
- **THEN** 头像上叠加半透明遮罩层
- **AND** 遮罩层中显示编辑图标（如相机图标）和提示文案

#### Scenario: 点击头像区域触发文件选择
- **WHEN** 用户点击头像圆形区域
- **THEN** 系统弹出文件选择对话框
- **AND** 文件选择仅允许图片格式（jpg/png/gif/webp）

#### Scenario: 上传中头像区域显示 loading
- **WHEN** 头像正在上传中
- **THEN** 头像区域显示 loading 状态（Spinner 或旋转图标）
- **AND** 头像区域不可重复点击

### Requirement: 头像裁剪弹窗
系统 SHALL 在用户选择图片后弹出裁剪弹窗，允许用户调整裁剪区域后再上传。

#### Scenario: 选择图片后弹出裁剪弹窗
- **WHEN** 用户选择了一张有效图片文件
- **THEN** 系统弹出裁剪弹窗（antd Modal）
- **AND** 弹窗中显示图片和圆形裁剪框（1:1 比例）
- **AND** 用户可拖拽和缩放调整裁剪区域

#### Scenario: 确认裁剪后上传
- **WHEN** 用户在裁剪弹窗中点击确认按钮
- **THEN** 系统使用 Canvas API 将裁剪区域输出为 Blob
- **AND** 调用 `fileService.uploadAvatar(blob)` 上传裁剪后的图片
- **AND** 上传成功后调用 `onAvatarUpdate` 回调刷新页面数据
- **AND** 新头像在页面上立即生效

#### Scenario: 取消裁剪
- **WHEN** 用户在裁剪弹窗中点击取消按钮或关闭弹窗
- **THEN** 弹窗关闭，不上传任何内容，头像保持不变

### Requirement: 文件前端校验
系统 SHALL 在前端对选择的文件进行类型和大小校验，不通过时不上传。

#### Scenario: 文件类型校验
- **WHEN** 用户选择了非图片类型的文件
- **THEN** 系统显示错误提示"请选择图片文件（JPG/PNG/GIF/WEBP）"
- **AND** 不打开裁剪弹窗，不发起上传请求

#### Scenario: 文件大小校验
- **WHEN** 用户选择了超过 5MB 的图片文件
- **THEN** 系统显示错误提示"图片大小不能超过 5MB"
- **AND** 不打开裁剪弹窗，不发起上传请求

### Requirement: 上传失败处理
系统 SHALL 在头像上传失败时给出错误提示并恢复可操作状态。

#### Scenario: 上传失败
- **WHEN** 头像上传请求失败（网络错误或服务端错误）
- **THEN** 系统显示错误提示"头像上传失败，请重试"
- **AND** 关闭裁剪弹窗
- **AND** 恢复头像区域为可点击状态
