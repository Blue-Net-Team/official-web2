## ADDED Requirements

### Requirement: Permission enforcement via AOP
The system MUST enforce permissions for endpoints annotated with `@Permission` using AOP.

#### Scenario: AOP intercepts annotated method
- **WHEN** a request targets a method annotated with `@Permission`
- **THEN** the AOP interceptor SHALL evaluate access before method execution

### Requirement: Public access handling
The system MUST allow unrestricted access to endpoints with access level PUBLIC.

#### Scenario: Public endpoint access
- **WHEN** a PUBLIC endpoint is requested
- **THEN** the request SHALL proceed without authentication or authorization checks

### Requirement: Authenticated access handling
The system MUST require valid authentication for endpoints with access level AUTHENTICATED.

#### Scenario: Authenticated endpoint access
- **WHEN** an AUTHENTICATED endpoint is requested without a valid token
- **THEN** the system SHALL return 401 Unauthorized

### Requirement: Protected access handling
The system MUST require role permission checks for endpoints with access level PROTECTED.

#### Scenario: Protected endpoint access
- **WHEN** a PROTECTED endpoint is requested by a user without the required permission
- **THEN** the system SHALL return 403 Forbidden

### Requirement: Orphan permission behavior
The system MUST treat permissions with no role associations as publicly accessible.

#### Scenario: Orphan permission access
- **WHEN** a permission has no role associations
- **THEN** the endpoint SHALL be treated as public access

### Requirement: Deny unannotated endpoints
The system MUST deny access to unannotated /api/** endpoints by default.

#### Scenario: Unannotated endpoint access
- **WHEN** a request targets an /api/** endpoint without `@Permission`
- **THEN** the system SHALL return 403 Forbidden
