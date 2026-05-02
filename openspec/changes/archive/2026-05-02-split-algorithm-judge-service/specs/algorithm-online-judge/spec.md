## MODIFIED Requirements

### Requirement: Algorithm question standard input output mode
The system SHALL support algorithm questions using standard input and standard output.

#### Scenario: Candidate opens algorithm question
- **WHEN** a candidate opens an algorithm question detail page
- **THEN** the system SHALL provide candidate-visible statement content, input description, output description, constraints, examples, configured language templates, and confirmed per-language limits

### Requirement: Starter code defines allowed languages
The system SHALL treat the keys of algorithm question `starterCode` as the candidate-visible language templates and SHALL allow formal submission only for languages that also have confirmed judge language limits for the question.

#### Scenario: Candidate submits configured language
- **WHEN** an algorithm question has `starterCode.python` and the candidate submits language `python` with a confirmed `python` judge language limit
- **THEN** the system SHALL accept the language for validation and judging

#### Scenario: Candidate submits unconfigured language
- **WHEN** an algorithm question only has `starterCode.python` and the candidate submits language `java`
- **THEN** the system SHALL reject the request because the language is not supported by the question

#### Scenario: Candidate submits language without confirmed limit
- **WHEN** an algorithm question has `starterCode.python` but no confirmed `python` judge language limit
- **THEN** the system SHALL reject the formal submission because the language is not ready for judging

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
The system SHALL create an asynchronous judge job when a candidate submits an algorithm answer for grading and SHALL publish only the judge job id to the queue.

#### Scenario: Submit algorithm answer
- **WHEN** a candidate submits code and a supported language for an algorithm question
- **THEN** the system SHALL save the answer, create a judge job with status `PENDING` and a `source_code` snapshot, publish a queue message containing the job id, and return a job id

#### Scenario: Judge worker completes job
- **WHEN** the Judge Service finishes all formal testcases for a judge job
- **THEN** the system SHALL save the case results, update the job status, and create or update the automatic judgement

### Requirement: Sandbox execution
The Judge Service SHALL execute generator code, standard solution code, and candidate code in an isolated sandbox with CPU, memory, wall-clock time, process, filesystem, network, generated-file-size, and output-size limits.

#### Scenario: Code exceeds time limit
- **WHEN** candidate code runs longer than the configured language-specific time limit
- **THEN** the Judge Service SHALL terminate the execution and mark the case as time limit exceeded

#### Scenario: Code attempts network access
- **WHEN** candidate code attempts to access the network from the sandbox
- **THEN** the sandbox SHALL prevent the access

#### Scenario: Sandbox infrastructure failure
- **WHEN** the sandbox or Judge Service infrastructure fails before producing a valid candidate-code result
- **THEN** the judge job SHALL remain retryable or be flagged for operational review and SHALL NOT create an algorithm judgement result code

### Requirement: Hidden testcase failure details are visible
The system SHALL show failed hidden testcase input, expected output, and actual output to the candidate after formal submission judging completes.

#### Scenario: Hidden testcase wrong answer
- **WHEN** a submitted algorithm answer fails a hidden testcase
- **THEN** the candidate judgement result SHALL include the failed testcase input, expected output, actual output, and failure status

### Requirement: Judge result polling
The system SHALL provide a polling API for algorithm run and submit jobs through the Backend.

#### Scenario: Poll running job
- **WHEN** the frontend polls a judge job that is still running
- **THEN** the system SHALL return the current job status without final case results

#### Scenario: Poll completed job
- **WHEN** the frontend polls a completed judge job
- **THEN** the system SHALL return the final job status, case results, and judgement summary when applicable

## ADDED Requirements

### Requirement: Judge Service is an independent runtime
The system SHALL run formal algorithm judging in an independently deployed Judge Service rather than inside the Backend process.

#### Scenario: Backend creates judge task
- **WHEN** the Backend accepts a formal algorithm submission
- **THEN** it SHALL persist the answer and judge job, then publish the judge job id to RabbitMQ without executing candidate code

#### Scenario: Judge Service consumes judge task
- **WHEN** the Judge Service receives a judge job id
- **THEN** it SHALL load the job, current testcases, and confirmed language limits from the database before executing the candidate code

### Requirement: Formal judging uses OSS testcases
The Judge Service SHALL use generated testcase input and output objects from the judge OSS bucket for formal judging.

#### Scenario: Execute formal testcase from OSS
- **WHEN** the Judge Service judges a formal submission
- **THEN** it SHALL download each configured testcase input and expected output from the judge bucket and execute the candidate code against those files

#### Scenario: Missing testcase object
- **WHEN** a required testcase input or output object is missing from OSS
- **THEN** the Judge Service SHALL mark the judge job as requiring operational review and SHALL NOT create a candidate result code
