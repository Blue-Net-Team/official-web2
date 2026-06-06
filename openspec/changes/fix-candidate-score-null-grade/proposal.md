## Why

在考核评分页面的「人员视图」中，当考核时间的 `grade` 为 `null`（不限年级）时，查询返回空结果，考生列表无法显示。而「题目视图」正常，因为后者的 SQL 不预先按年级过滤考生范围。根本原因是 PostgreSQL 三值逻辑：`NULL = NULL` 返回 `UNKNOWN`（视为 false），导致 `candidate_users` CTE 永远为空。

Issue #42 已报告此问题。

## What Changes

- 修复 `AssessmentJudgementMapper.xml` 中 `selectCandidateScoreRows` 的 `candidate_users` CTE：
  - `direction` 条件：`u.direction = t.direction` → `(t.direction IS NULL OR u.direction = t.direction)`
  - `grade` 条件：`COALESCE(...) = t.grade` → `(t.grade IS NULL OR COALESCE(...) = t.grade)`
- 新增 `AssessmentJudgementMapperIntegrationTest`：验证 `grade=null` 时人员视图能正确返回考生评分矩阵
- 新增 `AssessmentJudgementAppServiceImplTest` 用例：验证 `listCandidateScoreboard` 在 `grade=null` 时返回正确聚合结果

## Capabilities

### New Capabilities
- *(none)*

### Modified Capabilities
- *(none)* — 这是纯实现层 bug 修复，不改变任何 spec-level 需求。人员视图「应按考核时间的方向/年级过滤考生」这一需求已在 `assessment-judgement` 中定义，无需变更。

## Impact

- **SQL 文件**：`src/backend/src/main/resources/infrastructure/repository/mapper/AssessmentJudgementMapper.xml`
- **新增测试**：`src/backend/src/test/java/com/bluenet/web/infrastructure/repository/mapper/AssessmentJudgementMapperIntegrationTest.java`
- **补充测试**：`src/backend/src/test/java/com/bluenet/web/application/service/impl/AssessmentJudgementAppServiceImplTest.java`
- **前端**：无需修改，修复后 `/admin/assessment/judge/score` 人员视图即可正常显示
- **API**：无接口变更，仅修复查询逻辑
