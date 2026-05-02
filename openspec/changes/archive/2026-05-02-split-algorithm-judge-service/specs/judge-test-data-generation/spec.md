## ADDED Requirements

### Requirement: Judge test data assets use isolated OSS storage
The system SHALL store algorithm judge generator files, standard solution files, generated manifest files, generated test inputs, generated expected outputs, and optional judge logs in a dedicated judge OSS bucket on the same OSS service used by the main application, and that bucket is not directly accessible to candidates.

#### Scenario: Store uploaded generator and standard solutions
- **WHEN** an administrator uploads a generator and standard solutions for an algorithm question
- **THEN** the system SHALL store those files under the judge bucket using the shared OSS connection and persist their object keys and hashes in the database

#### Scenario: Candidate cannot access judge bucket
- **WHEN** a candidate requests a judge test data object directly or through normal business file APIs
- **THEN** the system SHALL deny access to the judge bucket object

### Requirement: Backend generates manifest from admin configuration
The system SHALL generate the test data manifest from administrator-provided generator metadata, standard solution metadata, testcase configuration, and benchmark configuration.

#### Scenario: Generate manifest for testcase configuration
- **WHEN** an administrator submits generator metadata, standard solutions, testcase categories, generator arguments, weights, hidden flags, sample flags, and benchmark settings
- **THEN** the Backend SHALL generate a manifest, store it in the judge bucket, and persist the manifest object key

#### Scenario: Reject incomplete test data configuration
- **WHEN** an administrator attempts to generate a manifest without a generator, at least one standard solution, or at least one testcase configuration
- **THEN** the system SHALL reject the request with a validation error

### Requirement: Testcase categories are recorded
The system SHALL allow each testcase configuration to record a category describing the purpose of the testcase.

#### Scenario: Configure common testcase categories
- **WHEN** an administrator configures testcases for an algorithm question
- **THEN** the system SHALL allow categories including `SAMPLE`, `NORMAL`, `EDGE`, `EMPTY`, `MINIMUM`, `MAXIMUM`, `LARGE`, `RANDOM`, `WORST_CASE`, `SPECIAL`, and `REGRESSION`

#### Scenario: Persist testcase generation metadata
- **WHEN** the system creates a testcase record
- **THEN** it SHALL persist the testcase number, name, category, group name, generator arguments, input object key, output object key, weight, hidden flag, and sample flag

### Requirement: Judge Service generates test input and expected output
The Judge Service SHALL generate formal testcase input files by running the configured generator in the sandbox and SHALL generate expected output files by running a configured standard solution in the sandbox.

#### Scenario: Generate input output pair
- **WHEN** the Judge Service processes a test data generation task
- **THEN** it SHALL run the generator once per configured testcase, upload each generated input to the judge bucket, run the standard solution against that input, upload the expected output to the judge bucket, and persist testcase records

#### Scenario: Generator fails
- **WHEN** the generator fails, exceeds resource limits, or produces invalid output for any testcase
- **THEN** the system SHALL mark the test data configuration as failed and SHALL NOT publish the generated testcase set

#### Scenario: Standard solution fails while generating output
- **WHEN** the standard solution fails, exceeds resource limits, or produces no valid expected output for any generated input
- **THEN** the system SHALL mark the test data configuration as failed and SHALL NOT publish the generated testcase set

### Requirement: Standard solutions derive per-language limits
The system SHALL derive suggested per-language time and memory limits by benchmarking each configured language standard solution in the Judge Service environment.

#### Scenario: Benchmark standard solution
- **WHEN** test inputs and expected outputs have been generated for a question
- **THEN** the Judge Service SHALL run each configured language standard solution across the generated testcase set multiple times and record p95 time, max time, peak memory, and suggested resource limits

#### Scenario: Persist confirmed language limits
- **WHEN** an administrator confirms benchmark-derived language limits
- **THEN** the system SHALL persist final time, memory, and output limits for each supported language and use those limits for formal judging

#### Scenario: Unsupported language has no limit
- **WHEN** a language has no confirmed language limit for the algorithm question
- **THEN** the system SHALL reject formal submissions in that language

### Requirement: Test data updates replace current configuration
The system SHALL maintain only the current test data configuration for each algorithm question and SHALL replace current testcase records and language limits when new test data is published.

#### Scenario: Publish replacement test data
- **WHEN** an administrator publishes newly generated test data for a question
- **THEN** the system SHALL replace the current problem configuration, testcase records, and language limits for that question

#### Scenario: Rejudge uses latest data
- **WHEN** an existing submission is rejudged after test data has been replaced
- **THEN** the system SHALL use the latest published testcases and language limits
