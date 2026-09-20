## 1. 改造 DBIntegrationTest

- [x] 1.1 移除 `@BeforeEach` 中的逐用例 `flyway.clean() + flyway.migrate()`，移除 `@AfterEach` 中的 `flyway.clean()`
- [x] 1.2 加入按 `DataSource` 实例身份隔离的静态存储：已迁移数据源集合、`tb_role` 只读快照、`tb_direction_learning_step` 种子快照（用 `ConcurrentHashMap` 与同步块，避免多线程首次迁移竞态）
- [x] 1.3 实现首次迁移：某 `DataSource` 首次出现时执行一次 `flyway.clean() + flyway.migrate()`，成功后标记为已迁移并立即执行两处快照
- [x] 1.4 实现 `tb_role` 只读快照：迁移后对其执行 `SELECT *`，保存列顺序与行数据
- [x] 1.5 实现 `tb_direction_learning_step` 种子快照：迁移后对其执行 `SELECT *`，保存列顺序与行数据
- [x] 1.6 实现清空逻辑：查询 `pg_tables` 中 `schemaname='public' AND tablename LIKE 'tb\_%'` **且排除 `tb_role`** 的全部表，拼接为一条 `TRUNCATE TABLE ... RESTART IDENTITY CASCADE` 执行
- [x] 1.7 实现 `tb_direction_learning_step` 回填：按快照的列集合动态生成 `INSERT`，逐行回填，正确处理 `null` 值
- [x] 1.8 实现序列修正：回填后对该表执行 `setval(pg_get_serial_sequence('tb_direction_learning_step','id'), MAX(id))`，空表时回退为 1
- [x] 1.9 实现只读保护检查：`@AfterEach` 中比对 `tb_role` 当前内容与快照，不一致时以明确的失败信息抛出（提示「测试写入了只读参考表」），使失败归因到违规用例
- [x] 1.10 组装 `@BeforeEach`：未迁移则先迁移，随后清空 + 回填；已迁移则直接清空 + 回填
- [x] 1.11 补充 Javadoc：说明为何不再逐用例迁移、`tb_role` 为何只读且不参与清空、`tb_direction_learning_step` 为何用快照而非硬编码、为何按数据源而非全局标记

## 2. 修正角色引用（自造角色与硬编码 ID）

- [x] 2.1 `RolePermissionRepositoryImplIntegrationTest`：把 9 处 `createRole("X")` 改为 `RoleFixture.roleId(roleMapper, RoleType.Y)`；需要两个不同角色的用例用 `MEMBER` 与 `CANDIDATE`（照抄该类第 59 / 103 / 104 / 119 / 120 行的既有写法）
- [x] 2.2 `RolePermissionManageAppServiceImplIntegrationTest`：把 12 处 `createRole("ROLE_TEST_*")` 改为使用种子角色；成对的 A/B 角色用 `MEMBER` 与 `CANDIDATE`
- [x] 2.3 `UserRepositoryImplIntegrationTest`：把 `findPage_shouldFilterAndPaginate` 中的 `RoleDO.builder().name("TEST_ROLE")` 改为 `RoleFixture.roleId(roleMapper, RoleType.CANDIDATE)`
- [x] 2.4 检索确认全库不再有测试写入 `tb_role`：`grep -rn "roleMapper.insert\|RoleDO.builder().name" src/test` 应无残留
- [x] 2.5 逐个运行上述 3 个类，确认 0 失败，且只读保护检查未触发
- [x] 2.6 删除 `RoleFixture.defaultRoleId(RoleType)`，新增注入式「角色名 → 真实 ID」映射（`bindRoleIds` / `unbindRoleIds` / `roleIdFor`），并修正类注释与 Javadoc
- [x] 2.7 `UserFixture.Builder.withRoleType` 改为从注入映射解析（保留 API，工厂方法共 55 处调用点不动）；`DBIntegrationTest` 在 `@BeforeEach` 注入、`@AfterEach` 清除；纯单元测试 `AssessmentAnswerDomainServiceImplTest` 的 2 处改用显式 `withRoleId(Long)`
- [x] 2.8 `AdminUserAppServiceImplIntegrationTest`：注入 `RoleMapper`，3 处 `RoleFixture.defaultRoleId(...)` 改为 `RoleFixture.roleId(roleMapper, ...)`
- [x] 2.9 `UserInfoAppServiceImplIntegrationTest`：注入 `RoleMapper`，2 处 `RoleFixture.defaultRoleId(...)` 改为 `RoleFixture.roleId(roleMapper, ...)`
- [x] 2.10 `RoleRepositoryImplIntegrationTest`：2 处 `defaultRoleId` 改为先 `roleRepository.findByName(...)` 取 ID，再用于 `findById` / 断言
- [x] 2.11 `MemberRepositoryImplIntegrationTest`：`createUser` 辅助方法内的 `withRoleType(...)` 改为 `withRoleId(RoleFixture.roleId(roleMapper, ...))`
- [x] 2.12 检索确认：`grep -rn "defaultRoleId" src/test` 无结果；`withRoleType` 仅允许存在于 `UserFixture` 内部（方法定义 + 4 个工厂），且不得再出现硬编码 ID
- [x] 2.13 运行上述 4 个类，确认 0 失败

## 3. 权限相关测试类 mock 掉扫描器

