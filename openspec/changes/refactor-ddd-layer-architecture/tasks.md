## 1. 安全上下文基础设施改造

**目标**：将 `UserCTX` 从 `ThreadLocal<UserVO>` 改为 `ThreadLocal<SecurityPrincipal>`，使安全上下文持有 `User` Entity、`RoleType` 和 `permissions`。

#### 测试边界
- **输入条件**：请求携带有效 JWT，或测试使用 `@WithSecurityPrincipal`
- **前置状态**：原 `UserCTX` 为 `ThreadLocal<UserVO>`
- **后置状态**：`UserCTX` 返回 `SecurityPrincipal`，业务代码通过 `getCurrentUser()` 获取 `User` Entity

#### 实现步骤
- [ ] 1.1 新增 `SecurityPrincipal` 记录类，包含 `User user`、`RoleType roleType`、`Set<String> permissions`
- [ ] 1.2 新增 `RoleTypeResolver`，根据 `roleId` 解析 `RoleType`
- [ ] 1.3 改造 `UserCTX` 为 `ThreadLocal<SecurityPrincipal>`，提供 `getPrincipal()`、`getCurrentUser()`、`getCurrentRoleType()`、`getCurrentPermissions()`、`setPrincipal()`、`clear()`
- [ ] 1.4 改造 `JwtAuthenticationFilter`：查询 `User` Entity，解析 `RoleType`，加载 permissions，构造 `SecurityPrincipal`
- [ ] 1.5 改造 `PermissionAspect`：从 `SecurityPrincipal` 读取 `roleType` 和 `permissions`，超管直接放行
- [ ] 1.6 改造 `AuthSessionIssuer` 接收 `User` Entity 而非 `UserVO`
- [ ] 1.7 改造 `AuthResult` 移除 `UserVO`，Controller 层需要时调用 `UserInfoAppService`
- [ ] 1.8 新增 `@WithSecurityPrincipal` 测试注解及 `WithSecurityPrincipalContextFactory`，替换 `@WithUserVO`
- [ ] 1.9 编译通过，相关单元测试与集成测试通过

---

## 2. User 聚合重构

**目标**：`UserRepository` 返回 `User` Entity，`User` Entity 承载状态变更规则，删除 `UserDomainService`。

#### 测试边界
- **输入条件**：用户请求更新头像、资料、邮箱、密码
- **前置状态**：`UserRepository.findById` 返回 `UserVO`，`UserInfoAppServiceImpl` 调用 `UserDomainService`
- **后置状态**：`UserRepository.findById` 返回 `User`，应用层直接调用 `User` Entity + `UserRepository.save()`

#### 实现步骤
- [ ] 2.1 改造 `UserRepository` 接口：`findById`、`findByEmail`、`findByStudentId`、`findByGithubId` 返回 `Optional<User>`
- [ ] 2.2 改造 `UserRepositoryImpl`：移除 `convertToVO`，查询结果直接返回 `User` Entity；保留管理员批量操作方法
- [ ] 2.3 `User` Entity 增加 `updateAvatar(Long)`、`updateProfile(...)`、`updateQrcodeId(Long)`、`changeEmail(String)` 方法
- [ ] 2.4 重写 `UserInfoAppServiceImpl`：直接注入 `UserRepository`，每个方法遵循 `load → modify → save` 模式
- [ ] 2.5 修复 `AdminUserAppServiceImpl.updateUser`：调用 `user.updateAdminFields(...)` 后 `userRepository.save(user)`，删除 `UserRepository.updateAdminFields(...)`
- [ ] 2.6 迁移 `AuthDomainServiceImpl`、`UserOnboardingServiceImpl`、`GitHubAuthProvider` 等所有 `UserVO` 引用到 `User` Entity
- [ ] 2.7 删除 `UserDomainService` 接口和实现
- [ ] 2.8 删除 `UserVO`，如果 API 需要用户信息则使用 `UserInfoResult`
- [ ] 2.9 更新 `UserInfoAppServiceImplTest` 等单元测试
- [ ] 2.10 编译通过，User 相关接口测试通过

---

## 3. Comment 聚合重构

**目标**：`Comment` Entity 承载评论规则，删除 `CommentDomainService`。

#### 测试边界
- **输入条件**：用户添加、更新、删除评论
- **前置状态**：`CommentAppServiceImpl` 透传 `CommentDomainService`
- **后置状态**：`CommentAppServiceImpl` 直接调用 `CommentRepository`

