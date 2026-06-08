## MODIFIED Requirements

### Requirement: Manual review for file upload answers
The system SHALL allow users with team member or higher permission to score file upload answers, with support for team-wide or individual member scoring.

#### Scenario: Member scores file upload answer
- **WHEN** a team member submits a valid score for a file upload answer
- **THEN** the system SHALL save a manual judgement and expose it in the answer review view
- **AND** the system SHALL also save a comment record in `tb_comment` for multi-reviewer visibility

#### Scenario: Member scores team member individually
- **WHEN** a team member submits a score for a specific team member's FILE_UPLOAD answer
- **THEN** the system SHALL save a manual judgement for that member only
- **AND** other team members' judgements SHALL remain unchanged

#### Scenario: Candidate cannot score file upload answer
- **WHEN** a candidate attempts to score any file upload answer
- **THEN** the system SHALL reject the operation with a forbidden response

### Requirement: Team member judgement auto-creation
The system SHALL create judgements for all team members when an automatic or finalized judgement is recorded for a team leader's answer in a team-enabled assessment.

#### Scenario: Automatic objective judgement propagates to team members
- **WHEN** the system creates an automatic judgement (AUTO source) for a team leader's single choice, multiple choice, or algorithm answer
- **AND** the assessment allows teams
- **THEN** the system SHALL create an identical automatic judgement for each team member with the same score, result code, and max score

#### Scenario: Admin finalized judgement propagates to team members
- **WHEN** a direction administrator confirms a final score for a team leader's FILE_UPLOAD answer
- **THEN** the system SHALL create an `ADMIN_FINALIZED` judgement for the leader
- **AND** the system SHALL create an identical `ADMIN_FINALIZED` judgement for each team member
