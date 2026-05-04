## MODIFIED Requirements

### Requirement: Password encryption

The system SHALL encrypt the system user's password using SHA-256 followed by BCrypt before storing it in the database, to ensure compatibility with the frontend login flow.

#### Scenario: Password storage with SHA-256 pre-hashing
- **WHEN** the system user is created
- **THEN** the password SHALL first be hashed using SHA-256 algorithm
- **AND** the SHA-256 hash SHALL then be encrypted using BCrypt password encoder
- **AND** the final encrypted password SHALL be stored in the database

#### Scenario: Password verification matches frontend login flow
- **WHEN** a user logs in with the correct password through the frontend
- **THEN** the frontend SHALL send SHA-256 hash of the password
- **AND** the backend SHALL verify the SHA-256 hash against the stored BCrypt-encrypted value
- **AND** the verification SHALL succeed

#### Scenario: SHA-256 hash format
- **WHEN** hashing the password with SHA-256
- **THEN** the output SHALL be a lowercase hexadecimal string
- **AND** the string SHALL be 64 characters long (256 bits)
