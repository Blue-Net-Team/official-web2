## ADDED Requirements

### Requirement: Extended algorithm content model
The system SHALL allow administrators to configure algorithm question content with statement content, input description, output description, constraints, examples, default run testcases, formal testcases, starter code templates, time limit, and memory limit.

#### Scenario: Create algorithm question with extended content
- **WHEN** an administrator creates an algorithm question with valid extended content
- **THEN** the system SHALL persist the content as polymorphic algorithm question JSON

#### Scenario: Missing starter code
- **WHEN** an administrator creates an algorithm question without any starter code language template
- **THEN** the system SHALL reject the request because at least one language template is required

### Requirement: Algorithm testcase categories
The system SHALL distinguish algorithm examples, default run testcases, and formal submission testcases.

#### Scenario: Configure separate testcase groups
- **WHEN** an administrator configures examples, run testcases, and formal testcases with different inputs
- **THEN** the system SHALL persist each group separately and preserve their intended usage

### Requirement: Candidate-safe algorithm content
The system SHALL return only candidate-visible algorithm content to candidates and MUST NOT expose formal testcase data before a formal judgement result is available.

#### Scenario: Candidate views algorithm question before submission
- **WHEN** a candidate requests algorithm question detail before submitting
- **THEN** the system SHALL return statement content, descriptions, constraints, examples, starter code, and limits without formal testcase expected outputs

#### Scenario: Administrator views algorithm question
- **WHEN** an administrator requests algorithm question detail
- **THEN** the system SHALL return the full algorithm content including formal testcases
