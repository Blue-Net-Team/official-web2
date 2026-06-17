## MODIFIED Requirements

### Requirement: Independent scoring for team members
The system SHALL allow direction administrators to score each team member independently, while providing a default option to apply the leader's score to members who have not yet been judged.

#### Scenario: Administrator scores entire team with same score on first finalize
- **WHEN** a direction administrator confirms a final score for a team leader's FILE_UPLOAD answer
- **AND** the leader does not yet have an `ADMIN_FINALIZED` judgement
- **THEN** the system SHALL create an `ADMIN_FINALIZED` judgement for the leader
- **AND** the system SHALL automatically create an identical `ADMIN_FINALIZED` judgement for each team member who does not yet have an `ADMIN_FINALIZED` judgement for their answer
- **AND** team members who already have an `ADMIN_FINALIZED` judgement SHALL remain unchanged

#### Scenario: Administrator re-finalizes score for team leader
- **WHEN** a direction administrator confirms a final score for a team leader's FILE_UPLOAD answer
- **AND** the leader already has an `ADMIN_FINALIZED` judgement
- **THEN** the system SHALL update the leader's existing `ADMIN_FINALIZED` judgement with the new score
- **AND** the system SHALL NOT modify any team member's judgement

#### Scenario: Administrator adjusts individual member score
- **WHEN** a direction administrator modifies the score for a specific team member's answer
- **THEN** the system SHALL create or update an `ADMIN_FINALIZED` judgement for that member only
- **AND** other team members' judgements SHALL remain unchanged
