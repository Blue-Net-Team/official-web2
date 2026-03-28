## ADDED Requirements

### Requirement: JWT Token Generation
The system SHALL provide a utility to generate JWT tokens containing user identification information.

#### Scenario: Generate token for valid user
- **WHEN** generating a token for user with ID 123
- **THEN** the token SHALL contain a unique identifier (jti)
- **THEN** the token SHALL contain the userId (sub claim)
- **THEN** the token SHALL contain issuance time (iat claim)
- **THEN** the token SHALL contain expiration time 12 hours later (exp claim)
- **THEN** the token SHALL be signed with a configured secret key

### Requirement: JWT Token Parsing
The system SHALL provide a utility to parse and validate JWT tokens from string format.

#### Scenario: Parse valid token
- **WHEN** parsing a valid JWT string
- **THEN** the system SHALL extract the jti, userId, and expiration time
- **THEN** the system SHALL return a structured payload object

#### Scenario: Parse expired token
- **WHEN** parsing a token that has expired
- **THEN** the system SHALL return null or throw an appropriate exception

#### Scenario: Parse invalid signature token
- **WHEN** parsing a token with invalid signature
- **THEN** the system SHALL return null or throw an appropriate exception
- **THEN** the system SHALL NOT trust the token contents

#### Scenario: Parse malformed token
- **WHEN** parsing a malformed JWT string
- **THEN** the system SHALL return null or throw an appropriate exception

### Requirement: JWT Payload Structure
The system SHALL define a structured payload class for JWT claims.

#### Scenario: Payload contains required fields
- **WHEN** creating a JwtPayload object
- **THEN** it SHALL contain userId (Long)
- **THEN** it SHALL contain jti (UUID or String)
- **THEN** it SHALL contain issuedAt (Long timestamp)
- **THEN** it SHALL contain expiration (Long timestamp)

### Requirement: JWT Configuration
The system SHALL support configurable JWT parameters through application configuration.

#### Scenario: Configuration loaded from properties
- **WHEN** the application starts
- **THEN** it SHALL load JWT secret key from configuration
- **THEN** it SHALL load token expiration time (default 12 hours)
- **THEN** the JwtUtil SHALL use these configured values
