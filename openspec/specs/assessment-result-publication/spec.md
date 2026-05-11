## ADDED Requirements

### Requirement: Assessment results can be published per assessment time
The system SHALL allow direction administrators or higher roles to publish assessment results for a given assessment time, making comments and finalized scores visible to candidates.

#### Scenario: Direction administrator publishes results
- **WHEN** a direction administrator calls the publish API for an assessment time
- **THEN** the system SHALL set `results_published_at` to the current timestamp for that assessment time
- **AND** candidates SHALL be able to view their comments and finalized scores

#### Scenario: Results already published
- **WHEN** a direction administrator calls the publish API for an assessment time that already has `results_published_at` set
- **THEN** the system SHALL allow the operation and update the timestamp to the current time

#### Scenario: Member attempts to publish results
- **WHEN** a team member attempts to call the publish API
- **THEN** the system SHALL reject the operation with a forbidden response

#### Scenario: Candidate views unpublished results
- **WHEN** a candidate requests their assessment results before publication
- **THEN** the system SHALL withhold comments and finalized scores
- **AND** return only the existence of the judgement without detailed feedback

### Requirement: Publishing triggers email notification
The system SHALL send email notifications to decided candidates when results are published.

#### Scenario: Publish sends emails to decided candidates
- **WHEN** results are published for an assessment time
- **AND** there are candidates with decisions for that assessment time
- **THEN** the system SHALL send an HTML email to each decided candidate containing their pass/eliminate result
- **AND** return the count of emails sent

#### Scenario: Publish with no decided candidates
- **WHEN** results are published for an assessment time with no decided candidates
- **THEN** the system SHALL set the publication timestamp without sending emails
- **AND** return count 0
