## MODIFIED Requirements

### Requirement: Test data updates replace current configuration
The system SHALL maintain only the current test data configuration for each algorithm question and SHALL replace current testcase records and language limits when new test data is published. Because testcase object keys are content addressed, a regeneration that produces unchanged content yields the same object keys as the previous generation; the system MUST clear the previous generation's testcase objects before generating, so that the objects written during the current generation are never the ones removed.

#### Scenario: Publish replacement test data
- **WHEN** an administrator publishes newly generated test data for a question
- **THEN** the system SHALL replace the current problem configuration, testcase records, and language limits for that question

#### Scenario: Rejudge uses latest data
- **WHEN** an existing submission is rejudged after test data has been replaced
- **THEN** the system SHALL use the latest published testcases and language limits

#### Scenario: Regenerate with unchanged generator content
- **WHEN** an administrator triggers test data generation again and the generator source and `generatorArgs` are unchanged so that the generated inputs and expected outputs are byte identical to the previous generation
- **THEN** the system SHALL remove the previous generation's testcase objects before running the generator, SHALL write the current generation's input and output objects afterwards, and the benchmark step SHALL read those objects successfully

#### Scenario: Testcase object removal precedes object writes
- **WHEN** the system processes a test data generation task for a configuration that already has testcase records
- **THEN** every removal of a previous testcase object SHALL occur before any object write performed by that same task

#### Scenario: Generation failure supersedes previous assets
- **WHEN** the generator or the standard solution fails, times out, or exceeds resource limits during a regeneration
- **THEN** the system SHALL mark the test data configuration as failed and SHALL NOT restore the previous generation's testcase assets, so that a failed regeneration always leaves the configuration in a failed state rather than falling back to the earlier testcase set

#### Scenario: Object removal failure does not abort generation
- **WHEN** removing a previous testcase object fails for any reason
- **THEN** the system SHALL log the failure and continue the generation task instead of failing the configuration
