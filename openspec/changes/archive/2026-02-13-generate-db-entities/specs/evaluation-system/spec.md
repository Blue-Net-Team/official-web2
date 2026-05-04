## ADDED Requirements

### Requirement: Evaluation time scheduling
The EvaluationTime entity SHALL define assessment schedules with direction-specific and epoch-based configuration.

#### Scenario: Evaluation time configuration
- **WHEN** creating EvaluationTime
- **THEN** direction MUST be one of: computer_vision, structural_design, embedded
- **THEN** epoch MUST be 0 for final evaluation, or positive integer for round number
- **THEN** start_time MUST be before end_time
- **THEN** time_limit boolean controls if time_limit_minutes is enforced

### Requirement: Question content polymorphism
The EvaluationQuestion entity SHALL support multiple question types with polymorphic JSON content.

#### Scenario: Question type and content validation
- **WHEN** creating a question
- **THEN** question_type MUST be one of: single_choice, multiple_choice, file_upload, algorithm
- **THEN** content field SHALL store type-specific JSON structure
- **WHEN** question_type is single_choice
- **THEN** content SHALL include options array and correct_answer field
- **WHEN** question_type is algorithm
- **THEN** content SHALL include test_cases, time_limit, and memory_limit fields

### Requirement: Answer submission with language tracking
The EvaluationAnswer entity SHALL store user submissions with optional file attachments and programming language.

#### Scenario: Answer submission validation
- **WHEN** submitting an answer
- **WHEN** question_type is algorithm
- **THEN** language field MUST be one of: python, c, cpp, java, javascript
- **THEN** content SHALL store the code
- **WHEN** question_type is file_upload
- **THEN** file_id MUST reference a File entity

### Requirement: Comment and scoring system
The Comment entity SHALL allow team members to review and score answers.

#### Scenario: Comment creation rules
- **WHEN** creating a comment
- **THEN** user_id MUST reference a team member (role >= ROLE_MEMBER)
- **THEN** score MAY be provided with the comment
- **THEN** comment_time SHALL be set automatically
- **THEN** final_score SHALL be calculated from average of all comment scores after evaluation ends
