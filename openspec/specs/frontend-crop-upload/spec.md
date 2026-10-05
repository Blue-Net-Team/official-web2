# frontend-crop-upload

共享图片裁剪上传 hook（`useImageCropUpload`）：统一的文件校验、objectURL 生命周期管理、裁剪弹窗编排与通道注入式上传；以及自包含的头像上传组件（AvatarUpload）契约。

## Requirements

### Requirement: 共享裁剪上传 Hook
系统 SHALL 提供 `useImageCropUpload` hook，统一封装「选文件 → 校验 → objectURL 管理 → 裁剪弹窗 → 裁剪后上传」全流程。上传通道 SHALL 通过函数注入（签名 `(file: File, type: FileType) => Promise<number | null>`），hook MUST NOT 感知具体通道实现（预签名直传或后端中转均可注入）。hook SHALL 输出裁剪配置（裁剪框形状、宽高比、输出尺寸、弹窗标题）、文件类型与校验规则（允许类型、最大体积）作为入参。

#### Scenario: 选择文件后进入裁剪弹窗
- **WHEN** 调用方通过 `selectFile(file)` 传入一个通过校验的图片文件
- **THEN** hook 为该文件创建 objectURL 并打开裁剪弹窗
- **AND** 不发起任何上传请求

#### Scenario: 确认裁剪后上传并返回 fileId
- **WHEN** 用户在裁剪弹窗中点击确认
- **THEN** hook 将裁剪区域输出为 Blob（格式与原图一致，PNG 保留透明通道）
- **AND** 包装为 File 后调用注入的 upload 函数上传
- **AND** 上传成功后通过出参暴露 fileId

#### Scenario: 取消裁剪不上传
- **WHEN** 用户在裁剪弹窗中点击取消或关闭弹窗
- **THEN** hook 不发起上传，fileId 保持为空
- **AND** objectURL 被 revoke

#### Scenario: 注入不同上传通道
- **WHEN** 调用方注入预签名直传 upload（`usePresignedUpload`）或旧通道 upload（`fileService.upload` 适配函数）
- **THEN** hook 行为一致，仅上传实现不同

### Requirement: objectURL 生命周期管理
hook SHALL 在裁剪确认与取消两条路径上都 revoke 当前 objectURL，MUST NOT 遗留未释放的 objectURL。重新选择文件时，旧 objectURL SHALL 在创建新 objectURL 之前释放。

#### Scenario: 确认裁剪后释放 objectURL
- **WHEN** 用户确认裁剪（无论上传成败）
- **THEN** 本次选择的 objectURL 被 revoke

#### Scenario: 重复选择文件不泄漏
- **WHEN** 用户连续选择多张图片（每次都在上一次确认/取消后）
- **THEN** 任意时刻未 revoke 的 objectURL 数量不超过 1

### Requirement: 统一文件校验
hook SHALL 在上传前对文件进行类型与大小校验，默认允许类型为 jpeg/png/webp，默认最大体积 5MB；校验不通过时 SHALL 通过 message 提示且不打开裁剪弹窗。校验规则 SHALL 可由调用方覆盖。

#### Scenario: 类型不在白名单
- **WHEN** 用户选择的文件类型不在允许列表（如 GIF、HEIC、非图片文件）
- **THEN** 系统显示"请选择图片文件（JPG/PNG/WEBP）"错误提示
- **AND** 不打开裁剪弹窗，不发起上传请求

#### Scenario: 超过大小限制
- **WHEN** 用户选择的图片超过 5MB
- **THEN** 系统显示"图片大小不能超过 5MB"错误提示
- **AND** 不打开裁剪弹窗，不发起上传请求

### Requirement: AvatarUpload 自包含组件
`AvatarUpload` 组件 SHALL 自包含裁剪与上传能力：内部调用 `usePresignedUpload` 与 `useImageCropUpload`（round 裁剪框、1:1、输出 512、文件类型 `AVATAR`），自行管理预览图、上传进度与上传中状态；对消费方暴露 `onUploaded(fileId)` 回调与命令式 `reset()`（经 ref），消费方 MUST NOT 再编写裁剪/上传样板代码。

#### Scenario: 消费方零裁剪样板接入
- **WHEN** 消费方仅渲染 `<AvatarUpload ref={ref} onUploaded={...} />`，不编写任何裁剪/上传状态代码
- **THEN** 用户选择图片后弹出圆形裁剪弹窗
- **AND** 确认并上传成功后 `onUploaded` 收到 fileId
- **AND** 取消裁剪后 `onUploaded` 不被调用
- **AND** 调用 `ref.current.reset()` 后预览与 fileId 被清除
