## MODIFIED Requirements

### Requirement: Learning path data storage

The system SHALL store learning path steps in `tb_direction_learning_step` table with direction, sort order, title, and related link URL fields. The link field SHALL be named `related_url` and carry the semantics of "相关链接" (any related resource link, not limited to videos). The ordering field SHALL be named `sort_order` and SHALL carry ordering semantics only; it SHALL NOT be unique, SHALL NOT be exposed to clients, and the display step number SHALL be derived by the client from list position.

#### Scenario: Database table structure
- **WHEN** the system initializes
- **THEN** `tb_direction_learning_step` table exists with columns: id, direction, sort_order, title, related_url

#### Scenario: No uniqueness constraint on ordering
- **WHEN** the system writes new `sort_order` values for multiple steps of the same direction
- **THEN** the system MUST NOT enforce any uniqueness constraint on the combination of direction and sort_order

#### Scenario: Ordering column replacement migration
- **WHEN** migration V28 executes
- **THEN** column `sort_order` is added and populated from the existing `step_number` values, constraint `uk_direction_step` is dropped, and column `step_number` is removed, with the relative order of every direction's steps preserved

---

### Requirement: Public API for learning path retrieval

The system SHALL provide a public API endpoint `GET /api/v1/directions/{slug}/learning-path` to retrieve learning path data for a specific direction. Step objects in the response SHALL expose the link field as `relatedLink` and SHALL NOT expose any step number or sort order field. The order of the `steps` array SHALL be the authoritative display order, sorted by `sort_order` ascending.

#### Scenario: Successful retrieval with valid slug
- **WHEN** client requests `GET /api/v1/directions/cv/learning-path`
- **THEN** system returns HTTP 200 with learning path data for computer vision direction

#### Scenario: Successful retrieval with related links
- **WHEN** client requests learning path and steps have related URLs
- **THEN** system returns response with relatedLink field populated

#### Scenario: Successful retrieval without related links
- **WHEN** client requests learning path and steps have no related URLs
- **THEN** system returns response with relatedLink field as null

#### Scenario: Steps ordered by sort order
- **WHEN** client requests learning path for a direction whose steps have non-contiguous sort_order values
- **THEN** system returns steps sorted by sort_order ascending, without renumbering or rejecting the gaps

#### Scenario: Step payload contains no number field
- **WHEN** client requests learning path
- **THEN** each element of `steps` contains only id, title and relatedLink, and contains no `stepNumber` field

#### Scenario: Invalid direction slug
- **WHEN** client requests `GET /api/v1/directions/invalid/learning-path`
- **THEN** system returns HTTP 404 with error message

---

### Requirement: Admin API for learning path management

The system SHALL provide admin API endpoints for CRUD operations on learning path steps. Request and response DTOs SHALL use `relatedLink` as the link field name and SHALL NOT carry any step number field. Newly created steps SHALL be appended to the end of their direction's order.

#### Scenario: Create learning step
- **WHEN** admin requests `POST /api/v1/admin/directions/{slug}/learning-steps` with valid title and optional related link
- **THEN** system creates new learning step and returns HTTP 201

#### Scenario: Created step appended to the end
- **WHEN** admin creates a learning step in a direction that already has N steps
- **THEN** the new step's sort_order is greater than every existing step's sort_order in that direction

#### Scenario: Update learning step
- **WHEN** admin requests `PUT /api/v1/admin/directions/learning-steps/{id}` with valid title and optional related link
- **THEN** system updates the step's title and related link, leaves its sort_order unchanged, and returns HTTP 200

#### Scenario: Delete learning step
- **WHEN** admin requests `DELETE /api/v1/admin/directions/learning-steps/{id}`
- **THEN** system deletes learning step and returns HTTP 204

#### Scenario: Delete does not renumber remaining steps
- **WHEN** admin deletes a learning step that is not the last one in its direction
- **THEN** system leaves the sort_order values of remaining steps unchanged and does not close the resulting gap

#### Scenario: Unauthorized access
- **WHEN** unauthenticated user requests admin endpoints
- **THEN** system returns HTTP 401

---

### Requirement: Permission control for learning path management

The system SHALL enforce permission checks on learning path management endpoints.

#### Scenario: Public endpoint access
- **WHEN** any user accesses `GET /api/v1/directions/{slug}/learning-path`
- **THEN** system allows access without authentication

#### Scenario: Admin endpoint access
- **WHEN** admin accesses management endpoints
- **THEN** system requires `direction-learning-path:create/update/delete/sort` permission

## ADDED Requirements

### Requirement: Batch reorder API for learning steps

The system SHALL provide an admin endpoint to overwrite the display order of all learning steps of one direction in a single request.

`PUT /api/v1/admin/directions/{slug}/learning-steps/sort` with body `{ "items": [ { "id": <number>, "sortOrder": <number> } ] }`.

The system SHALL apply all items within one transaction, SHALL reject any item whose step does not belong to the given direction, and SHALL require permission `direction-learning-path:sort`.

#### Scenario: Reorder all steps of a direction
- **WHEN** admin submits a payload containing every step of the direction with new sortOrder values
- **THEN** system persists all new sortOrder values in one transaction and subsequent learning path queries return the new order

#### Scenario: Reorder accepts non-contiguous values
- **WHEN** admin submits sortOrder values with gaps or with values that temporarily collide with existing values
- **THEN** system persists them without raising a uniqueness error

#### Scenario: Item from another direction rejected
- **WHEN** admin submits a payload where one item's step id belongs to a different direction than the one in the path
- **THEN** system rejects the request, applies no changes, and returns an error response

#### Scenario: Unknown step id rejected
- **WHEN** admin submits a payload containing a step id that does not exist
- **THEN** system rejects the request and applies no changes
