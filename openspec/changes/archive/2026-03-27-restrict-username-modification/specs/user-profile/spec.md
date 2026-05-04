## MODIFIED Requirements

### Requirement: 用户可以更新基本信息

系统 SHALL 允许用户更新自己的基本信息。不同角色的可修改字段权限如下：

| 字段 | CANDIDATE | MEMBER 及以上 |
|------|-----------|---------------|
| username | ❌ 不可修改 | ✅ 可修改 |
| nickname | ✅ 可修改 | ✅ 可修改 |
| bio | ✅ 可修改 | ✅ 可修改 |
| gender | ❌ 不可修改 | ✅ 可修改 |
| college | ❌ 不可修改 | ✅ 可修改 |
| major | ❌ 不可修改 | ✅ 可修改 |
| direction | ❌ 不可修改 | ✅ 可修改 |

#### Scenario: 成功更新基本信息（MEMBER 角色）
- **WHEN** MEMBER 角色用户请求 PUT /api/v1/user/info，包含 username、nickname、bio 等字段
- **THEN** 系统更新用户信息
- **AND** 系统返回 200 成功响应

#### Scenario: 成功更新基本信息（CANDIDATE 角色）
- **WHEN** CANDIDATE 角色用户请求 PUT /api/v1/user/info，仅包含 nickname、bio 字段
- **THEN** 系统更新用户信息
- **AND** 系统返回 200 成功响应

#### Scenario: CANDIDATE 尝试修改 username
- **WHEN** CANDIDATE 角色用户请求 PUT /api/v1/user/info，包含 username 字段
- **THEN** 系统返回 403 Forbidden 错误
- **AND** 错误消息为"只有成员及以上角色才能修改用户名、性别、学院、专业和报名方向"

#### Scenario: CANDIDATE 尝试修改受限字段
- **WHEN** CANDIDATE 角色用户请求 PUT /api/v1/user/info，包含 gender、college、major 或 direction 字段
- **THEN** 系统返回 403 Forbidden 错误
- **AND** 错误消息为"只有成员及以上角色才能修改用户名、性别、学院、专业和报名方向"

#### Scenario: 更新时部分字段为空
- **WHEN** 已登录用户请求 PUT /api/v1/user/info，仅包含部分字段
- **THEN** 系统仅更新提供的字段
- **AND** 未提供的字段保持原值
