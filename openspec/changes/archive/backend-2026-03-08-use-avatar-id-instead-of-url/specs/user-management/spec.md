## ADDED Requirements

### Requirement: User info response contains avatar file ID
The system SHALL return user's avatar file ID instead of avatar URL in user info response.

#### Scenario: Get current user info with avatar
- **WHEN** GET /api/v1/user/info is called by authenticated user with avatar
- **THEN** response SHALL include `avatarFileId` field with the file ID value
- **AND** response SHALL NOT include `avatarUrl` field

#### Scenario: Get current user info without avatar
- **WHEN** GET /api/v1/user/info is called by authenticated user without avatar
- **THEN** response SHALL include `avatarFileId` field with null value
- **AND** response SHALL NOT include `avatarUrl` field

## REMOVED Requirements

### Requirement: User info response contains avatar URL
**Reason**: Avatar URL is replaced by avatar file ID for frontend flexibility
**Migration**: Use `avatarFileId` to construct download URL: `/api/v1/file/download/{avatarFileId}`