- [x] 3.1 为 `PermissionAppServiceImplIntegrationTest` 加 `@Import(TestSecurityConfig.class)`
- [x] 3.2 为 `PermissionRepositoryImplIntegrationTest` 加 `@Import(TestSecurityConfig.class)`
- [x] 3.3 为 `RolePermissionManageAppServiceImplIntegrationTest` 加 `@Import(TestSecurityConfig.class)`
- [x] 3.4 为 `RolePermissionRepositoryImplIntegrationTest` 加 `@Import(TestSecurityConfig.class)`
- [x] 3.5 运行这 4 个类，确认启动日志中不再出现 `Starting permission scan`，且 0 失败

## 4. 抽样验证

- [x] 4.1 验证只读保护：临时构造一个向 `tb_role` 插入的用例，确认它**失败**且失败信息指向该用例本身，随后删除该临时用例
- [x] 4.2 验证依赖学习路径种子的场景：运行 `LearningPathAppServiceImplIntegrationTest`，确认 `hasSize(6)`（4 行种子 + 2 行自建）通过
- [x] 4.3 验证可变表回填有效：确认学习路径用例自建的数据不泄漏到同类的其他用例
- [x] 4.4 验证角色名查询可用：运行 `RolePermissionRepositoryImplIntegrationTest`，确认 `selectByName("MEMBER")` 返回非空
- [x] 4.5 验证序列修正：确认回填后插入新学习步骤不产生主键冲突
- [x] 4.6 验证迁移次数：实测等于 **2 × 不同 `DataSource` 数量**（1 次 Spring 启动迁移 + 1 次基类重置迁移），远小于用例数：2 个类→2 次、4 个共享上下文的类→2 次、8 个类（4 个上下文）→8 次。**不以「等于测试类数」作为通过条件**
- [x] 4.7 验证带自定义 `@AfterEach` 的子类：抽查 2–3 个含 `@AfterEach` 的 DB 层类，确认基类保护检查与子类清理并行执行且互不干扰

## 5. 全量 DB 层验证

- [x] 5.1 运行 `application/service/impl` 全部类，确认 0 失败（基线 423 个用例 0 失败）→ **423 用例 0 失败，425s；36 个类只产生 14 个数据源（28 次迁移）**
- [x] 5.2 运行 `infrastructure/repository/impl` 全部类，确认 0 失败（基线 277 个用例 0 失败）→ **281 用例 0 失败，135s；36 个类只产生 2 个数据源（4 次迁移）**
- [x] 5.3 运行 `api/controller` 下 3 个 DB 层类（`AdminAiTraceControllerIntegrationTest`、`FileDownloadControllerIntegrationTest`、`FileUploadControllerIntegrationTest`）及 `infrastructure` 下其余 DB 层类，确认 0 失败 → **231 用例 0 失败，436s**
- [x] 5.4 统计全量 DB 层日志中的迁移执行次数，确认等于 2 × 不同 `DataSource` 数量，且远小于用例数 → **appimpl 28、repoimpl 4、其余 14；全量后端 40 次＝20 个数据源 × 2**
- [x] 5.5 确认无「表不存在」「主键冲突」「种子数据缺失」「测试写入了只读参考表」类错误 → **DB 层无上述错误**；日志中的 `relation "tb_audit" does not exist`（805 次）来自 `APIIntegrationTest` 接口层（空 schema 设计，0 迁移），已单独复现确认为**改动前既有行为**，与 `DBIntegrationTest` 无关，不在本变更范围

## 6. 耗时量化

- [x] 6.1 记录改动后 DB 层两个大包的耗时，与基线对比（基线：`application/service/impl` 1209s、`infrastructure/repository/impl` 684s）→ **425s、135s**
- [x] 6.2 全量运行后端测试，记录总耗时与用例数，与基线对比（基线：1914 个用例 2839s）→ **1917 用例 1194s（19.9 分钟），0 失败，提速 2.38×**。注：`surefire` 的 `excludes` 是 pom 中硬编码的 `<exclude>`，`-Dsurefire.excludes=` 无法覆盖；改用 `-Dtest='*Test,*Tests,*TestCase,*IntegrationTest,Test*'` 覆盖全部用例
- [x] 6.3 用实测数字替换 proposal.md 中「约 28 分钟」的估算，并注明 TRUNCATE、快照回填、只读保护的实测单次耗时 → **TRUNCATE 111ms + 回填 27ms + 角色映射注入 1.7ms + 只读校验 2ms ≈ 142ms/用例**
- [x] 6.4 若收益显著低于预期，回溯确认瓶颈（TRUNCATE 本身、回填、还是 Spring 上下文启动），并记入 design 的待定项 → 收益**高于**预期（预计 47→28 分钟，实测 47.3→19.9 分钟）；瓶颈为 TRUNCATE（占清理开销 78%），已记入 design 的后续可优化项

## 7. 收尾

- [x] 7.1 执行 `./mvnw -B clean test-compile` 确认编译通过 → `test-compile` BUILD SUCCESS。注：`clean` 因外部 `java.exe` 占用 `target/` 而失败（Windows 文件锁，非代码问题），改为不 clean 的 `test-compile` 完成校验
- [x] 7.2 执行 `./mvnw -B spotless:check` 确认格式合规 → 首次发现 Javadoc 折行违规，`spotless:apply` 后复检 BUILD SUCCESS；改动文件仅限本变更涉及的 11 个测试文件
- [x] 7.3 确认 `src/main/java` 零改动、`pom.xml` 与 `.github` 零改动 → `git status` 已确认
- [x] 7.4 确认 design 的待定项已全部关闭：硬编码角色 ID 已删除、迁移次数表述已改为按数据源、`PermissionScanner` 的 `@Order(100)` 无需处理
