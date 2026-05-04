## ADDED Requirements

### Requirement: Enrollment API permission control
The enrollment API endpoints SHALL enforce proper permission control based on endpoint type.

#### Scenario: Public endpoints access
- **WHEN** accessing POST /api/v1/enrollments or GET /api/v1/enrollments/check-student-id
- **THEN** system SHALL allow access without authentication

#### Scenario: Admin endpoints access
- **WHEN** accessing /api/v1/admin/enrollments/* endpoints
- **THEN** system SHALL require authentication with ADMIN role
- **THEN** system SHALL return 403 Forbidden for non-admin users

### Requirement: Enrollment data validation on submission
The system SHALL validate all enrollment data fields during submission.

#### Scenario: Student ID format validation
- **WHEN** submitting enrollment with invalid student_id format
- **THEN** system SHALL reject with 400 Bad Request
- **THEN** error message SHALL indicate student_id must be 12-13 characters

#### Scenario: Direction value validation
- **WHEN** submitting enrollment with invalid direction value
- **THEN** system SHALL reject with 400 Bad Request
- **THEN** error message SHALL list valid direction values

#### Scenario: College existence validation
- **WHEN** submitting enrollment with non-existing college_id
- **THEN** system SHALL reject with 400 Bad Request

#### Scenario: Grade range validation
- **WHEN** submitting enrollment with grade outside valid range (1-6)
- **THEN** system SHALL reject with 400 Bad Request

### Requirement: User creation on enrollment approval
The system SHALL automatically create a user account when an enrollment is approved.

#### Scenario: Create new user on approval
- **WHEN** admin approves an enrollment
- **THEN** system SHALL check if user with same student_id exists
- **THEN** if not exists, system SHALL create new User with enrollment data
- **THEN** new User SHALL have role set to "member"
- **THEN** new User SHALL have disable set to false

#### Scenario: Skip user creation if exists
- **WHEN** admin approves an enrollment and user with same student_id already exists
- **THEN** system SHALL skip user creation
- **THEN** system SHALL log a warning message

### Requirement: Enrollment audit logging
The system SHALL log all enrollment-related actions for audit purposes.

#### Scenario: Log enrollment submission
- **WHEN** a new enrollment is submitted
- **THEN** system SHALL create audit log with action "ENROLLMENT_SUBMIT"

#### Scenario: Log enrollment approval
- **WHEN** admin approves an enrollment
- **THEN** system SHALL create audit log with action "ENROLLMENT_APPROVE"
- **THEN** log SHALL include admin user ID and enrollment ID

#### Scenario: Log enrollment rejection
- **WHEN** admin rejects an enrollment
- **THEN** system SHALL create audit log with action "ENROLLMENT_REJECT"
- **THEN** log SHALL include admin user ID, enrollment ID, and rejection reason
