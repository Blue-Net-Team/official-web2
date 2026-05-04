## ADDED Requirements

### Requirement: Enrollment DTOs contain avatar file ID
The system SHALL return avatar file ID instead of avatar URL in all enrollment-related DTOs.

#### Scenario: Get enrollment detail with avatar
- **WHEN** GET /api/v1/enrollments/{id} is called for enrollment with avatar
- **THEN** response SHALL include `avatarFileId` field with the file ID value
- **AND** response SHALL NOT include `avatarUrl` field

#### Scenario: Get enrollment list with avatars
- **WHEN** GET /api/v1/enrollments is called
- **THEN** each enrollment item SHALL include `avatarFileId` field
- **AND** response SHALL NOT include `avatarUrl` field

#### Scenario: Get enrollment without avatar
- **WHEN** viewing enrollment without avatar file
- **THEN** `avatarFileId` SHALL be null

## REMOVED Requirements

### Requirement: Enrollment DTOs contain avatar URL
**Reason**: Avatar URL is replaced by avatar file ID for consistency with file download API
**Migration**: Use `avatarFileId` to construct download URL: `/api/v1/file/download/{avatarFileId}`
