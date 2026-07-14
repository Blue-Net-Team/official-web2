# repository-readmodel-separation Specification

## Purpose
TBD - created by archiving change refactor-ddd-layer-architecture. Update Purpose after archive.
## Requirements
### Requirement: Domain repositories return entities for aggregate roots
The system SHALL ensure all domain repository interfaces return their aggregate root entities for CRUD operations. Repository interfaces SHALL NOT return application-layer result objects.

#### Scenario: Role repository returns Role entity
- **WHEN** `RoleRepository.findByName("MEMBER")` is invoked
- **THEN** it SHALL return `Optional<Role>` instead of `Optional<RoleVO>`

### Requirement: Pseudo-VOs used as entity substitutes are removed
The system SHALL delete or rename the following domain VO classes that are used as entity substitutes: `FileVO`, `QrcodeVO`, `ExperienceVO`, `VerifyCodeVO`, `RoleVO`, `AssessmentDecisionVO`, `AssessmentJudgementVO`.

#### Scenario: Compile after VO removal
- **WHEN** the project is compiled
- **THEN** the above classes SHALL either be removed or moved to an application-layer result package

### Requirement: Domain ReadModels are allowed for complex list queries
The system SHALL permit domain repositories to return domain-layer ReadModels / Projections for complex list queries that aggregate data from multiple tables, provided these objects remain in the domain layer.

#### Scenario: Achievement list query returns ReadModel
- **WHEN** `AchievementRepository.findAchievementListItems(...)` is invoked
- **THEN** it MAY return a domain-layer `AchievementListItem` containing fields from `Achievement` and `Competition`

### Requirement: Application-layer ReadModels are assembled by application services
The system SHALL have application services assemble application-layer ReadModels / Results from one or more repositories, or delegate to an application-layer QueryRepository for complex queries.

#### Scenario: User info result assembly
- **WHEN** `UserInfoAppServiceImpl.getMyInfo(userId)` is invoked
- **THEN** it SHALL load the `User` entity and query `CollegeRepository` / `RoleRepository` to assemble the `UserInfoResult`

### Requirement: Infrastructure repositories do not leak data objects
The system SHALL ensure repository implementations convert Data Objects to Entities or domain ReadModels before returning; DO objects SHALL NOT leak past the infrastructure layer boundary.

#### Scenario: Repository implementation conversion
- **WHEN** any repository implementation queries the database
- **THEN** it SHALL convert `*DO` to Entity or domain ReadModel before returning to callers

