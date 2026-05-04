## ADDED Requirements

### Requirement: Role reference consistency
All code implementations SHALL use the RoleType enum for role references and SHALL NOT use hardcoded role name strings for permission checking.

#### Scenario: File download permission check uses RoleType
- **WHEN** checking file download permissions in FileDownloadServiceImpl
- **THEN** the code SHALL use RoleType.MEMBER or RoleType.SUPER_ADMIN instead of "MEMBER" or "ADMIN" strings

#### Scenario: Role hierarchy checking uses RoleHierarchy utility
- **WHEN** checking if a user has sufficient role level
- **THEN** the code SHALL use RoleHierarchy.hasRoleLevel() instead of direct string comparison

#### Scenario: Test environment uses correct role names
- **WHEN** creating test roles in integration tests
- **THEN** the role names SHALL be "SUPER_ADMIN", "DIRECTION_ADMIN", "MEMBER", or "CANDIDATE" only
- **THEN** the role names SHALL NOT be "ADMIN"

## MODIFIED Requirements

### Requirement: Role definition
The system MUST support the following roles:

| Role Identifier | Role Name | Description |
|-----------------|-----------|-------------|
| `SUPER_ADMIN` | 超级管理员 | 团队负责人或 Web 技术负责人，拥有最高权限 |
| `DIRECTION_ADMIN` | 方向管理员 | 各方向（视觉、结构、电控等）的管理员 |
| `MEMBER` | 团队成员 | 正式团队成员 |
| `CANDIDATE` | 考生 | 已发放账号、正在考核中的用户 |

**Enforcement Rule:** All code implementations MUST reference these roles exclusively through the RoleType enum. Hardcoded role name strings are prohibited.

#### Scenario: Valid role references
- **WHEN** code references the SUPER_ADMIN role
- **THEN** it SHALL use RoleType.SUPER_ADMIN or RoleType.SUPER_ADMIN.getName()
- **THEN** it SHALL NOT use the literal string "SUPER_ADMIN" for permission logic

#### Scenario: Invalid role detection
- **WHEN** code review finds hardcoded role strings like "ADMIN" in permission checks
- **THEN** the implementation SHALL be rejected and fixed before deployment