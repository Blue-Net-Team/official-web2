## Purpose

Competition management providing CRUD operations, sorting management, and photo association for competitions displayed on the website homepage and competition list.

## Requirements

### Requirement: Competition entity storage
The Competition entity SHALL store competition information with logo, name, short name, summary, detail, and sorting order.

#### Scenario: Competition creation
- **WHEN** creating a Competition record
- **THEN** name MUST NOT be empty
- **THEN** short_name MAY be empty
- **THEN** logo_file_id MUST reference a valid File record with type NORMAL_IMG
- **THEN** summary SHALL provide brief introduction (max 500 characters)
- **THEN** detail SHALL provide detailed introduction
- **THEN** sort_order SHALL default to 0
- **THEN** enabled SHALL default to TRUE

#### Scenario: Competition sorting
- **WHEN** querying competition list
- **THEN** competitions SHALL be sorted by sort_order DESC, then by created_at DESC
- **THEN** only enabled competitions SHALL be returned for public endpoints

### Requirement: Competition image association via IntroduceImage
The competition images SHALL be stored in tb_introduce_image table with type=COMPETITION and competition_id.

#### Scenario: Competition image creation
- **WHEN** creating an IntroduceImage record for competition
- **THEN** type MUST be COMPETITION
- **THEN** competition_id MUST reference a valid Competition record
- **THEN** file_id MUST reference a valid File record with type NORMAL_IMG
- **THEN** description MAY provide photo description
- **THEN** sort_order SHALL default to 0

#### Scenario: Competition image limit
- **WHEN** adding images to a competition
- **THEN** the total number of images SHALL NOT exceed 20 per competition

### Requirement: Public competition list endpoint
The system SHALL provide a public endpoint to get competition list with brief information.

#### Scenario: Get competition list with limit
- **WHEN** requesting GET /api/v1/competitions with limit parameter
- **THEN** system SHALL return at most limit competitions
- **THEN** each competition SHALL include id, name, shortName, summary, logoUrl
- **THEN** competitions SHALL be sorted by sort_order DESC, created_at DESC
- **THEN** only enabled competitions SHALL be returned
- **THEN** limit SHALL default to 10 if not provided
- **THEN** limit SHALL NOT exceed 50

#### Scenario: Competition without logo
- **WHEN** a competition has no logo_file_id
- **THEN** logoUrl SHALL be null

### Requirement: Public competition detail endpoint
The system SHALL provide a public endpoint to get single competition detail.

#### Scenario: Get competition detail
- **WHEN** requesting GET /api/v1/competitions/{id}
- **THEN** system SHALL return competition with id, name, shortName, summary, detail, logoUrl
- **THEN** system SHALL return associated images from tb_introduce_image where type=COMPETITION and competition_id={id}
- **THEN** images SHALL be sorted by sort_order ASC
- **THEN** each image SHALL include id, url, description

#### Scenario: Competition not found
- **WHEN** requesting a non-existent competition id
- **THEN** system SHALL return 404 error

#### Scenario: Competition disabled
- **WHEN** requesting a disabled competition
- **THEN** system SHALL return 404 error

### Requirement: Admin competition creation
The system SHALL provide an admin endpoint to create competition.

#### Scenario: Create competition
- **WHEN** admin requests POST /api/v1/admin/competitions with valid data
- **THEN** system SHALL create a new Competition record
- **THEN** system SHALL return the created competition with id

#### Scenario: Create competition with invalid logo
- **WHEN** admin requests POST /api/v1/admin/competitions with non-existent logo_file_id
- **THEN** system SHALL return 400 error

### Requirement: Admin competition update
The system SHALL provide an admin endpoint to update competition.

#### Scenario: Update competition
- **WHEN** admin requests PUT /api/v1/admin/competitions/{id} with valid data
- **THEN** system SHALL update the Competition record
- **THEN** system SHALL return the updated competition

#### Scenario: Update non-existent competition
- **WHEN** admin requests PUT /api/v1/admin/competitions/{id} with non-existent id
- **THEN** system SHALL return 404 error

### Requirement: Admin competition deletion
The system SHALL provide an admin endpoint to delete competition.

#### Scenario: Delete competition
- **WHEN** admin requests DELETE /api/v1/admin/competitions/{id}
- **THEN** system SHALL delete the Competition record
- **THEN** system SHALL delete all associated IntroduceImage records where type=COMPETITION and competition_id={id}
- **THEN** system SHALL return success message

#### Scenario: Delete non-existent competition
- **WHEN** admin requests DELETE /api/v1/admin/competitions/{id} with non-existent id
- **THEN** system SHALL return 404 error

### Requirement: Admin competition sort adjustment
The system SHALL provide an admin endpoint to adjust competition sort order.

#### Scenario: Update sort order
- **WHEN** admin requests PUT /api/v1/admin/competitions/{id}/sort with sortOrder
- **THEN** system SHALL update the competition's sort_order
- **THEN** system SHALL return success message

### Requirement: Admin competition image management
The system SHALL provide admin endpoints to manage competition images via IntroduceImage.

#### Scenario: Add competition image
- **WHEN** admin requests POST /api/v1/admin/competitions/{id}/images with file_id
- **THEN** system SHALL create a new IntroduceImage record with type=COMPETITION and competition_id={id}
- **THEN** system SHALL return the created image association

#### Scenario: Add image to non-existent competition
- **WHEN** admin requests POST /api/v1/admin/competitions/{id}/images with non-existent competition id
- **THEN** system SHALL return 404 error

#### Scenario: Add image exceeding limit
- **WHEN** admin requests POST /api/v1/admin/competitions/{id}/images when competition already has 20 images
- **THEN** system SHALL return 400 error with message "每个竞赛最多关联20张照片"

#### Scenario: Delete competition image
- **WHEN** admin requests DELETE /api/v1/admin/competitions/{id}/images/{imageId}
- **THEN** system SHALL delete the IntroduceImage record
- **THEN** system SHALL return success message

#### Scenario: Delete non-existent image
- **WHEN** admin requests DELETE /api/v1/admin/competitions/{id}/images/{imageId} with non-existent imageId
- **THEN** system SHALL return 404 error
