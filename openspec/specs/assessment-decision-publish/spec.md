## Requirements

### Requirement: Publish assessment decision results via email
The system SHALL allow direction administrators or higher roles to send email notifications to all decided candidates for a given assessment time.

For global assessments (`direction = null, grade = null`):
- The final round determination SHALL NOT depend on direction or grade — the system SHALL query `MAX(epoch)` unconditionally
- The email notification SHALL handle `direction = null` gracefully: use a generic label (e.g., "全局") as the direction name
- The email content SHALL still include candidate name, direction label, epoch number, pass/eliminate result as before

When a candidate passes the global final assessment:
- The system SHALL promote the candidate from CANDIDATE to MEMBER role
- The system SHALL asynchronously send a GitHub organization invitation to the candidate
- The GitHub invitation SHALL NOT block the role promotion or email notification
- The GitHub invitation SHALL be sent by `githubId` if bound, otherwise by `email`

#### Scenario: Direction administrator publishes results
- **WHEN** a direction administrator calls the publish API with a valid `assessmentTimeId`
- **AND** there are candidates with decisions (`passed` is not null) for that assessment time
- **THEN** the system SHALL set `results_published_at` to the current timestamp for that assessment time
- **AND** the system SHALL send an HTML email to each decided candidate containing their name, assessment direction, epoch number, pass/eliminate result, and optionally their finalized score
- **AND** return the count of emails sent

#### Scenario: Direction administrator publishes global assessment results
- **WHEN** a direction administrator calls the publish API for a global assessment time
- **THEN** the system SHALL correctly identify whether this is the final round based on global maximum epoch
- **AND** send email notifications with appropriate direction label

#### Scenario: Global assessment email direction label
- **WHEN** results are published for a global assessment
- **THEN** the email SHALL use "全局" as the direction label in the notification content

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

#### Scenario: Candidate promoted and invited to GitHub
- **WHEN** a candidate passes the global final assessment
- **AND** the candidate is promoted from CANDIDATE to MEMBER
- **THEN** the system SHALL asynchronously send a GitHub organization invitation
- **AND** the invitation SHALL use `githubId` if the user has bound GitHub, otherwise use `email`
- **AND** the candidate SHALL be assigned to the GitHub team matching their direction

#### Scenario: GitHub invitation failure does not affect publish
- **WHEN** a candidate passes the global final assessment
- **AND** the GitHub organization invitation fails
- **THEN** the role promotion SHALL still succeed
- **AND** the email notification SHALL still be sent
- **AND** the failure SHALL be logged

### Requirement: Frontend publish button integration
The system SHALL replace the placeholder publish button notification with an actual API call that triggers email dispatch.

#### Scenario: User clicks publish button
- **WHEN** an authorized user clicks the "发布本轮结果" button
- **THEN** the frontend SHALL call the publish API and display the number of emails sent on success
- **OR** display an error message on failure
- **AND** upon successful publication, the frontend SHALL refresh the assessment data to reflect the published state
