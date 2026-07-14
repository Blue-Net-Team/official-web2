## ADDED Requirements

### Requirement: SecurityPrincipal holds User Entity, RoleType and permissions
The system SHALL represent the current authenticated user through a `SecurityPrincipal` object that contains the `User` entity, the resolved `RoleType`, and the set of permission strings.

#### Scenario: Request authentication populates SecurityPrincipal
- **WHEN** the JWT authentication filter validates a token
- **THEN** it SHALL query the `User` entity, resolve the `RoleType` from the user's `roleId`, load permissions from `PermissionCache`, and store a `SecurityPrincipal` in `UserCTX`

### Requirement: UserCTX exposes User Entity and security attributes
The system SHALL provide `UserCTX.getCurrentUser()` returning the `User` entity, `UserCTX.getCurrentRoleType()` returning `RoleType`, and `UserCTX.getCurrentPermissions()` returning the permission set.

#### Scenario: Business code retrieves current user entity
- **WHEN** application code calls `UserCTX.getCurrentUser()`
- **THEN** it SHALL receive the `User` entity stored in the current `SecurityPrincipal`

### Requirement: PermissionAspect validates permissions from SecurityPrincipal
The system SHALL validate protected endpoint access by reading `RoleType` and permissions from `SecurityPrincipal` instead of from `UserVO`.

#### Scenario: Super admin bypasses permission check
- **WHEN** a request reaches a protected endpoint and the current `SecurityPrincipal.roleType` is `SUPER_ADMIN`
- **THEN** the permission aspect SHALL allow the request without checking individual permissions

#### Scenario: Protected endpoint requires permission
- **WHEN** a request reaches a protected endpoint and the current `SecurityPrincipal.permissions` does not contain the required permission value
- **THEN** the permission aspect SHALL reject the request with a forbidden error

### Requirement: Test security context factory uses SecurityPrincipal
The system SHALL provide a `@WithSecurityPrincipal` test annotation and factory that construct a `SecurityPrincipal` from test attributes, replacing `@WithUserVO`.

#### Scenario: Test runs with authenticated user
- **WHEN** a test method is annotated with `@WithSecurityPrincipal(userId = 1, roleId = 2)`
- **THEN** `UserCTX.getPrincipal()` SHALL return a non-null `SecurityPrincipal` with the specified user and role
