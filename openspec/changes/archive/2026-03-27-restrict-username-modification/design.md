## Context

当前系统通过 `UserInfoServiceImpl.validateProfileUpdatePermission()` 方法验证用户信息更新的权限。该方法已经实现了对 `gender`、`college`、`major`、`direction` 字段的权限控制（仅 MEMBER 及以上可修改），但遗漏了 `username` 字段的检查。

当前权限验证逻辑：
```java
private void validateProfileUpdatePermission(UserVO user, UpdateProfileRequestDTO request) {
    RoleType role = RoleType.fromName(user.getRoleName());

    if (role == RoleType.CANDIDATE) {
        if (request.getGender() != null || request.getCollege() != null
                || request.getMajor() != null || request.getDirection() != null) {
            throw new Forbidden("只有成员及以上角色才能修改性别、学院、专业和报名方向");
        }
    }
}
```

## Goals / Non-Goals

**Goals:**
- 在 `validateProfileUpdatePermission` 方法中添加 `username` 字段的权限检查
- 保持与现有字段权限控制逻辑的一致性
- CANDIDATE 角色尝试修改 `username` 时返回 403 Forbidden

**Non-Goals:**
- 不修改数据库 schema
- 不修改 API 接口定义
- 不修改前端代码

## Decisions

### Decision 1: 在现有验证方法中添加 username 检查

**选择**: 在 `validateProfileUpdatePermission` 方法中添加 `username` 字段检查

**理由**:
- 与现有 `gender`、`college`、`major`、`direction` 字段的验证逻辑保持一致
- 代码改动最小，只需添加一个条件判断
- 错误消息可以复用现有的提示

**备选方案**:
- 单独创建 `username` 验证方法：增加代码复杂度，没有额外收益
- 在 Controller 层验证：与应用层职责分离原则冲突

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| CANDIDATE 用户已修改过 username | 无需处理，变更后新请求将被拒绝 |
| 错误消息不够明确 | 可考虑将错误消息改为"只有成员及以上角色才能修改用户名、性别、学院、专业和报名方向" |
