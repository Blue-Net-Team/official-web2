## ADDED Requirements

### Requirement: Team member answer auto-creation
The system SHALL automatically create an answer record for each team member when the team leader submits a FILE_UPLOAD answer for a team-enabled assessment.

#### Scenario: Leader submits and member answers are created
- **WHEN** a team leader submits a FILE_UPLOAD answer for a question in a team-enabled assessment
- **THEN** the system SHALL create an answer record for the leader (existing behavior)
- **AND** the system SHALL create an identical answer record for each team member with the same `question_id`, `content`, `file_id`, `language`, `submit_time`, and `team_id`
- **AND** all answer records SHALL be created within a single transaction

#### Scenario: Leader updates and member answers are synchronized
- **WHEN** a team leader updates a FILE_UPLOAD answer for a team-enabled assessment
- **THEN** the system SHALL update the leader's answer record (existing behavior)
- **AND** the system SHALL synchronize the same `content`, `file_id`, `language`, and `submit_time` to all team members' answer records for the same question

### Requirement: Team-expanded scoring queries
The system SHALL include team members in scoring queries by expanding team answers through `team_id` when the assessment allows teams.

#### Scenario: Question submissions include team members
- **WHEN** a direction administrator queries question submissions for a FILE_UPLOAD question in a team-enabled assessment
- **THEN** the system SHALL return submissions for all candidates who have an answer for that question, including team members whose answers were auto-created from the leader's submission
- **AND** each submission SHALL display the candidate's own user information (name, student id)

#### Scenario: Candidate scoreboard includes team members
- **WHEN** a direction administrator queries the candidate scoreboard for a team-enabled assessment
- **THEN** the system SHALL return scoring data for all candidates, including team members
- **AND** team members SHALL show the same submitted/judged status as the leader for FILE_UPLOAD questions
- **AND** team members SHALL show their own independent scores if individually judged

#### Scenario: Question scoreboard counts team members
- **WHEN** the system aggregates question-level statistics (submitted count, judged count, pending count, average score)
- **AND** the assessment allows teams
- **THEN** the system SHALL count all team members' answer records in the statistics

### Requirement: Independent scoring for team members
The system SHALL allow direction administrators to score each team member independently, while providing a default option to apply the leader's score to all members.

#### Scenario: Administrator scores entire team with same score
- **WHEN** a direction administrator confirms a final score for a team leader's FILE_UPLOAD answer
- **THEN** the system SHALL create an `ADMIN_FINALIZED` judgement for the leader
- **AND** the system SHALL automatically create an identical `ADMIN_FINALIZED` judgement for each team member with the same score

#### Scenario: Administrator adjusts individual member score
- **WHEN** a direction administrator modifies the score for a specific team member's answer
- **THEN** the system SHALL create or update an `ADMIN_FINALIZED` judgement for that member only
- **AND** other team members' judgements SHALL remain unchanged

### Requirement: Global assessment scoring access
The system SHALL allow scoring and decision-making for global assessments (direction=null) by skipping direction-based scope restrictions.

#### Scenario: Super admin accesses global assessment scoring
- **WHEN** a super admin selects "global" direction in the scoring page
- **THEN** the system SHALL load all global assessment times (direction is null)
- **AND** the system SHALL display candidates from all directions

#### Scenario: Direction admin scores global assessment
- **WHEN** a direction admin accesses a global assessment scoring page
- **THEN** the system SHALL allow access without direction permission check
- **AND** the system SHALL display all candidates regardless of direction