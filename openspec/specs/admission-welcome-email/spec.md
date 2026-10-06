## ADDED Requirements

### Requirement: Admission welcome email template
The system SHALL provide a registered message template with code `ADMISSION_WELCOME` for the admission welcome email, containing code-defined metadata (name, subject, description, variable list, default HTML content) and supporting runtime content/subject overrides persisted to the database like all other message templates.

#### Scenario: Template is listed in management UI
- **WHEN** an administrator lists message templates
- **THEN** the list SHALL include the `ADMISSION_WELCOME` template with its name, subject, description, and variable list

#### Scenario: Template content override
- **WHEN** an administrator updates the `ADMISSION_WELCOME` template content via the management API
- **THEN** the new content SHALL be persisted and used for subsequent admission emails
- **AND** the content SHALL only use supported variables

### Requirement: Admission welcome email content
When a candidate passes the global final assessment, the system SHALL send an HTML admission welcome email to the candidate's registered email address instead of the regular result notification email. The email SHALL congratulate the candidate, welcome them as a new member of the team, and mention that a GitHub organization invitation will arrive separately and should be watched for. The email SHALL NOT contain result text such as "录取" from the result notification template.

#### Scenario: Final round pass sends welcome email
- **WHEN** results are published for a global final assessment
- **AND** a candidate's decision is passed
- **THEN** the system SHALL send the admission welcome email to the candidate's email address
- **AND** the email SHALL contain the candidate's nickname, the direction label, and welcome content
- **AND** the email SHALL mention the GitHub organization invitation
- **AND** the system SHALL NOT send the regular result notification email to that candidate

#### Scenario: Final round pass with missing email
- **WHEN** a candidate passes the global final assessment
- **AND** the candidate has no registered email
- **THEN** the system SHALL skip the welcome email and log a warning
- **AND** role promotion to MEMBER SHALL still complete

#### Scenario: GitHub invitation failure does not block email
- **WHEN** the GitHub organization invitation fails or is disabled
- **THEN** the admission welcome email SHALL still be sent
- **AND** the email content SHALL remain unchanged (it only asks the candidate to watch for the invitation)
