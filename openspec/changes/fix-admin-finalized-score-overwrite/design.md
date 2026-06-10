## Context

当前 `AssessmentJudgementDomainServiceImpl.finalizeJudgement()` 始终调用 `assessmentJudgementRepository.save()`（底层为 `INSERT`），导致同一答案多次确认最终评分时产生多条 `ADMIN_FINALIZED` 记录（Issue #41）。

组队场景下，`AssessmentJudgementAppServiceImpl.propagateFinalizedJudgementToTeamMembers()` 同样直接 `batchInsert` 全队，每次都会覆盖队员的独立评分，违背了"可以给成员单独评分"的业务需求。

## Goals / Non-Goals

**Goals:**
- 修复 Issue #41：`ADMIN_FINALIZED` 记录支持覆盖更新，不再产生重复记录。
- 组队场景下，队长首次评分时传播给无评分的队员；队长再次评分时仅更新自己，保护队员的独立评分。
- 所有修改通过 Repository 层批量 SQL 完成，避免 Java 循环 execute SQL。

**Non-Goals:**
- 不改动 `MANUAL` / `AUTO` 等其他 source 类型的评判逻辑。
- 不改动前端 API 契约（`FinalizeScoreRequestDTO` 不变）。
- 不在数据库层面添加唯一约束（Flyway 无迁移）。
- 不处理"人为删除数据库记录导致队长再次评分时队员无评分"的异常边界。

## Decisions

### 1. 用"先查后更新"替代纯 INSERT

`finalizeJudgement()` 在保存前查询该 `answer_id` 是否已有 `ADMIN_FINALIZED` 记录：
- 存在 → 复用原 `id`，调用 `update()` 覆盖。
- 不存在 → 调用 `save()` 新建。

**Rationale**：无需 Flyway 迁移，纯业务层逻辑控制，对现有数据零影响。

### 2. 队长再次评分不传播，用"是否已有 ADMIN_FINALIZED"作为触发条件

`finalizeScore()` 的逻辑分支：
```
if (当前 answer 已有 ADMIN_FINALIZED) {
    // 再次评分 → 只覆盖当前 answer
} else {
    // 首次评分
    if (是队长) { 传播给无评分队员 }
    else { 只覆盖当前 answer }
}
```

**Rationale**：简单可靠，"已有评分"是天然的"非首次"标识。不需要额外维护"是否已被传播过"的状态。

### 3. 传播过滤在 Java 内存中完成，插入用 batchInsert

步骤：
1. 一次 SQL 批量查询队员哪些已有 `ADMIN_FINALIZED`。
2. Java 内存过滤（`HashSet` 判断），构建待插入列表。
3. 一次 `batchInsert` SQL 写入。

**Rationale**：满足"不用 Java 循环 execute SQL"的要求。过滤逻辑简单，内存开销可忽略（队伍通常 2-5 人）。

## Risks / Trade-offs

- **[Risk]** `findLatestByAnswerIdAndSource` 引入额外查询，每次 `finalizeScore` 多 1 次 SQL。  
  **Mitigation**：该查询命中 `idx_assessment_judgement_answer_id` 索引，且 answer_id + source 过滤结果集极小，性能影响可忽略。

- **[Risk]** 组队场景下，如果队员在队长首次评分前已被单独评分，队长首次评分时不会覆盖该队员。  
  **Mitigation**：这是预期行为，符合"支持队员独立评分"的需求。

- **[Risk]** 并发场景下，两个管理员同时给同一答案评分，可能产生重复记录。  
  **Mitigation**：概率极低（最终评分由方向管理员操作，同一方向通常 1-2 人）。如后续出现，可通过数据库唯一约束兜底。
