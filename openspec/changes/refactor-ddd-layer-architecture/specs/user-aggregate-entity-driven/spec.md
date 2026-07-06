## ADDED Requirements

### Requirement: UserRepository returns User Entity
The system SHALL change `UserRepository` query methods such as `findById`, `findByEmail`, `findByStudentId`, and `findByGithubId` to return `Optional<User>` instead of `Optional<UserVO>`.

#### Scenario: Find user by id returns entity
- **WHEN** `UserRepository.findById(1L)` is invoked
- **THEN** it SHALL return an `Optional<User>` containing the user entity without role name, college name, or permissions

### Requirement: User Entity exposes state change methods
The system SHALL add state change methods to the `User` entity including `updateAvatar(Long)`, `updateProfile(...)`, `updateQrcodeId(Long)`, `changeEmail(String)`, and `changePassword(String)`.

#### Scenario: Update user avatar through entity
- **WHEN** application code loads a `User` entity, calls `user.updateAvatar(fileId)`, and invokes `userRepository.save(user)`
- **THEN** the user's avatar file id SHALL be persisted

### Requirement: UserInfoAppService uses UserRepository directly
The system SHALL remove `UserDomainService` and have `UserInfoAppServiceImpl` directly use `UserRepository` to load and save `User` entities.

#### Scenario: Update profile via application service
- **WHEN** `UserInfoAppServiceImpl.updateProfile(userId, command)` is invoked
- **THEN** it SHALL load the `User` entity, invoke `user.updateProfile(...)`, and save the entity through `UserRepository`

### Requirement: AdminUser update uses save semantics
The system SHALL change `AdminUserAppServiceImpl.updateUser` to load the `User` entity, invoke `updateAdminFields(...)`, and call `userRepository.save(user)` instead of separately invoking a field-level update method.

#### Scenario: Admin updates user fields
- **WHEN** `AdminUserAppServiceImpl.updateUser(command)` is invoked
- **THEN** it SHALL modify the entity in memory and persist it through a single `save` call

### Requirement: UserVO is removed
The system SHALL delete `com.bluenet.web.domain.model.vo.UserVO` after all references are migrated to `User` entity or application-layer result objects.

#### Scenario: Compile project after migration
- **WHEN** the project is compiled after the migration
- **THEN** no source file SHALL reference `UserVO`
