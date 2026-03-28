## ADDED Requirements

### Requirement: Test role consistency
Test environments and test cases SHALL use the correct role names as defined in the RBAC specification: SUPER_ADMIN, DIRECTION_ADMIN, MEMBER, and CANDIDATE only. Tests SHALL NOT create or reference the non-existent "ADMIN" role.

#### Scenario: Integration tests create correct roles
- **WHEN** setting up test data in integration tests
- **THEN** created roles SHALL have names "SUPER_ADMIN", "DIRECTION_ADMIN", "MEMBER", or "CANDIDATE"
- **THEN** no role SHALL be named "ADMIN"

#### Scenario: @WithUserVO annotation uses correct role names
- **WHEN** using @WithUserVO annotation in test classes
- **THEN** the roleName parameter SHALL be "SUPER_ADMIN", "DIRECTION_ADMIN", "MEMBER", or "CANDIDATE"
- **THEN** the roleName parameter SHALL NOT be "ADMIN"

#### Scenario: Test permission checking reflects production
- **WHEN** testing permission-dependent functionality
- **THEN** test user roles SHALL match production role definitions
- **THEN** test expectations SHALL align with production permission logic

### Requirement: Test environment validation
Test setup SHALL validate that the test database contains only valid roles as per RBAC specification.

#### Scenario: Test database role validation
- **WHEN** running integration tests
- **THEN** test setup SHALL verify that tb_role table contains only valid role names
- **THEN** any "ADMIN" role records SHALL be cleaned up before test execution

## MODIFIED Requirements

### Requirement: All entities basic operations
All 18 entities SHALL have basic CRUD functionality verified. Test data creation for user-related entities SHALL use correct role references.

#### Scenario: Entity CRUD smoke test with correct roles
- **WHEN** running EntityCrudTest for User entity
- **THEN** test SHALL create users with valid role IDs referencing SUPER_ADMIN, DIRECTION_ADMIN, MEMBER, or CANDIDATE roles
- **THEN** test SHALL NOT assign users to non-existent "ADMIN" role

#### Scenario: Test user creation validation
- **WHEN** creating test users in any test
- **THEN** the assigned role SHALL exist in the database with a valid name
- **THEN** the role name SHALL NOT be "ADMIN"