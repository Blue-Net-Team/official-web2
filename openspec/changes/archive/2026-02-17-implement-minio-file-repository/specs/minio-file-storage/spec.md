## ADDED Requirements

### Requirement: MinIO Configuration Properties
The system SHALL provide a configuration properties class to load MinIO settings from application.yml.

#### Scenario: Load MinIO configuration on startup
- **WHEN** the application starts
- **THEN** the system SHALL load MinIO endpoint, port, accessKey, secretKey, and useSSL flag from the `minio` configuration section

### Requirement: MinIO Client Configuration
The system SHALL create and configure a MinioClient bean for interacting with the MinIO server.

#### Scenario: Initialize MinIO client on startup
- **WHEN** the application context is initialized
- **THEN** the system SHALL create a MinioClient instance using the configured endpoint, port, accessKey, secretKey, and SSL settings

### Requirement: Bucket Auto-Creation
The system SHALL automatically create buckets based on FileType enum values if they do not exist when the application starts.

#### Scenario: Create missing buckets on startup
- **WHEN** the application starts
- **THEN** the system SHALL iterate through all FileType enum values
- **AND** for each FileType, check if the corresponding bucket (from FileType.getValue()) exists
- **AND** create any bucket that does not exist

### Requirement: File Save Operation
The system SHALL implement the saveFile method to upload files to MinIO.

#### Scenario: Save file to MinIO
- **WHEN** saveFile is called with a filename, InputStream, and FileType
- **THEN** the system SHALL upload the file to the bucket corresponding to the FileType
- **AND** use the provided filename as the object name in MinIO

#### Scenario: Save file from Resource
- **WHEN** saveFile is called with a filename, Resource, and FileType
- **THEN** the system SHALL extract the InputStream from the Resource
- **AND** upload the file to the appropriate bucket

### Requirement: File Load Operation
The system SHALL implement the loadFile method to download files from MinIO.

#### Scenario: Load file from MinIO
- **WHEN** loadFile is called with a filename and FileType
- **THEN** the system SHALL return a Resource containing the file from the corresponding bucket
- **AND** throw an exception if the file does not exist

### Requirement: File Type to Bucket Mapping
The system SHALL map FileType enum values to their corresponding bucket names.

#### Scenario: Map FileType to bucket
- **WHEN** a file operation is performed with a FileType
- **THEN** the system SHALL use the following mapping:
  - AVATAR -> "avatar"
  - NORMAL_IMG -> "normal-img"
  - ASSESSMENT_ATTACHMENT -> "assessment-attachment"
  - WORK -> "work"
  - QRCODE -> "qrcode"
