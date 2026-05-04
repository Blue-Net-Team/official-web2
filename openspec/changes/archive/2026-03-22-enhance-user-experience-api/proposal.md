## Why

当前用户经历管理接口存在权限控制缺陷：所有已登录用户（包括考生CANDIDATE）都能创建、修改、删除经历，不符合业务需求。同时，团队成员的经历信息无法对外公开展示，缺少公开访问接口。

## What Changes

- **权限控制增强**: 修改经历管理接口（创建/更新/删除）的权限控制，限制只有MEMBER及以上角色才能管理经历
- **新增公开接口**: 添加查看团队成员经历的公开接口，未登录用户可查看成员经历列表
- **修复代码缺陷**: 修复 `UserExperienceDomainServiceImpl.getExperienceById()` 中的权限校验bug
- **完善测试覆盖**: 添加完整的单元测试、集成测试和边界测试

## Capabilities

### New Capabilities

- `public-member-experience`: 公开查看团队成员经历的能力，支持未登录用户访问

### Modified Capabilities

- `user-experience`: 用户经历管理能力，修改权限控制要求，限制只有MEMBER及以上角色才能管理经历

## Impact

**受影响的代码模块**:
- `UserExperienceController`: 修改权限注解，调整访问级别
- `MemberController`: 新增公开查看成员经历的接口
- `UserExperienceDomainServiceImpl`: 修复权限校验bug
- `MemberDetailDTO`: 可选优化，添加经历列表字段

**数据库变更**:
- 需要在 `tb_permission` 表中添加新权限：`user:experience:create`、`user:experience:update`、`user:experience:delete`
- 需要在 `tb_role_permission` 表中为 MEMBER、DIRECTION_ADMIN、SUPER_ADMIN 角色分配这些权限

**API变更**:
- 修改现有接口的权限要求（向后兼容，不影响已有功能）
- 新增公开接口 `GET /api/v1/members/{memberId}/experiences`

**测试覆盖**:
- 单元测试：Controller、Service、Repository 各层
- 集成测试：API 接口的各种场景
- 边界测试：权限、参数、异常等边界条件
