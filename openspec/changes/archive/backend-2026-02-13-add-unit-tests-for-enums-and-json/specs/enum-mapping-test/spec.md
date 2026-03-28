## ADDED Requirements

### Requirement: Direction enum mapping validation
The Direction enum SHALL be correctly mapped to and from database values.

#### Scenario: Direction enum to database value
- **WHEN** saving Direction.COMPUTER_VISION to database
- **THEN** the stored value SHALL be "computer_vision"
- **WHEN** reading "computer_vision" from database
- **THEN** the value SHALL be mapped to Direction.COMPUTER_VISION

#### Scenario: All Direction enum values mapping
- **WHEN** testing all Direction enum values
- **THEN** each value SHALL round-trip correctly between Java and database

### Requirement: FileType enum mapping validation
The FileType enum SHALL be correctly mapped to and from database values.

#### Scenario: FileType enum to database value
- **WHEN** saving FileType.AVATAR to database
- **THEN** the stored value SHALL be "avatar"
- **WHEN** reading "avatar" from database
- **THEN** the value SHALL be mapped to FileType.AVATAR

### Requirement: All enums mapping validation
All 8 enums (Direction, FileType, ExperienceType, AchievementType, EnrollStatus, ImageType, QuestionType, ProgrammingLanguage) SHALL be tested.

#### Scenario: Batch enum mapping test
- **WHEN** running EnumMappingTest
- **THEN** all 8 enums SHALL pass round-trip validation
- **THEN** each enum value SHALL match expected database string
