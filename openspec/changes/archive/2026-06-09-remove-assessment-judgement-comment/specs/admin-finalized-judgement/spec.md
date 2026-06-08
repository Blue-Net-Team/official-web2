## MODIFIED Requirements

### Requirement: Direction administrator can finalize a score for file upload answers
The system SHALL allow direction administrators or higher roles to set a final authoritative score for a file upload answer after reviewing all member comments.

#### Scenario: Direction administrator finalizes score
- **WHEN** a direction administrator submits a final score for a file upload answer
- **THEN** the system SHALL create or update the judgement record for that answer with source `ADMIN_FINALIZED`, the administrator's user id as reviewer, and the provided score
- **AND** the finalized judgement SHALL become the authoritative score displayed in scoreboards and candidate views

#### Scenario: Direction administrator finalizes score without having commented
- **WHEN** a direction administrator attempts to finalize a score but has not yet added their own comment to the answer
- **THEN** the system SHALL reject the operation with a bad request response indicating they must comment first

#### Scenario: Member attempts to finalize score
- **WHEN** a team member attempts to set a final score for an answer
- **THEN** the system SHALL reject the operation with a forbidden response

#### Scenario: Finalized score overrides previous manual judgement
- **WHEN** a direction administrator finalizes a score for an answer that already has a `MANUAL` judgement
- **THEN** the system SHALL insert a new `ADMIN_FINALIZED` judgement that becomes the latest record
- **AND** the previous `MANUAL` judgement SHALL remain in history but not be displayed as the current score
