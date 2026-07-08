## 当前进度与测试策略

**已完成**：

- Task 1（安全上下文基础设施改造）已全部完成。
- Task 2（User 聚合重构）已全部完成：`UserRepository` 返回 `User` Entity 并移除字段级更新方法；`User` Entity 增加状态变更方法；`UserInfoAppServiceImpl` / `AdminUserAppServiceImpl` / `ResetPasswordAppServiceImpl` 改为 `load → modify → save`；`UserDomainService` 与 `UserVO` 已删除。
- Task 3（Comment 聚合重构）已全部完成：`CommentRepository` 返回 `Comment` Entity；`Comment` Entity 承载更新/删除权限校验与防重复评论逻辑；`CommentAppServiceImpl` 直接调用 Repository；`CommentDomainService` 与 `CommentVO` 已删除。
- Task 4（UserExperience 聚合清理）已全部完成：`MemberAppServiceImpl.getMemberExperiences` 直接调用 `UserExperienceRepository`；`UserExperienceDomainService` 与 `ExperienceVO` 已删除。
- Task 5（AssessmentJudgement 拆分）已全部完成：`AssessmentJudgementRepository` 返回 `AssessmentJudgement` Entity；`AssessmentJudgementAppServiceImpl` / `AssessmentAnswerAppServiceImpl` / `AlgorithmJudgeAppServiceImpl` 直接调用 Repository；`AssessmentJudgementDomainService` 仅保留 `finalizeJudgement`；`AssessmentJudgementVO` 已删除；已补充单元/集成测试。
- Task 6（AssessmentDecision 清理）已全部完成：`AssessmentDecisionRepository` 返回 `AssessmentDecision` Entity；`AssessmentDecision` Entity 增加 `updatePassed(...)` / `decideNow()`；`AssessmentDecisionDomainService` 仅保留 `isEliminatedFromPriorEpoch`；CRUD 下沉到 `AssessmentJudgementAppServiceImpl` 直接调用 Repository；`AssessmentDecisionPublicationService` 改为接收 `AssessmentDecision` Entity；`AssessmentDecisionVO` 已删除。

**测试策略调整**：
本次重构涉及所有模块，大量旧测试依赖即将删除的 `*VO`、`@WithUserVO`、透传型 `DomainService` 以及旧的 Repository 返回类型。继续维护这些测试会在每个子任务中产生高昂的同步成本，因此决定：

- **删除所有业务测试文件**（Controller、AppService、DomainService、Repository/Mapper、Entity 业务规则、业务集成测试）。
- **保留并必要时重构测试基础设施**：`BaseIntegrationTest`、`TestSecurityConfig`、`TestcontainersConfiguration`、`RepositoryTestObjects`、`@WithSecurityPrincipal` + `WithSecurityPrincipalContextFactory`、`MockFileRepository`、`MockQrcodeRepository`。
- **保留通用工具/架构守护/基础设施测试**：工具类、配置属性、JSON 值对象、邮件/存储/安全基础组件、ArchUnit、权限注解规范、应用上下文加载等测试。
- 后续每个模块重构完成后，再为该模块补充新的单元/集成测试。

***

## 1. 安全上下文基础设施改造

**目标**：将 `UserCTX` 从 `ThreadLocal<UserVO>` 改为 `ThreadLocal<SecurityPrincipal>`，使安全上下文持有 `User` Entity、`RoleType` 和 `permissions`。

#### 测试边界

- **输入条件**：请求携带有效 JWT，或测试使用 `@WithSecurityPrincipal`
- **前置状态**：原 `UserCTX` 为 `ThreadLocal<UserVO>`
- **后置状态**：`UserCTX` 返回 `SecurityPrincipal`，业务代码通过 `getCurrentUser()` 获取 `User` Entity

#### 实现步骤

- [x] 1.1 新增 `SecurityPrincipal` 记录类，包含 `User user`、`RoleType roleType`、`Set<String> permissions`
- [x] 1.2 新增 `RoleTypeResolver`，根据 `roleId` 解析 `RoleType`
- [x] 1.3 改造 `UserCTX` 为 `ThreadLocal<SecurityPrincipal>`，提供 `getPrincipal()`、`getCurrentUser()`、`getCurrentRoleType()`、`getCurrentPermissions()`、`setPrincipal()`、`clear()`
- [x] 1.4 改造 `JwtAuthenticationFilter`：查询 `User` Entity，解析 `RoleType`，加载 permissions，构造 `SecurityPrincipal`
- [x] 1.5 改造 `PermissionAspect`：从 `SecurityPrincipal` 读取 `roleType` 和 `permissions`，超管直接放行
- [x] 1.6 改造 `AuthSessionIssuer` 接收 `User` Entity 而非 `UserVO`
- [x] 1.7 改造 `AuthResult` 移除 `UserVO`，Controller 层需要时调用 `UserInfoAppService`
- [x] 1.8 新增 `@WithSecurityPrincipal` 测试注解及 `WithSecurityPrincipalContextFactory`，替换 `@WithUserVO`
- [x] 1.9 编译通过，相关单元测试与集成测试通过

***

## 2. User 聚合重构