#### 实现步骤
- [ ] 3.1 确保 `CommentRepository` 返回 `Comment` Entity
- [ ] 3.2 `Comment` Entity 增加 `update(Long userId, String content, BigDecimal score)` 和 `delete(Long userId)` 权限校验
- [ ] 3.3 `Comment` Entity 增加防止重复评论的工厂方法或校验逻辑
- [ ] 3.4 重写 `CommentAppServiceImpl`：直接调用 `CommentRepository`
- [ ] 3.5 删除 `CommentDomainService` 接口和实现
- [ ] 3.6 删除或重命名 `CommentVO` 为应用层结果对象
- [ ] 3.7 更新 `CommentAppServiceImplTest` 等测试
- [ ] 3.8 编译通过，Comment 接口测试通过

---

## 4. UserExperience 聚合清理

**目标**：删除遗留的 `UserExperienceDomainService`，让 `MemberAppService` 直接调用 `UserExperienceRepository`。

#### 测试边界
- **输入条件**：查询成员经历列表
- **前置状态**：`MemberAppServiceImpl` 调用 `UserExperienceDomainService`
- **后置状态**：`MemberAppServiceImpl` 直接调用 `UserExperienceRepository`

#### 实现步骤
- [ ] 4.1 确认 `UserExperienceAppServiceImpl` 已直接调用 `UserExperienceRepository`
- [ ] 4.2 改造 `MemberAppServiceImpl.getMemberExperiences` 直接调用 `UserExperienceRepository`
- [ ] 4.3 删除 `UserExperienceDomainService` 接口和实现
- [ ] 4.4 删除或重命名 `ExperienceVO` 为应用层结果对象
- [ ] 4.5 更新相关测试
- [ ] 4.6 编译通过

---

## 5. AssessmentJudgement 拆分

**目标**：CRUD 下沉到 `AssessmentJudgement` Entity + Repository，仅保留 `finalizeJudgement` 在 DomainService。

#### 测试边界
- **输入条件**：创建、更新、查询、最终评定评判记录
- **前置状态**：所有评判操作通过 `AssessmentJudgementDomainService`，返回 `AssessmentJudgementVO`
- **后置状态**：普通 CRUD 走 Entity+Repository，`finalizeJudgement` 保留在 DomainService

#### 实现步骤
- [ ] 5.1 改造 `AssessmentJudgementRepository` 返回 `AssessmentJudgement` Entity
- [ ] 5.2 `AssessmentJudgement` Entity 增加 `update(...)` 等业务方法
- [ ] 5.3 重写 `AssessmentJudgementAppServiceImpl` 直接调用 Repository
- [ ] 5.4 精简 `AssessmentJudgementDomainService`：仅保留 `finalizeJudgement`
- [ ] 5.5 迁移 `AssessmentAnswerAppServiceImpl` 等引用点从 `AssessmentJudgementVO` 到 `AssessmentJudgement` Entity
- [ ] 5.6 删除 `AssessmentJudgementVO`
- [ ] 5.7 更新相关测试
- [ ] 5.8 编译通过

---

## 6. AssessmentDecision 清理

**目标**：CRUD 下沉到 `AssessmentDecision` Entity + Repository，保留跨轮次淘汰规则。

#### 测试边界
- **输入条件**：保存/查询决策、判断用户是否被前期轮次淘汰、发布决策
- **前置状态**：`AssessmentDecisionRepository` 返回 `AssessmentDecisionVO`
- **后置状态**：Repository 返回 `AssessmentDecision` Entity，发布服务使用 `User` Entity

#### 实现步骤
- [ ] 6.1 改造 `AssessmentDecisionRepository` 返回 `AssessmentDecision` Entity
- [ ] 6.2 `AssessmentDecision` Entity 增加 `updatePassed(...)` 等方法
- [ ] 6.3 迁移 `AssessmentDecisionDomainService` 中的 CRUD 到 AppService/Repository
- [ ] 6.4 保留 `isEliminatedFromPriorEpoch` 在 `AssessmentDecisionDomainService`
- [ ] 6.5 改造 `AssessmentDecisionPublicationService`：使用 `User` Entity 和 `RoleTypeResolver`
- [ ] 6.6 迁移 `AssessmentQuestionAppServiceImpl`、`AssessmentTimeAppServiceImpl` 等引用点
- [ ] 6.7 删除 `AssessmentDecisionVO`
- [ ] 6.8 更新相关测试
- [ ] 6.9 编译通过

---

## 7. AssessmentAnswerDomainService 新增

**目标**：将 `AssessmentAnswerAppServiceImpl` 中答案提交/更新的业务规则下沉到新的 `AssessmentAnswerDomainService`。

#### 测试边界
- **输入条件**：考生提交或更新答案
- **前置状态**：所有规则在 `AssessmentAnswerAppServiceImpl` 中
- **后置状态**：`AssessmentAnswerDomainService` 协调 Question/Time/Session/Team/Decision/Judgement/File

