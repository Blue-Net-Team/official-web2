## ADDED Requirements

### Requirement: Student ID Login Authentication
The system SHALL provide an endpoint for users to authenticate using student ID and password.

#### Scenario: Successful login with valid credentials
- **WHEN** user provides valid studentId and password
- **THEN** the system SHALL verify the credentials against stored password hash
- **THEN** the system SHALL generate a new JWT token
- **THEN** the system SHALL store the token in whitelist
- **THEN** the system SHALL return the JWT token and user information
- **THEN** any previous token for this user SHALL be invalidated

#### Scenario: Login with invalid studentId
- **WHEN** user provides a studentId that does not exist
- **THEN** the system SHALL return authentication failure response
- **THEN** the system SHALL NOT generate any token

#### Scenario: Login with wrong password
- **WHEN** user provides correct studentId but wrong password
- **THEN** the system SHALL return authentication failure response
- **THEN** the system SHALL NOT generate any token

#### Scenario: Login with disabled account
- **WHEN** user provides valid credentials for a disabled account
- **THEN** the system SHALL return account disabled response
- **THEN** the system SHALL NOT generate any token

### Requirement: Login Request Validation
The system SHALL validate login request parameters before authentication.

#### Scenario: Empty studentId
- **WHEN** login request has empty or null studentId
- **THEN** the system SHALL return validation error
- **THEN** the system SHALL NOT attempt authentication

#### Scenario: Empty password
- **WHEN** login request has empty or null password
- **THEN** the system SHALL return validation error
- **THEN** the system SHALL NOT attempt authentication

### Requirement: Login Response Structure
The system SHALL return a standardized response for successful login.

#### Scenario: Successful login response format
- **WHEN** login is successful
- **THEN** the response SHALL contain a JWT token (String)
- **THEN** the response SHALL contain user information (UserInfo DTO)
- **THEN** the response SHALL NOT contain refreshToken field

## MODIFIED Requirements

### Requirement: User Authentication Response DTO
The UserAuthResponseDTO SHALL represent the response structure for authentication operations.

#### Scenario: Response DTO structure
- **WHEN** creating a UserAuthResponseDTO
- **THEN** it SHALL contain token field (String, JWT token)
- **THEN** it SHALL contain userInfo field (UserInfo object)
- **THEN** it SHALL NOT contain refreshToken field
