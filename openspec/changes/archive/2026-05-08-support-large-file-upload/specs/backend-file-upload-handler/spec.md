## ADDED Requirements

### Requirement: 预签名上传 URL 生成
系统 SHALL 在 `ObjectStorage` 接口及其实现类中支持生成预签名 PUT URL，供前端直传文件到 OSS。

#### Scenario: 阿里云 OSS 生成预签名上传 URL
- **WHEN** 后端调用 `aliyunOssObjectStorage.getPresignedUploadUrl(...)`
- **THEN** 使用 `OSS.generatePresignedUrl` 生成 PUT URL
- **AND** URL SHALL 绑定 bucket、objectKey、Content-Type、过期时间

#### Scenario: MinIO 生成预签名上传 URL
- **WHEN** 后端调用 `minioObjectStorage.getPresignedUploadUrl(...)`
- **THEN** 使用 `MinioClient.getPresignedObjectUrl` 生成 PUT URL
- **AND** URL SHALL 绑定 bucket、objectKey、过期时间

### Requirement: 上传回调校验
系统 SHALL 在 `/api/v1/file/confirm-upload` 接口中校验 OSS 上文件的实际 ETag、大小和 Content-Type，与预期值比对通过后才将文件标记为可用。

#### Scenario: 校验通过
- **WHEN** 后端收到 confirm-upload 请求
- **AND** OSS HEAD 返回的 ETag 与 callbackToken 中的预期 hash 一致
- **THEN** tb_file 状态更新为 ACTIVE

#### Scenario: 校验失败
- **WHEN** 后端收到 confirm-upload 请求
- **AND** OSS HEAD 返回的 ETag 与预期 hash 不一致
- **THEN** 删除 OSS 对象，tb_file 状态更新为 REJECTED，返回 400
