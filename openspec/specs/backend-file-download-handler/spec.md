## MODIFIED Requirements

### Requirement: File download with permission validation
The system SHALL validate user permissions before allowing file downloads based on file type. Permission checking SHALL use the RoleType enum and RoleHierarchy utility class for role level comparisons, and SHALL NOT use hardcoded role name strings.

#### Scenario: Download work file as team member
- **WHEN** team member (role >= RoleType.MEMBER) requests any work file
- **THEN** system SHALL return the work file
- **Implementation Requirement**: Permission check SHALL use `RoleHierarchy.hasRoleLevel(userRole, RoleType.MEMBER)` instead of string comparison

#### Scenario: Download work file denied for insufficient role
- **WHEN** user with role below MEMBER requests another candidate's work file
- **THEN** system SHALL deny access with 403 Forbidden
- **Implementation Requirement**: Role comparison SHALL use `RoleHierarchy.hasRoleLevel()` method

### Requirement: File download endpoint
The system SHALL provide RESTful endpoints for downloading files by file ID. Permission validation logic SHALL be implemented using type-safe role references.

#### Scenario: Download by file ID with permission validation
- **WHEN** user makes GET request to /api/v1/file/download/{fileId}
- **THEN** system SHALL validate permissions using RoleType enum and RoleHierarchy utility
- **AND** system SHALL return file with appropriate Content-Type header
- **Implementation Requirement**: Business logic SHALL reference roles through RoleType enum constants, not string literals

## ADDED Requirements

### Requirement: Role-based permission checking implementation
File download permission checking SHALL be implemented using the RoleHierarchy utility class for all role level comparisons.

#### Scenario: FileDownloadServiceImpl uses RoleHierarchy
- **WHEN** FileDownloadServiceImpl checks if user has at least MEMBER role
- **THEN** the code SHALL call `RoleHierarchy.hasRoleLevel(userRole, RoleType.MEMBER)`
- **THEN** the code SHALL NOT compare role name strings directly

#### Scenario: No hardcoded role strings in permission logic
- **WHEN** reviewing FileDownloadServiceImpl implementation
- **THEN** there SHALL be no hardcoded "ADMIN", "MEMBER", "DIRECTION_ADMIN", or "SUPER_ADMIN" strings in permission checking logic
- **THEN** all role references SHALL go through RoleType enum