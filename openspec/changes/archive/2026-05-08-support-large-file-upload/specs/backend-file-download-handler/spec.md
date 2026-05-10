## MODIFIED Requirements

### Requirement: File download endpoint
The system SHALL provide RESTful endpoints for downloading files by file ID. For files larger than a configurable threshold or for all files in presigned-download mode, the system SHALL return a presigned GET URL (or 302 Redirect) instead of proxying the file content through the backend.

#### Scenario: Download by file ID with permission validation (small file)
- **WHEN** user makes GET request to /api/v1/file/download/{fileId} for a small file
- **THEN** system SHALL validate permissions using RoleType enum and RoleHierarchy utility
- **AND** system MAY return 302 Redirect to presigned GET URL (or proxy for backward compatibility)

#### Scenario: Download by file ID with permission validation (large file)
- **WHEN** user makes GET request to /api/v1/file/download/{fileId} for a large file
- **THEN** system SHALL validate permissions
- **AND** system SHALL generate presigned GET URL with expiry
- **AND** system SHALL return 302 Redirect to the presigned URL

### Requirement: File download with permission validation
The system SHALL validate user permissions before allowing file downloads based on file type. Permission checking SHALL use the RoleType enum and RoleHierarchy utility class for role level comparisons, and SHALL NOT use hardcoded role name strings. Download authorization SHALL occur before presigned URL generation.

#### Scenario: Download work file as team member
- **WHEN** team member (role >= RoleType.MEMBER) requests any work file
- **THEN** system SHALL generate presigned URL and return redirect

#### Scenario: Download work file denied for insufficient role
- **WHEN** user with role below MEMBER requests another candidate's work file
- **THEN** system SHALL deny access with 403 Forbidden before generating any URL

## ADDED Requirements

### Requirement: Presigned download URL generation
The system SHALL support generating presigned GET URLs for both MinIO and Aliyun OSS providers, with configurable expiry duration.

#### Scenario: Generate Aliyun OSS presigned download URL
- **WHEN** `aliyunOssObjectStorage.getPresignedDownloadUrl(...)` is called
- **THEN** it SHALL use `OSS.generatePresignedUrl` with GET method and expiry

#### Scenario: Generate MinIO presigned download URL
- **WHEN** `minioObjectStorage.getPresignedDownloadUrl(...)` is called
- **THEN** it SHALL use `MinioClient.getPresignedObjectUrl` with GET method and expiry
