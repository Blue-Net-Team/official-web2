## ADDED Requirements

### Requirement: 创建答案
系统 SHALL 提供接口 `POST /api/v1/assessment-answers`，允许已认证用户提交考核答案。请求体包含 `questionId`（必填）和 `fileId`（文件上传题必填）。系统 SHALL 在创建答案前校验：
1. 题目存在且属于当前用户可见的考核时间
2. 用户未对该题目提交过答案（防止重复提交）
3. fileId 对应的文件存在（如果提供了 fileId）

创建成功后 SHALL 返回答案记录（含 id、submitTime）。

#### Scenario: 正常提交文件上传题答案
- **WHEN** 已认证用户 POST `/api/v1/assessment-answers` body=`{questionId: 1, fileId: 100}`
- **THEN** 系统创建 answer 记录（userId=当前用户, questionId=1, fileId=100, submitTime=now），返回 200 + 答案DTO

#### Scenario: 重复提交答案被拒绝
- **WHEN** 已认证用户已对 questionId=1 提交过答案，再次 POST `/api/v1/assessment-answers` body=`{questionId: 1, fileId: 200}`
- **THEN** 返回 409 冲突错误，提示"已提交过答案"

#### Scenario: 题目不存在
- **WHEN** POST body=`{questionId: 9999, fileId: 100}`
- **THEN** 返回 404 错误，提示"题目不存在"

#### Scenario: 未认证用户
- **WHEN** 未认证用户 POST `/api/v1/assessment-answers`
- **THEN** 返回 401 错误

### Requirement: 查询当前用户答案
系统 SHALL 提供接口 `GET /api/v1/assessment-answers?questionId=X`，允许已认证用户查询自己对指定题目的答案记录。返回答案详情（含 fileId、submitTime），未作答时返回 null。

#### Scenario: 已作答查询
- **WHEN** 已认证用户 GET `/api/v1/assessment-answers?questionId=1`，且该用户已提交答案
- **THEN** 返回 200 + 答案DTO（id, questionId, fileId, submitTime）

#### Scenario: 未作答查询
- **WHEN** 已认证用户 GET `/api/v1/assessment-answers?questionId=1`，且该用户未提交答案
- **THEN** 返回 200 + data=null

### Requirement: 答案权限控制
答案接口 SHALL 使用 `@RequiresPermission` 注解，访问级别为 `AUTHENTICATED`。用户只能查看和创建自己的答案。

#### Scenario: 用户只能查看自己的答案
- **WHEN** 用户 A 查询 questionId=1 的答案
- **THEN** 仅返回用户 A 自己的答案记录，不返回其他用户的
