## Purpose

College management UI for admin to manage college reference data.

## Requirements

### Requirement: College list view
Admin SHALL view all colleges in a table.

#### Scenario: Display college list
- **WHEN** admin navigates to `/admin/college`
- **THEN** a table SHALL display all colleges with columns: ID, name, actions
- **THEN** admin SHALL click a row to view details
- **THEN** admin SHALL click "New College" to create

### Requirement: College CRUD operations
Admin SHALL perform CRUD operations on colleges.

#### Scenario: Create college
- **WHEN** admin creates a college
- **THEN** admin SHALL provide name (required, max 100 chars)
- **THEN** college SHALL be created via POST `/api/v1/admin/colleges`

#### Scenario: Update college
- **WHEN** admin updates a college
- **THEN** admin SHALL modify name (required, max 100 chars)
- **THEN** college SHALL be updated via PUT `/api/v1/admin/colleges/{id}`

#### Scenario: Delete college
- **WHEN** admin deletes a college
- **THEN** confirmation modal SHALL be displayed
- **THEN** college SHALL be deleted via DELETE `/api/v1/admin/colleges/{id}`
- **THEN** error message SHALL be displayed if college has associated users/enrollments

### Requirement: Permission control
College management SHALL be restricted to admins.

#### Scenario: Admin access
- **WHEN** user has role level >= 3
- **THEN** college management menu SHALL be visible
- **WHEN** user has role level < 3
- **THEN** accessing `/admin/college` SHALL be redirected or show 403
