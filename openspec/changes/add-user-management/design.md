## Context

现有系统已具备完整的用户认证、报名审核、权限管理和自身信息管理功能，但管理员无法通过界面管理已创建的用户账号。SUPER_ADMIN 需要直接操作用户数据时只能访问数据库，存在安全风险和操作不便。

## Goals / Non-Goals

**Goals:**
- 为 SUPER_ADMIN 提供完整的用户生命周期管理能力
- 支持分页列表、详情查看、信息更新、密码重置、单删/批量删除
- 所有管理操作记录审计日志
- 删除用户时级联物理清理关联数据

**Non-Goals:**
- 不改动现有报名（enroll）→ 创建用户流程
- 不新增数据库表结构（复用现有表）
- 不支持非 SUPER_ADMIN 角色操作用户管理
- 不提供用户软删除（严格物理删除）

## Decisions

### 1. 接口路径统一放在 `/api/v1/admin/users`
- **理由**：与现有 `/api/v1/admin/*` 命名规范一致，前端路由对应 `/admin/users`
- **替代方案**：`/api/v1/user/admin`（否决，与自身管理接口混淆）

### 2. 权限控制使用 `@RequiresPermission` + `AccessLevel.PROTECTED`
- **理由**：遵循项目现有权限注解规范，权限标识格式统一为 `user:manage:*`
- 具体权限：
  - `user:manage:list` — 查询列表
  - `user:manage:detail` — 查看详情
  - `user:manage:update` — 更新信息
  - `user:manage:delete` — 删除用户
  - `user:manage:reset-password` — 重置密码
  - `user:manage:batch-operate` — 批量操作

### 3. 删除策略：物理删除 + 级联清理
- **理由**：项目规范明确不使用软删除；用户注销/清理是合理场景
- **级联范围**：
  1. `tb_user_experience`
  2. `tb_user_achievement`
  3. `tb_assessment_answer`
  4. `tb_assessment_session`
  5. `tb_comment`
  6. `tb_file`（头像文件，同步清理 MinIO）
  7. 最后删除 `tb_user`
- **事务**：整个级联删除在一个事务中执行

### 4. 重置密码由管理员直接指定
- **理由**：用户明确要求，管理员场景不需要邮箱验证流程
- **安全**：接口要求 `newPassword` + `confirmPassword` 二次确认，防止前端传参错误
- **存储**：使用 BCrypt 加密后存入 `tb_user.password`

### 5. 审计日志通过现有 `AuditAspect` 自动记录
- **理由**：项目已配置 AOP 审计切面，所有 Controller 层写操作自动记录
- **无需额外代码**：只需确保 Controller 方法被正确拦截

### 6. 列表查询使用 MyBatis-Plus 分页 + 动态 SQL
- **理由**：与现有 `AdminEnrollController` 等保持一致，复用 `PageQuery` 模式
- **筛选字段**：roleId、direction、collegeId、keyword（学号/姓名模糊搜索）

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| 物理删除误操作 | 前端二次确认弹窗；批量操作限制单次最大数量（如 50） |
| 级联删除性能问题 | 用户量目前较小（< 1000），单次删除在秒级；必要时加索引优化 |
| 删除用户后关联数据引用失效 | 其他表通过 user_id 查询时自然为空，不影响系统运行 |
| 管理员密码重置后用户无法登录 | 重置密码后用户需使用新密码登录，符合预期 |

## Migration Plan

无需数据库迁移。本变更纯代码层面，新增 API 和前端页面，不影响现有数据。

## Open Questions

- 批量操作单次最大数量限制（暂定 50）是否满足需求？
- 用户列表默认排序方式（暂定按 id 倒序，最新注册在前）？
