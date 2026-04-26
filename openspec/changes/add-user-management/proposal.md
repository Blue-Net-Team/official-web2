## Why

目前系统管理员后台缺少用户管理功能。SUPER_ADMIN 无法查看系统用户列表、修改用户角色或禁用状态、重置用户密码，也无法批量管理用户。这导致用户管理完全依赖数据库直接操作，存在安全风险和效率问题。

## What Changes

- 新增后端管理员用户管理 API（`/api/v1/admin/users/*`），仅 SUPER_ADMIN 可访问
- 支持分页查询用户列表（按角色、方向、学院筛选，按学号/姓名搜索）
- 支持查看用户详情（含关联数据统计）
- 支持更新用户基础信息（角色、方向、禁用状态等）
- 支持重置用户密码（管理员指定新密码，需二次确认）
- 支持删除用户（物理删除，级联清理关联数据）
- 支持批量操作：批量删除、批量禁用/启用、批量修改角色
- 所有管理操作记录审计日志（tb_audit）
- 新增前端管理员用户管理页面 `/admin/users`

## Capabilities

### New Capabilities
- `admin-user-management`: 管理员用户管理功能，包含用户列表查询、详情查看、信息更新、密码重置、删除及批量操作

### Modified Capabilities
- `backend-user-management`: 扩展现有用户管理规范，增加管理员操作相关的用户实体行为要求（级联删除、审计日志记录）

## Impact

- 后端：新增 `AdminUserController` 及配套 DDD 分层代码（Application Service、Domain Service、Repository 扩展）
- 数据库：无需表结构变更，复用现有 `tb_user` 及相关关联表
- 前端：新增 `/admin/users` 页面及 API 服务
- 权限：新增 `user:manage:*` 系列权限标识
- 审计：所有写操作通过 `AuditAspect` 自动记录
