## MODIFIED Requirements

### Requirement: MinIO Client Configuration
The system SHALL create and configure a MinioClient bean for interacting with the MinIO server when MinIO is the selected storage provider. The system SHALL also support generating presigned URLs for upload and download operations.

#### Scenario: Initialize MinIO client on startup
- **WHEN** the application context is initialized with `storage.provider` set to `minio`
- **THEN** the system SHALL create a MinioClient instance using the configured endpoint, port, accessKey, secretKey, and SSL settings

#### Scenario: Generate MinIO presigned upload URL
- **WHEN** `minioObjectStorage.getPresignedUploadUrl(...)` is called
- **THEN** the system SHALL call `minioClient.getPresignedObjectUrl` with method PUT, bucket, object name, and expiry

#### Scenario: Generate MinIO presigned download URL
- **WHEN** `minioObjectStorage.getPresignedDownloadUrl(...)` is called
- **THEN** the system SHALL call `minioClient.getPresignedObjectUrl` with method GET, bucket, object name, and expiry

## ADDED Requirements

### Requirement: MinIO multipart upload for large files
For files larger than a configurable threshold, the system SHALL use MinIO multipart upload capabilities to handle large file uploads efficiently through the backend proxy path.

#### Scenario: Save large file to MinIO via backend proxy
- **WHEN** `minioObjectStorage.put()` is called with a large file InputStream
- **THEN** the system SHALL use multipart upload with configured part size (default 10MB)
- **AND** memory usage SHALL not exceed the part size buffer
