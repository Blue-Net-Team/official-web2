## MODIFIED Requirements

### Requirement: Assessment time scheduling
The AssessmentTime entity SHALL define assessment schedules with direction-specific and epoch-based configuration. Table name SHALL be tb_assessment_time. The `grade` field SHALL store enrollment year (e.g. 2024, 2025) instead of grade sequence number (1, 2, 3).

#### Scenario: Assessment time configuration
- **WHEN** creating AssessmentTime
- **THEN** direction MUST be one of: computer_vision, structural_design, embedded
- **THEN** epoch MUST be 0 for final assessment, or positive integer for round number
- **THEN** grade MUST be a valid enrollment year (e.g. 2024)
- **THEN** start_time MUST be before end_time
- **THEN** time_limit boolean controls if time_limit_minutes is enforced
