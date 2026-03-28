## Purpose

Assessment system providing assessment scheduling, question bank management, answer submission, and scoring functionality.

## Requirements

### Requirement: Assessment time scheduling
The AssessmentTime entity SHALL define assessment schedules with direction-specific and epoch-based configuration. Table name SHALL be tb_assessment_time. Permission identifiers SHALL use prefix assessment-time:* (e.g. assessment-time:view-self, assessment-time:view-all, assessment-time:update-direction, assessment-time:update-final, assessment-time:update-all).

#### Scenario: Assessment time configuration
- **WHEN** creating AssessmentTime
- **THEN** direction MUST be one of: computer_vision, structural_design, embedded
- **THEN** epoch MUST be 0 for final assessment, or positive integer for round number
- **THEN** start_time MUST be before end_time
- **THEN** time_limit boolean controls if time_limit_minutes is enforced

### Requirement: Question content polymorphism
The AssessmentQuestion entity SHALL support multiple question types with polymorphic JSON content. Table name SHALL be tb_assessment_question; foreign key to schedule SHALL be assessment_time_id. Permission identifiers SHALL use assessment-question:* (e.g. assessment-question:manage).

#### Scenario: Question type and content validation
- **WHEN** creating a question
- **THEN** question_type MUST be one of: single_choice, multiple_choice, file_upload, algorithm
- **THEN** content field SHALL store type-specific JSON structure
- **WHEN** question_type is single_choice
- **THEN** content SHALL include options array and correct_answer field
- **WHEN** question_type is algorithm
- **THEN** content SHALL include test_cases, time_limit, and memory_limit fields

### Requirement: Answer submission with language tracking
The AssessmentAnswer entity SHALL store user submissions with optional file attachments and programming language. Table name SHALL be tb_assessment_answer. Permission identifiers SHALL use assessment-answer:* (e.g. assessment-answer:submit, assessment-answer:view-all, assessment-answer:download).

#### Scenario: Answer submission validation
- **WHEN** submitting an answer
- **WHEN** question_type is algorithm
- **THEN** language field MUST be one of: python, c, cpp, java, javascript
- **THEN** content SHALL store the code
- **WHEN** question_type is file_upload
- **THEN** file_id MUST reference a File entity

### Requirement: Comment and scoring system
The Comment entity SHALL allow team members to review and score answers. answer_id SHALL reference tb_assessment_answer.id. Permission identifiers for result visibility SHALL use assessment-result:* (e.g. assessment-result:view-self).

#### Scenario: Comment creation rules
- **WHEN** creating a comment
- **THEN** user_id MUST reference a team member (role >= ROLE_MEMBER)
- **THEN** score MAY be provided with the comment
- **THEN** comment_time SHALL be set automatically
- **THEN** final_score SHALL be calculated from average of all comment scores after assessment ends
