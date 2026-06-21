## Context

`GET /api/v1/assessment-times` 是当前用户（尤其是 CANDIDATE）高频访问的接口，用于展示“我的考核”列表。当前实现位于 `AssessmentTimeAppServiceImpl.listAssessmentTimesForUser`，其对分页结果中的每条考核记录都执行：

1. `AssessmentQuestionRepository.countByAssessmentTimeId(entity.getId())`
2. `AssessmentAnswerRepository.countByUserIdAndAssessmentTimeId(currentUser.getId(), entity.getId())`
3. `AssessmentDecisionDomainService.isEliminatedFromPriorEpoch(currentUser.getId(), entity)`

第 3 步内部又会重复执行 `assessmentDecisionRepository.findEliminatedDecisionsByUserId(userId)` 和 `assessmentTimeRepository.findById(decisionTimeId)`。一页 20 条记录时，总 SQL 往返可达 60~120 次，且随分页大小线性增长。

本次变更目标是在不改变接口契约的前提下，把这些循环内的单条查询改造为循环外的批量查询，并在 Java 内存中完成结果组装。

## Goals / Non-Goals

**Goals:**
- 将 `listAssessmentTimesForUser` 的数据库往返次数从 O(N) 降至 O(1)，与分页大小无关。
- 保持 `GET /api/v1/assessment-times` 的响应字段、分页结构和业务语义完全不变。
- 新增批量查询方法，同时保留现有单条查询方法以兼容其他调用方。
- 通过测试验证优化后结果与原逐条查询逻辑一致。

**Non-Goals:**
- 不修改 `tb_assessment_question`、`tb_assessment_answer`、`tb_assessment_decision`、`tb_assessment_time` 表结构。
- 不调整前端 `AssessmentCard` 组件或接口消费逻辑。
- 不修改考核分配过滤规则（保留 `my-assessments-query` 中定义的方向/年级/答题关联过滤）。
- 不引入缓存、消息队列或新的外部依赖。

## Decisions

### 1. 使用 `IN` 子句批量查询，而非单条复杂 JOIN
**决策**：新增 `countByAssessmentTimeIds(List<Long>)`、`countByUserIdAndAssessmentTimeIds(Long, List<Long>)` 和 `findAllById(List<Long>)` 三个批量方法，分别用 `IN` 子句查询。

**理由**：
- 与现有 MyBatis mapper 风格一致，改动范围可控。
- 避免单次 SQL 过度复杂导致优化器选择次优计划；三个独立批量查询仍可利用现有索引。
- 便于分别单测每个批量方法的边界行为。

**替代方案**：用一个包含 `LEFT JOIN` 和 `GROUP BY` 的大 SQL 同时返回题目数、答题数和考核场次信息。该方案 SQL 复杂度高，且当题目/答案表数据量大时可能影响分页查询性能，因此不采用。

### 2. 返回 `Map<Long, Integer>` 供内存查找
**决策**：批量 count 方法返回 `Map<assessmentTimeId, count>`，缺失 ID 默认按 0 处理。

**理由**：
- `Page.map()` 回调中可通过 `map.getOrDefault(entity.getId(), 0)` O(1) 取值。
- 避免在应用层再次遍历列表做匹配。

### 3. 在应用层批量预加载淘汰决策，并传入领域服务
**决策**：`listAssessmentTimesForUser` 先一次性查询当前用户的淘汰决策列表及其关联考核场次，组装成 `Map<Long, AssessmentTime>`，再调用改造后的 `isEliminatedFromPriorEpoch(targetTime, eliminatedDecisions, decisionTimeMap)`。

**理由**：
- 把 issue #30（外层 N+1）和 #31（内层单条 findById）一起解决。
- 领域服务保持纯内存判断，便于单元测试，不依赖 Spring 事务或数据库。

### 4. 保留现有单条 count 方法
**决策**：`countByAssessmentTimeId(Long)` 和 `countByUserIdAndAssessmentTimeId(Long, Long)` 继续保留。

**理由**：
- `getAssessmentProgress(Long userId, Long assessmentTimeId)` 等单条场景仍在使用，避免不必要的影响面。
- 符合开闭原则，新增批量方法而非替换。

### 5. 使用自定义结果对象作为批量 count 的 MyBatis 返回
**决策**：MyBatis 批量 count SQL 返回一个自定义 record / data object（如 `CountResult { Long timeId; Long count; }`），然后在 RepositoryImpl 中转换为 `Map<Long, Integer>`。

**理由**：
- MyBatis `resultType="java.util.HashMap"` 默认 key 为列名小写字符串，类型不明确，容易出错。
- 自定义对象可提供编译时类型安全，且便于测试断言。

## Risks / Trade-offs

- **[Risk]** `IN` 子句传入空列表时 MyBatis 渲染成 `IN ()` 会触发 SQL 语法错误。  
  **Mitigation**：RepositoryImpl 在调用 mapper 前检查列表非空；为空时直接返回空 Map。

- **[Risk]** 批量 `IN` 列表过长可能影响部分数据库版本的执行计划或参数解析。  
  **Mitigation**：考核列表分页默认 20 条，批量 ID 数量受分页大小限制，通常不超过 100，远低于风险阈值；如需更大分页可在 RepositoryImpl 内分批。

- **[Risk]`countByUserIdAndAssessmentTimeIds` 使用 `JOIN tb_assessment_question` 并按 `assessment_time_id` 分组，若某用户在同一考核下答题数巨大，分组结果集不会放大（每个考核一条），但 JOIN 过程扫描数据量较大。  
  **Mitigation**：该 SQL 与原 `countByUserIdAndAssessmentTimeId` 结构一致，只是多了 `IN` 和 `GROUP BY`，现有索引（`tb_assessment_answer.user_id`、`tb_assessment_question.assessment_time_id`）仍可有效过滤。

- **[Risk]** 改造后 `isEliminatedFromPriorEpoch` 的逻辑与原逻辑必须逐行等价，否则可能误标淘汰状态。  
  **Mitigation**：将原方法中的 `isSameDirectionAndGrade` 和 `isPriorEpoch` 私有方法复用或原样迁移到新的内存判断路径中；补充集成测试覆盖全局考核、方向考核、不限年级等场景。

- **[Trade-off]** 批量查询会一次性占用更多内存（三个 Map 和决策列表），但分页大小通常 20，数据量极小，收益远大于成本。

## Migration Plan

无需数据迁移或灰度发布。变更仅涉及应用层代码和新增 SQL：

1. 在测试环境部署后，调用 `GET /api/v1/assessment-times` 并抓取 SQL 日志，确认批量查询生效。
2. 对比优化前后响应内容是否一致。
3. 全量回归通过后即可上线。

## Open Questions

无。
