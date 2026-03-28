## ADDED Requirements

### Requirement: User gender field support
The User entity SHALL include a gender field to represent user's gender with three possible values: male, female, or unknown.

#### Scenario: Gender field validation
- **WHEN** creating or updating a User entity
- **THEN** gender MUST be one of: male, female, unknown
- **THEN** gender field MUST default to unknown if not provided
- **THEN** gender SHALL be stored in database as lowercase snake_case string

#### Scenario: Gender enum mapping
- **WHEN** storing Gender.MALE
- **THEN** database value SHALL be "male"
- **WHEN** storing Gender.FEMALE
- **THEN** database value SHALL be "female"
- **WHEN** storing Gender.UNKNOWN
- **THEN** database value SHALL be "unknown"

#### Scenario: Gender field in data transfer
- **WHEN** user data is transferred via DTO
- **THEN** gender field MUST be included in the transfer object
- **THEN** gender value MUST be validated against allowed enum values

#### Scenario: Gender field in view representation
- **WHEN** user data is returned via VO
- **THEN** gender field MUST be included in the view object
- **THEN** gender value MUST be serialized as string representation

## MODIFIED Requirements

### Requirement: User entity data structure
The User entity SHALL represent system users with complete profile information and role-based access control.

#### Scenario: User entity fields validation
- **WHEN** creating a User entity
- **THEN** student_id MUST be 12-13 characters
- **THEN** email MUST be valid format
- **THEN** role_id MUST reference an existing Role
- **THEN** direction MUST be one of: computer_vision, structural_design, embedded
- **THEN** disable field MUST default to false
- **THEN** gender field MUST default to unknown
