## Why

`GET /api/v1/assessment-times`（我的考核列表）在 `AssessmentTimeAppServiceImpl.listAssessmentTimesForUser` 中对每一页考核记录都进行多次单条数据库查询：统计题目总数、统计已完成答题数、判断往期淘汰状态。一页 20 条记录时，总 SQL 往返可达 60~120 次，造成明显的 IO 放大和响应延迟。本次变更通过批量查询将同类数据一次性拉取到内存，再由 Java 进行聚合拼接，把查询复杂度从 O(N) 降到 O(1)。

## What Changes

- 在 `AssessmentQuestionRepository` / `AssessmentQuestionMapper` 中新增 `countByAssessmentTimeIds(List<Long>)`，按考核场次 ID 批量统计题目数量。
- 在 `AssessmentAnswerRepository` / `AssessmentAnswerMapper` 中新增 `countByUserIdAndAssessmentTimeIds(Long, List<Long>)`，按用户 + 考核场次 ID 批量统计已完成答题数量。
- 在 `AssessmentTimeRepository` / `AssessmentTimeMapper` 中新增 `findAllById(List<Long>)`，批量加载考核场次信息。
- 改造 `AssessmentTimeAppServiceImpl.listAssessmentTimesForUser`：在 `Page.map()` 外批量查询 count 和淘汰决策数据，循环内只做内存取值与拼接。
- 改造 `AssessmentDecisionDomainServiceImpl.isEliminatedFromPriorEpoch`：支持接收已预加载的淘汰决策列表和考核场次 Map，避免循环内重复查询。
- 补充/更新对应单元测试和集成测试，验证优化后结果与原逻辑一致。

## Capabilities

### New Capabilities

- `assessment-list-batch-queries`: 我的考核列表接口的批量查询与内存聚合优化能力，保持接口契约不变。

### Modified Capabilities

无。`GET /api/v1/assessment-times` 的接口契约、字段含义和过滤规则保持不变（参见 `openspec/specs/my-assessments-query/spec.md`），仅内部查询方式优化。

## Impact

- **后端**：`AssessmentTimeAppServiceImpl`、`AssessmentDecisionDomainServiceImpl`、相关 Repository/Mapper/Impl/Mapper XML。
- **接口**：无 API 契约变更；响应字段和分页结构保持不变。
- **数据库**：新增 3 条批量查询 SQL，删除/减少原逐条 count 和逐条 `findById` 调用。
- **测试**：新增批量 Repository 单元测试；更新 `listAssessmentTimesForUser` 集成测试以覆盖批量路径。
- **性能**：列表接口 SQL 往返次数从与分页大小相关变为固定 5 次左右。
