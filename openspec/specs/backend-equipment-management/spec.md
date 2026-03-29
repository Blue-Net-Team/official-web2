## Purpose

Equipment management providing CRUD operations for laboratory equipment display with image association, sorting, and admin management capabilities.

## Requirements

### Requirement: Equipment entity with structured fields
The Equipment entity SHALL store equipment information with title, brand, description, and image association.

#### Scenario: Equipment creation with all fields
- **WHEN** creating an Equipment record
- **THEN** name MUST NOT be null or empty
- **THEN** name MUST NOT exceed 100 characters
- **THEN** brand MAY be null and MUST NOT exceed 100 characters
- **THEN** description MAY be null
- **THEN** image_file_id MAY reference a valid File record
- **THEN** sort_order SHALL default to 0

#### Scenario: Equipment query with file information
- **WHEN** querying Equipment list
- **THEN** system SHALL return Equipment with associated file information
- **THEN** file information SHALL include file ID and URL

### Requirement: Public equipment list query
The system SHALL provide a public endpoint to query all equipment.

#### Scenario: List all equipment
- **WHEN** requesting GET /api/v1/equipments
- **THEN** system SHALL return all equipment
- **THEN** equipment SHALL be sorted by sort_order DESC
- **THEN** each item SHALL include id, name, brand, description, imageUrl

#### Scenario: Empty equipment list
- **WHEN** requesting GET /api/v1/equipments and no equipment exists
- **THEN** system SHALL return empty array

### Requirement: Admin equipment creation
The system SHALL provide an admin endpoint to create equipment.

#### Scenario: Create equipment with image
- **WHEN** admin requests POST /api/v1/admin/equipments with name, brand, description, imageFileId
- **THEN** system SHALL create a new Equipment record
- **THEN** system SHALL return the created equipment with id

#### Scenario: Create equipment without image
- **WHEN** admin requests POST /api/v1/admin/equipments without imageFileId
- **THEN** system SHALL create a new Equipment record with null image_file_id

#### Scenario: Create equipment with empty name
- **WHEN** admin requests POST /api/v1/admin/equipments with empty or null name
- **THEN** system SHALL return 400 validation error

### Requirement: Admin equipment update
The system SHALL provide an admin endpoint to update equipment.

#### Scenario: Update equipment fields
- **WHEN** admin requests PUT /api/v1/admin/equipments/{id} with updated fields
- **THEN** system SHALL update the equipment record
- **THEN** system SHALL return the updated equipment

#### Scenario: Update non-existent equipment
- **WHEN** admin requests PUT /api/v1/admin/equipments/{id} with non-existent id
- **THEN** system SHALL return 404 error

### Requirement: Admin equipment deletion
The system SHALL provide an admin endpoint to delete equipment.

#### Scenario: Delete equipment
- **WHEN** admin requests DELETE /api/v1/admin/equipments/{id}
- **THEN** system SHALL delete the equipment record
- **THEN** system SHALL return 200 success

#### Scenario: Delete non-existent equipment
- **WHEN** admin requests DELETE /api/v1/admin/equipments/{id} with non-existent id
- **THEN** system SHALL return 404 error

### Requirement: Admin equipment image upload
The system SHALL provide an admin endpoint to upload equipment image.

#### Scenario: Upload equipment image
- **WHEN** admin requests POST /api/v1/admin/equipments/{id}/image with multipart file
- **THEN** system SHALL upload file to MinIO with FileType NORMAL_IMG
- **THEN** system SHALL update equipment's image_file_id
- **THEN** system SHALL return file info with id, name, url

#### Scenario: Upload image for non-existent equipment
- **WHEN** admin requests POST /api/v1/admin/equipments/{id}/image with non-existent id
- **THEN** system SHALL return 404 error

### Requirement: Equipment management permission
All admin equipment endpoints SHALL require admin permission.

#### Scenario: Access admin endpoint without authentication
- **WHEN** unauthenticated user requests any /api/v1/admin/equipments endpoint
- **THEN** system SHALL return 401 error

#### Scenario: Access admin endpoint without admin role
- **WHEN** non-admin user requests any /api/v1/admin/equipments endpoint
- **THEN** system SHALL return 403 error
