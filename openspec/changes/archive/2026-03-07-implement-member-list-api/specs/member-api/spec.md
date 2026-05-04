## Purpose

团队成员公开API接口，提供团队成员列表查询、成员详情查询、方向负责人查询功能。

## ADDED Requirements

### Requirement: 获取团队成员列表
系统应允许访客分页查询团队成员列表。

#### Scenario: 成功获取团队成员列表
- **WHEN** 访客请求 GET /api/v1/members
- **THEN** 系统应返回分页的成员列表
- **THEN** 每个成员应包含 id、username、nickname、direction、job、avatarUrl、college、major、enrollmentYear 字段
- **THEN** enrollmentYear 从学号前4位推断
- **THEN** 接口应为公开接口，无需登录
- **THEN** 仅返回未禁用且角色级别 >= MEMBER 的用户

#### Scenario: 按方向筛选成员
- **WHEN** 访客请求 GET /api/v1/members?direction=computer_vision
- **THEN** 系统应返回计算机视觉方向的成员列表
- **THEN** direction 参数可选，值为 computer_vision、structural_design、embedded 之一

#### Scenario: 分页参数
- **WHEN** 访客请求 GET /api/v1/members?page=0&size=10
- **THEN** 系统应返回第1页，每页10条记录
- **THEN** page 默认为 0，size 默认为 20，最大为 100

#### Scenario: 成员排序规则
- **WHEN** 访客请求 GET /api/v1/members
- **THEN** 系统应按入学年份降序排列成员（新人在前）
- **THEN** 入学年份从学号前4位推断（如 `20210001001` → 入学年份 2021）
- **THEN** 同年级成员按 ID 升序排列

### Requirement: 获取成员详情
系统应允许访客查看指定成员的详细信息。

#### Scenario: 成功获取成员详情
- **WHEN** 访客请求 GET /api/v1/members/{id}
- **THEN** 系统应返回成员的详细信息
- **THEN** 成员详情应包含 id、username、nickname、direction、job、avatarUrl、college、major、gender、githubUsername、wechatQrcode 字段
- **THEN** 接口应为公开接口，无需登录

#### Scenario: 成员不存在
- **WHEN** 访客请求 GET /api/v1/members/{id} 且 id 对应的用户不存在
- **THEN** 系统应返回404错误
- **THEN** 错误信息应提示成员不存在

#### Scenario: 成员已禁用
- **WHEN** 访客请求 GET /api/v1/members/{id} 且该用户已被禁用
- **THEN** 系统应返回404错误
- **THEN** 错误信息应提示成员不存在

#### Scenario: 成员角色不足
- **WHEN** 访客请求 GET /api/v1/members/{id} 且该用户角色为 CANDIDATE
- **THEN** 系统应返回404错误
- **THEN** 错误信息应提示成员不存在

### Requirement: 获取方向负责人
系统应允许访客查询各方向的负责人信息。

#### Scenario: 成功获取方向负责人列表
- **WHEN** 访客请求 GET /api/v1/members/direction-leaders
- **THEN** 系统应返回所有三个方向的负责人信息
- **THEN** 每个方向应包含 direction、directionName、leader 字段
- **THEN** leader 应包含 id、username、nickname、avatarUrl 字段
- **THEN** 接口应为公开接口，无需登录

#### Scenario: 方向暂无负责人
- **WHEN** 某方向没有符合条件的负责人
- **THEN** 该方向的 leader 字段应为 null
- **THEN** 其他方向的负责人正常返回

#### Scenario: 方向负责人判定规则
- **GIVEN** 用户角色为 DIRECTION_ADMIN 或 SUPER_ADMIN
- **AND** 用户的 direction 字段与该方向匹配
- **THEN** 该用户为该方向的负责人
