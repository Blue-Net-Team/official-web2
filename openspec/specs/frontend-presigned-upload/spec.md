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

### Requirement: 前端文件上传全面使用预签名直传通道
前端所有文件上传场景 MUST 使用预签名直传通道（`usePresignedUpload`：prepare-upload → PUT 直传 OSS → confirm-upload），MUST NOT 存在经后端中转的上传调用（`fileService.upload` / `POST /api/v1/file/upload`）。以下消费方 SHALL 全部走预签名通道：报名页头像、个人主页头像、竞赛 Logo、竞赛封面、考核题目（QuestionDetail / QuestionDrawer）、BugReport、报名表单管理页、场地图片（VenueDrawer）、设备图片（EquipmentDrawer）、管理端二维码（QrcodeDrawer）、个人微信二维码（ProfileInfo）、成就附件（AchievementDrawer）。

#### Scenario: 场地图片预签名上传
- **WHEN** 管理员在场地表单中选择图片上传
- **THEN** 浏览器直接 PUT 到预签名 URL，不经后端中转
- **AND** 上传成功后 fileId 回填场地表单

#### Scenario: 设备图片预签名上传
- **WHEN** 管理员在设备表单中选择图片上传
- **THEN** 走预签名直传通道
- **AND** 上传成功后 fileId 回填设备表单

#### Scenario: 竞赛封面预签名上传
- **WHEN** 管理员在竞赛表单中选择封面图片
- **THEN** 走预签名直传通道且不经过裁剪弹窗
- **AND** 上传成功后 coverFileId 回填表单

#### Scenario: 二维码预签名上传
- **WHEN** 管理员在二维码管理或个人资料页上传二维码图片
- **THEN** 走预签名直传通道（QRCODE 类型）
- **AND** 上传成功后 fileId 回填对应表单

#### Scenario: 成就附件预签名上传
- **WHEN** 管理员在成就表单中上传附件文件
- **THEN** 走预签名直传通道，文件类型保持不变（NORMAL_IMG）
- **AND** 上传成功后 fileId 回填成就表单

#### Scenario: 前端无旧通道调用残留
- **WHEN** 全局搜索 `fileService.upload` 或 `POST /file/upload` 的调用
- **THEN** 前端代码中不存在任何匹配
- **AND** `file.service.ts` 中不再定义 `upload` 方法
