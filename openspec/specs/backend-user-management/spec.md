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

### Requirement: Admin user management
The system SHALL provide administrator user management capabilities accessible only to SUPER_ADMIN role.

#### Scenario: List users with pagination and filtering
- **WHEN** GET /api/v1/admin/users is called by SUPER_ADMIN
- **THEN** response SHALL return paginated user list
- **AND** support filtering by role_id, direction, college_id
- **AND** support searching by student_id or username (fuzzy match)
- **AND** support sorting by id, student_id, created_at

#### Scenario: Get user detail with statistics
- **WHEN** GET /api/v1/admin/users/{id} is called by SUPER_ADMIN
- **THEN** response SHALL return full user profile
- **AND** include count of experiences, achievements, assessment answers

#### Scenario: Update user information
- **WHEN** PUT /api/v1/admin/users/{id} is called by SUPER_ADMIN
- **THEN** user fields (role_id, direction, disable, job, etc.) SHALL be updated
- **AND** audit log SHALL record the action with operator and target user

#### Scenario: Reset user password
- **WHEN** PUT /api/v1/admin/users/{id}/password is called by SUPER_ADMIN with confirmed new password
- **THEN** user password SHALL be updated with BCrypt hash
- **AND** existing sessions MAY remain valid
- **AND** audit log SHALL record the password reset action

#### Scenario: Delete user with cascade
- **WHEN** DELETE /api/v1/admin/users/{id} is called by SUPER_ADMIN
- **THEN** user SHALL be physically deleted
- **AND** associated records SHALL be cascade deleted: experiences, achievements, assessment answers, assessment sessions, comments
- **AND** associated avatar file SHALL be deleted from storage
- **AND** audit log SHALL record the deletion action

#### Scenario: Batch disable users
- **WHEN** POST /api/v1/admin/users/batch-disable is called by SUPER_ADMIN with user IDs
- **THEN** all specified users' disable flag SHALL be set to true
- **AND** audit log SHALL record the batch action with target count

#### Scenario: Batch delete users
- **WHEN** POST /api/v1/admin/users/batch-delete is called by SUPER_ADMIN with user IDs
- **THEN** all specified users SHALL be physically deleted with cascade
- **AND** audit log SHALL record the batch action with target count

#### Scenario: Batch update user roles
- **WHEN** POST /api/v1/admin/users/batch-role is called by SUPER_ADMIN with user IDs and role_id
- **THEN** all specified users' role_id SHALL be updated
- **AND** audit log SHALL record the batch action

### Requirement: Enum value mapping
All enumeration values SHALL be stored in database as lowercase snake_case strings.

#### Scenario: Direction enum storage
- **WHEN** storing Direction.COMPUTER_VISION
- **THEN** database value SHALL be "computer_vision"
- **WHEN** storing ExperienceType.COMPETITION
- **THEN** database value SHALL be "competition"
