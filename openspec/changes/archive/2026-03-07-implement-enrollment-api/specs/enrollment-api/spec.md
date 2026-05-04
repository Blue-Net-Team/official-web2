## Purpose

Enrollment REST API providing endpoints for external users to submit applications and administrators to manage enrollment records.

## Requirements

### Requirement: Submit enrollment application
The system SHALL provide a public endpoint for external users to submit enrollment applications without authentication.

#### Scenario: Successful enrollment submission
- **WHEN** POST /api/v1/enrollments with valid enrollment data and non-existing student_id
- **THEN** system SHALL create a new enrollment with status "pending"
- **THEN** system SHALL store avatarId if provided (reference to File table)
- **THEN** system SHALL return 201 Created with enrollment ID

#### Scenario: Enrollment with avatar
- **WHEN** POST /api/v1/enrollments with avatarId referencing an existing File
- **THEN** system SHALL link the avatar file to the enrollment
- **THEN** avatar file type SHALL be "avatar"

#### Scenario: Duplicate student ID detection
- **WHEN** POST /api/v1/enrollments with existing student_id and forceUpdate=false or not provided
- **THEN** system SHALL return 409 Conflict
- **THEN** response body SHALL include existing enrollment info (id, username, status, direction, createdAt)

#### Scenario: Force update existing enrollment
- **WHEN** POST /api/v1/enrollments with existing student_id and forceUpdate=true
- **THEN** system SHALL update the existing enrollment with new data
- **THEN** system SHALL update avatarId if provided
- **THEN** system SHALL reset status to "pending" if previously rejected
- **THEN** system SHALL return 200 OK with updated enrollment info

#### Scenario: Invalid enrollment data
- **WHEN** POST /api/v1/enrollments with invalid data
- **THEN** system SHALL return 400 Bad Request with validation errors

#### Scenario: Invalid avatar ID
- **WHEN** POST /api/v1/enrollments with non-existing avatarId
- **THEN** system SHALL return 400 Bad Request with error "头像文件不存在"

### Requirement: Admin list enrollments
The system SHALL provide an admin endpoint to list enrollment records with pagination and filtering.

#### Scenario: List all enrollments
- **WHEN** GET /api/v1/admin/enrollments with valid admin credentials
- **THEN** system SHALL return paginated enrollment list
- **THEN** each item SHALL include id, username, studentId, direction, status, createdAt

#### Scenario: Filter by status
- **WHEN** GET /api/v1/admin/enrollments?status=pending
- **THEN** system SHALL return only enrollments with pending status

#### Scenario: Filter by direction
- **WHEN** GET /api/v1/admin/enrollments?direction=computer_vision
- **THEN** system SHALL return only enrollments with computer_vision direction

#### Scenario: Pagination
- **WHEN** GET /api/v1/admin/enrollments?page=2&size=20
- **THEN** system SHALL return the second page with 20 items
- **THEN** response SHALL include totalElements, totalPages, currentPage

### Requirement: Admin get enrollment detail
The system SHALL provide an admin endpoint to get detailed enrollment information.

#### Scenario: Get enrollment detail
- **WHEN** GET /api/v1/admin/enrollments/{id} with valid admin credentials
- **THEN** system SHALL return complete enrollment information
- **THEN** response SHALL include all enrollment fields and college name

#### Scenario: Enrollment not found
- **WHEN** GET /api/v1/admin/enrollments/{id} with non-existing ID
- **THEN** system SHALL return 404 Not Found

### Requirement: Admin approve enrollment
The system SHALL provide an admin endpoint to approve an enrollment application.

#### Scenario: Successful approval
- **WHEN** PUT /api/v1/admin/enrollments/{id}/approve with valid admin credentials
- **THEN** system SHALL update enrollment status to "approved"
- **THEN** system SHALL create a User account if student_id not exists
- **THEN** system SHALL copy avatarId from enrollment to the new User account
- **THEN** system SHALL return 200 OK with updated enrollment

#### Scenario: Approve already processed enrollment
- **WHEN** PUT /api/v1/admin/enrollments/{id}/approve on non-pending enrollment
- **THEN** system SHALL return 400 Bad Request with error message

#### Scenario: Enrollment not found for approval
- **WHEN** PUT /api/v1/admin/enrollments/{id}/approve with non-existing ID
- **THEN** system SHALL return 404 Not Found

### Requirement: Admin reject enrollment
The system SHALL provide an admin endpoint to reject an enrollment application.

#### Scenario: Successful rejection
- **WHEN** PUT /api/v1/admin/enrollments/{id}/reject with valid admin credentials and optional reason
- **THEN** system SHALL update enrollment status to "rejected"
- **THEN** system SHALL return 200 OK with updated enrollment

#### Scenario: Reject already processed enrollment
- **WHEN** PUT /api/v1/admin/enrollments/{id}/reject on non-pending enrollment
- **THEN** system SHALL return 400 Bad Request with error message

### Requirement: Enrollment statistics
The system SHALL provide an admin endpoint to get enrollment statistics.

#### Scenario: Get statistics
- **WHEN** GET /api/v1/admin/enrollments/statistics with valid admin credentials
- **THEN** system SHALL return counts grouped by status
- **THEN** system SHALL return counts grouped by direction
- **THEN** system SHALL return total count
