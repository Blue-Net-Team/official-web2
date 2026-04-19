## ADDED Requirements

### Requirement: Configurable object storage provider
The system SHALL select the object storage provider from provider-neutral storage configuration.

#### Scenario: Select MinIO provider
- **WHEN** `storage.provider` is configured as `minio`
- **THEN** the system SHALL use the MinIO storage adapter for object save, load, delete, initialization, and health operations

#### Scenario: Select Aliyun OSS provider
- **WHEN** `storage.provider` is configured as `aliyun-oss`
- **THEN** the system SHALL use the Aliyun OSS storage adapter for object save, load, delete, initialization, and health operations

#### Scenario: Unsupported provider
- **WHEN** `storage.provider` contains an unsupported value
- **THEN** the application SHALL fail startup with a clear configuration error

### Requirement: Single bucket object layout
The system SHALL store all uploaded file objects in the configured storage bucket and use the file type value as the object key prefix.

#### Scenario: Resolve object location
- **WHEN** a file operation is performed for a `FileType` and filename
- **THEN** the bucket SHALL be read from `storage.bucket`
- **AND** the object key SHALL be `<FileType.getValue()>/<filename>`

#### Scenario: Save avatar object
- **WHEN** an avatar named `avatar-123.png` is saved with `storage.bucket` set to `bluenet`
- **THEN** the object SHALL be stored in bucket `bluenet`
- **AND** the object key SHALL be `avatar/avatar-123.png`

#### Scenario: Save work object
- **WHEN** a work file named `work-123.zip` is saved with `storage.bucket` set to `bluenet`
- **THEN** the object SHALL be stored in bucket `bluenet`
- **AND** the object key SHALL be `work/work-123.zip`

### Requirement: Provider-neutral file repository boundary
The system SHALL keep file metadata persistence and business association queries independent from the selected object storage provider.

#### Scenario: Store file metadata with selected provider
- **WHEN** a file is uploaded using either MinIO or Aliyun OSS
- **THEN** the system SHALL persist the file metadata through the same file repository behavior
- **AND** only the binary object operation SHALL vary by provider

#### Scenario: Query file business associations
- **WHEN** querying file-related assessment answers, assessment questions, or assessment times
- **THEN** the result SHALL NOT depend on whether the selected provider is MinIO or Aliyun OSS

### Requirement: Storage health check
The system SHALL expose object storage health for the selected provider.

#### Scenario: Selected provider is healthy
- **WHEN** the selected provider connectivity check succeeds
- **THEN** the storage health indicator SHALL report `UP`
- **AND** include the selected provider name in health details

#### Scenario: Selected provider is unavailable
- **WHEN** the selected provider connectivity check fails
- **THEN** the storage health indicator SHALL report `DOWN`
- **AND** include the selected provider name and failure details

### Requirement: Storage capability wiring
The system SHALL wire file and QR code capabilities based on provider-neutral storage availability instead of MinIO-specific enablement.

#### Scenario: Aliyun OSS is selected
- **WHEN** `storage.enabled` is true and `storage.provider` is `aliyun-oss`
- **THEN** file upload, file download, and QR code persistence services SHALL be available

#### Scenario: MinIO is disabled by provider switch
- **WHEN** `storage.provider` is not `minio`
- **THEN** non-MinIO business components SHALL NOT be disabled solely because MinIO-specific configuration is inactive
