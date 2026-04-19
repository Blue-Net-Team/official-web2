## ADDED Requirements

### Requirement: Algorithm question standard input output mode
The system SHALL support algorithm questions using standard input and standard output.

#### Scenario: Candidate opens algorithm question
- **WHEN** a candidate opens an algorithm question detail page
- **THEN** the system SHALL provide candidate-visible statement content, input description, output description, constraints, examples, configured language templates, and limits

### Requirement: Starter code defines allowed languages
The system SHALL treat the keys of algorithm question `starterCode` as the complete set of languages allowed for that question.

#### Scenario: Candidate submits configured language
- **WHEN** an algorithm question has `starterCode.python` and the candidate submits language `python`
- **THEN** the system SHALL accept the language for validation and judging

#### Scenario: Candidate submits unconfigured language
- **WHEN** an algorithm question only has `starterCode.python` and the candidate submits language `java`
- **THEN** the system SHALL reject the request because the language is not supported by the question

### Requirement: Algorithm run does not affect score
The system SHALL allow candidates to run algorithm code without creating a formal assessment judgement or changing the final answer score.

#### Scenario: Run system default testcase
- **WHEN** a candidate runs code against the algorithm question's default run testcase
- **THEN** the system SHALL execute the code and return per-case status, input, expected output, actual output, time, memory, and errors when present

#### Scenario: Run custom input
- **WHEN** a candidate runs code against custom input without expected output
- **THEN** the system SHALL return execution status, stdout, stderr, time, and memory without marking the result correct or incorrect

### Requirement: Default run testcases are separate from examples
The system SHALL allow algorithm question examples and default run testcases to be configured independently.

#### Scenario: Example differs from default run testcase
- **WHEN** an algorithm question has one example and a different default run testcase
- **THEN** the candidate-facing statement SHALL show the example while the run action SHALL execute the default run testcase

### Requirement: Algorithm submit creates asynchronous judge job
The system SHALL create an asynchronous judge job when a candidate submits an algorithm answer for grading.

#### Scenario: Submit algorithm answer
- **WHEN** a candidate submits code and a supported language for an algorithm question
- **THEN** the system SHALL save the answer, create a judge job with status `PENDING`, publish a queue message, and return a job id

#### Scenario: Judge worker completes job
- **WHEN** the Judge Worker finishes all formal testcases for a judge job
- **THEN** the system SHALL save the case results, update the job status, and create or update the automatic judgement

### Requirement: Sandbox execution
The Judge Worker SHALL execute candidate code in an isolated sandbox with CPU, memory, wall-clock time, process, filesystem, network, and output-size limits.

#### Scenario: Code exceeds time limit
- **WHEN** candidate code runs longer than the configured time limit
- **THEN** the Judge Worker SHALL terminate the execution and mark the case as time limit exceeded

#### Scenario: Code attempts network access
- **WHEN** candidate code attempts to access the network from the sandbox
- **THEN** the sandbox SHALL prevent the access

#### Scenario: Sandbox infrastructure failure
- **WHEN** the sandbox or Judge Worker infrastructure fails before producing a valid candidate-code result
- **THEN** the judge job SHALL remain retryable or be flagged for operational review and SHALL NOT create an algorithm judgement result code

### Requirement: Hidden testcase failure details are visible
The system SHALL show failed hidden testcase input, expected output, and actual output to the candidate after formal submission judging completes.

#### Scenario: Hidden testcase wrong answer
- **WHEN** a submitted algorithm answer fails a hidden testcase
- **THEN** the candidate judgement result SHALL include the failed testcase input, expected output, actual output, and failure status

### Requirement: Judge result polling
The system SHALL provide a polling API for algorithm run and submit jobs.

#### Scenario: Poll running job
- **WHEN** the frontend polls a judge job that is still running
- **THEN** the system SHALL return the current job status without final case results

#### Scenario: Poll completed job
- **WHEN** the frontend polls a completed judge job
- **THEN** the system SHALL return the final job status, case results, and judgement summary when applicable
