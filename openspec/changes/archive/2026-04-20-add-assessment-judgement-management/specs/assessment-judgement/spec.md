## ADDED Requirements

### Requirement: Assessment judgement management scope selection
The system SHALL require administrators to select an assessment direction and assessment time before showing assessment judgement management data.

#### Scenario: Initial score page load
- **WHEN** an administrator opens the assessment score management page
- **THEN** the system SHALL show direction and assessment time filters and SHALL NOT load question submissions until an assessment time is selected

#### Scenario: Direction administrator scope
- **WHEN** a direction administrator opens the assessment score management page
- **THEN** the system SHALL restrict the direction filter to that administrator's own direction

#### Scenario: Assessment time selection
- **WHEN** an administrator selects a direction
- **THEN** the system SHALL load assessment time options for that direction and allow the administrator to select one assessment time

### Requirement: Question score management question view
The system SHALL provide a question-oriented score management view for the selected assessment time.

#### Scenario: Question view lists questions
- **WHEN** an administrator selects an assessment time and opens the question view
- **THEN** the system SHALL list the assessment questions for that assessment time with question number, title, question type, max score, submitted count, judged count, pending count, and average score

#### Scenario: Question view filters by question type
- **WHEN** an administrator selects a question type filter
- **THEN** the system SHALL update the question list to include only matching question types in the selected assessment time

#### Scenario: Question submission table has no operation column
- **WHEN** an administrator selects a question in the question view
- **THEN** the system SHALL show a submission table without an operation column and SHALL allow row click to open the submission review drawer

#### Scenario: Submission table row opens review drawer
- **WHEN** an administrator clicks a submission table row
- **THEN** the system SHALL open an AntD Drawer showing candidate information, submission information, display judgement, score input, and comment input

#### Scenario: Algorithm submission history expands
- **WHEN** an administrator expands an algorithm submission row with multiple judged records
- **THEN** the system SHALL show all returned history records and mark the display judgement record

### Requirement: Manual file upload score drawer
The system SHALL allow authorized reviewers to score file upload submissions from the right-side review drawer.

#### Scenario: Reviewer enters score manually
- **WHEN** an authorized team member or higher role reviews a file upload submission
- **THEN** the drawer SHALL provide an input field for the numeric score instead of score selection buttons

#### Scenario: Score input is bounded by max score
- **WHEN** the drawer shows a file upload submission with a known max score
- **THEN** the score input SHALL prevent values below 0 or above the question max score on the client and the backend SHALL reject invalid values

#### Scenario: Reviewer enters comment
- **WHEN** an authorized reviewer scores a file upload submission
- **THEN** the drawer SHALL label the text field as "评论" and save it as the judgement comment

#### Scenario: Objective submission is read-only
- **WHEN** an administrator opens a single choice, multiple choice, or algorithm submission in the review drawer
- **THEN** the system SHALL show the automatic judgement as read-only and SHALL NOT allow manual score submission

### Requirement: Candidate score management view
The system SHALL provide a candidate-oriented score management view for the selected assessment time.

#### Scenario: Candidate view lists score matrix
- **WHEN** an administrator opens the candidate view for a selected assessment time
- **THEN** the system SHALL list candidates with their total score, max score, judged question count, pending judgement count, and per-question score states

#### Scenario: Candidate search
- **WHEN** an administrator searches by candidate name or student id
- **THEN** the system SHALL update the candidate score view to include only matching candidates in the selected assessment time

#### Scenario: Candidate score detail opens drawer
- **WHEN** an administrator clicks a candidate or one of the candidate's question score items
- **THEN** the system SHALL show that candidate's relevant submission and judgement details without leaving the page

### Requirement: Assessment judgement aggregation APIs
The system SHALL expose management APIs that aggregate assessment questions, submissions, display judgements, submission histories, candidates, and decisions for a selected assessment time.

