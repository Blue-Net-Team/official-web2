## Context

当前后端已按 DDD 四层架构（api / application / domain / infrastructure）分包，但实现层面存在三类问题：

1. **DomainService 职责不清**：`UserDomainService`、`CommentDomainService`、`UserExperienceDomainService` 等只做 CRUD 透传，把本可放在 Entity 中的状态变更规则上移到了领域服务；而 `AssessmentAnswerAppServiceImpl` 又因缺少领域服务，把跨聚合协调逻辑堆积在应用层。
2. **Repository 返回读模型**：`UserRepository.findById` 返回 `UserVO`，`RoleRepository.findByName` 返回 `RoleVO`，`AssessmentDecisionRepository` 返回 `AssessmentDecisionVO`。这些带 `id` 的 `*VO` 本质上是读模型/DTO，导致 Entity 行为被架空，基础设施层承担读模型组装职责。
3. **CQRS 与 VO 命名混乱**：应用层所有参数都放在 `command/` 包下，查询参数也叫 `*Command`；`domain.model.vo` 包中混杂了 Entity 替代品、读模型、应用层结果对象，与 DDD 中 Value Object 的严格定义冲突。

本次重构的目标是在不改变外部 HTTP API 契约的前提下，统一内部实现模式，使四层职责清晰。

## Goals / Non-Goals

**Goals：**
- `Repository` 接口统一返回领域 `Entity`，并提供 `save(Entity)` 语义。
- `Entity` 承载自身状态变更规则；只做 CRUD 透传的 `DomainService` 全部删除。
- 复杂跨聚合协调下沉到真正的 `DomainService`（如 `AssessmentAnswerDomainService`、`AssessmentTeamDomainService`）。
- 安全上下文从 `UserVO` 改为 `SecurityPrincipal`（`User` Entity + `RoleType` + `permissions`）。
- 删除作为 Entity/DTO 替代品的伪 `VO`，把真正的读模型正名为 `*ReadModel` / `*ListItem`。
- 引入 `application/query` 包，查询参数使用 `*Query` 命名。

**Non-Goals：**
- 不修改数据库 schema。
- 不引入新的外部依赖或框架。
- 不修改已有 HTTP API 的 URL、请求参数和响应字段（内部 `AuthResult` 等契约调整除外）。
- 不把所有查询都改造为完整 CQRS（数据库分离、事件溯源等），仅在应用层规范命名和职责。

## Decisions

### Decision 1：安全上下文使用 `SecurityPrincipal` 而非直接 `User` Entity

**选择**：`UserCTX` 持有 `SecurityPrincipal`，其中包含 `User` Entity、`RoleType`、`Set<String> permissions`。

**理由**：
- 满足“安全上下文改为 Entity”的诉求，业务代码可通过 `UserCTX.getCurrentUser()` 拿到 `User` Entity。
- `User` Entity 本身不存储 `roleName` 和 `permissions`（它们是 `roleId` 的派生数据），权限判断需要额外信息。
- 每次请求在 `JwtAuthenticationFilter` 中一次性组装 `SecurityPrincipal`，后续权限校验直接从上下文读取，避免重复查询。

**替代方案**：仅让 `UserCTX` 返回 `User` Entity，权限校验时每次都查 `PermissionCache`。该方案 Entity 更纯净，但会让 `PermissionAspect` 等高频调用点反复访问缓存；当前方案在纯净性和性能之间取得平衡。

### Decision 2：Repository 返回 Entity，复杂查询使用 Domain 层 ReadModel

**选择**：
- 单聚合 CRUD：`Repository` 返回 `Entity`。
- 复杂列表查询：`Repository` 返回 domain 层定义的 `*ReadModel` / `*ListItem`。
- 统计/报表：由 application 层 `QueryRepository` 返回 application 层 ReadModel。

**理由**：
- 严格 DDD 要求 Repository 操作聚合根并返回 Entity。
- 但复杂列表查询（如成就列表关联竞赛信息）如果返回 Entity 再组装，会产生 N+1 问题。
- Domain 层 ReadModel 仍是领域概念内的投影，不违反分层；application 层 QueryRepository 用于纯查询优化，属于 CQRS-lite。

**替代方案**：所有查询都在 application 层组装。该方案最严格，但会增加应用层复杂度和查询次数。

### Decision 3：Entity 中不存放默认 `null` 的派生字段

**选择**：`User` Entity 只保留 `roleId`，不添加默认 `null` 的 `roleName` 或 `permissions` 字段。

**理由**：
- 派生字段会导致 Entity 状态依赖加载方式，同一类出现“完整版”和“精简版”两种合法状态，容易 NPE 和脏数据。
- `roleName` 和 `permissions` 由 `RoleTypeResolver` 和 `PermissionCache` 按需解析，保持 Entity 纯净。

