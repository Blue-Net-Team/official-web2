## MODIFIED Requirements

### Requirement: Configurable object storage provider
The system SHALL select the object storage provider from provider-neutral storage configuration. The provider-neutral `ObjectStorage` interface SHALL be extended to support presigned URL generation for both upload and download operations.

#### Scenario: Select MinIO provider with presigned URL support
- **WHEN** `storage.provider` is configured as `minio`
- **THEN** the system SHALL use the MinIO storage adapter for object save, load, delete, initialization, health operations, AND presigned URL generation

#### Scenario: Select Aliyun OSS provider with presigned URL support
- **WHEN** `storage.provider` is configured as `aliyun-oss`
- **THEN** the system SHALL use the Aliyun OSS storage adapter for object save, load, delete, initialization, health operations, AND presigned URL generation

### Requirement: Provider-neutral file repository boundary
The system SHALL keep file metadata persistence and business association queries independent from the selected object storage provider. The file repository SHALL support both direct proxy uploads and presigned URL workflows transparently.

#### Scenario: Store file metadata with presigned upload workflow
- **WHEN** a file is uploaded using presigned URL workflow
- **THEN** the system SHALL persist the file metadata through the same file repository behavior
- **AND** the tb_file status SHALL transition from PENDING to ACTIVE upon confirmation

## ADDED Requirements

### Requirement: ObjectStorage interface presigned URL methods
The `ObjectStorage` interface SHALL declare `getPresignedUploadUrl` and `getPresignedDownloadUrl` methods, and both MinIO and Aliyun OSS adapters SHALL implement them.

#### Scenario: Get presigned upload URL from selected provider
- **WHEN** `objectStorage.getPresignedUploadUrl(fileType, filename, contentType, size, expiry)` is called
- **THEN** the selected provider SHALL return a valid presigned PUT URL

#### Scenario: Get presigned download URL from selected provider
- **WHEN** `objectStorage.getPresignedDownloadUrl(fileType, filename, expiry)` is called
- **THEN** the selected provider SHALL return a valid presigned GET URL
