## ADDED Requirements

### Requirement: 用户可以获取画像信息

系统应允许用户获取自己的画像信息，包括基本信息和Tab计数。

#### Scenario: 成功获取用户画像
- **WHEN** 已登录用户请求 GET /api/v1/user/profile
- **THEN** 系统返回用户基本信息（id、name、nickname、studentId、grade、college、major、direction、bio、email、role、avatar）
- **AND** 系统返回Tab计数（projects、competitions、internships）

#### Scenario: 未登录用户请求画像
- **WHEN** 未登录用户请求 GET /api/v1/user/profile
- **THEN** 系统返回401 Unauthorized错误

### Requirement: 用户可以更新基本信息

系统应允许用户更新自己的基本信息，包括昵称、个人简介。

#### Scenario: 成功更新基本信息
- **WHEN** 已登录用户请求 PUT /api/v1/user/profile，包含nickname、bio字段
- **THEN** 系统更新用户信息
- **AND** 系统返回更新后的用户基本信息

#### Scenario: 更新时部分字段为空
- **WHEN** 已登录用户请求 PUT /api/v1/user/profile，仅包含部分字段
- **THEN** 系统仅更新提供的字段
- **AND** 未提供的字段保持原值

### Requirement: 用户基本信息字段定义

系统应返回以下用户基本信息字段：

| 字段 | 类型 | 说明 | 数据来源 |
|------|------|------|----------|
| id | string | 用户ID | tb_user.id |
| name | string | 真实姓名 | tb_user.username |
| nickname | string? | 昵称 | tb_user.nickname |
| studentId | string | 学号 | tb_user.student_id |
| grade | string | 年级 | 根据学号计算 |
| college | string | 学院名称 | tb_college.name (JOIN) |
| major | string | 专业 | tb_user.major |
| direction | string | 方向 | tb_user.direction |
| bio | string? | 个人简介 | tb_user.bio |
| email | string | 邮箱 | tb_user.email |
| role | string | 角色类型 | tb_role.name (JOIN) |
| avatar | string? | 头像URL | tb_file.url (JOIN) |

#### Scenario: 年级计算逻辑
- **WHEN** 用户学号为"202401000001"
- **THEN** 系统计算年级为"大一"（学号前4位为入学年份，当前年份-入学年份=0）

#### Scenario: 方向显示格式
- **WHEN** 用户方向为 COMPUTER_VISION
- **THEN** 系统返回"计算机视觉"

### Requirement: Tab计数计算规则

系统应按以下规则计算Tab计数：

| 字段 | 计算规则 |
|------|----------|
| projects | 用户项目经历数（tb_user_experience中type=PROJECT） |
| competitions | 用户竞赛经历数（tb_user_experience中type=COMPETITION） |
| internships | 用户实习经历数（tb_user_experience中type=INTERNSHIP） |

#### Scenario: 计算经历数量
- **WHEN** 用户有2条项目经历、3条竞赛经历、1条实习经历
- **THEN** tabCounts = {projects: 2, competitions: 3, internships: 1}
