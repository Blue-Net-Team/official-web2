## ADDED Requirements

### Requirement: 前端可获取用户经历列表

前端 SHALL 通过 `GET /api/v1/user/experiences` 获取当前用户的经历列表，支持按类型过滤。

#### Scenario: 获取全部经历
- **WHEN** 用户访问个人主页经历相关 Tab
- **THEN** 系统调用 `getExperiences()` API
- **AND** 展示真实的经历数据

#### Scenario: 按类型过滤经历
- **WHEN** 用户访问项目/竞赛/实习 Tab
- **THEN** 系统调用 `getExperiences(type)` API 传递对应类型
- **AND** 仅展示该类型的经历

### Requirement: 前端可创建用户经历

前端 SHALL 通过 `POST /api/v1/user/experiences` 创建新的经历记录。

#### Scenario: 创建项目经历
- **WHEN** MEMBER 及以上用户点击添加项目经历并提交表单
- **THEN** 系统调用 `createExperience({ type: 'project', ... })` API
- **AND** 创建成功后刷新列表并显示成功提示

#### Scenario: CANDIDATE 尝试创建经历
- **WHEN** CANDIDATE 用户点击添加经历
- **THEN** 前端显示"仅成员及以上可添加经历"提示
- **AND** 不显示添加按钮或禁用添加功能

### Requirement: 前端可更新用户经历

前端 SHALL 通过 `PUT /api/v1/user/experiences/{id}` 更新指定经历。

#### Scenario: 更新经历
- **WHEN** MEMBER 及以上用户修改已有经历并保存
- **THEN** 系统调用 `updateExperience(id, data)` API
- **AND** 更新成功后刷新列表

### Requirement: 前端可删除用户经历

前端 SHALL 通过 `DELETE /api/v1/user/experiences/{id}` 删除指定经历。

#### Scenario: 删除经历
- **WHEN** MEMBER 及以上用户删除某条经历
- **THEN** 系统调用 `deleteExperience(id)` API
- **AND** 删除成功后从列表中移除该项
