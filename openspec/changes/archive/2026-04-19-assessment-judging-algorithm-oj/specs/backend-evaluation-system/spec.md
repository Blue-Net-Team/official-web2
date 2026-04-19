## ADDED Requirements

### Requirement: Type-based answer evaluation dispatch
The system SHALL dispatch answer evaluation based on question type after a candidate creates or updates an answer.

#### Scenario: Single choice answer dispatch
- **WHEN** a candidate submits a single choice answer
- **THEN** the system SHALL compare the answer content with the configured correct answer and create an automatic judgement

#### Scenario: Multiple choice answer dispatch
- **WHEN** a candidate submits a multiple choice answer
- **THEN** the system SHALL compare the submitted answer set with the configured correct answer set and create an automatic judgement

#### Scenario: File upload answer dispatch
- **WHEN** a candidate submits a file upload answer
- **THEN** the system SHALL save the answer and leave the judgement pending manual review

#### Scenario: Algorithm answer dispatch
- **WHEN** a candidate submits an algorithm answer
- **THEN** the system SHALL save the answer and create an asynchronous judge job instead of evaluating the code in the request thread

### Requirement: Objective scoring
The system SHALL score single choice and multiple choice answers automatically using the question score as the maximum score.

#### Scenario: Single choice correct
- **WHEN** the submitted single choice answer equals the configured correct answer
- **THEN** the automatic judgement SHALL award the full question score

#### Scenario: Single choice incorrect
- **WHEN** the submitted single choice answer does not equal the configured correct answer
- **THEN** the automatic judgement SHALL award zero score

#### Scenario: Multiple choice exactly matches
- **WHEN** the submitted multiple choice answer set exactly equals the configured correct answer set
- **THEN** the automatic judgement SHALL award the full question score

#### Scenario: Multiple choice does not exactly match
- **WHEN** the submitted multiple choice answer set differs from the configured correct answer set
- **THEN** the automatic judgement SHALL award zero score

### Requirement: Programming language in answer submissions
The system SHALL accept and persist programming language for algorithm answer submissions and ignore language for non-algorithm answer submissions.

#### Scenario: Algorithm answer includes language
- **WHEN** a candidate submits an algorithm answer with supported language and code content
- **THEN** the system SHALL persist both the code content and programming language

#### Scenario: Non-algorithm answer includes language
- **WHEN** a candidate submits a non-algorithm answer with a language field
- **THEN** the system SHALL ignore the language field for evaluation
