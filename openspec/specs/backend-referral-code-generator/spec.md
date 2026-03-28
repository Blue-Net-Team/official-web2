# Referral Code Generator

## Requirements

### Requirement: Generate unique referral code
The system SHALL generate a unique 8-character referral code consisting of uppercase letters (A-Z) and digits (0-9).

#### Scenario: Generate new referral code
- **WHEN** a new referral code is requested
- **THEN** the system returns an 8-character string containing only uppercase letters and digits

#### Scenario: Ensure uniqueness on collision
- **WHEN** a generated code already exists in the database
- **THEN** the system regenerates a new code and retries up to 10 times

### Requirement: Validate referral code format
The system SHALL validate that a referral code matches the expected format (8 uppercase alphanumeric characters).

#### Scenario: Valid code format
- **WHEN** validating code "ABC12345"
- **THEN** the system returns true

#### Scenario: Invalid code too short
- **WHEN** validating code "ABC123"
- **THEN** the system returns false

#### Scenario: Invalid code lowercase
- **WHEN** validating code "abc12345"
- **THEN** the system returns false

#### Scenario: Invalid code special characters
- **WHEN** validating code "ABC12-45"
- **THEN** the system returns false

### Requirement: Query user by referral code
The system SHALL provide a method to query a user by their referral code.

#### Scenario: Find existing user
- **WHEN** querying by a valid referral code that exists
- **THEN** the system returns the associated user

#### Scenario: Code not found
- **WHEN** querying by a referral code that does not exist
- **THEN** the system returns null or empty
