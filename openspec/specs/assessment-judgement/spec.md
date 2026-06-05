## ADDED Requirements

### Requirement: Authority judgement source for administrative finalization
The system SHALL support a new judgement source `ADMIN_FINALIZED` to represent scores set by direction administrators after multi-reviewer deliberation.

#### Scenario: Finalized judgement is recorded
- **WHEN** a direction administrator confirms a final score for a file upload answer
- **THEN** the system SHALL store a judgement with source `ADMIN_FINALIZED`, reviewer id set to the administrator, and the confirmed score

## MODIFIED Requirements

### Requirement: Unified question judgement records
The system SHALL record a question-level judgement for each assessed answer, including question id, answer id, candidate id, score, max score, judgement status, normalized result code, judgement source, reviewer identity when applicable, comment, and timestamps.

#### Scenario: Automatic judgement is recorded
- **WHEN** a single choice, multiple choice, or algorithm answer is evaluated by the system
- **THEN** the system SHALL store a judgement with source `AUTO`, reviewer type `SYSTEM`, and a normalized result code

#### Scenario: Algorithm automatic judgement is recorded by Judge Service
- **WHEN** the independent Judge Service completes a formal algorithm judge job
- **THEN** it SHALL directly create or update the automatic assessment judgement record for the answer

#### Scenario: Manual judgement is recorded
- **WHEN** an authorized member scores a file upload answer
- **THEN** the system SHALL store a judgement with source `MANUAL`, reviewer id, score, comment, and judgement time

### Requirement: Objective judgement result codes
The system SHALL use normalized result codes for automatically judged objective answers so that pass rates and result distributions can be aggregated consistently.

#### Scenario: Choice answer result code
- **WHEN** a single choice or multiple choice answer is automatically judged
- **THEN** the system SHALL record result code `AC` for a correct answer or `WA` for an incorrect answer

#### Scenario: Algorithm answer result code
- **WHEN** an algorithm answer is automatically judged by the independent Judge Service
- **THEN** the system SHALL record one primary result code from `AC`, `WA`, `TLE`, `RE`, `CE`, or `MLE`

#### Scenario: Choice answer is judged synchronously
- **WHEN** a single choice or multiple choice answer is submitted
- **THEN** the system SHALL synchronously record result code `AC` or `WA` without creating a pending judgement result

#### Scenario: Judge infrastructure failure is not a result code
- **WHEN** a sandbox, queue, OSS, database, or Judge Service infrastructure failure prevents an algorithm job from completing a valid judgement
- **THEN** the system SHALL NOT record an objective judgement result code for the candidate and SHALL keep the judge job retryable or flagged for operational review

### Requirement: Manual review for file upload answers
The system SHALL allow users with team member or higher permission to score and comment on file upload answers.

#### Scenario: Member scores file upload answer
- **WHEN** a team member submits a valid score and comment for a file upload answer
- **THEN** the system SHALL save the manual judgement and expose it in the answer review view
- **AND** the system SHALL also save a comment record in `tb_comment` for multi-reviewer visibility

#### Scenario: Candidate cannot score file upload answer
- **WHEN** a candidate attempts to score any file upload answer
- **THEN** the system SHALL reject the operation with a forbidden response

### Requirement: Objective judgements are read-only to humans
The system SHALL prevent users from manually changing the score of automatically judged single choice, multiple choice, and algorithm answers.

#### Scenario: Member attempts to modify automatic judgement score
- **WHEN** a team member submits a manual score update for a single choice, multiple choice, or algorithm answer
- **THEN** the system SHALL reject the operation and preserve the automatic judgement

### Requirement: Judgement result visibility
The system SHALL allow candidates to view their own judgement results and allow team members or higher roles to view candidate judgement results within their authorized scope.

#### Scenario: Candidate views own result
- **WHEN** a candidate requests the judgement result for their submitted answer
- **THEN** the system SHALL return that candidate's judgement result

#### Scenario: Member views candidate result
- **WHEN** a team member requests judgement results within their authorized assessment scope
- **THEN** the system SHALL return the matching candidate judgement results

### Requirement: Assessment pass decision
The system SHALL allow direction administrators or higher roles to set the final pass decision for a candidate assessment based on the question judgement results.

#### Scenario: Direction administrator marks candidate as passed
- **WHEN** a direction administrator sets a candidate assessment decision to passed
- **THEN** the system SHALL save the decision, decision maker, decision time, and optional decision comment
- **AND** if the assessment time does not yet have `results_published_at` set, the system SHALL set it to the current time

#### Scenario: Member cannot set final decision
- **WHEN** a team member attempts to set the final pass decision
- **THEN** the system SHALL reject the operation with a forbidden response

#### Scenario: Eliminated candidate from prior epoch is excluded from decision workspace
- **WHEN** a direction administrator queries the decision workspace for an assessment time
- **AND** a candidate has a `passed = false` decision for a prior epoch of the same direction and grade combination
- **THEN** the system SHALL exclude that candidate from the workspace candidate list
- **AND** the system SHALL exclude that candidate from the workspace statistics
- **AND** the system SHALL still include candidates with a `passed = false` decision for the current epoch
