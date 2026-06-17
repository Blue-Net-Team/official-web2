## Why

当前考生在通过全局最终考核（录取）后，系统仅发送「录取」邮件，不会自动将用户角色从 `CANDIDATE` 变更为 `MEMBER`。管理员需要手动进入后台批量修改角色，既容易遗漏，也违反「录取即成为组员」的业务直觉。自动升级角色可以闭环考核录取流程，减少人工操作。

## What Changes

- 新增 `AssessmentDecisionPublicationService`：负责单个考生的决策发布，包含邮件发送和角色升级，带独立事务。
- 重构 `AssessmentJudgementAppServiceImpl.publishDecisions()`：由循环发送邮件改为遍历调用新服务，单个考生失败不影响其他考生。
- 扩展 `UserRepository`：新增 `batchUpdateRole(List<Long> userIds, RoleType roleType)`，按角色枚举批量更新。
- 扩展 `UserRepositoryImpl`：通过 `RoleMapper.selectByName` 解析角色枚举对应的数据库 ID，再批量更新用户角色。
- 新增单元测试：覆盖录取升级、方向考核不升级、淘汰不升级、已升级用户幂等、单考生失败继续处理等场景。

## Capabilities

### New Capabilities

- `assessment-admission-role-promotion`：考生通过全局最终考核后自动升级为组员的能力。

### Modified Capabilities

- `assessment-decision-publication`：决策发布由仅发邮件扩展为「邮件 + 角色升级」的组合动作。
- `user-role-management`：用户仓储新增按 `RoleType` 枚举批量更新角色的接口。

## Impact

- **后端 API**：无新增接口，仅修改 `publishDecisions` 内部行为。
- **数据库**：无表结构变更，复用 `tb_user.role_id`、`tb_role`。
- **邮件服务**：复用现有 `MessageDispatcher`，行为不变。
- **前端**：无需改动，现有发布决策入口行为保持一致。
