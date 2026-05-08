## ADDED Requirements

### Requirement: 预签名上传准备接口
系统 SHALL 提供 `POST /api/v1/file/prepare-upload` 接口，接收文件名、文件类型、文件大小、内容类型，校验用户权限后返回预签名 PUT URL 和 callbackToken。

#### Scenario: 已登录用户准备上传大文件
- **WHEN** 已登录用户 POST `/api/v1/file/prepare-upload` body=`{filename: "work.zip", type: "WORK", size: 150000000, contentType: "application/zip"}`
- **THEN** 系统 SHALL 校验用户对该文件类型的上传权限
- **AND** 生成随机 objectKey（格式 `{fileType}-{uuid}.{ext}`）
- **AND** 调用 ObjectStorage 生成预签名 PUT URL（过期 15 分钟）
- **AND** 生成 callbackToken（JWT，含 fileId、预期 MD5 hash、过期时间）
- **AND** 在 tb_file 插入状态为 PENDING 的记录
- **AND** 返回 200 + `{uploadUrl, fileId, callbackToken}`

#### Scenario: 未登录用户尝试准备上传非 AVATAR 文件
- **WHEN** 未登录用户 POST `/api/v1/file/prepare-upload` body=`{type: "WORK"}`
- **THEN** 系统 SHALL 返回 401 Unauthorized

#### Scenario: 文件大小超过系统上限
- **WHEN** 用户 POST `/api/v1/file/prepare-upload` body=`{size: 500000000}`（超过 200MB 上限）
- **THEN** 系统 SHALL 返回 413 Payload Too Large

### Requirement: 预签名上传确认接口
系统 SHALL 提供 `POST /api/v1/file/confirm-upload` 接口，接收 fileId 和 callbackToken，校验后从 OSS 读取文件元数据（ETag、大小、Content-Type），通过后才将 tb_file 状态更新为 ACTIVE。

#### Scenario: 成功确认上传
- **WHEN** 前端 POST `/api/v1/file/confirm-upload` body=`{fileId: 123, callbackToken: "xxx"}`
- **THEN** 系统 SHALL 校验 callbackToken 未过期、签名正确、fileId 匹配
- **AND** 向 OSS 发起 HEAD 请求获取文件 ETag（MD5）
- **AND** 比对 ETag 与 callbackToken 中的预期 hash
- **AND** 校验文件大小和 Content-Type 符合预期
- **AND** 将 tb_file 状态更新为 ACTIVE
- **AND** 返回 200 + FileInfo

#### Scenario: ETag 不匹配（内容被篡改）
- **WHEN** 前端 POST `/api/v1/file/confirm-upload`，但 OSS 上文件 ETag 与预期 hash 不符
- **THEN** 系统 SHALL 拒绝确认，返回 400 Bad Request
- **AND** 调用 ObjectStorage 删除该 OSS 对象
- **AND** 将 tb_file 状态更新为 REJECTED
- **AND** 记录审计日志

#### Scenario: callbackToken 过期
- **WHEN** 前端在 15 分钟后才 POST `/api/v1/file/confirm-upload`
- **THEN** 系统 SHALL 返回 410 Gone（或 401）
- **AND** 清理对应的 PENDING 记录和 OSS 对象（或留待定时任务清理）

### Requirement: 预签名上传安全约束
预签名 PUT URL SHALL 绑定 objectKey、HTTP Method、Content-Type、过期时间，且 objectKey 使用后端生成的随机 UUID，不可被预测。

#### Scenario: 攻击者尝试使用预签名 URL 上传不同 Content-Type
- **WHEN** 攻击者拿到预签名 URL（绑定了 Content-Type=image/png），但 PUT 请求发送 Content-Type=application/octet-stream
- **THEN** OSS SHALL 拒绝该请求（403 SignatureDoesNotMatch 或 400）

#### Scenario: 攻击者尝试覆盖其他文件
- **WHEN** 攻击者修改预签名 URL 中的 objectKey
- **THEN** 签名验证失败，OSS SHALL 拒绝该请求
