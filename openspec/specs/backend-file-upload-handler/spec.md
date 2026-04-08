## Purpose

File upload handling with validation, size limits, and event triggering after successful persistence.

## Requirements

### Requirement: File upload with event triggering
The system SHALL accept file uploads and trigger a FileSaveEvent after successful persistence.

#### Scenario: Avatar upload
- **WHEN** user uploads an avatar file
- **THEN** system SHALL save file to storage
- **AND** system SHALL create File record with type AVATAR
- **AND** system SHALL publish FileSaveEvent with file metadata

### Requirement: 上传考题作品
系统 SHALL 提供接口 `POST /api/v1/file/upload/assessment/work`，接收参数 `questionId`（而非 answerId）和 `file`。系统 SHALL 校验：
1. 用户已认证
2. 题目存在
3. 用户方向与题目所属考核方向匹配

上传成功后，文件存储到 MinIO 的 `work` 桶，返回 FileInfo（含 fileId）。

#### Scenario: 正常上传作品文件
- **WHEN** 已认证用户 POST `/api/v1/file/upload/assessment/work` params=`{questionId: 1, file: my-project.zip}`
- **THEN** 文件存储到 MinIO work 桶，tb_file 新增记录，返回 200 + FileInfo

#### Scenario: 题目不存在
- **WHEN** POST params=`{questionId: 9999, file: ...}`
- **THEN** 返回 404 错误，提示"题目不存在"

#### Scenario: 方向不匹配
- **WHEN** 用户的 direction 与题目所属考核的 direction 不一致
- **THEN** 返回 403 错误，提示"方向不匹配"

#### Scenario: 未认证用户
- **WHEN** 未认证用户调用上传接口
- **THEN** 返回 401 错误

#### Scenario: Assessment attachment upload
- **WHEN** admin uploads an attachment for a question
- **THEN** system SHALL save file to storage
- **AND** system SHALL create File record with type ASSESSMENT_ATTACHMENT
- **AND** system SHALL publish FileSaveEvent with file metadata and question context

#### Scenario: QRCode upload
- **WHEN** user uploads a QRCode image
- **THEN** system SHALL save file to storage
- **AND** system SHALL create File record with type QRCODE
- **AND** system SHALL publish FileSaveEvent with file metadata and QRCode context

### Requirement: File type validation
The system SHALL validate uploaded files against allowed types for each upload endpoint.

#### Scenario: Invalid file type rejected
- **WHEN** user uploads a file with disallowed extension
- **THEN** system SHALL reject the upload with 400 Bad Request
- **AND** system SHALL return error message indicating allowed file types

### Requirement: File size limit
The system SHALL enforce file size limits based on file type.

#### Scenario: Exceed size limit
- **WHEN** user uploads a file exceeding the type-specific size limit
- **THEN** system SHALL reject the upload with 413 Payload Too Large
