## Context

录用决策工作台 `getDecisionWorkspace()` 通过 `findCandidateScoreRows()` 从 `tb_user` 中按当前考核的 `direction + grade` 匹配所有 `CANDIDATE` 角色用户，再聚合为考生评分矩阵。该 SQL 不检查考生是否已在相同 direction+grade 的更早 epoch 中被淘汰。

考生端在 `candidate-elimination-restriction` 中已实现双重拦截：
- 考核列表 SQL 使用 `NOT EXISTS` 排除 prior epoch 淘汰考生；
- 单资源访问（题目列表/详情/提交）调用 `AssessmentDecisionDomainService.isEliminatedFromPriorEpoch()` 校验。

管理端工作台遗漏了同样的校验，是第一轮淘汰考生仍出现在第二轮决策列表的根因。

## Goals / Non-Goals

**Goals:**
- 录用决策工作台返回的考生列表和统计应与管理预期一致：已在前序轮次淘汰的考生不应出现在后续轮次的决策页面中。
- 保持与考生端一致的淘汰语义（same direction + grade，prior epoch）。
- 不引入新的数据库查询或外部依赖，复用现有的 `isEliminatedFromPriorEpoch()` 和 `AssessmentTime` 加载逻辑。

**Non-Goals:**
- 不改变当前轮次淘汰考生的展示（本轮 `passed = false` 的考生仍保留在「淘汰」筛选中）。
- 不改动前端 UI、API 契约或数据库 schema。
- 不涉及考生端已有的淘汰限制逻辑。

## Decisions

### Decision 1: 在应用层过滤而非 SQL 层

- **选择**：在 `AssessmentJudgementAppServiceImpl.getDecisionWorkspace()` 拿到 `AssessmentTime` 和评分矩阵后，逐一对考生调用 `isEliminatedFromPriorEpoch()` 过滤。
- **理由**：
  - 复用已有的领域校验方法，语义一致，避免在复杂 CTE SQL 中重复实现淘汰判定。
  - `findCandidateScoreRows()` 也被独立的 `listCandidateScoreboard()` 接口使用，SQL 层改动会影响评分矩阵展示范围（该接口用于评分，不一定要排除淘汰考生）。
- **替代方案**：修改 `selectCandidateScoreRows` CTE 加入 `NOT EXISTS` 子查询 — rejected，因为会改变 `listCandidateScoreboard()` 的行为，且 CTE 本身已较复杂。

### Decision 2: 统计基于过滤后的列表计算

- **选择**：`calculateDecisionStatistics()` 接收过滤后的 `scoreboards` 列表。
- **理由**：确保页面上「候选人 / 待决策 / 通过 / 淘汰」四个数字与列表实际展示的人数一致，避免统计与列表脱节。

### Decision 3: 仅排除 prior epoch，保留当前轮次淘汰记录

- **选择**：使用 `isEliminatedFromPriorEpoch(userId, currentTime)`，而不是把任何 `passed = false` 的考生都排除。
- **理由**：当前轮次已经被淘汰的考生需要保留在工作台中，否则「决策状态 = 淘汰」筛选会丢失本轮已决策数据，管理员也无法查看/修改本轮决策。

## Risks / Trade-offs

- [Risk] `isEliminatedFromPriorEpoch()` 对每个考生都会查询该用户的所有淘汰决策和对应考核时间，时间复杂度为 O(n × m)。
  → Mitigation：一页决策候选人数通常不超过几百人，且 `tb_assessment_decision` 已有 `(assessment_time_id, user_id)` 索引；`assessmentTimeRepository.findById()` 缓存命中率较高。若后续出现性能问题，可在该方法或调用方加缓存。
- [Risk] 过滤后如果所有考生都被排除，前端会显示空列表。
  → Mitigation：这是预期行为，与考生端过滤逻辑一致，前端已有「暂无候选人」空状态。
- [Risk] 发布邮件时 `publishDecisions()` 只读 `tb_assessment_decision`，不受本过滤影响。由于决策记录只在管理员显式操作后产生，已淘汰考生不会有当前轮次的决策记录，因此无需额外处理。
