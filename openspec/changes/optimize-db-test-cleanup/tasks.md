## 1. 改造 DBIntegrationTest

- [ ] 1.1 移除 `@BeforeEach` 中的逐用例 `flyway.clean() + flyway.migrate()`，移除 `@AfterEach` 中的 `flyway.clean()`
- [ ] 1.2 加入按 `DataSource` 实例身份隔离的静态存储：已迁移数据源集合、`tb_role` 只读快照、`tb_direction_learning_step` 种子快照（用 `ConcurrentHashMap` 与同步块，避免多线程首次迁移竞态）
- [ ] 1.3 实现首次迁移：某 `DataSource` 首次出现时执行一次 `flyway.clean() + flyway.migrate()`，成功后标记为已迁移并立即执行两处快照
- [ ] 1.4 实现 `tb_role` 只读快照：迁移后对其执行 `SELECT *`，保存列顺序与行数据
- [ ] 1.5 实现 `tb_direction_learning_step` 种子快照：迁移后对其执行 `SELECT *`，保存列顺序与行数据
- [ ] 1.6 实现清空逻辑：查询 `pg_tables` 中 `schemaname='public' AND tablename LIKE 'tb\_%'` **且排除 `tb_role`** 的全部表，拼接为一条 `TRUNCATE TABLE ... RESTART IDENTITY CASCADE` 执行
- [ ] 1.7 实现 `tb_direction_learning_step` 回填：按快照的列集合动态生成 `INSERT`，逐行回填，正确处理 `null` 值
- [ ] 1.8 实现序列修正：回填后对该表执行 `setval(pg_get_serial_sequence('tb_direction_learning_step','id'), MAX(id))`，空表时回退为 1
- [ ] 1.9 实现只读保护检查：`@AfterEach` 中比对 `tb_role` 当前内容与快照，不一致时以明确的失败信息抛出（提示「测试写入了只读参考表」），使失败归因到违规用例
- [ ] 1.10 组装 `@BeforeEach`：未迁移则先迁移，随后清空 + 回填；已迁移则直接清空 + 回填
- [ ] 1.11 补充 Javadoc：说明为何不再逐用例迁移、`tb_role` 为何只读且不参与清空、`tb_direction_learning_step` 为何用快照而非硬编码、为何按数据源而非全局标记

## 2. 修正自造角色的测试类

- [ ] 2.1 `RolePermissionRepositoryImplIntegrationTest`：把 9 处 `createRole("X")` 改为 `RoleFixture.roleId(roleMapper, RoleType.Y)`；需要两个不同角色的用例用 `MEMBER` 与 `CANDIDATE`（照抄该类第 59 / 103 / 104 / 119 / 120 行的既有写法）
- [ ] 2.2 `RolePermissionManageAppServiceImplIntegrationTest`：把 12 处 `createRole("ROLE_TEST_*")` 改为使用种子角色；成对的 A/B 角色用 `MEMBER` 与 `CANDIDATE`
- [ ] 2.3 `UserRepositoryImplIntegrationTest`：把 `findPage_shouldFilterAndPaginate` 中的 `RoleDO.builder().name("TEST_ROLE")` 改为 `RoleFixture.roleId(roleMapper, RoleType.CANDIDATE)`
- [ ] 2.4 检索确认全库不再有测试写入 `tb_role`：`grep -rn "roleMapper.insert\|RoleDO.builder().name" src/test` 应无残留
- [ ] 2.5 逐个运行上述 3 个类，确认 0 失败，且只读保护检查未触发

## 3. 权限相关测试类 mock 掉扫描器

- [ ] 3.1 为 `PermissionAppServiceImplIntegrationTest` 加 `@Import(TestSecurityConfig.class)`
- [ ] 3.2 为 `PermissionRepositoryImplIntegrationTest` 加 `@Import(TestSecurityConfig.class)`
- [ ] 3.3 为 `RolePermissionManageAppServiceImplIntegrationTest` 加 `@Import(TestSecurityConfig.class)`
- [ ] 3.4 为 `RolePermissionRepositoryImplIntegrationTest` 加 `@Import(TestSecurityConfig.class)`
- [ ] 3.5 运行这 4 个类，确认启动日志中不再出现 `Starting permission scan`，且 0 失败

## 4. 抽样验证

- [ ] 4.1 验证只读保护：临时构造一个向 `tb_role` 插入的用例，确认它**失败**且失败信息指向该用例本身，随后删除该临时用例
- [ ] 4.2 验证依赖学习路径种子的场景：运行 `LearningPathAppServiceImplIntegrationTest`，确认 `hasSize(6)`（4 行种子 + 2 行自建）通过
- [ ] 4.3 验证可变表回填有效：确认学习路径用例自建的数据不泄漏到同类的其他用例
- [ ] 4.4 验证角色名查询可用：运行 `RolePermissionRepositoryImplIntegrationTest`，确认 `selectByName("MEMBER")` 返回非空
- [ ] 4.5 验证序列修正：确认回填后插入新学习步骤不产生主键冲突
- [ ] 4.6 验证迁移次数：统计日志中 `Successfully applied 28 migrations` 的次数，应等于测试类数而非用例数
- [ ] 4.7 验证带自定义 `@AfterEach` 的子类：抽查 2–3 个含 `@AfterEach` 的 DB 层类，确认基类保护检查与子类清理并行执行且互不干扰

## 5. 全量 DB 层验证

- [ ] 5.1 运行 `application/service/impl` 全部类，确认 0 失败（改动前基线：423 个用例 0 失败）
- [ ] 5.2 运行 `infrastructure/repository/impl` 全部类，确认 0 失败（改动前基线：277 个用例 0 失败）
- [ ] 5.3 运行 `api/controller` 下 3 个 DB 层类（`AdminAiTraceControllerIntegrationTest`、`FileDownloadControllerIntegrationTest`、`FileUploadControllerIntegrationTest`）及 `infrastructure` 下其余 DB 层类，确认 0 失败
- [ ] 5.4 统计全量 DB 层日志中的迁移执行次数，确认约等于类数而非用例数
- [ ] 5.5 确认无「表不存在」「主键冲突」「种子数据缺失」「测试写入了只读参考表」类错误

## 6. 耗时量化

- [ ] 6.1 记录改动后 DB 层两个大包的耗时，与基线对比（基线：`application/service/impl` 1209s、`infrastructure/repository/impl` 684s）
- [ ] 6.2 全量运行后端测试（`-Dsurefire.excludes=`），记录总耗时与用例数，与基线对比（基线：1914 个用例 2839s）
- [ ] 6.3 用实测数字替换 proposal.md 中「约 28 分钟」的估算，并注明 TRUNCATE、快照回填、只读保护的实测单次耗时
- [ ] 6.4 若收益显著低于预期，回溯确认瓶颈（TRUNCATE 本身、回填、还是 Spring 上下文启动），并记入 design 的待定项

## 7. 收尾

- [ ] 7.1 执行 `./mvnw -B clean test-compile` 确认编译通过
- [ ] 7.2 执行 `./mvnw -B spotless:check` 确认格式合规
- [ ] 7.3 确认 `src/main/java` 零改动、`pom.xml` 与 `.github` 零改动
- [ ] 7.4 若采纳 design 待定项，修正 `AdminUserAppServiceImplIntegrationTest`（3 处）与 `UserInfoAppServiceImplIntegrationTest`（2 处）对 `RoleFixture.defaultRoleId` 的误用
