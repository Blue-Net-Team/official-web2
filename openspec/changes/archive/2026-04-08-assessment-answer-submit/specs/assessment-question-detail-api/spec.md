## ADDED Requirements

### Requirement: 用户端查询题目详情
系统 SHALL 提供接口 `GET /api/v1/assessment-questions/{id}`，允许已认证用户查询单个题目详情。与列表接口不同，此接口 SHALL 返回题目 `content`（题目描述内容），供答题页展示。

权限校验：CANDIDATE 用户只能查看自己方向+年级的考核题目。

#### Scenario: 正常查询题目详情
- **WHEN** 已认证用户 GET `/api/v1/assessment-questions/1`
- **THEN** 返回 200 + 题目DTO（含 id, title, content, questionType, score, attachmentId）

#### Scenario: 题目不存在
- **WHEN** GET `/api/v1/assessment-questions/9999`
- **THEN** 返回 404 错误

#### Scenario: CANDIDATE 越权访问
- **WHEN** CANDIDATE 用户访问不属于自己方向/年级的题目详情
- **THEN** 返回 403 错误
