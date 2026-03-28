# System User Initialization

## Requirements

### Requirement: System user configuration properties

The system SHALL provide configuration properties for the system user in `application.yml`, including username, password, and student ID, with support for environment variable overrides.

#### Scenario: Default configuration values
- **WHEN** no environment variables are set
- **THEN** the system SHALL use default username `system`, default password `admin123`, and default student ID `000000000000`

#### Scenario: Environment variable override
- **WHEN** environment variables `SYSTEM_USER_USERNAME`, `SYSTEM_USER_PASSWORD`, or `SYSTEM_USER_STUDENT_ID` are set
- **THEN** the system SHALL use the values from environment variables instead of defaults

### Requirement: Automatic system user initialization

The system SHALL automatically create the system user in the database when the application starts, if the user does not already exist.

#### Scenario: First startup with no existing system user
- **WHEN** the application starts and no user with student ID `000000000000` exists
- **THEN** the system SHALL create a new user with the configured username, student ID, and BCrypt-encrypted password

#### Scenario: Subsequent startup with existing system user
- **WHEN** the application starts and a user with student ID `000000000000` already exists
- **THEN** the system SHALL skip user creation and log an informational message

### Requirement: Password encryption

The system SHALL encrypt the system user's password using BCrypt before storing it in the database.

#### Scenario: Password storage
- **WHEN** the system user is created
- **THEN** the password SHALL be encrypted using BCrypt password encoder
- **AND** the encrypted password SHALL be stored in the database

### Requirement: System user identification

The system user SHALL be uniquely identified by the student ID field with a 12-digit zero value (`000000000000`).

#### Scenario: Unique identification
- **WHEN** checking for existing system user
- **THEN** the system SHALL query by student ID `000000000000`
- **AND** this student ID SHALL be reserved exclusively for the system user
