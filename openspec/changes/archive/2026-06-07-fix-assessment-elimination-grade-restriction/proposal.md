## Why

Issue #27 和 #44 反馈：考生在 grade=null（不限年级）的方向考核中被淘汰后，无法正确限制后续有具体 grade 的考核（包括同方向更高轮次和全局最终考核）。当前 `isSameDirectionAndGrade` 方法中，当 `eliminatedTime.grade == null` 时错误返回 `false`，导致淘汰限制对"不限年级"的考核完全失效。

同时，业务规则要求同方向同轮次只能有一种 grade 形式：要么一个 grade=null（不限年级），要么多个具体 grade 值。当前数据库 `UNIQUE (direction, epoch, grade)` 约束由于 PostgreSQL `NULL != NULL` 语义，无法阻止 `(CV, 1, null)` 和 `(CV, 1, 2024)` 同时存在。

## What Changes

- **修复淘汰限制 grade 匹配逻辑**：`AssessmentDecisionDomainServiceImpl.isSameDirectionAndGrade` 中，当 `eliminatedTime.grade == null` 时，应返回 `true`（不限年级的淘汰限制所有年级），而非当前的 `false`
- **修复全局考核场景**：目标为全局考核（`direction=null, epoch=0`）时，`eliminatedTime.grade == null` 同样应返回 `true`
- **增加同方向同轮次 grade 互斥校验**：`AssessmentTimeAppServiceImpl` 创建和更新考核时，检查同方向同轮次是否已存在 grade 形式冲突（已有 grade=null 则禁止创建 grade!=null，反之亦然）
- **补充单元测试**：增加 `eliminatedTime.grade == null` 覆盖全局考核和同方向后续考核的测试用例

## Capabilities

### New Capabilities
- （无新增 capability）

### Modified Capabilities
- `assessment-time-management`：创建和更新考核时增加同方向同轮次 grade 互斥校验规则

## Impact

- **后端**：`AssessmentDecisionDomainServiceImpl`、`AssessmentDecisionDomainServiceImplTest`、`AssessmentTimeAppServiceImpl`
- **数据库**：无 schema 变更，仅应用层校验增强
- **前端**：无需修改