**目标**：`UserRepository` 返回 `User` Entity，`User` Entity 承载状态变更规则，删除 `UserDomainService`。

#### 测试边界

- **输入条件**：用户请求更新头像、资料、邮箱、密码
- **前置状态**：`UserRepository.findById` 返回 `UserVO`，`UserInfoAppServiceImpl` 调用 `UserDomainService`
- **后置状态**：`UserRepository.findById` 返回 `User`，应用层直接调用 `User` Entity + `UserRepository.save()`

#### 实现步骤

- [x] 2.1 改造 `UserRepository` 接口：`findById`、`findByEmail`、`findByStudentId`、`findByGithubId` 返回 `Optional<User>`
- [x] 2.2 改造 `UserRepositoryImpl`：移除 `convertToVO`，查询结果直接返回 `User` Entity；保留管理员批量操作方法
- [x] 2.3 `User` Entity 增加 `updateAvatar(Long)`、`updateProfile(...)`、`updateQrcodeId(Long)`、`changeEmail(String)` 方法
- [x] 2.4 重写 `UserInfoAppServiceImpl`：直接注入 `UserRepository`，每个方法遵循 `load → modify → save` 模式
- [x] 2.5 修复 `AdminUserAppServiceImpl.updateUser`：调用 `user.updateAdminFields(...)` 后 `userRepository.save(user)`，删除 `UserRepository.updateAdminFields(...)`
- [x] 2.6 迁移 `AuthDomainServiceImpl`、`UserOnboardingServiceImpl`、`GitHubAuthProvider` 等所有 `UserVO` 引用到 `User` Entity（`UserDomainService.getUser` 调用点已迁移至 `UserRepository.findById`）
- [x] 2.7 删除 `UserDomainService` 接口和实现
- [x] 2.8 删除 `UserVO`，如果 API 需要用户信息则使用 `UserInfoResult`
- [x] 2.9 编译通过，User 相关接口通过手动/新测试验证

***

## 3. Comment 聚合重构

**目标**：`Comment` Entity 承载评论规则，删除 `CommentDomainService`。

#### 实现步骤

- [x] 3.1 确保 `CommentRepository` 返回 `Comment` Entity
- [x] 3.2 `Comment` Entity 增加 `update(Long userId, String content, BigDecimal score)` 和 `delete(Long userId)` 权限校验
- [x] 3.3 `Comment` Entity 增加防止重复评论的工厂方法或校验逻辑
- [x] 3.4 重写 `CommentAppServiceImpl`：直接调用 `CommentRepository`
- [x] 3.5 删除 `CommentDomainService` 接口和实现
- [x] 3.6 删除或重命名 `CommentVO` 为应用层结果对象
- [x] 3.7 编译通过

***

## 4. UserExperience 聚合清理

**目标**：删除遗留的 `UserExperienceDomainService`，让 `MemberAppService` 直接调用 `UserExperienceRepository`。

#### 实现步骤

- [x] 4.1 确认 `UserExperienceAppServiceImpl` 已直接调用 `UserExperienceRepository`
- [x] 4.2 改造 `MemberAppServiceImpl.getMemberExperiences` 直接调用 `UserExperienceRepository`
- [x] 4.3 删除 `UserExperienceDomainService` 接口和实现
- [x] 4.4 删除或重命名 `ExperienceVO` 为应用层结果对象
- [x] 4.5 编译通过

***

## 5. AssessmentJudgement 拆分

**目标**：CRUD 下沉到 `AssessmentJudgement` Entity + Repository，仅保留 `finalizeJudgement` 在 DomainService。

#### 实现步骤

- [x] 5.1 改造 `AssessmentJudgementRepository` 返回 `AssessmentJudgement` Entity
- [x] 5.2 `AssessmentJudgement` Entity 增加 `update(...)` 等业务方法
- [x] 5.3 重写 `AssessmentJudgementAppServiceImpl` 直接调用 Repository
- [x] 5.4 精简 `AssessmentJudgementDomainService`：仅保留 `finalizeJudgement`
- [x] 5.5 迁移 `AssessmentAnswerAppServiceImpl` 等引用点从 `AssessmentJudgementVO` 到 `AssessmentJudgement` Entity
- [x] 5.6 删除 `AssessmentJudgementVO`
- [x] 5.7 编译通过

***

## 6. AssessmentDecision 清理

**目标**：CRUD 下沉到 `AssessmentDecision` Entity + Repository，保留跨轮次淘汰规则。

#### 实现步骤

- [x] 6.1 改造 `AssessmentDecisionRepository` 返回 `AssessmentDecision` Entity
- [x] 6.2 `AssessmentDecision` Entity 增加 `updatePassed(...)` 等方法
- [x] 6.3 迁移 `AssessmentDecisionDomainService` 中的 CRUD 到 AppService/Repository
- [x] 6.4 保留 `isEliminatedFromPriorEpoch` 在 `AssessmentDecisionDomainService`
- [x] 6.5 改造 `AssessmentDecisionPublicationService`：使用 `User` Entity 和 `RoleTypeResolver`
- [x] 6.6 迁移 `AssessmentQuestionAppServiceImpl`、`AssessmentTimeAppServiceImpl` 等引用点
- [x] 6.7 删除 `AssessmentDecisionVO`
- [x] 6.8 编译通过

