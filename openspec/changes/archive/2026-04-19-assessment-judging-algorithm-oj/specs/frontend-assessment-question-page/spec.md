## ADDED Requirements

### Requirement: Algorithm question editor experience for candidates
The assessment question detail page SHALL render an algorithm question workspace with statement, examples, language selector, starter code, code editor, run controls, submit controls, and result panels.

#### Scenario: Candidate opens algorithm question with one template
- **WHEN** the algorithm question provides only a Python starter template
- **THEN** the page SHALL select Python and prevent choosing unconfigured languages

#### Scenario: Candidate opens algorithm question with multiple templates
- **WHEN** the algorithm question provides multiple starter templates
- **THEN** the page SHALL allow the candidate to choose one of the configured languages

### Requirement: Run algorithm code from question page
The assessment question detail page SHALL allow candidates to run algorithm code against system default testcases or custom input without submitting a scored answer.

#### Scenario: Run default testcase
- **WHEN** the candidate clicks run with system default testcase mode
- **THEN** the page SHALL create a run job, poll the job status, and display input, expected output, actual output, and status for each default testcase

#### Scenario: Run custom input
- **WHEN** the candidate clicks run with custom input
- **THEN** the page SHALL create a run job, poll the job status, and display stdout, stderr, status, time, and memory without showing correctness

### Requirement: Submit algorithm answer from question page
The assessment question detail page SHALL allow candidates to submit algorithm code as a formal answer and view the automatic judgement result.

#### Scenario: Submit algorithm answer
- **WHEN** the candidate clicks submit for an algorithm question
- **THEN** the page SHALL send the selected language and code, receive a judge job id, poll until completion, and show the judgement summary

#### Scenario: Hidden testcase fails
- **WHEN** the completed algorithm judgement includes failed hidden testcase details
- **THEN** the page SHALL display the failed input, expected output, actual output, and failure status

### Requirement: Automatic objective result display
The assessment question detail page SHALL display automatic judgement results for single choice, multiple choice, and algorithm answers after submission.

#### Scenario: Choice answer is submitted
- **WHEN** the candidate submits a single choice or multiple choice answer
- **THEN** the page SHALL show whether the answer was judged correct and the awarded score after the backend returns the judgement
