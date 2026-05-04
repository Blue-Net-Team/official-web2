## ADDED Requirements

### Requirement: 前端可获取当前用户信息

前端 SHALL 通过 `GET /api/v1/user/info` 获取当前登录用户的基本信息。

#### Scenario: 成功获取用户信息
- **WHEN** 已登录用户访问个人主页
- **THEN** 系统调用 `getUserInfo()` API 获取用户信息
- **AND** 页面展示真实的用户数据

#### Scenario: 未登录用户访问
- **WHEN** 未登录用户访问个人主页
- **THEN** 系统重定向到登录页

### Requirement: 前端可更新用户信息

前端 SHALL 通过 `PUT /api/v1/user/info` 更新用户信息，根据用户角色控制可修改字段。

#### Scenario: CANDIDATE 更新基本信息
- **WHEN** CANDIDATE 角色用户修改昵称或个人简介
- **THEN** 系统调用 `updateProfile()` API
- **AND** 更新成功后显示成功提示

#### Scenario: MEMBER 更新扩展信息
- **WHEN** MEMBER 及以上角色用户修改用户名、性别、学院、专业或方向
- **THEN** 系统调用 `updateProfile()` API
- **AND** 更新成功后显示成功提示

#### Scenario: CANDIDATE 尝试修改受限字段
- **WHEN** CANDIDATE 角色用户尝试修改用户名、性别、学院、专业或方向
- **THEN** 前端禁止编辑这些字段（输入框禁用）

### Requirement: 前端可获取 Tab 计数

前端 SHALL 通过 `GET /api/v1/user/tab-counts` 获取 Tab 计数。

#### Scenario: 显示 Tab 计数
- **WHEN** 用户访问个人主页
- **THEN** 系统调用 `getTabCounts()` API
- **AND** Tab 标签显示对应的数据计数