#### Scenario: Query question scoreboard
- **WHEN** an administrator queries the question scoreboard for an assessment time
- **THEN** the system SHALL return questions with submission and judgement summary metrics scoped to that assessment time

#### Scenario: Query question submissions
- **WHEN** an administrator queries submissions for a question
- **THEN** the system SHALL return each candidate's latest answer summary, file id when present, submit time, display judgement, and history records for that question

#### Scenario: Algorithm submission display uses best judged record
- **WHEN** an algorithm candidate has multiple judged records for the same question
- **THEN** the system SHALL use the highest score judged record as the display judgement
- **AND** the system SHALL mark that record in the returned history records

#### Scenario: Query candidate scoreboard
- **WHEN** an administrator queries the candidate scoreboard for an assessment time
- **THEN** the system SHALL return candidate identity fields, per-question submission and judgement states, total score, max score, and pending judgement count

#### Scenario: Direction scope is enforced
- **WHEN** a direction administrator queries any assessment judgement aggregation API for another direction
- **THEN** the system SHALL reject the request or return no out-of-scope data

### Requirement: Assessment decision workspace
The system SHALL provide a decision workspace for direction administrators or higher roles to decide which candidates pass a selected assessment time.

#### Scenario: Decision workspace statistics
- **WHEN** an authorized decision maker opens the decision workspace for an assessment time
- **THEN** the system SHALL show exactly these statistic cards: candidates, pending decisions, passed, and eliminated

#### Scenario: Decision workspace candidate list
- **WHEN** an authorized decision maker opens the decision workspace for an assessment time
- **THEN** the system SHALL list candidates with identity fields, total score, judged question count, pending judgement count, and current decision status

#### Scenario: Decision status filter
- **WHEN** an authorized decision maker selects a decision status filter
- **THEN** the system SHALL update the candidate list to show matching pending, passed, or eliminated candidates

#### Scenario: Decision details
- **WHEN** an authorized decision maker selects a candidate in the decision workspace
- **THEN** the system SHALL show the candidate's score evidence, question results, current decision, and optional decision comment

### Requirement: Automatic save of assessment pass decisions
The system SHALL automatically save an assessment pass decision when an authorized decision maker clicks pass or eliminate.

#### Scenario: Click pass
- **WHEN** a direction administrator or higher role clicks "通过" for a candidate in a selected assessment time
- **THEN** the system SHALL save a passed decision for that candidate and refresh the candidate's decision status

#### Scenario: Click eliminate
- **WHEN** a direction administrator or higher role clicks "淘汰" for a candidate in a selected assessment time
- **THEN** the system SHALL save a not-passed decision for that candidate and refresh the candidate's decision status

#### Scenario: Existing decision is overwritten
- **WHEN** an authorized decision maker changes a candidate from passed to eliminated or from eliminated to passed
- **THEN** the system SHALL overwrite the existing decision for the same candidate and assessment time with the new decision, decision maker, decision time, and optional comment

#### Scenario: Team member cannot decide
- **WHEN** a team member attempts to save a pass or eliminate decision
- **THEN** the system SHALL reject the operation with a forbidden response

### Requirement: Assessment result publish entry
The system SHALL provide a visible publish entry for the selected assessment time without sending result emails in this change.

#### Scenario: Publish entry is visible after selecting assessment time
- **WHEN** an authorized decision maker opens the decision workspace for a selected assessment time
- **THEN** the system SHALL show a "发布本轮结果" entry near the candidate list actions

#### Scenario: Publish entry does not send email
- **WHEN** an authorized decision maker activates the publish entry before the notification backend is implemented
- **THEN** the system SHALL clearly indicate that result notification delivery is not implemented and SHALL NOT send emails

#### Scenario: Publish entry does not replace decisions
- **WHEN** an authorized decision maker uses pass or eliminate decisions and then opens the publish entry
- **THEN** the system SHALL preserve all saved candidate decisions and SHALL NOT create or modify decisions from the publish entry itself