**替代方案**：在 `User` 中增加 `roleName` 和 `permissions` 字段，由特定工厂方法填充。该方案会让 Entity 背负安全上下文职责，不推荐。

### Decision 4：考核模块新增两个 DomainService

**选择**：新增 `AssessmentAnswerDomainService` 和 `AssessmentTeamDomainService`。

**理由**：
- `AssessmentAnswerAppServiceImpl` 当前 500+ 行，协调了 User、Question、Time、Session、Team、Decision、Judgement、File 等多个聚合，应用层过于臃肿。
- 答案提交和组队生命周期是典型的跨聚合协调，适合放在 DomainService。
- 但简单 CRUD（如题目创建、时间创建）仍走 Entity + Repository。

**替代方案**：全部规则继续放在 AppService。该方案会导致应用层持续膨胀，不符合 DDD 分层目标。

### Decision 5：评论模块删除 DomainService，规则下沉 Entity

**选择**：删除 `CommentDomainService`，由 `Comment` Entity 承载“只能评论一次”“只能改自己的评论”规则。

**理由**：
- 这些规则只涉及 `Comment` 自身状态，不需要跨聚合协调。
- `CommentAppServiceImpl` 当前只是透传，删除后反而更简洁。

### Decision 6：查询参数从 `*Command` 改为 `*Query`

**选择**：新增 `application/query` 包，所有只读操作参数命名为 `*Query`；写操作参数保留在 `application/command`。

**理由**：
- 当前 `GetUserListCommand`、`GetTrendsCommand` 等命名与 CQRS 语义冲突。
- 重命名成本低，但能显著提升代码可读性和规范一致性。

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| `UserCTX` 改造影响面广，所有 Controller、AppService、测试都引用 | 按批次改造，先改基础设施层，再改业务模块；每完成一个模块运行测试验证 |
| `PermissionAspect` 从 `roleName` 改为 `roleId` 解析，若 role 数据不一致可能导致权限判断错误 | 新增 `RoleTypeResolver` 并增加测试覆盖；改造后重点回归权限相关接口 |
| `UserRepository.findById` 返回对象切换导致大量调用点编译错误 | 按模块逐个改造，利用编译错误定位所有调用点；优先改造核心模块 |
| 读模型组装从基础设施层移到应用层后，可能出现 N+1 或循环依赖 | 复杂列表查询保留 domain 层 ReadModel，由 Repository 一次 SQL 关联查询返回；必要时使用批量查询 |
| 约 25+ 个测试文件引用 `UserVO`/`@WithUserVO`，改造工作量大 | 提供 `@WithSecurityPrincipal` 测试注解和测试工具类；按模块分批更新测试 |
| 删除 DomainService 后，部分业务规则被分散到 Entity，可能遗漏事务边界 | 应用层保持 `@Transactional` 控制；跨聚合操作保留在 DomainService 或应用服务编排 |
| ArchUnit 规则可能需要同步更新 | 改造过程中同步检查 `ConverterLayerArchTest` 等架构测试，必要时调整规则 |

## Migration Plan

1. **批次一**：基础设施层（SecurityPrincipal、UserCTX、RoleTypeResolver、PermissionAspect、JwtAuthenticationFilter、测试注解）。
2. **批次二**：User 聚合（Repository 返回 Entity、Entity 方法、删除 UserDomainService、重写 UserInfoAppService、删除 UserVO）。
3. **批次三**：简单 DomainService 下沉（Comment、UserExperience、AssessmentJudgement 拆分）。
4. **批次四**：复杂模块重构（AssessmentAnswerDomainService、AssessmentTeamDomainService、AssessmentDecision 清理）。
5. **批次五**：Repository VO 清理（Role、AssessmentDecision、AssessmentJudgement、File、Qrcode、Experience、VerifyCode）。
6. **批次六**：读模型归位与 CQRS 命名（Achievement、Competition、Audit* 改名；新增 query 包；Command 改名 Query）。
7. **批次七**：测试补全、ArchUnit 规则更新、后端开发手册更新。

每完成一个批次，运行编译和全量测试，确保不破坏既有功能。

## Open Questions

- 是否需要在 `RoleTypeResolver` 中缓存 `roleId → RoleType` 映射？若角色表极小，每次查库亦可接受。
- `AssessmentAnswerDomainService` 是否需要拆分为更细粒度（如答案提交、客观题评判、团队答案同步）？待实现时根据代码规模决定。
- `AuthResult` 是否完全不携带用户信息，还是改为携带 `UserInfoResult`？前端调用 `/api/v1/user/info/me` 获取详情更安全，但可能增加一次请求。
