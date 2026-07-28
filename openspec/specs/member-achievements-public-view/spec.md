# member-achievements-public-view Specification

## Purpose
TBD - created by archiving change replace-competition-experience-with-member-achievements. Update Purpose after archive.
## Requirements
### Requirement: 公开接口按成员查询成就

系统SHALL提供公开接口，允许访客按用户 ID 查询该用户关联的官方成就列表。

#### Scenario: 获取成员成就列表
- **WHEN** 客户端请求 `GET /api/v1/members/{memberId}/achievements`
- **THEN** 系统返回该成员关联的成就列表，按获奖日期倒序排列
- **AND** 每个成就包含标题、类型、关联项、获奖日期、奖项级别、奖项名称、文件 URL、系统内成员、外部协作者

#### Scenario: 成员无成就
- **WHEN** 客户端请求某成员的成就列表，但该成员未关联任何成就
- **THEN** 系统返回空数组

#### Scenario: 成员不存在
- **WHEN** 客户端请求不存在的成员 ID 的成就列表
- **THEN** 系统返回 404 错误，提示“成员不存在”

### Requirement: 成员主页展示个人成就

系统SHALL在成员公开主页展示“个人成就”Tab，仅展示由管理员维护的成就数据。

#### Scenario: 查看成员个人成就
- **WHEN** 访客访问 `/members/{id}` 并点击“个人成就”Tab
- **THEN** 系统调用 `GET /api/v1/members/{id}/achievements`
- **AND** 展示该成员关联的成就卡片列表

#### Scenario: 个人成就卡片展示
- **WHEN** 个人成就列表加载完成
- **THEN** 每个卡片展示成就标题、类型标签、获奖日期、奖项级别、奖项名称、证书/奖杯图片
- **AND** 展示系统内成员头像和姓名（可点击跳转成员主页）
- **AND** 展示外部协作者纯文本标签

#### Scenario: 个人成就空状态
- **WHEN** 成员没有任何关联成就
- **THEN** “个人成就”Tab 展示空状态提示，如“暂无个人成就”

### Requirement: 个人中心展示只读个人成就

系统SHALL在个人中心新增“个人成就”只读 Tab，数据来源与成员主页一致。

#### Scenario: 个人中心查看个人成就
- **WHEN** 已登录用户访问 `/profile` 并点击“个人成就”Tab
- **THEN** 系统调用 `GET /api/v1/members/{currentUserId}/achievements`
- **AND** 展示只读成就列表，不提供新增、编辑、删除按钮

