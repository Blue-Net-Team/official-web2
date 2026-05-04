## ADDED Requirements

### Requirement: Required system roles
The system MUST define the roles SUPER_ADMIN, DIRECTION_ADMIN, MEMBER, and CANDIDATE.

#### Scenario: Initial role presence
- **WHEN** the system initializes role data
- **THEN** all required roles SHALL exist in the roles table

### Requirement: Candidate role usage
The system MUST assign the CANDIDATE role to users with accounts who are actively participating in assessments.

#### Scenario: Candidate role assignment
- **WHEN** an account is issued for a candidate
- **THEN** the user SHALL be assigned the CANDIDATE role by default

### Requirement: Role hierarchy
The system MUST support the hierarchy SUPER_ADMIN > DIRECTION_ADMIN > MEMBER > CANDIDATE for permission inheritance.

#### Scenario: Hierarchical permission access
- **WHEN** a user has a higher-level role
- **THEN** they SHALL inherit permissions of all lower-level roles

### Requirement: User role required
The system MUST require every user to have a non-null role assignment.

#### Scenario: Missing role
- **WHEN** a user record is created or updated without a role
- **THEN** the operation SHALL be rejected

### Requirement: Role-permission association
The system MUST manage role-permission associations via a role-permission relation table.

#### Scenario: Role permission mapping
- **WHEN** permissions are assigned to a role
- **THEN** the role-permission relation SHALL record the association
