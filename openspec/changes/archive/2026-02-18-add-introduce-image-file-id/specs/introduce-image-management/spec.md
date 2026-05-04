## ADDED Requirements

### Requirement: IntroduceImage entity with file association
The IntroduceImage entity SHALL store introduction image metadata with file association.

#### Scenario: IntroduceImage creation with file
- **WHEN** creating an IntroduceImage record
- **THEN** fileId MUST reference a valid File record
- **THEN** type MUST be one of: laboratory, equipment, team_photo, direction, competition, patent, paper
- **THEN** description SHALL provide detailed information about image content

#### Scenario: IntroduceImage query with file information
- **WHEN** querying IntroduceImage list
- **THEN** system SHALL return IntroduceImage with associated file information
- **THEN** file information SHALL include file ID, name, and URL

### Requirement: IntroduceImage query operations
The system SHALL provide query operations for IntroduceImage management.

#### Scenario: List introduce images by type
- **WHEN** requesting introduce image list with type parameter
- **THEN** system SHALL filter introduce images by type
- **THEN** type MUST be one of: laboratory, equipment, team_photo, direction, competition, patent, paper
- **THEN** each item SHALL include id, type, description, fileId, and fileUrl

#### Scenario: List introduce images by type and direction
- **WHEN** requesting introduce image list with type=direction and direction parameter
- **THEN** system SHALL filter introduce images where type equals direction
- **THEN** system SHALL further filter where description matches direction enum value
- **THEN** direction MUST be one of: COMPUTER_VISION, STRUCTURAL_DESIGN, EMBEDDED
- **THEN** each item SHALL include id, type, description, fileId, and fileUrl

#### Scenario: Invalid direction parameter
- **WHEN** requesting introduce image list with type not equal to direction but direction parameter is provided
- **THEN** system SHALL return validation error
- **THEN** error message SHALL indicate direction parameter is only valid when type=direction

### Requirement: UserVO file URL access
The UserVO SHALL provide complete URLs for avatar and QR code for frontend use.

#### Scenario: UserVO contains avatar URL
- **WHEN** querying user information
- **THEN** UserVO SHALL contain avatarUrl field with complete file URL
- **THEN** frontend SHALL be able to use avatarUrl directly without additional requests

#### Scenario: UserVO contains QR code URL
- **WHEN** querying user information
- **THEN** UserVO SHALL contain wechatQrCodeUrl field with complete file URL
- **THEN** frontend SHALL be able to use wechatQrCodeUrl directly without additional requests
