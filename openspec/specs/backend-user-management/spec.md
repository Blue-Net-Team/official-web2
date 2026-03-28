## Purpose

User management module providing user entity data structure, role-based permission system, user experience tracking, and achievement system.

## Requirements

### Requirement: User info response contains avatar file ID
The system SHALL return user's avatar file ID instead of avatar URL in user info response.

#### Scenario: Get current user info with avatar
- **WHEN** GET /api/v1/user/info is called by authenticated user with avatar
- **THEN** response SHALL include `avatarFileId` field with the file ID value
- **AND** response SHALL NOT include `avatarUrl` field

#### Scenario: Get current user info without avatar
- **WHEN** GET /api/v1/user/info is called by authenticated user without avatar
- **THEN** response SHALL include `avatarFileId` field with null value
- **AND** response SHALL NOT include `avatarUrl` field

### Requirement: User entity data structure
The User entity SHALL represent system users with complete profile information and role-based access control.

#### Scenario: User entity fields validation
- **WHEN** creating a User entity
- **THEN** student_id MUST be 12-13 characters
- **THEN** email MUST be valid format
- **THEN** role_id MUST reference an existing Role
- **THEN** direction MUST be one of: computer_vision, structural_design, embedded
- **THEN** disable field MUST default to false

### Requirement: Role-based permission system
The system SHALL support role-based access control with roles, permissions, and their associations.

#### Scenario: Role permission assignment
- **WHEN** a Role is created
- **THEN** it MUST have a unique name identifier
- **WHEN** permissions are assigned to a role
- **THEN** the RolePermission association SHALL link role_id and permission_id

### Requirement: User experience tracking
The system SHALL track user experiences including competitions, projects, and internships.

#### Scenario: Experience record creation
- **WHEN** a UserExperience is created
- **THEN** type MUST be one of: competition, project, internship
- **THEN** start_time MUST be before or equal to end_time
- **THEN** user_id MUST reference an existing User

### Requirement: Achievement system
The system SHALL record user achievements including papers, patents, and competition awards.

#### Scenario: Achievement and user association
- **WHEN** an Achievement is created
- **THEN** type MUST be one of: paper, patent, competition
- **WHEN** linking achievement to user
- **THEN** the UserAchievement association SHALL store user_id and achievement_id

### Requirement: Enum value mapping
All enumeration values SHALL be stored in database as lowercase snake_case strings.

#### Scenario: Direction enum storage
- **WHEN** storing Direction.COMPUTER_VISION
- **THEN** database value SHALL be "computer_vision"
- **WHEN** storing ExperienceType.COMPETITION
- **THEN** database value SHALL be "competition"
