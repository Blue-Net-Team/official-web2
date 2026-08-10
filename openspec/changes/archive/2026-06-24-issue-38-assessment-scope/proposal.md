## Why

`AssessmentDecisionDomainServiceImpl.isSameDirectionAndGrade` 通过 `direction == null && epoch == 0`、`grade == null`、`epoch > 0` 等 null 判断与魔术数字隐式表达「全局最终考核」「方向考核」「年级通配」等领域概念。这些语义没有显式建模，导致新开发者难以理解，且相同规则可能在其他领域服务中重复实现。Issue #38 要求将这些概念显式化到 `AssessmentTime` 领域模型中，使匹配规则可命名、可复用、可测试。

## What Changes

- 在 `AssessmentTime` 领域模型中引入 `AssessmentScope` 值对象，封装 `(direction, epoch)` 的组合语义：
  - `isGlobalFinal()`：全局最终考核（`direction = null` 且 `epoch = 0`）
  - `isDirectional()`：方向考核（`direction != null` 且 `epoch > 0`）
  - `matches(AssessmentScope other)`：判断两个 scope 是否在淘汰匹配语义下兼容
- 引入 `GradePolicy`（或同语义方法）显式表达「年级通配」规则：
  - `isWildcard()`：`grade == null`
  - `matches(GradePolicy other)`：任一不限则匹配，否则精确相等
- 重构 `AssessmentDecisionDomainServiceImpl.isSameDirectionAndGrade` 与 `isPriorEpoch`，用新的显式方法替代 null/魔术数字判断。
- 补充/更新 `AssessmentTime` 与 `AssessmentDecisionDomainServiceImpl` 的单元测试，确保行为等价。
- **不修改**数据库 schema、REST API 契约、前端代码或外部行为。

## Capabilities

### New Capabilities

- `assessment-elimination-matching`：定义考核淘汰决策的匹配规则——方向考核的淘汰如何限制后续方向考核与全局最终考核，以及年级通配如何影响匹配范围。

### Modified Capabilities

- 无。本次变更为纯领域层重构，不改变任何现有能力的外部行为或需求。

## Impact

- **领域层**：`AssessmentTime` 实体新增值对象相关方法；`AssessmentDecisionDomainServiceImpl` 两个私有方法重写。
- **测试层**：`AssessmentTimeTest`、`AssessmentDecisionDomainServiceImplTest` 需补充/调整用例。
- **应用层/API 层**：无影响。
- **基础设施层**：无影响，不新增/修改表、索引或 Mapper。
