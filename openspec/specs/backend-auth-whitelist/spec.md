## Purpose
Service for managing JWT token whitelist, supporting token storage, validation, and revocation using Redis.

## Requirements

### Requirement: Token Whitelist Storage
The system SHALL maintain a whitelist of valid JWT tokens in Redis for verification and revocation.

#### Scenario: Store new token in whitelist
- **WHEN** a user successfully logs in
- **THEN** the system SHALL store the token jti in Redis
- **THEN** the Redis key SHALL be "auth:token:{jti}"
- **THEN** the Redis value SHALL be the userId (as String)
- **THEN** the Redis entry SHALL have TTL of 12 hours (43200 seconds)

#### Scenario: Retrieve token from whitelist
- **WHEN** validating a token
- **THEN** the system SHALL check if key "auth:token:{jti}" exists in Redis
- **THEN** if key exists, the system SHALL return the associated userId
- **THEN** if key does not exist, the system SHALL return null

#### Scenario: Token expires naturally
- **WHEN** a token reaches its expiration time
- **THEN** the Redis entry SHALL automatically expire due to TTL
- **THEN** subsequent validation SHALL return null

### Requirement: Single Active Token Per User
The system SHALL enforce that each user has only one active token at a time.

#### Scenario: New login invalidates old token
- **WHEN** user logs in while already having an active token
- **THEN** the system SHALL delete the old token entry from Redis
- **THEN** the system SHALL store the new token entry
- **THEN** the old token SHALL no longer be valid for authentication

### Requirement: Token Revocation
The system SHALL provide functionality to revoke (invalidate) tokens before their natural expiration.

#### Scenario: User logout revokes token
- **WHEN** user requests logout with a valid JWT
- **THEN** the system SHALL extract jti from the JWT
- **THEN** the system SHALL delete the Redis entry "auth:token:{jti}"
- **THEN** subsequent requests with this token SHALL be rejected

#### Scenario: Logout with already revoked token
- **WHEN** user requests logout with a token that is not in whitelist
- **THEN** the system SHALL return success (idempotent operation)
- **THEN** the system SHALL NOT throw an error

### Requirement: Token Validation Service
The system SHALL provide a service to validate tokens against the whitelist.

#### Scenario: Validate active token
- **WHEN** validating a token that exists in whitelist
- **THEN** the service SHALL return Optional containing the userId

#### Scenario: Validate revoked token
- **WHEN** validating a token that has been revoked
- **THEN** the service SHALL return Optional.empty()

#### Scenario: Validate expired token
- **WHEN** validating a token that has naturally expired
- **THEN** the service SHALL return Optional.empty()

#### Scenario: Validate non-existent token
- **WHEN** validating a token jti that was never stored
- **THEN** the service SHALL return Optional.empty()

### Requirement: Whitelist Service Configuration
The system SHALL support configurable whitelist parameters.

#### Scenario: Token TTL configuration
- **WHEN** application starts
- **THEN** it SHALL load token TTL from configuration (default 12 hours)
- **THEN** the AuthTokenService SHALL use this TTL for new entries
