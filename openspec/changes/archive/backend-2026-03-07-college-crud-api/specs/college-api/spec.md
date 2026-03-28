## Purpose

学院管理API接口，提供学院的增删查改功能。

## ADDED Requirements

### Requirement: 获取学院列表
系统应允许用户获取所有学院的列表。

#### Scenario: 成功获取学院列表
- **WHEN** 用户请求 GET /api/v1/colleges
- **THEN** 系统应返回所有学院的列表
- **THEN** 每个学院应包含 id 和 name 字段
- **THEN** 接口应为公开接口，无需登录

### Requirement: 创建学院
系统应允许管理员创建新的学院。

#### Scenario: 管理员成功创建学院
- **WHEN** 管理员请求 POST /api/v1/admin/colleges
- **THEN** 系统应创建新的学院记录
- **THEN** 返回创建的学院信息
- **THEN** 需要管理员权限 (college:create)

#### Scenario: 创建学院名称重复
- **WHEN** 管理员请求创建已存在的学院名称
- **THEN** 系统应返回400错误
- **THEN** 错误信息应提示学院名称已存在

### Requirement: 更新学院
系统应允许管理员更新学院信息。

#### Scenario: 管理员成功更新学院
- **WHEN** 管理员请求 PUT /api/v1/admin/colleges/{id}
- **THEN** 系统应更新指定学院的信息
- **THEN** 返回更新后的学院信息
- **THEN** 需要管理员权限 (college:update)

#### Scenario: 更新不存在的学院
- **WHEN** 管理员请求更新不存在的学院ID
- **THEN** 系统应返回404错误
- **THEN** 错误信息应提示学院不存在

### Requirement: 删除学院
系统应允许管理员删除学院。

#### Scenario: 管理员成功删除学院
- **WHEN** 管理员请求 DELETE /api/v1/admin/colleges/{id}
- **THEN** 系统应删除指定学院
- **THEN** 需要管理员权限 (college:delete)

#### Scenario: 删除不存在的学院
- **WHEN** 管理员请求删除不存在的学院ID
- **THEN** 系统应返回404错误
- **THEN** 错误信息应提示学院不存在

#### Scenario: 删除有关联用户的学院
- **WHEN** 管理员请求删除有用户关联的学院
- **THEN** 系统应返回400错误
- **THEN** 错误信息应提示学院存在关联用户，无法删除

#### Scenario: 删除有关联报名的学院
- **WHEN** 管理员请求删除有报名记录关联的学院
- **THEN** 系统应返回400错误
- **THEN** 错误信息应提示学院存在关联报名记录，无法删除
