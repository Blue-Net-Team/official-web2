## MODIFIED Requirements

### Requirement: AssessmentTimeDTO 包含进度信息

`AssessmentTimeDTO` SHALL 在列表查询接口 `GET /api/v1/assessment-times` 的响应中新增 `totalQuestions`（Integer）和 `completedQuestions`（Integer）字段。

- `totalQuestions`: 该考核时间关联的题目总数
- `completedQuestions`: 当前登录用户在该考核时间下已提交答案的题目数

#### Scenario: 列表接口返回进度信息
- **WHEN** 考生请求 `GET /api/v1/assessment-times`
- **THEN** 每个 assessment time 条目包含 `totalQuestions` 和 `completedQuestions` 字段

#### Scenario: 考核时间无题目
- **WHEN** 考核时间未关联任何题目
- **THEN** `totalQuestions` 为 0，`completedQuestions` 为 0

#### Scenario: 考生未答任何题目
- **WHEN** 考生未在该考核时间下提交任何答案
- **THEN** `completedQuestions` 为 0
