## ADDED Requirements

### Requirement: Objective question pass rate statistics
The system SHALL provide pass rate statistics for objective questions based on the latest formal judgement result per candidate and question.

#### Scenario: Single choice pass rate
- **WHEN** a single choice question has 10 submitted candidate judgements and 7 latest judgements have result code `AC`
- **THEN** the question statistics SHALL report submitted count 10, accepted count 7, and pass rate 0.7

#### Scenario: Multiple choice pass rate
- **WHEN** a multiple choice question has latest judgements with result codes `AC`, `WA`, and `WA`
- **THEN** the question statistics SHALL report submitted count 3, accepted count 1, and pass rate 0.3333 rounded or formatted by the presentation layer

### Requirement: Algorithm result distribution statistics
The system SHALL provide ACM-style result distribution statistics for algorithm questions, including `AC`, `WA`, `TLE`, `RE`, `CE`, and `MLE`.

#### Scenario: Algorithm result distribution
- **WHEN** an algorithm question has latest formal judgements with result codes `AC`, `AC`, `WA`, `TLE`, `RE`, and `CE`
- **THEN** the question statistics SHALL report accepted count 2, submitted count 6, pass rate 0.3333, and distribution counts for each result code

#### Scenario: Memory limit exceeded is counted separately
- **WHEN** an algorithm question has a latest formal judgement with result code `MLE`
- **THEN** the question statistics SHALL count it under `MLE` and SHALL NOT merge it into `RE` or `WA`

#### Scenario: Infrastructure failure is excluded
- **WHEN** an algorithm judge job fails because of sandbox, queue, or worker infrastructure failure before a valid judgement is produced
- **THEN** the question statistics SHALL NOT count that job in submitted count, accepted count, pass rate, or result distribution

### Requirement: Latest formal judgement aggregation
The system SHALL aggregate question statistics using only each candidate's latest formal judgement for the question.

#### Scenario: Candidate has multiple formal submissions
- **WHEN** a candidate has an older `WA` judgement and a newer `AC` judgement for the same algorithm question
- **THEN** the question statistics SHALL count only the newer `AC` judgement for that candidate

#### Scenario: Run jobs do not affect statistics
- **WHEN** a candidate runs algorithm code without formal submission
- **THEN** the run result SHALL NOT affect submitted count, accepted count, pass rate, or result distribution

### Requirement: Question statistics visibility
The system SHALL expose question statistics to authorized users and SHALL keep the access policy explicit for candidate-facing usage.

#### Scenario: Administrator views question statistics
- **WHEN** an administrator requests statistics for a question within their authorized scope
- **THEN** the system SHALL return submitted count, accepted count, pass rate, and result distribution

#### Scenario: Candidate-facing statistics are requested
- **WHEN** candidate-facing question statistics are enabled and a candidate requests a question's statistics
- **THEN** the system SHALL return only aggregate statistics and SHALL NOT expose other candidates' identities or submissions

#### Scenario: Unauthorized statistics request
- **WHEN** a user without permission requests question statistics
- **THEN** the system SHALL reject the request with a forbidden response
