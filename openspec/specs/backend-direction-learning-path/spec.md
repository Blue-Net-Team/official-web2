# direction-learning-path Specification

## Purpose
TBD - created by archiving change add-direction-learning-path-api. Update Purpose after archive.
## Requirements
### Requirement: Learning path data storage

The system SHALL store learning path steps in `tb_direction_learning_step` table with direction, step number, title, and video URL fields.

#### Scenario: Database table structure
- **WHEN** the system initializes
- **THEN** `tb_direction_learning_step` table exists with columns: id, direction, step_number, title, video_url

#### Scenario: Unique constraint on direction and step
- **WHEN** inserting a learning step
- **THEN** the combination of direction and step_number MUST be unique

---

### Requirement: Public API for learning path retrieval

The system SHALL provide a public API endpoint `GET /api/v1/directions/{slug}/learning-path` to retrieve learning path data for a specific direction.

#### Scenario: Successful retrieval with valid slug
- **WHEN** client requests `GET /api/v1/directions/cv/learning-path`
- **THEN** system returns HTTP 200 with learning path data for computer vision direction

#### Scenario: Successful retrieval with video links
- **WHEN** client requests learning path and steps have video URLs
- **THEN** system returns response with videoLink field populated

#### Scenario: Successful retrieval without video links
- **WHEN** client requests learning path and steps have no video URLs
- **THEN** system returns response with videoLink field as null

#### Scenario: Invalid direction slug
- **WHEN** client requests `GET /api/v1/directions/invalid/learning-path`
- **THEN** system returns HTTP 404 with error message

---

### Requirement: Admin API for learning path management

The system SHALL provide admin API endpoints for CRUD operations on learning path steps.

#### Scenario: Create learning step
- **WHEN** admin requests `POST /api/v1/admin/directions/{slug}/learning-steps` with valid data
- **THEN** system creates new learning step and returns HTTP 201

#### Scenario: Update learning step
- **WHEN** admin requests `PUT /api/v1/admin/directions/learning-steps/{id}` with valid data
- **THEN** system updates learning step and returns HTTP 200

#### Scenario: Delete learning step
- **WHEN** admin requests `DELETE /api/v1/admin/directions/learning-steps/{id}`
- **THEN** system deletes learning step and returns HTTP 204

#### Scenario: Unauthorized access
- **WHEN** unauthenticated user requests admin endpoints
- **THEN** system returns HTTP 401

---

### Requirement: Direction slug to enum mapping

The system SHALL map frontend slugs (cv, embed, struct) to backend Direction enum values.

#### Scenario: CV slug mapping
- **WHEN** API receives slug "cv"
- **THEN** system maps to Direction.COMPUTER_VISION

#### Scenario: Embed slug mapping
- **WHEN** API receives slug "embed"
- **THEN** system maps to Direction.EMBEDDED

#### Scenario: Struct slug mapping
- **WHEN** API receives slug "struct"
- **THEN** system maps to Direction.STRUCTURAL_DESIGN

---

### Requirement: Permission control for learning path management

The system SHALL enforce permission checks on learning path management endpoints.

#### Scenario: Public endpoint access
- **WHEN** any user accesses `GET /api/v1/directions/{slug}/learning-path`
- **THEN** system allows access without authentication

#### Scenario: Admin endpoint access
- **WHEN** admin accesses management endpoints
- **THEN** system requires `direction-learning-path:create/update/delete` permission

---

### Requirement: Initial data migration

The system SHALL initialize default learning path data for all three directions during migration.

#### Scenario: Default data for computer vision
- **WHEN** migration executes
- **THEN** system inserts 4 learning steps for COMPUTER_VISION direction

#### Scenario: Default data for embedded
- **WHEN** migration executes
- **THEN** system inserts 4 learning steps for EMBEDDED direction

#### Scenario: Default data for structural design
- **WHEN** migration executes
- **THEN** system inserts 4 learning steps for STRUCTURAL_DESIGN direction

