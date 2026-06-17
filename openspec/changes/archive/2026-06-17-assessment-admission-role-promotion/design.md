## Context

当前考核录取流程如下：

1. 报名审批通过时创建 `User`，角色固定为 `CANDIDATE`。
2. 方向管理员在决策工作台为考生设置通过/淘汰决策，保存到 `tb_assessment_decision`。
3. 管理员点击「发布决策」后，`AssessmentJudgementAppServiceImpl.publishDecisions()` 遍历决策，为每个考生发送邮件。
4. 全局最终考核（`direction == null && epoch == 0`）通过时邮件文案为「录取」，但系统不会修改用户角色。

现有基础设施：
- `RoleType` 枚举定义 `CANDIDATE`、`MEMBER`、`DIRECTION_ADMIN`、`SUPER_ADMIN`。
- `UserVO` 已包含 `roleName`，可用于幂等判断。
- `RoleMapper.selectByName(String name)` 已存在，SQL 在 `RoleMapper.xml` 中。
- `UserRepositoryImpl` 已注入 `RoleMapper`，可直接使用。
- `MessageDispatcher.dispatchAsync` 为异步邮件入队，通常不会抛异常。

## Goals / Non-Goals

**Goals:**
- 考生通过全局最终考核后，系统自动将其角色从 `CANDIDATE` 升级为 `MEMBER`。
- 角色升级与邮件发布绑定在「发布决策」操作中。
- 单个考生处理失败不影响其他考生。
- 已升级或高角色用户不会被降级或重复升级。

**Non-Goals:**
- 不修改报名、方向考核、淘汰等其他流程的角色状态。
- 不修改 `AssessmentDecision` 保存逻辑（`decideAssessment` 仍只保存决策）。
- 不修改前端页面或 API 签名。
- 不修改全局最终考核的判定规则（`AssessmentTime.isGlobalFinalAssessment()`）。
- 不处理录取方向变更，沿用考生原有 `direction`。

## Decisions

### 1. 触发时机：发布决策时升级

**选择**：在 `publishDecisions()` 发布决策时升级角色，而非 `decideAssessment()` 保存决策时。

**理由**：「发布决策」是业务上的官宣动作，管理员先确认决策、再统一发布。把邮件和角色升级一起放在发布时刻，语义更统一，也避免决策反复修改导致角色反复变动。

**替代方案**：保存决策时升级。优势是更早完成状态变更，但决策可能被覆盖，角色也需要同步回滚，复杂度更高。

### 2. 事务粒度：每个考生独立事务

**选择**：将单个考生的「邮件 + 角色升级」封装到独立应用服务 `AssessmentDecisionPublicationService.publish(...)` 中，并加 `@Transactional`。

**理由**：
- 一个考生处理失败不会导致整批回滚。
- 邮件异步入队和角色更新在同一事务中，入队失败则角色不升级。

**注意**：同一类内部调用带 `@Transactional` 的方法不会生效，因此必须抽出独立 Spring 托管的 Service。

### 3. 失败处理：记录日志并继续

**选择**：`publishDecisions()` 遍历每个考生，捕获单个考生的所有异常，记录 `ERROR` 日志后继续处理下一个，最终返回成功数。

**理由**：最大化成功率，避免一个无邮箱或数据库瞬错的考生阻塞整批发布。

### 4. 幂等设计：仅升级当前为 CANDIDATE 的用户

**选择**：升级前检查 `UserVO.roleName` 是否等于 `RoleType.CANDIDATE.getName()`，仅当当前角色为考生时才升级。

**理由**：
- 防止重复点击「发布决策」导致重复更新。
- 防止误将 `DIRECTION_ADMIN` 或 `SUPER_ADMIN` 降级为 `MEMBER`。

### 5. 仓储接口：新增 RoleType 枚举入参的重载

**选择**：新增 `UserRepository.batchUpdateRole(List<Long> userIds, RoleType roleType)`。

**理由**：
- 上层应用服务持有 `RoleType` 枚举，不需要知道数据库 role_id。
- `UserRepositoryImpl` 通过已有的 `RoleMapper.selectByName` 解析为 `role_id` 后更新。
- 保留旧的 `batchUpdateRole(List<Long>, Long)` 给 Admin 批量改角色用，避免无关改动。

### 6. 批量更新方式：逐条 updateById

**选择**：`UserRepositoryImpl` 内部循环调用 `userMapper.updateById`。

**理由**：与现有 `batchUpdateDisable`、`batchUpdateRole(List, Long)` 保持一致；MyBatis-Plus 的 `updateById` 会自动忽略 null 字段，只更新 `role_id`。

## Risks / Trade-offs

- **[邮件与角色强耦合]** → 按业务语义这是合理的：发布决策 = 官宣 = 状态变更。未来若需「只发邮件不改角色」，可再拆分。
- **[异步邮件真实失败]** → `dispatchAsync` 仅入队，队列消费失败不会回滚角色。这是现有消息架构的固有问题，非本次引入。
- **[全局最终考核判定]** → 复用现有 `isGlobalFinalAssessment()`，规则稳定为 `direction == null && epoch == 0`。
- **[单用户失败不可见]** → 失败仅记录日志，返回值仍是成功数。若后续需要展示失败列表，可扩展返回对象。
