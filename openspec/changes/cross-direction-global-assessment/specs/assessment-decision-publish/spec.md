## MODIFIED Requirements

### Requirement: Publish assessment decision results via email (global assessment support)

The system SHALL allow direction administrators or higher roles to send email notifications to all decided candidates for a given assessment time.

For global assessments (`direction = null, grade = null`):
- The final round determination SHALL NOT depend on direction or grade — the system SHALL query `MAX(epoch)` unconditionally
- The email notification SHALL handle `direction = null` gracefully: use a generic label (e.g., "全局") as the direction name
- The email content SHALL still include candidate name, direction label, epoch number, pass/eliminate result as before

#### Scenario: Direction administrator publishes global assessment results
- **WHEN** a direction administrator calls the publish API for a global assessment time
- **THEN** the system SHALL correctly identify whether this is the final round based on global maximum epoch
- **AND** send email notifications with appropriate direction label

#### Scenario: Global assessment email direction label
- **WHEN** results are published for a global assessment
- **THEN** the email SHALL use "全局" as the direction label in the notification content