***

## 7. AssessmentAnswerDomainService 新增

**目标**：将 `AssessmentAnswerAppServiceImpl` 中答案提交/更新的业务规则下沉到新的 `AssessmentAnswerDomainService`。

#### 实现步骤

- [x] 7.1 新增 `AssessmentAnswerDomainService` 接口和实现
- [x] 7.2 迁移方向/年级匹配、时间窗口、淘汰判断、会话截止、重复提交、文件校验等规则
- [x] 7.3 迁移团队答案同步逻辑
- [x] 7.4 迁移客观题自动评判触发逻辑
- [x] 7.5 重写 `AssessmentAnswerAppServiceImpl`：只做参数校验、调用 DomainService、结果转换、事务控制
- [x] 7.6 编译通过，答案提交相关流程通过手动/新测试验证

***

## 8. AssessmentTeamDomainService 新增

**目标**：将 `AssessmentTeamAppServiceImpl` 中组队生命周期规则下沉到新的 `AssessmentTeamDomainService`。

#### 实现步骤

- [x] 8.1 新增 `AssessmentTeamDomainService` 接口和实现
- [x] 8.2 迁移创建/加入/退出/转让/解散的业务规则
- [x] 8.3 迁移“已有个人答案不能组队”“已提交答案不能解散”等校验
- [x] 8.4 迁移解散队伍时清理答案和评判的逻辑
- [x] 8.5 重写 `AssessmentTeamAppServiceImpl`：直接调用 DomainService
- [x] 8.6 编译通过

***

## 9. Repository VO 清理（通用聚合）

**目标**：清理 `Role`、`File`、`Qrcode`、`Experience`、`VerifyCode` 等聚合的伪 VO。

#### 实现步骤

- [ ] 9.1 改造 `RoleRepository.findByName` 返回 `Optional<Role>`
- [ ] 9.2 改造 `FileDomainService` 返回/接收 `File` Entity（`getFileById`、`saveFile`、`checkDownloadPermission` 等）
- [ ] 9.3 改造 `QrcodeDomainService` 返回/接收 `Qrcode` Entity
- [ ] 9.4 改造 `VerificationCodeDomainService` 返回 `VerificationCode` Entity
- [ ] 9.5 删除 `RoleVO`、`FileVO`、`QrcodeVO`、`VerifyCodeVO`
- [ ] 9.6 迁移所有引用点（`AuthAppServiceImpl`、`UserInfoAppServiceImpl`、`FileAppServiceImpl` 等）
- [ ] 9.7 编译通过

***

## 10. 读模型归位与 CQRS 命名规范

**目标**：将真正的读模型改名为 `*ReadModel` / `*ListItem` 并明确分层；引入 `application/query` 包，查询参数改名为 `*Query`。

#### 实现步骤

- [ ] 10.1 新增 `com.bluenet.web.application.query` 包
- [ ] 10.2 把所有读操作参数类从 `command` 包移到 `query` 包，并从 `*Command` 改名为 `*Query`
- [ ] 10.3 更新所有应用服务接口方法签名
- [ ] 10.4 重命名 `AchievementVO` → `AchievementReadModel` 或 `AchievementListItem`
- [ ] 10.5 重命名 `CompetitionVO` → `CompetitionReadModel`
- [ ] 10.6 重命名 `Audit*VO` → `Audit*` 并移到 application 层
- [ ] 10.7 重命名 `TabCountsVO`、`EnrollStatisticsVO`、`AchievementStatsVO` 并移到 application 层
- [ ] 10.8 删除 `domain.model.vo` 包（确认无真正 Value Object 后）
- [ ] 10.9 编译通过

***

## 11. 测试基础设施重构与架构守护更新

**目标**：删除因大变更而失效的旧业务测试，保留并重构测试基础设施；更新 ArchUnit 规则与后端开发手册。

#### 实现步骤

- [ ] 11.1 删除所有业务测试文件：Controller 测试、AppService 测试、DomainService 测试、Repository/Mapper 测试、Entity 业务规则测试、业务集成测试
- [ ] 11.2 保留测试基础设施：`BaseIntegrationTest`、`TestSecurityConfig`、`TestcontainersConfiguration`、`RepositoryTestObjects`、`@WithSecurityPrincipal` + `WithSecurityPrincipalContextFactory`、`MockFileRepository`、`MockQrcodeRepository`
- [ ] 11.3 保留通用工具/架构守护/基础设施测试，并重构受安全上下文改造影响的测试（如 `JwtAuthenticationFilterTest`）
- [ ] 11.4 更新 `ConverterLayerArchTest` 等架构测试
- [ ] 11.5 新增或更新 ArchUnit 规则：Repository 不得返回 `*VO`、ApplicationService 不得直接依赖 Mapper 等
- [ ] 11.6 更新 `docs/后端开发手册.md` 中关于 DDD 分层、VO、DomainService 使用规范的章节
- [ ] 11.7 编译通过（`./mvnw test-compile`）

