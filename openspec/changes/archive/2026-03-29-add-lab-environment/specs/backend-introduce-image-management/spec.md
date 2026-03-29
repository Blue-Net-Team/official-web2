## Purpose

Introduction image management providing query operations for website introduction images with categorization, file association, and competition image support.

## MODIFIED Requirements

### Requirement: IntroduceImage entity with file association
The IntroduceImage entity SHALL store introduction image metadata with file association.

#### Scenario: IntroduceImage creation with file
- **WHEN** creating an IntroduceImage record
- **THEN** fileId MUST reference a valid File record
- **THEN** type MUST be one of: team_photo, direction, competition, patent, paper
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
- **THEN** type MUST be one of: team_photo, direction, competition, patent, paper
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

### Requirement: Upload introduce image endpoint
The system SHALL provide an endpoint to upload introduce images with file and metadata in a single request.

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

## REMOVED Requirements

### Requirement: Laboratory and Equipment image types
**Reason**: Laboratory and Equipment image types are replaced by dedicated Venue and Equipment tables with structured fields (title, subtitle/brand, description) for better data organization and display flexibility.

**Migration**:
1. Existing `LABORATORY` type images in `tb_introduce_image` should be manually migrated to `tb_venue` table
2. Existing `EQUIPMENT` type images in `tb_introduce_image` should be manually migrated to `tb_equipment` table
3. After migration, delete the old records from `tb_introduce_image`
4. Update `ImageType` enum to remove `LABORATORY` and `EQUIPMENT` values
