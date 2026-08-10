## Context

当前 `AssessmentDecisionDomainServiceImpl.isSameDirectionAndGrade` 用以下隐式约定表达领域概念：

- `direction == null && epoch == 0` → 全局最终考核
- `direction != null` → 方向考核（epoch 是否有效由单独方法判断）
- `grade == null` → 年级通配

这些规则散落在领域服务的私有方法里，通过 `null` 判断和魔术数字实现。虽然 `AssessmentTime` 已经提供了 `isGlobalFinalAssessment()`，但匹配逻辑本身仍未显式建模。Issue #38 要求把这套规则内聚到 `AssessmentTime` 领域模型中，使「全局考核」「方向考核」「年级通配」成为可命名、可复用的领域概念。

## Goals / Non-Goals

**Goals：**
- 在 `AssessmentTime` 中显式建模 `AssessmentScope`（方向 + 轮次）和年级匹配语义。
- 消除 `AssessmentDecisionDomainServiceImpl` 中 `isSameDirectionAndGrade` 与 `isPriorEpoch` 的 `null` 判断和魔术数字。
- 保持现有外部行为 100% 不变，所有现有单元测试继续通过。
- 不引入新的依赖或框架。

**Non-Goals：**
- 不修改数据库 schema、Mapper、REST API 或前端。
- 不重构应用层与领域层之间的 VO/DTO 契约。
- 不扩展新的考核类型（如跨方向联合考核），仅对现有语义显式化。
- 不修改 `AssessmentTime` 的创建/重建校验逻辑（如拒绝非法组合）。

## Decisions

### Decision 1：采用 `AssessmentScope` 值对象封装 `(direction, epoch)` 语义

**选择**：在 `AssessmentTime` 中引入 `AssessmentScope` 值对象（Java `record`），提供：
- `isGlobalFinal()`：`direction == null && epoch == 0`
- `isDirectional()`：`direction != null`（不依赖 epoch，epoch 有效性由其他方法负责）
- `isFinalRound()`：`epoch == 0`
- `isValidDirectionalEpoch()`：`epoch != null && epoch > 0`
- `matches(AssessmentScope other)`：用于淘汰匹配时的 scope 兼容性判断

**理由**：
- 把「全局最终考核」「方向考核」从魔术数字判断提升为显式领域语言。
- 值对象不可变，符合 `(direction, epoch)` 作为组合概念的特性。
- 未来若扩展新的考核类型，可在 `AssessmentScope` 内新增类型判断，而不用在多个服务里改 `if` 分支。

**替代方案**：在 `AssessmentTime` 实体上直接加 `isDirectionalAssessment()` 和 `matchesDirection()` 方法。该方案改动更小，但会把「scope 关系逻辑」留在实体上，长期会让 `AssessmentTime` 变胖。值对象方案职责更清晰。

### Decision 2：年级通配内聚到 `AssessmentTime` 的行为方法

**选择**：在 `AssessmentTime` 上提供 `matchesGrade(AssessmentTime other)` 方法，封装「任一 `grade` 为 null 则匹配，否则精确相等」的规则。

**理由**：
- 年级匹配是 `AssessmentTime` 自身属性之间的比较，放在实体上自然。
- 不需要单独引入 `GradePolicy` 值对象，避免过度设计。

**替代方案**：引入 `GradePolicy` 值对象。对于当前仅有一种通配规则的场景，单独值对象收益有限，但未来若出现更复杂的年级策略（如年级范围）可考虑。

### Decision 3：`isPriorEpoch` 的语义也显式化

**选择**：把 `isPriorEpoch(Integer priorEpoch, Integer currentEpoch)` 中的规则拆成显式方法：
- `AssessmentScope.isValidDirectionalEpoch()`：`epoch != null && epoch > 0`
- `AssessmentScope.isFinalRound()`：`epoch != null && epoch == 0`
- `isPriorEpoch` 内部逻辑改写为基于 scope 和 epoch 的显式比较

**理由**：
- 当前 `priorEpoch <= 0` 和 `currentEpoch == 0` 的判断同样是魔术数字语义。
- 与 `AssessmentScope` 结合后，「只有方向考核的淘汰才构成有效 prior」「目标处于最终轮次时任何有效 prior 都算更早」会更清晰。

### Decision 4：保持 Repository 和 Mapper 不变

**选择**：`AssessmentTime` 的重建逻辑 `reconstruct(...)` 参数不变，数据库 DO/PO 不变。

**理由**：
- 本次变更是纯领域模型重构，不动数据层可减少回归风险。
- `AssessmentScope` 在 `AssessmentTime` 内部按需构造，不持久化。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 重构后行为不等价 | 现有 `AssessmentDecisionDomainServiceImplTest` 已覆盖全部边界场景，改完后全量运行该测试类。 |
| 新引入的 `AssessmentScope` 与现有 `isGlobalFinalAssessment()` 语义重复 | 保留 `isGlobalFinalAssessment()` 作为便捷方法，其实现可委托给 `getScope().isGlobalFinal()`，避免破坏既有调用方。 |
| 过度设计 | 本次只引入 `AssessmentScope` 一个值对象，不引入 `GradePolicy` 或 `AssessmentType` 枚举，保持最小有效改动。 |
| 其他领域服务也依赖 `epoch > 0` 等判断 | 本次仅重构 `AssessmentDecisionDomainServiceImpl` 与 `AssessmentTime`，其他位置（如 `QrcodeDomainServiceImpl`）的 `epoch` 判断不在范围内。 |

## Migration Plan

无需迁移。本次变更不修改数据库、不修改 API、不修改前端。部署时直接替换后端代码即可。

## Open Questions

1. `AssessmentScope` 是否需要在 `AssessmentTime.create(...)` 中校验非法组合（如 `direction=null, epoch!=0`）？本次变更保持创建校验不变，留待后续讨论。
