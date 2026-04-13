## ADDED Requirements

### Requirement: 用户头像更新接口
系统 SHALL 提供接口 `PUT /api/v1/users/avatar`，允许已登录用户通过 fileId 更新头像。

#### Scenario: 成功更新头像
- **WHEN** 已登录用户 PUT `/api/v1/users/avatar` body=`{fileId: 123}`
- **THEN** 系统 SHALL 校验 fileId 对应的文件存在且类型为 AVATAR
- **AND** 系统 SHALL 更新 tb_user.avatar_id 为 fileId
- **AND** 返回 200 + 更新后的用户信息

#### Scenario: 文件不存在
- **WHEN** PUT `/api/v1/users/avatar` body=`{fileId: 9999}`
- **THEN** 返回 404 错误，提示文件不存在

#### Scenario: 文件类型不匹配
- **WHEN** PUT `/api/v1/users/avatar` body=`{fileId: 123}` 但文件类型不是 AVATAR
- **THEN** 返回 400 错误，提示文件类型不匹配

#### Scenario: 未登录用户
- **WHEN** 未登录用户 PUT `/api/v1/users/avatar`
- **THEN** 返回 401 Unauthorized
