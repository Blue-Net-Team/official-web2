## ADDED Requirements

### Requirement: 成员简要信息包含性别字段

系统应在成员简要信息 DTO (`MemberBriefDTO`) 中包含性别字段，以便前端能够展示成员性别信息。

#### Scenario: 获取成员列表返回性别信息
- **WHEN** 调用 `GET /api/v1/members` 接口
- **THEN** 返回的 `MemberBriefDTO` 列表中每个成员包含 `gender` 字段
- **AND** `gender` 字段值为 `MALE`、`FEMALE`、`UNKNOWN` 或 `null`

#### Scenario: 性别字段正确映射
- **WHEN** 成员在数据库中有性别记录
- **THEN** `MemberBriefDTO.gender` 应正确返回对应的 `Gender` 枚举值

#### Scenario: 性别字段为空处理
- **WHEN** 成员在数据库中没有性别记录
- **THEN** `MemberBriefDTO.gender` 应返回 `null`
