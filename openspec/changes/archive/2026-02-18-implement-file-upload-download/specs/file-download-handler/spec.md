## ADDED Requirements

### Requirement: File download with permission validation
The system SHALL validate user permissions before allowing file downloads based on file type.

#### Scenario: Download own avatar
- **WHEN** authenticated user requests their own avatar
- **THEN** system SHALL return the avatar file

#### Scenario: Download work file as candidate
- **WHEN** candidate requests their own work file
- **THEN** system SHALL return the work file

#### Scenario: Download work file as team member
- **WHEN** team member (role >= MEMBER) requests any work file
- **THEN** system SHALL return the work file

#### Scenario: Download work file denied for other candidates
- **WHEN** candidate requests another candidate's work file
- **THEN** system SHALL deny access with 403 Forbidden

#### Scenario: Download assessment attachment
- **WHEN** user with matching direction requests assessment attachment
- **THEN** system SHALL return the attachment file

#### Scenario: Download assessment attachment denied for wrong direction
- **WHEN** user with non-matching direction requests assessment attachment
- **THEN** system SHALL deny access with 403 Forbidden

#### Scenario: Download normal image
- **WHEN** any authenticated user requests a normal image
- **THEN** system SHALL return the image file

#### Scenario: Download QRCode
- **WHEN** any user requests a QRCode image
- **THEN** system SHALL return the QRCode file

### Requirement: File download endpoint
The system SHALL provide RESTful endpoints for downloading files by file ID.

#### Scenario: Download by file ID
- **WHEN** user makes GET request to /api/v1/file/download/{fileId}
- **THEN** system SHALL validate permissions
- **AND** system SHALL return file with appropriate Content-Type header
- **AND** system SHALL set Content-Disposition for download

#### Scenario: File not found
- **WHEN** user requests a non-existent or deleted file
- **THEN** system SHALL return 404 Not Found
