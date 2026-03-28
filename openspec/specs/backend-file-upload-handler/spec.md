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

#### Scenario: Work file upload
- **WHEN** candidate uploads a work file for an assessment
- **THEN** system SHALL save file to storage
- **AND** system SHALL create File record with type WORK
- **AND** system SHALL publish FileSaveEvent with file metadata and answer context

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
