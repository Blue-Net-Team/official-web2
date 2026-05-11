## ADDED Requirements

### Requirement: Authority judgement source for administrative finalization
The system SHALL support a new judgement source `ADMIN_FINALIZED` to represent scores set by direction administrators after multi-reviewer deliberation.

#### Scenario: Finalized judgement is recorded
- **WHEN** a direction administrator confirms a final score for a file upload answer
- **THEN** the system SHALL store a judgement with source `ADMIN_FINALIZED`, reviewer id set to the administrator, and the confirmed score

## MODIFIED Requirements

### Requirement: Manual review for file upload answers
The system SHALL allow users with team member or higher permission to score and comment on file upload answers.

#### Scenario: Member scores file upload answer
- **WHEN** a team member submits a valid score and comment for a file upload answer
- **THEN** the system SHALL save the manual judgement and expose it in the answer review view
- **AND** the system SHALL also save a comment record in `tb_comment` for multi-reviewer visibility

#### Scenario: Candidate cannot score file upload answer
- **WHEN** a candidate attempts to score any file upload answer
- **THEN** the system SHALL reject the operation with a forbidden response

### Requirement: Assessment pass decision
The system SHALL allow direction administrators or higher roles to set the final pass decision for a candidate assessment based on the question judgement results.

#### Scenario: Direction administrator marks candidate as passed
- **WHEN** a direction administrator sets a candidate assessment decision to passed
- **THEN** the system SHALL save the decision, decision maker, decision time, and optional decision comment
- **AND** if the assessment time does not yet have `results_published_at` set, the system SHALL set it to the current time

#### Scenario: Member cannot set final decision
- **WHEN** a team member attempts to set the final pass decision
- **THEN** the system SHALL reject the operation with a forbidden response
