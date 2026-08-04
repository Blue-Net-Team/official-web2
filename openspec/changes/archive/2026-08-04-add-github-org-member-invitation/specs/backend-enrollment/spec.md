## MODIFIED Requirements

### Requirement: Enrollment application entity
The Enroll entity SHALL represent external user applications with student ID as unique identifier.

The enrollment form SHALL display a tooltip or hint next to the email field explaining that:
- The email will be used for assessment notifications
- The email will be used for GitHub organization invitation upon passing the final assessment
- If the applicant has a GitHub account, the email SHOULD be the one associated with that GitHub account

#### Scenario: Enrollment data validation
- **WHEN** creating an Enrollment
- **THEN** student_id MUST be 12-13 characters and unique
- **THEN** status MUST be one of: pending, approved, rejected
- **THEN** direction MUST be one of: computer_vision, structural_design, embedded
- **THEN** avatar_id MAY reference a File entity
- **THEN** college_id MUST reference an existing College

#### Scenario: Email field GitHub hint
- **WHEN** a user views the enrollment form
- **THEN** the email field SHALL display a hint explaining its use for GitHub organization invitation
- **AND** the hint SHALL advise using the GitHub-associated email if the user has a GitHub account

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
