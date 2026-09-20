# security-principal-context Specification

## Purpose
TBD - created by archiving change refactor-ddd-layer-architecture. Update Purpose after archive.
## Requirements
### Requirement: SecurityPrincipal holds User Entity, RoleType and permissions
The system SHALL represent the current authenticated user through a `SecurityPrincipal` object that contains the `User` entity, the resolved `RoleType`, and the set of permission strings.

权限集 SHALL 在认证时从持久层直接查询得到，MUST NOT 依赖应用启动阶段预加载的内存缓存。理由：预加载缓存的内容取决于 Bean 初始化顺序，而权限数据的写入方（权限扫描器）在缓存加载之后才执行，导致缓存可能为空；且缓存读取无持久层回退，空的缓存会使全部权限校验失败。

每请求的权限查询次数 SHALL 为一次（认证阶段），`PermissionAspect` 的判定 MUST NOT 再次查询持久层。

#### Scenario: Request authentication populates SecurityPrincipal
- **WHEN** the JWT authentication filter validates a token
- **THEN** it SHALL query the `User` entity, resolve the `RoleType` from the user's `roleId`, load the permission values from the role-permission persistence, and store a `SecurityPrincipal` in `UserCTX`
- **THEN** the loaded permission values SHALL reflect the current state of the database, independent of application startup ordering

#### Scenario: 权限数据在认证时即时可见
- **WHEN** 某个角色的权限绑定在数据库中被修改，随后该角色的用户发起请求
- **THEN** 该请求携带的 `SecurityPrincipal` SHALL 反映修改后的权限集
- **THEN** 系统 SHALL NOT 需要重启或手动刷新缓存才能生效

#### Scenario: 角色无权限绑定时不授予任何权限
- **WHEN** 认证通过但该角色在持久层中没有任何权限绑定
- **THEN** `SecurityPrincipal` 的权限集 SHALL 为空集合
- **THEN** 后续受保护接口访问 SHALL 被拒绝（`SUPER_ADMIN` 的角色级别绕过逻辑除外）

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

