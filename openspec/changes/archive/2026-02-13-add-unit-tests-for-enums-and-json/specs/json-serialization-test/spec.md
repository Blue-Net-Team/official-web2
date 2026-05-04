## ADDED Requirements

### Requirement: SingleChoiceContent JSON serialization
SingleChoiceContent SHALL serialize to and deserialize from JSON correctly.

#### Scenario: SingleChoiceContent serialization
- **WHEN** serializing SingleChoiceContent with options and correctAnswer
- **THEN** the JSON SHALL contain "type": "single_choice"
- **THEN** the JSON SHALL contain the options array
- **THEN** the JSON SHALL contain the correctAnswer field

#### Scenario: SingleChoiceContent deserialization
- **WHEN** deserializing JSON with type "single_choice"
- **THEN** the object SHALL be instance of SingleChoiceContent
- **THEN** all fields SHALL be correctly populated

### Requirement: AlgorithmContent JSON serialization
AlgorithmContent SHALL serialize to and deserialize from JSON correctly.

#### Scenario: AlgorithmContent serialization
- **WHEN** serializing AlgorithmContent with test cases
- **THEN** the JSON SHALL contain "type": "algorithm"
- **THEN** the JSON SHALL contain testCases array
- **THEN** the JSON SHALL contain timeLimit and memoryLimit

#### Scenario: AlgorithmContent deserialization
- **WHEN** deserializing JSON with type "algorithm"
- **THEN** the object SHALL be instance of AlgorithmContent
- **THEN** test cases SHALL be correctly parsed

### Requirement: QuestionContent polymorphic deserialization
QuestionContent SHALL use type discriminator for polymorphic deserialization.

#### Scenario: Type discriminator validation
- **WHEN** deserializing different content types
- **THEN** "single_choice" SHALL create SingleChoiceContent
- **THEN** "multiple_choice" SHALL create MultipleChoiceContent
- **THEN** "file_upload" SHALL create FileUploadContent
- **THEN** "algorithm" SHALL create AlgorithmContent
