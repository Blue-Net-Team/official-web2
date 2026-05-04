## Why

当前后端 `PUT /api/v1/user/info` 接口允许所有已登录用户修改 `username` 字段，但没有权限控制。这与 `gender`、`college`、`major`、`direction` 等字段的权限控制不一致（这些字段仅 MEMBER 及以上可修改）。用户名是用户身份的重要标识，应该有更严格的修改限制。

## What Changes

- 在 `UserInfoServiceImpl.validateProfileUpdatePermission()` 方法中添加 `username` 字段的权限检查
- 只有 MEMBER 及以上角色才能修改 `username` 字段
- CANDIDATE 角色尝试修改 `username` 时返回 403 Forbidden 错误

## Capabilities

### New Capabilities

无新增能力。

### Modified Capabilities

- `user-profile-update`: 修改用户信息更新的权限要求，增加 `username` 字段的权限控制

## Impact

- **代码影响**: `UserInfoServiceImpl.java` 中的 `validateProfileUpdatePermission` 方法
- **API 影响**: `PUT /api/v1/user/info` 接口的权限行为变更
- **用户影响**: CANDIDATE 角色用户将无法通过 API 修改自己的用户名
