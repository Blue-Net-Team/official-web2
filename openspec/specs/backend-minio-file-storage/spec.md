## MODIFIED Requirements

### Requirement: MinIO Configuration Properties
The system SHALL provide configuration properties to load MinIO settings from the provider-neutral `storage.minio` configuration section when MinIO is the selected storage provider.

#### Scenario: Load MinIO configuration on startup
- **WHEN** the application starts with `storage.provider` set to `minio`
- **THEN** the system SHALL load MinIO endpoint, port, accessKey, secretKey, and useSSL flag from the `storage.minio` configuration section
- **AND** the system SHALL load the object storage bucket from `storage.bucket`

### Requirement: MinIO Client Configuration
The system SHALL create and configure a MinioClient bean for interacting with the MinIO server when MinIO is the selected storage provider.

#### Scenario: Initialize MinIO client on startup
- **WHEN** the application context is initialized with `storage.provider` set to `minio`
- **THEN** the system SHALL create a MinioClient instance using the configured endpoint, port, accessKey, secretKey, and SSL settings

### Requirement: Bucket Auto-Creation
The system SHALL automatically ensure the configured storage bucket exists in MinIO when the application starts.

#### Scenario: Create configured bucket on startup
- **WHEN** the application starts with `storage.provider` set to `minio`
- **THEN** the system SHALL check whether the bucket configured by `storage.bucket` exists
- **AND** create the configured bucket if it does not exist
- **AND** SHALL NOT create separate buckets from `FileType` enum values

### Requirement: File Save Operation
The system SHALL implement the saveFile method to upload files to MinIO using the configured bucket and `FileType` object key prefix.

#### Scenario: Save file to MinIO
- **WHEN** saveFile is called with a filename, InputStream, and FileType
- **THEN** the system SHALL upload the file to the bucket configured by `storage.bucket`
- **AND** use `<FileType.getValue()>/<filename>` as the object name in MinIO

#### Scenario: Save file from Resource
- **WHEN** saveFile is called with a filename, Resource, and FileType
- **THEN** the system SHALL extract the InputStream from the Resource
- **AND** upload the file to the configured bucket using the `FileType` object key prefix

### Requirement: File Load Operation
The system SHALL implement the loadFile method to download files from MinIO using the configured bucket and `FileType` object key prefix.

#### Scenario: Load file from MinIO
- **WHEN** loadFile is called with a filename and FileType
- **THEN** the system SHALL return a Resource containing the file from the bucket configured by `storage.bucket`
- **AND** the system SHALL read the object at `<FileType.getValue()>/<filename>`
- **AND** throw an exception if the file does not exist

### Requirement: File Type to Bucket Mapping
The system SHALL use FileType enum values as object key prefixes and SHALL NOT use them as bucket names.

#### Scenario: Map FileType to object key prefix
- **WHEN** a file operation is performed with a FileType
- **THEN** the system SHALL use the configured `storage.bucket` as the bucket
- **AND** the system SHALL use the following object key prefixes:
  - AVATAR -> `avatar/`
  - NORMAL_IMG -> `normal-img/`
  - ASSESSMENT_ATTACHMENT -> `assessment-attachment/`
  - WORK -> `work/`
  - QRCODE -> `qrcode/`
