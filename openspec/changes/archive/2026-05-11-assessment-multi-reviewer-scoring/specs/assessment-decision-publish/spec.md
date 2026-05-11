## MODIFIED Requirements

### Requirement: Publish assessment decision results via email
The system SHALL allow direction administrators or higher roles to send email notifications to all decided candidates for a given assessment time.

#### Scenario: Direction administrator publishes results
- **WHEN** a direction administrator calls the publish API with a valid `assessmentTimeId`
- **AND** there are candidates with decisions (`passed` is not null) for that assessment time
- **THEN** the system SHALL set `results_published_at` to the current timestamp for that assessment time
- **AND** the system SHALL send an HTML email to each decided candidate containing their name, assessment direction, epoch number, pass/eliminate result, and optionally their finalized score
- **AND** return the count of emails sent

#### Scenario: No decided candidates
- **WHEN** a direction administrator calls the publish API with a valid `assessmentTimeId`
- **AND** no candidates have decisions for that assessment time
- **THEN** the system SHALL set `results_published_at` to the current timestamp
- **AND** return count 0 without sending any emails

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

### Requirement: Frontend publish button integration
The system SHALL replace the placeholder publish button notification with an actual API call that triggers email dispatch.

#### Scenario: User clicks publish button
- **WHEN** an authorized user clicks the "发布本轮结果" button
- **THEN** the frontend SHALL call the publish API and display the number of emails sent on success
- **OR** display an error message on failure
- **AND** upon successful publication, the frontend SHALL refresh the assessment data to reflect the published state
