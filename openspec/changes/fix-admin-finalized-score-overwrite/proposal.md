## Why

Issue #41 报告管理员多次确认最终评分时，评判记录未被覆盖更新，而是在 `tb_assessment_judgement` 中产生了多条 `ADMIN_FINALIZED` 记录。根因是 `finalizeJudgement()` 和 `propagateFinalizedJudgementToTeamMembers()` 始终执行 `INSERT`，不做覆盖检查。

## What Changes

- **修复覆盖更新**：`AssessmentJudgementDomainServiceImpl.finalizeJudgement()` 从纯 `INSERT` 改为"先查后更新"——同一 `answer_id` 的 `ADMIN_FINALIZED` 记录重复提交时覆盖原记录而非新增。
- **重构组队传播逻辑**：`AssessmentJudgementAppServiceImpl.finalizeScore()` 重写传播触发条件：
  - 队长**首次**确认最终评分 → 更新队长记录 + 批量传播给所有还没有 `ADMIN_FINALIZED` 的队员。
  - 队长**再次**确认最终评分 → 仅覆盖队长自己的记录，不再传播，避免覆盖队员的独立评分。
  - 队员被单独评分时 → 仅覆盖该队员自己的记录。
- **新增 Repository 方法**：
  - `findLatestByAnswerIdAndSource(answerId, source)` — 查询指定答案的指定来源最新评判。
  - `findAnswerIdsBySource(List<Long> answerIds, source)` — 批量查询哪些答案已有指定来源的评判。
- **删除冗余传播代码**：移除 `propagateFinalizedJudgementToTeamMembers()` 旧实现，替换为基于首次/非首次判断的新逻辑。

## Capabilities

### New Capabilities

（无新增 Capability，本变更是对现有行为的修复和优化。）

### Modified Capabilities

- `admin-finalized-judgement`：确认最终评分接口的行为变更——同一管理员对同一答案的多次评分由"新增记录"变为"覆盖更新"；组队场景下传播行为由"每次都传播"变为"仅首次传播"。
- `assessment-team-scoring`：组队评分传播规则变更——队长再次评分不再覆盖已独立评分的队员。

## Impact

- **后端**：`AssessmentJudgementDomainServiceImpl`、`AssessmentJudgementAppServiceImpl`、`AssessmentJudgementRepository`、`AssessmentJudgementMapper` 及对应 XML。
- **数据库**：无需 Flyway 迁移，利用已有表结构，逻辑层处理覆盖。
- **前端**：无需改动，API 契约不变（`FinalizeScoreRequestDTO` 已在前序提交中移除 `comment` 字段）。
- **测试**：需更新 `AssessmentJudgementAppServiceImplTest`、`AssessmentJudgementDomainServiceImplTest` 及集成测试中的断言。
