## Why

当前后端四层架构（api / application / domain / infrastructure）已经建立，但实现上存在模式不统一的问题：大量聚合的 CRUD 操作通过 `DomainService` 透传，Repository 返回带身份的 `*VO` 对象，安全上下文依赖 `UserVO` 这种读模型，应用层查询参数也叫 `*Command`。这导致领域层贫血、Entity 行为被架空、分层边界模糊，维护成本持续上升。本次重构旨在统一为 Entity 驱动、明确 CQRS 语义、去除伪 VO，使 DDD 分层的“形”与“神”一致。

## What Changes

- **安全上下文重构**：`UserCTX` 从 `ThreadLocal<UserVO>` 改为 `ThreadLocal<SecurityPrincipal>`，内部持有 `User` Entity + `RoleType` + `permissions`；`PermissionAspect` 基于 `roleId` 解析权限，不再依赖 `UserVO` 的 `roleName`/`permissions`。
- **Repository 返回 Entity**：`UserRepository`、`RoleRepository`、`AssessmentDecisionRepository`、`AssessmentJudgementRepository` 等不再返回 `*VO`，统一返回领域 Entity，并提供 `save(Entity)` 语义。
- **Entity 承载业务规则**：`User`、`Comment`、`AssessmentAnswer`、`AssessmentTeam`、`AssessmentTime` 等 Entity 增加状态变更方法，应用层直接调用 `Entity + Repository`。
- **DomainService 精简**：删除只做 CRUD 透传的 `UserDomainService`、`CommentDomainService`、`UserExperienceDomainService`；拆分 `AssessmentJudgementDomainService`；新增/保留真正的跨聚合协调服务（`AssessmentAnswerDomainService`、`AssessmentTeamDomainService`、`AssessmentDecisionDomainService`、`FileDomainService`、`AuthDomainService`）。
- **伪 VO 清理与正名**：删除作为 Entity 替代品的 `UserVO`、`FileVO`、`CommentVO`、`ExperienceVO`、`RoleVO`、`AssessmentDecisionVO`、`AssessmentJudgementVO`、`VerifyCodeVO`；把真正的读模型改名为 `*ReadModel` / `*ListItem`，并明确放在 domain 或 application 层。
- **CQRS 命名规范**：新增 `application/query` 包，把查询参数从 `*Command` 改名为 `*Query`；写参数保留在 `application/command`。
- **测试与架构守护更新**：替换 `@WithUserVO` 为 `@WithSecurityPrincipal`；更新 ArchUnit 规则；更新后端开发手册。

## Capabilities

### New Capabilities

- `security-principal-context`：定义安全上下文对象 `SecurityPrincipal`，包含 `User` Entity、`RoleType`、`permissions`，替代 `UserVO` 作为请求级上下文。
- `user-aggregate-entity-driven`：`User` Entity 承载头像、资料、邮箱、密码等状态变更规则，`UserRepository` 返回 `User`，`UserInfoAppService` 直接调用 `UserRepository`。
- `comment-aggregate-entity-driven`：`Comment` Entity 承载“只能评论一次”“只能改/删自己的评论”规则，删除 `CommentDomainService`。
- `assessment-domain-service-refactor`：考核相关聚合的 DomainService 治理，新增 `AssessmentAnswerDomainService`、`AssessmentTeamDomainService`，拆分 `AssessmentJudgementDomainService`，保留 `AssessmentDecisionDomainService` 核心规则。
- `repository-readmodel-separation`：Repository 返回 Entity；复杂查询使用 domain 层 ReadModel 或 application 层 QueryRepository，避免 Repository 返回 application 层对象。
- `query-command-separation`：应用层参数按 CQRS 语义分为 `command/` 和 `query/`，查询参数不再使用 `*Command` 命名。

### Modified Capabilities

<!-- 本次重构主要改变内部实现方式，不修改已有功能的外部行为。AuthResult 不再携带 UserVO 属于内部契约调整，对外 API 通过 ResponseConverter 保持兼容。 -->
- 无

## Impact

- **代码范围**：`src/backend/src/main/java/com/bluenet/web` 下的 api、application、domain、infrastructure 四层均受影响，核心涉及 User、Comment、Assessment*、File、Auth、Role/Permission 等聚合。
- **API 影响**：外部 HTTP API 契约尽量保持不变；内部 `AuthResult` 等应用层结果对象移除 `UserVO`。
- **测试影响**：约 25+ 个测试文件引用 `UserVO` / `@WithUserVO`，需要同步改造。
- **数据库影响**：无 schema 变更。
- **依赖影响**：无新增外部依赖。
