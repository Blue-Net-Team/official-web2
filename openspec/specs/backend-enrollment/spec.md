## Purpose

Enrollment system for external users to apply for team membership with student ID as unique identifier.

## Requirements

### Requirement: Enrollment application entity
The Enroll entity SHALL represent external user applications with student ID as unique identifier.

#### Scenario: Enrollment data validation
- **WHEN** creating an Enrollment
- **THEN** student_id MUST be 12-13 characters and unique
- **THEN** status MUST be one of: pending, approved, rejected
- **THEN** direction MUST be one of: computer_vision, structural_design, embedded
- **THEN** avatar_id MAY reference a File entity
- **THEN** college_id MUST reference an existing College

### Requirement: Enrollment status lifecycle
The system SHALL support enrollment status transitions from pending to approved or rejected.

#### Scenario: Status transition rules
- **WHEN** an enrollment is created
- **THEN** status SHALL default to pending
- **WHEN** status changes to approved
- **THEN** a User account MAY be created with the enrollment data

### Requirement: Duplicate enrollment handling
The system SHALL detect and handle duplicate enrollment attempts using student_id.

#### Scenario: Duplicate student ID detection
- **WHEN** submitting enrollment with existing student_id
- **THEN** system SHALL prompt for update confirmation
- **WHEN** update is confirmed
- **THEN** existing enrollment SHALL be updated with new data
