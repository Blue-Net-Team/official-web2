## ADDED Requirements

### Requirement: Send rejection notification email upon enrollment rejection
The system SHALL send an email notification to the applicant when their enrollment is rejected, including the rejection reason.

#### Scenario: Administrator rejects an enrollment
- **WHEN** an administrator rejects an enrollment with a reason
- **THEN** the system SHALL send an HTML email to the applicant's email address
- **AND** the email SHALL contain the applicant's name and the rejection reason
- **AND** the email send SHALL be asynchronous (non-blocking to the HTTP response)

#### Scenario: Applicant has no email address
- **WHEN** an administrator rejects an enrollment
- **AND** the enrollment record has no email address
- **THEN** the system SHALL log a warning and skip sending the email
- **AND** the rejection operation SHALL still complete successfully

#### Scenario: Email send failure during rejection
- **WHEN** an administrator rejects an enrollment
- **AND** the email dispatch fails due to a network or mail server error
- **THEN** the system SHALL log the failure
- **AND** the rejection operation SHALL still complete successfully (email failure is non-blocking)