#### 实现步骤
- [ ] 7.1 新增 `AssessmentAnswerDomainService` 接口和实现
- [ ] 7.2 迁移方向/年级匹配、时间窗口、淘汰判断、会话截止、重复提交、文件校验等规则
- [ ] 7.3 迁移团队答案同步逻辑
- [ ] 7.4 迁移客观题自动评判触发逻辑
- [ ] 7.5 重写 `AssessmentAnswerAppServiceImpl`：只做参数校验、调用 DomainService、结果转换、事务控制
- [ ] 7.6 更新 `AssessmentAnswerAppServiceImplTest`
- [ ] 7.7 编译通过，答案提交相关流程测试通过

---

## 8. AssessmentTeamDomainService 新增

**目标**：将 `AssessmentTeamAppServiceImpl` 中组队生命周期规则下沉到新的 `AssessmentTeamDomainService`。

#### 测试边界
- **输入条件**：创建队伍、加入队伍、退出队伍、转让队长、解散队伍
- **前置状态**：所有规则在 `AssessmentTeamAppServiceImpl` 中
- **后置状态**：`AssessmentTeamDomainService` 协调 Team/Time/Answer/Judgement

#### 实现步骤
- [ ] 8.1 新增 `AssessmentTeamDomainService` 接口和实现
- [ ] 8.2 迁移创建/加入/退出/转让/解散的业务规则
- [ ] 8.3 迁移“已有个人答案不能组队”“已提交答案不能解散”等校验
- [ ] 8.4 迁移解散队伍时清理答案和评判的逻辑
- [ ] 8.5 重写 `AssessmentTeamAppServiceImpl`：直接调用 DomainService
- [ ] 8.6 更新 `AssessmentTeamAppServiceImplTest`
- [ ] 8.7 编译通过，组队相关流程测试通过

---

## 9. Repository VO 清理（通用聚合）

**目标**：清理 `Role`、`File`、`Qrcode`、`Experience`、`VerifyCode` 等聚合的伪 VO。

#### 测试边界
- **输入条件**：查询/保存上述聚合的数据
- **前置状态**：部分 Repository 返回 VO 或 DomainService 使用 VO
- **后置状态**：Repository 返回 Entity，DomainService/AppService 使用 Entity

#### 实现步骤
- [ ] 9.1 改造 `RoleRepository.findByName` 返回 `Optional<Role>`
- [ ] 9.2 改造 `FileDomainService` 返回/接收 `File` Entity（`getFileById`、`saveFile`、`checkDownloadPermission` 等）
- [ ] 9.3 改造 `QrcodeDomainService` 返回/接收 `Qrcode` Entity
- [ ] 9.4 改造 `VerificationCodeDomainService` 返回 `VerificationCode` Entity
- [ ] 9.5 删除 `RoleVO`、`FileVO`、`QrcodeVO`、`VerifyCodeVO`
- [ ] 9.6 迁移所有引用点（`AuthAppServiceImpl`、`UserInfoAppServiceImpl`、`FileAppServiceImpl` 等）
- [ ] 9.7 更新相关测试
- [ ] 9.8 编译通过

---

## 10. 读模型归位与 CQRS 命名规范

**目标**：将真正的读模型改名为 `*ReadModel` / `*ListItem` 并明确分层；引入 `application/query` 包，查询参数改名为 `*Query`。

#### 测试边界
- **输入条件**：列表查询、统计查询
- **前置状态**：读模型叫 `*VO` 且放在 `domain.model.vo`；查询参数叫 `*Command`
- **后置状态**：读模型有明确命名和位置；查询参数在 `application.query` 包中且叫 `*Query`

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

---

## 11. 测试与架构守护更新

**目标**：更新单元测试、集成测试、ArchUnit 规则和后端开发手册，确保重构后的架构规范可维护。

#### 测试边界
- **输入条件**：运行全量测试
- **前置状态**：测试依赖 `UserVO`/`@WithUserVO`，ArchUnit 规则可能不覆盖新规范
- **后置状态**：全量测试通过，ArchUnit 规则符合新的分层约定

#### 实现步骤
- [ ] 11.1 更新所有引用 `UserVO`/`@WithUserVO` 的测试文件
- [ ] 11.2 更新 `JwtAuthenticationFilterTest`、`AuthAppServiceImplTest`、`UserInfoControllerTest` 等核心测试
- [ ] 11.3 更新 `ConverterLayerArchTest` 等架构测试
- [ ] 11.4 新增或更新 ArchUnit 规则：Repository 不得返回 `*VO`、ApplicationService 不得直接依赖 Mapper 等
- [ ] 11.5 更新 `docs/后端开发手册.md` 中关于 DDD 分层、VO、DomainService 使用规范的章节
- [ ] 11.6 运行 `./mvnw test` 或等价命令，全量测试通过
- [ ] 11.7 代码审查与清理，确保无调试代码和遗留 TODO
