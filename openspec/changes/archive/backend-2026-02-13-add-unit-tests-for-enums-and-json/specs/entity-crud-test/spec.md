## ADDED Requirements

### Requirement: User entity CRUD operations
User entity SHALL support basic CRUD operations via MyBatis Mapper.

#### Scenario: Create and read User
- **WHEN** inserting a new User
- **THEN** the user SHALL be persisted with correct values
- **THEN** the user SHALL be retrievable by ID

#### Scenario: User enum field persistence
- **WHEN** saving User with Direction.COMPUTER_VISION
- **THEN** the direction field SHALL be stored as "computer_vision"
- **THEN** reading the user back SHALL restore Direction.COMPUTER_VISION

### Requirement: EvaluationQuestion CRUD with JSON content
EvaluationQuestion SHALL correctly persist and retrieve JSON content.

#### Scenario: Save and load QuestionContent
- **WHEN** saving EvaluationQuestion with AlgorithmContent
- **THEN** the content SHALL be stored as JSON in database
- **THEN** reading the question back SHALL restore AlgorithmContent with all fields

### Requirement: All entities basic operations
All 18 entities SHALL have basic CRUD functionality verified.

#### Scenario: Entity CRUD smoke test
- **WHEN** running EntityCrudTest
- **THEN** all entities SHALL support insert and select operations
- **THEN** no exceptions SHALL be thrown during basic CRUD
