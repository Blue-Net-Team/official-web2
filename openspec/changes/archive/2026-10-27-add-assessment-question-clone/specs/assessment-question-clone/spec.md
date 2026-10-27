## ADDED Requirements

### Requirement: Administrator clones one question from the management list
The system SHALL provide a single-question clone action in the admin question list and SHALL allow an authorized administrator to select a target assessment time and edit the target question number.

#### Scenario: Open clone dialog
- **WHEN** an administrator clicks the clone action for a question
- **THEN** the system SHALL show the source question title, a target assessment time selector excluding the source assessment time, and an editable positive target question number

#### Scenario: Default target question number
- **WHEN** an administrator selects a target assessment time
- **THEN** the system SHALL set the target question number to one greater than the target's current maximum question number, or to 1 when the target has no questions

#### Scenario: Successful clone from the management list
- **WHEN** an administrator confirms a valid clone request
- **THEN** the system SHALL create the target question, show a success message, close the dialog, and leave the source question unchanged

### Requirement: Clone creates an independent question definition
The system SHALL create a new question ID under the target assessment time and SHALL copy the question type, title, complete content, correct answers, score, and attachment reference without creating a synchronization link to the source question.

#### Scenario: Clone a choice question
- **WHEN** an administrator clones a single-choice or multiple-choice question
- **THEN** the target SHALL contain the same statement, options, correct answer configuration, score, and attachment reference with a new question ID and the requested target question number

#### Scenario: Clone algorithm statement content
- **WHEN** an administrator clones an algorithm question
- **THEN** the target SHALL contain independent copies of the statement, input and output descriptions, constraints, examples, default run testcases, and starter code templates

#### Scenario: Edit source and target independently
- **WHEN** an administrator edits or deletes either the source question or its clone after cloning
- **THEN** the other question's definition and assessment time association SHALL remain unchanged

### Requirement: Clone excludes candidate and assessment runtime data
The system MUST NOT copy candidate answers, submission histories, judgements, comments, statistics, assessment sessions, teams, or other runtime data associated with the source question or assessment time.

#### Scenario: Source question has candidate activity
- **WHEN** an administrator clones a question that has answers, judgements, statistics, and assessment sessions
- **THEN** the new question SHALL have no copied candidate activity and all source activity SHALL remain associated with its original question and assessment time

### Requirement: Clone validates target and question number
The system SHALL reject a clone when the source question or target assessment time does not exist, when source and target assessment times are identical, or when the target question number is already used in the target assessment time.

#### Scenario: Target question number conflicts
- **WHEN** a clone request uses a question number already present in the target assessment time
- **THEN** the system SHALL return a conflict response and SHALL NOT create a partial question or judge configuration

#### Scenario: Target equals source assessment time
- **WHEN** a clone request targets the assessment time that already owns the source question
- **THEN** the system SHALL reject the request and SHALL NOT create a duplicate

#### Scenario: Concurrent target question number conflict
- **WHEN** another administrator occupies the displayed default question number before confirmation
- **THEN** the backend SHALL reject the conflicting submission and the frontend SHALL keep the dialog available for correction

### Requirement: Clone enforces an extensible direction policy
The system SHALL authorize both the source and target assessment times and SHALL currently permit cloning only between assessment times with the same direction while allowing different grades and epochs.

#### Scenario: Direction administrator clones across grades
- **WHEN** a direction administrator clones a question between different grades or epochs within their own direction
- **THEN** the system SHALL allow the operation when all other validations pass

#### Scenario: Direction administrator attempts cross-direction clone
- **WHEN** a direction administrator selects a source or target outside their own direction, or source and target directions differ
- **THEN** the system SHALL deny the operation regardless of frontend filtering

#### Scenario: Non-super-admin targets global assessment
- **WHEN** a user other than a super administrator attempts to clone from or to a global assessment
- **THEN** the system SHALL deny the operation

#### Scenario: Super administrator clones between global assessments
- **WHEN** a super administrator clones between two different global assessment times
- **THEN** the system SHALL allow the operation when all other validations pass

### Requirement: Algorithm clone rebuilds independent draft judge configuration
When a source algorithm question has judge configuration, the system SHALL copy its generator source, standard solution sources, primary standard language, testcase generation parameters, and benchmark parameters into a new configuration for the target question and SHALL reset that configuration to `DRAFT`.

#### Scenario: Clone configured algorithm question
- **WHEN** an administrator clones an algorithm question with complete judge source configuration
- **THEN** the target SHALL have a new judge configuration ID in `DRAFT`, pending benchmark state, equivalent source configuration, and no copied generated testcases or confirmed language limits

#### Scenario: Clone does not trigger generation
- **WHEN** an algorithm question clone completes
- **THEN** the system SHALL NOT automatically request test data generation, benchmarking, or resource-limit confirmation for the target

#### Scenario: Source algorithm question has no judge configuration
- **WHEN** an administrator clones an algorithm question without judge configuration
- **THEN** the system SHALL clone its statement and leave the target without judge configuration

#### Scenario: Source judge source asset is missing
- **WHEN** an existing source judge configuration references a missing or empty required generator or standard solution source
- **THEN** the clone SHALL fail without creating a usable partial target configuration and SHALL NOT modify the source

### Requirement: Judge asset ownership remains isolated
The system MUST write cloned generator, standard solution, and manifest objects under the target question's `questions/{targetQuestionId}/current/` namespace and MUST NOT reference mutable judge objects owned by the source question.

#### Scenario: Source and target content hashes match
- **WHEN** cloned generator or standard solution bytes are identical to the source bytes
- **THEN** the source and target MAY have the same content hash but MUST have different complete object keys containing their respective question IDs

#### Scenario: Manifest identifies the target
- **WHEN** the target judge configuration is created
- **THEN** its manifest SHALL contain the target question ID and target configuration ID rather than copied source identifiers

#### Scenario: Delete or regenerate source after cloning
- **WHEN** an administrator deletes the source question or regenerates its test data after cloning
- **THEN** the target judge source objects and configuration SHALL remain available and SHALL NOT lose files through source-owned cleanup

### Requirement: Clone API follows administrative API conventions
The system SHALL expose clone through an authenticated administrative endpoint protected by the unique `assessment-question:clone` permission and SHALL return the new question wrapped in `ResponseMessage<AssessmentQuestionDTO>`.

#### Scenario: Caller lacks clone permission
- **WHEN** a caller without `assessment-question:clone` invokes the clone endpoint
- **THEN** the system SHALL return an authorization error and SHALL NOT create a target question

#### Scenario: Caller clones successfully through the API
- **WHEN** an authorized caller submits a valid source question ID, target assessment time ID, and positive target question number
- **THEN** the response SHALL identify the newly created target question rather than the source question
