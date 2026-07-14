# query-command-separation Specification

## Purpose
TBD - created by archiving change refactor-ddd-layer-architecture. Update Purpose after archive.
## Requirements
### Requirement: Application query package exists
The system SHALL create `com.bluenet.web.application.query` package to hold read-only operation parameters.

#### Scenario: Query package contains list query
- **WHEN** a developer looks for a read operation parameter class
- **THEN** it SHALL be located under `application.query.<aggregate>`

### Requirement: Read operation parameters are named Query
The system SHALL rename all read-only operation parameter records from `*Command` to `*Query` and move them to the `application.query` package.

#### Scenario: User list query renamed
- **WHEN** the system provides a parameter object for listing admin users
- **THEN** it SHALL be named `GetUserListQuery` and located in `application.query.adminuser`

#### Scenario: Audit trends query renamed
- **WHEN** the system provides a parameter object for querying audit trends
- **THEN** it SHALL be named `GetTrendsQuery` and located in `application.query.auditstatistics`

### Requirement: Write operation parameters remain Command
The system SHALL keep all state-mutating operation parameter records in `application.command` with `*Command` naming.

#### Scenario: Create user command remains
- **WHEN** the system provides a parameter object for creating a user
- **THEN** it SHALL remain named `CreateUserCommand` and located in `application.command.adminuser`

### Requirement: Application service interfaces use Query for reads
The system SHALL update application service method signatures to accept `*Query` objects for read operations and `*Command` objects for write operations.

#### Scenario: Admin user service signature
- **WHEN** `AdminUserAppService.getUserList(...)` is invoked
- **THEN** it SHALL accept `GetUserListQuery` as its parameter

