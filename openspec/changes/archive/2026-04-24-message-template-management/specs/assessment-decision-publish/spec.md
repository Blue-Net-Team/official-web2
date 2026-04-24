## MODIFIED Requirements

### Requirement: Publish assessment decision results via email
The system SHALL allow direction administrators or higher roles to send email notifications to all decided candidates for a given assessment time. The email content SHALL distinguish between intermediate rounds and the final round.

#### Scenario: Direction administrator publishes intermediate round results
- **WHEN** a direction administrator calls the publish API with a valid `assessmentTimeId`
- **AND** the assessment time is NOT the final round for its direction
- **AND** there are candidates with decisions (`passed` is not null) for that assessment time
- **THEN** the system SHALL send an HTML email to each decided candidate containing their name, assessment direction, epoch number, and pass/eliminate result using the text "通过" or "未通过"
- **AND** return the count of emails sent

#### Scenario: Direction administrator publishes final round results
- **WHEN** a direction administrator calls the publish API with a valid `assessmentTimeId`
- **AND** the assessment time IS the final round for its direction
- **AND** there are candidates with decisions for that assessment time
- **THEN** for candidates with `passed=true`, the system SHALL send an email with result text "录取"
- **AND** for candidates with `passed=false`, the system SHALL send an email with result text "淘汰"
- **AND** return the count of emails sent

#### Scenario: No decided candidates
- **WHEN** a direction administrator calls the publish API with a valid `assessmentTimeId`
- **AND** no candidates have decisions for that assessment time
- **THEN** the system SHALL return count 0 without sending any emails

#### Scenario: Assessment time not found
- **WHEN** a direction administrator calls the publish API with a non-existent `assessmentTimeId`
- **THEN** the system SHALL reject the operation with an error response

#### Scenario: Member cannot publish results
- **WHEN** a team member attempts to call the publish API
- **THEN** the system SHALL reject the operation with a forbidden response

#### Scenario: Email send failure is non-blocking
- **WHEN** an individual email fails to send during publish
- **THEN** the system SHALL log the failure and continue sending remaining emails
- **AND** the returned count SHALL include only successfully dispatched emails
