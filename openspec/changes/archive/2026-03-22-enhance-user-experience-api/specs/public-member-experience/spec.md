# Public Member Experience Specification

定义公开查看团队成员经历的能力，允许未登录用户查看团队成员的项目、竞赛和实习经历。

## ADDED Requirements

### Requirement: 公开查看成员经历列表
系统SHALL允许未登录用户查看指定团队成员的经历列表。

#### Scenario: 未登录用户查看成员经历
- **WHEN** 未登录用户访问 `GET /api/v1/members/{memberId}/experiences`
- **THEN** 系统返回该成员的所有经历列表
- **AND** 返回数据包含经历ID、类型、标题、时间、详细内容
- **AND** 按开始时间倒序排列

#### Scenario: 按类型筛选成员经历
- **WHEN** 未登录用户访问 `GET /api/v1/members/{memberId}/experiences?type=project`
- **THEN** 系统仅返回该成员的项目经历
- **AND** type参数支持: project, competition, internship

#### Scenario: 查看不存在的成员经历
- **WHEN** 未登录用户访问不存在的成员ID
- **THEN** 系统返回404错误
- **AND** 错误消息为"成员不存在"

#### Scenario: 查看非团队成员经历
- **WHEN** 未登录用户访问考生(CANDIDATE)成员ID
- **THEN** 系统返回空列表
- **AND** 不返回任何错误

### Requirement: 成员经历数据格式
系统SHALL返回统一格式的经历数据，包含所有必要字段。

#### Scenario: 返回项目经历数据
- **WHEN** 查询成员的项目经历
- **THEN** 返回数据包含：
  - id: 经历ID
  - type: "project"
  - name: 项目名称
  - startDate: 开始时间
  - endDate: 结束时间
  - role: 角色
  - description: 项目描述
  - techStack: 技术栈列表
  - demoUrl: 演示链接

#### Scenario: 返回竞赛经历数据
- **WHEN** 查询成员的竞赛经历
- **THEN** 返回数据包含：
  - id: 经历ID
  - type: "competition"
  - name: 竞赛名称
  - date: 参赛时间
  - level: 竞赛级别
  - award: 获奖等级
  - teamSize: 团队人数
  - description: 竞赛描述
  - certificateUrl: 证书链接

#### Scenario: 返回实习经历数据
- **WHEN** 查询成员的实习经历
- **THEN** 返回数据包含：
  - id: 经历ID
  - type: "internship"
  - company: 公司名称
  - position: 职位
  - startDate: 开始时间
  - endDate: 结束时间
  - status: 状态(active/ended)
  - description: 工作描述
  - achievements: 成就列表

### Requirement: 接口访问性能
系统SHALL确保公开接口的响应性能满足要求。

#### Scenario: 响应时间要求
- **WHEN** 未登录用户查询成员经历
- **THEN** 系统在500ms内返回响应
- **AND** 支持并发访问

#### Scenario: 缓存机制
- **WHEN** 多次查询同一成员的经历
- **THEN** 系统使用缓存减少数据库查询
- **AND** 缓存有效期至少5分钟
