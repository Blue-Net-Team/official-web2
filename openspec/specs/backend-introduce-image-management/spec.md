## Purpose

Introduction image management providing query operations for website introduction images with categorization, file association, and competition image support.

## Requirements

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

### Requirement: IntroduceImage competition association
The IntroduceImage entity SHALL support association with a specific competition.

#### Scenario: IntroduceImage with competition association
- **WHEN** creating an IntroduceImage record with type=COMPETITION
- **THEN** competition_id MAY reference a valid Competition record
- **THEN** competition_id SHALL be null when type is not COMPETITION

#### Scenario: IntroduceImage sorting
- **WHEN** creating an IntroduceImage record
- **THEN** sort_order SHALL default to 0
- **THEN** sort_order SHALL be used for ordering images within the same category

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

### Requirement: IntroduceImage query by competition
The system SHALL provide query operations for IntroduceImage by competition.

#### Scenario: List introduce images by competition
- **WHEN** requesting introduce image list with type=COMPETITION and competitionId parameter
- **THEN** system SHALL filter introduce images where type equals COMPETITION
- **THEN** system SHALL further filter where competition_id equals the provided competitionId
- **THEN** images SHALL be sorted by sort_order ASC
- **THEN** each item SHALL include id, type, description, fileId, fileUrl, competitionId, sortOrder

#### Scenario: Invalid competitionId parameter
- **WHEN** requesting introduce image list with type not equal to COMPETITION but competitionId parameter is provided
- **THEN** system SHALL return validation error
- **THEN** error message SHALL indicate competitionId parameter is only valid when type=COMPETITION

### Requirement: IntroduceImage count by competition
The system SHALL provide count operation for IntroduceImage by competition.

#### Scenario: Count images by competition
- **WHEN** requesting count of images for a competition
- **THEN** system SHALL return the count of IntroduceImage records where type=COMPETITION and competition_id equals the provided competitionId

### Requirement: Upload introduce image endpoint
The system SHALL provide an endpoint to upload introduce images with file and metadata in a single request.

#### Scenario: Upload laboratory introduce image
- **WHEN** admin requests POST /api/v1/file/upload/introduce-image with file and type=LABORATORY
- **THEN** system SHALL save file to MinIO with FileType NORMAL_IMG
- **THEN** system SHALL create IntroduceImage record with type=LABORATORY
- **THEN** system SHALL return FileInfo with id, name, type, url

#### Scenario: Upload direction introduce image
- **WHEN** admin requests POST /api/v1/file/upload/introduce-image with file, type=DIRECTION, and direction parameter
- **THEN** system SHALL save file to MinIO with FileType NORMAL_IMG
- **THEN** system SHALL create IntroduceImage record with type=DIRECTION and direction field
- **THEN** system SHALL return FileInfo with id, name, type, url

#### Scenario: Upload introduce image with invalid direction
- **WHEN** admin requests POST /api/v1/file/upload/introduce-image with type not equal to DIRECTION but direction parameter is provided
- **THEN** system SHALL ignore direction parameter
- **THEN** system SHALL create IntroduceImage record without direction field

#### Scenario: Upload introduce image with description
- **WHEN** admin requests POST /api/v1/file/upload/introduce-image with file, type, and description
- **THEN** system SHALL create IntroduceImage record with description field

### Requirement: Upload introduce image permission
The introduce image upload endpoint SHALL require admin permission.

#### Scenario: Upload without authentication
- **WHEN** unauthenticated user requests POST /api/v1/file/upload/introduce-image
- **THEN** system SHALL return 401 error

#### Scenario: Upload without admin role
- **WHEN** non-admin user requests POST /api/v1/file/upload/introduce-image
- **THEN** system SHALL return 403 error

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
