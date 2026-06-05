## Why

Issue #25 报告：第一轮已被淘汰的考生（`cross_student`）在第二轮「录用决策」页面仍然出现，状态显示为「待决策」。

考生端已通过 `candidate-elimination-restriction` 完成过滤（列表 SQL 拦截 + 接口 `isEliminatedFromPriorEpoch` 校验），但管理端的录用决策工作台没有同步应用该规则，导致管理员可以对实际上已失去资格的考生做决策。

## What Changes

- 在 `AssessmentJudgementAppServiceImpl.getDecisionWorkspace()` 中，对 `listCandidateScoreboard()` 返回的考生评分矩阵调用 `AssessmentDecisionDomainService.isEliminatedFromPriorEpoch()` 进行过滤。
- 被过滤的考生不再出现在「录用决策」候选列表中，也不参与待决策/通过/淘汰的统计计数。
- **注意**：只过滤在**更早轮次**被淘汰的考生；当前轮次已被淘汰（`passed = false` 对应当前 `assessmentTimeId`）的考生保留显示，避免「淘汰」筛选丢失数据。
- 补充单元测试，覆盖「存在 prior epoch 淘汰考生时，工作台应排除该考生并更新统计」的场景。

## Capabilities

### New Capabilities
- 无

### Modified Capabilities
- `assessment-judgement`：在「Assessment pass decision」能力下，明确录用决策工作台返回的考生列表应排除在相同 direction+grade 组合的更早轮次中已被淘汰的考生。

## Impact

- 后端：`src/backend/.../AssessmentJudgementAppServiceImpl.java`
- 测试：`src/backend/.../AssessmentJudgementAppServiceImplTest.java`
- 前端：无改动，直接复用现有工作台展示逻辑
- 数据库：无改动
