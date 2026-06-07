## Context

当前考核淘汰限制由 `AssessmentDecisionDomainServiceImpl.isEliminatedFromPriorEpoch` 实现，核心逻辑在 `isSameDirectionAndGrade` 方法。Issue #27 和 #44 报告了两种失效场景：

1. 考生在 `grade=null`（不限年级）的方向考核中被淘汰后，无法限制同方向更高轮次中 `grade!=null` 的考核
2. 上述淘汰也无法限制 `grade!=null` 的全局最终考核（`direction=null, epoch=0`）

根因：`isSameDirectionAndGrade` 中当 `eliminatedTime.getGrade() == null` 时，对目标考核 `grade!=null` 错误返回 `false`。

另外，业务规则要求同方向同轮次只能有一种 grade 形式，当前数据库唯一约束无法阻止混存。

## Goals / Non-Goals

**Goals:**
- 修复 `isSameDirectionAndGrade` 中 `eliminatedTime.grade == null` 时的匹配逻辑
- 确保同方向后续考核和全局考核的淘汰限制正确生效
- 在应用层增加同方向同轮次 grade 互斥校验
- 补充覆盖边界场景的单元测试

**Non-Goals:**
- 不修改数据库 schema（唯一约束逻辑不变）
- 不修改前端代码
- 不引入新的淘汰机制（仅修复现有匹配逻辑）

## Decisions

**Decision 1: grade 匹配规则统一为"任一不限则全限"**
- `eliminatedTime.grade == null`：被淘汰的考核不限年级 → 限制该方向的所有年级
- `targetTime.grade == null`：目标考核不限年级 → 限制所有被淘汰的考生
- 两者均有具体值：必须相等
- Rationale：业务上"不限年级"意味着覆盖全部，淘汰应全局生效

**Decision 2: 全局考核场景与普通场景共用同一套 grade 匹配逻辑**
- 全局考核（`direction=null, epoch=0`）的特殊处理仅保留方向/轮次判断，grade 匹配复用通用逻辑
- Rationale：减少重复代码，避免未来再次遗漏边界场景

**Decision 3: grade 互斥校验放在应用层而非数据库层**
- 当前 `UNIQUE (direction, epoch, grade)` 无法通过数据库约束实现互斥（PostgreSQL `NULL != NULL`）
- 应用层在创建/更新时查询同方向同轮次已有记录，检查 grade 形式冲突
- Rationale：避免复杂的数据库约束改造，应用层校验足够覆盖业务场景

## Risks / Trade-offs

**[Risk]** 应用层 grade 互斥校验存在竞态条件（两个请求同时创建冲突的考核时间）
→ **Mitigation**：业务上由管理员串行操作，竞态概率极低；数据库唯一约束仍兜底防止完全重复记录。

**[Risk]** 修改 `isSameDirectionAndGrade` 可能影响已通过测试的旧场景
→ **Mitigation**：所有现有测试用例需重新验证，新增边界测试确保行为正确。
