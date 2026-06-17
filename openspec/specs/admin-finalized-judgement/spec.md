## ADDED Requirements

### Requirement: Direction administrator can finalize a score for file upload answers
The system SHALL allow direction administrators or higher roles to set a final authoritative score for a file upload answer after reviewing all member comments.

#### Scenario: Direction administrator finalizes score
- **WHEN** a direction administrator submits a final score for a file upload answer
- **THEN** the system SHALL create or update the judgement record for that answer with source `ADMIN_FINALIZED`, the administrator's user id as reviewer, and the provided score
- **AND** if the answer already has an `ADMIN_FINALIZED` judgement, the system SHALL update the existing record instead of creating a new one
- **AND** the finalized judgement SHALL become the authoritative score displayed in scoreboards and candidate views

#### Scenario: Direction administrator finalizes score without having commented
- **WHEN** a direction administrator attempts to finalize a score but has not yet added their own comment to the answer
- **THEN** the system SHALL reject the operation with a bad request response indicating they must comment first

#### Scenario: Member attempts to finalize score
- **WHEN** a team member attempts to set a final score for an answer
- **THEN** the system SHALL reject the operation with a forbidden response

#### Scenario: Finalized score overrides previous manual judgement
- **WHEN** a direction administrator finalizes a score for an answer that already has a `MANUAL` judgement
- **THEN** the system SHALL insert or update an `ADMIN_FINALIZED` judgement that becomes the latest record
- **AND** the previous `MANUAL` judgement SHALL remain in history but not be displayed as the current score

#### Scenario: Administrator re-finalizes score for same answer
- **WHEN** a direction administrator submits a final score for an answer that already has an `ADMIN_FINALIZED` judgement
- **THEN** the system SHALL update the existing `ADMIN_FINALIZED` record with the new score and reviewer information
- **AND** the system SHALL NOT create a duplicate `ADMIN_FINALIZED` record

### Requirement: Finalized judgements are distinguishable from manual judgements
The system SHALL track that a finalized judgement originates from a direction administrator's authoritative decision.

#### Scenario: Scoreboard displays finalized judgement
- **WHEN** the scoreboard queries the latest judgement for an answer
- **THEN** the system SHALL return the `ADMIN_FINALIZED` record if present, otherwise the `MANUAL` record
- **AND** the source field SHALL indicate whether the score is finalized or manual
