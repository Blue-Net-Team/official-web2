## Purpose

Venue management providing CRUD operations for laboratory venue display with image association, sorting, and admin management capabilities.

## ADDED Requirements

### Requirement: Venue entity with structured fields
The Venue entity SHALL store venue information with title, subtitle, description, and image association.

#### Scenario: Venue creation with all fields
- **WHEN** creating a Venue record
- **THEN** name MUST NOT be null or empty
- **THEN** name MUST NOT exceed 100 characters
- **THEN** subtitle MAY be null and MUST NOT exceed 100 characters
- **THEN** description MAY be null
- **THEN** image_file_id MAY reference a valid File record
- **THEN** sort_order SHALL default to 0

#### Scenario: Venue query with file information
- **WHEN** querying Venue list
- **THEN** system SHALL return Venue with associated file information
- **THEN** file information SHALL include file ID and URL

### Requirement: Public venue list query
The system SHALL provide a public endpoint to query all venues.

#### Scenario: List all venues
- **WHEN** requesting GET /api/v1/venues
- **THEN** system SHALL return all venues
- **THEN** venues SHALL be sorted by sort_order DESC
- **THEN** each item SHALL include id, name, subtitle, description, imageUrl

#### Scenario: Empty venue list
- **WHEN** requesting GET /api/v1/venues and no venues exist
- **THEN** system SHALL return empty array

### Requirement: Admin venue creation
The system SHALL provide an admin endpoint to create venues.

#### Scenario: Create venue with image
- **WHEN** admin requests POST /api/v1/admin/venues with name, subtitle, description, imageFileId
- **THEN** system SHALL create a new Venue record
- **THEN** system SHALL return the created venue with id

#### Scenario: Create venue without image
- **WHEN** admin requests POST /api/v1/admin/venues without imageFileId
- **THEN** system SHALL create a new Venue record with null image_file_id

#### Scenario: Create venue with empty name
- **WHEN** admin requests POST /api/v1/admin/venues with empty or null name
- **THEN** system SHALL return 400 validation error

### Requirement: Admin venue update
The system SHALL provide an admin endpoint to update venues.

#### Scenario: Update venue fields
- **WHEN** admin requests PUT /api/v1/admin/venues/{id} with updated fields
- **THEN** system SHALL update the venue record
- **THEN** system SHALL return the updated venue

#### Scenario: Update non-existent venue
- **WHEN** admin requests PUT /api/v1/admin/venues/{id} with non-existent id
- **THEN** system SHALL return 404 error

### Requirement: Admin venue deletion
The system SHALL provide an admin endpoint to delete venues.

#### Scenario: Delete venue
- **WHEN** admin requests DELETE /api/v1/admin/venues/{id}
- **THEN** system SHALL delete the venue record
- **THEN** system SHALL return 200 success

#### Scenario: Delete non-existent venue
- **WHEN** admin requests DELETE /api/v1/admin/venues/{id} with non-existent id
- **THEN** system SHALL return 404 error

### Requirement: Admin venue image upload
The system SHALL provide an admin endpoint to upload venue image.

#### Scenario: Upload venue image
- **WHEN** admin requests POST /api/v1/admin/venues/{id}/image with multipart file
- **THEN** system SHALL upload file to MinIO with FileType NORMAL_IMG
- **THEN** system SHALL update venue's image_file_id
- **THEN** system SHALL return file info with id, name, url

#### Scenario: Upload image for non-existent venue
- **WHEN** admin requests POST /api/v1/admin/venues/{id}/image with non-existent id
- **THEN** system SHALL return 404 error

### Requirement: Venue management permission
All admin venue endpoints SHALL require admin permission.

#### Scenario: Access admin endpoint without authentication
- **WHEN** unauthenticated user requests any /api/v1/admin/venues endpoint
- **THEN** system SHALL return 401 error

#### Scenario: Access admin endpoint without admin role
- **WHEN** non-admin user requests any /api/v1/admin/venues endpoint
- **THEN** system SHALL return 403 error
