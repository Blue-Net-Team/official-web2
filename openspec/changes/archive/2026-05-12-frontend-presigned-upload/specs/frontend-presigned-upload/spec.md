## ADDED Requirements

### Requirement: 前端预签名上传准备接口封装
系统 SHALL 在前端 `file.service.ts` 中提供 `prepareUpload` 方法，调用后端 `POST /api/v1/file/prepare-upload` 获取预签名上传 URL 和回调令牌。

#### Scenario: 匿名用户准备上传头像
- **WHEN** 未登录用户调用 `prepareUpload({filename: 'avatar.jpg', type: 'AVATAR', size: 1024, contentType: 'image/jpeg'})`
- **THEN** 使用 `publicClient` 发送请求
- **AND** 返回包含 `fileId`、`uploadUrl`、`callbackToken`、`filename`、`type` 的响应

#### Scenario: 已登录用户准备上传考核作品
- **WHEN** 已登录用户调用 `prepareUpload({filename: 'work.zip', type: 'WORK', size: 10485760, contentType: 'application/zip'})`
- **THEN** 使用 `apiClient` 发送请求
- **AND** 返回包含 `fileId`、`uploadUrl`、`callbackToken` 的响应

#### Scenario: 匿名用户准备上传非 AVATAR 类型被拒绝
- **WHEN** 未登录用户调用 `prepareUpload({filename: 'work.zip', type: 'WORK', ...})`
- **THEN** 后端返回 401 Unauthorized

### Requirement: 前端预签名上传确认接口封装
系统 SHALL 在前端 `file.service.ts` 中提供 `confirmUpload` 方法，调用后端 `POST /api/v1/file/confirm-upload` 完成上传确认。

#### Scenario: 上传成功后确认
- **WHEN** 前端直传 OSS 成功后调用 `confirmUpload({fileId: 123, callbackToken: 'xxx', md5: 'abc123', size: 1024})`
- **THEN** 后端校验 Token、MD5、大小、魔数
- **AND** 文件状态变为 ACTIVE
- **AND** 返回包含 `fileId`、`filename`、`type`、`status` 的响应

### Requirement: 统一预签名直传 Hook
系统 SHALL 提供 `usePresignedUpload` Hook，封装完整的三段式上传流程（准备 → 直传 OSS → 确认），管理 4 阶段状态。

#### Scenario: 正常上传流程
- **WHEN** 调用 `upload(file, 'AVATAR')`
- **THEN** 状态依次为 `preparing` → `uploading`（带进度） → `verifying` → `completed`
- **AND** `completed` 状态包含 `fileId`

#### Scenario: 上传过程中取消
- **WHEN** 用户在 `uploading` 阶段调用 `cancel()`
- **THEN** 中止 XHR 请求
- **AND** 状态变为 `idle`

#### Scenario: 检查阶段网络超时后重试
- **WHEN** `confirmUpload` 因网络超时而失败
- **THEN** Hook 自动重试 `confirmUpload`（最多 3 次，指数退避）
- **AND** 若后端返回文件已 ACTIVE，视为成功

### Requirement: 分段进度条映射
系统 SHALL 将 4 阶段上传状态映射为连续进度条数值，保持现有 UI 布局不变。

#### Scenario: 各阶段进度显示
- **WHEN** 状态为 `preparing`
- **THEN** 进度条显示 0% → 15% 的动画
- **WHEN** 状态为 `uploading` 且 PUT 进度为 50%
- **THEN** 进度条显示 15% + (50% × 70%) = 50%
- **WHEN** 状态为 `verifying`
- **THEN** 进度条显示 85% 并伴随脉动动画
- **WHEN** 状态为 `completed`
- **THEN** 进度条显示 100%

### Requirement: 前端 MD5 计算
系统 SHALL 在上传前计算文件 MD5，用于后端校验。

#### Scenario: 小文件 MD5 计算
- **WHEN** 上传 1MB 的图片文件
- **THEN** 使用 `spark-md5` 计算 MD5
- **AND** 计算过程不阻塞 UI 渲染

#### Scenario: 大文件 MD5 计算
- **WHEN** 上传 100MB 的压缩包
- **THEN** 使用 `spark-md5` 分片计算，每 10MB 让出事件循环
- **AND** 计算期间进度条停留在准备阶段
