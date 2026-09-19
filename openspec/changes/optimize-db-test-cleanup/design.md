## Context

`DBIntegrationTest` 当前实现（81 个测试类继承它，约 700 个用例）：

```java
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
public abstract class DBIntegrationTest {
    @Autowired private Flyway flyway;

    @BeforeEach void setUp()    { flyway.clean(); flyway.migrate(); }
    @AfterEach  void tearDown() { flyway.clean(); }
}
```

每用例 2 次 `flyway.clean()`（0.25s/次）+ 1 次 `flyway.migrate()`（1.10s/次）≈ 1.60s。全量本地测试执行了 766 次迁移，DB 层仅清理开销约 19 分钟。

**关键前提：DB 层保留真实数据库是正当设计，本变更不动它。** 41 个 AppService 中 30 个带 `@Transactional`，事务边界即被测对象；把仓储 mock 掉会让 `@Transactional` 空转，多仓储写入的原子性、异常回滚、提交时机都无从验证。App 层可转换为纯单元测试的只有 7 个非事务类 / 54 个用例（约 2.7 分钟），不属于本变更范围。

勘查结论（决定设计，详见 proposal）：

| 勘查项 | 结论 |
|---|---|
| 有种子数据的表 | 仅 `tb_role`（4 行）、`tb_direction_learning_step`（12 行），均来自 V1 |
| `tb_role` 在生产中是否只读 | 是。`RoleRepository` 无 `save`，`RoleMapper` 生产调用全是读，无创建角色入口 |
| 种子数据是否被后续迁移改写 | 是。V26 改标题、V28 写 `sort_order`，最终态 ≠ V1 初始态 |
| 测试是否写入种子表 | 是，3 个类约 21 处 `roleMapper.insert(role)` |
| 外键约束 | 仅 4 个，集中于 judge 表并带 `ON DELETE CASCADE` |
| `tb_permission` 内容来源 | 启动期 `PermissionScanner` 扫描注解写入，**非迁移** |
| 表总数 | 43 张，全部 `tb_` 前缀 |

## Goals / Non-Goals

**Goals:**

- 把 DB 层用例的清理成本从约 1.6s/用例降到毫秒级
- 保持数据隔离强度不下降，不引入任何新的污染路径
- 把 `tb_role` 的只读语义从「约定」变成「运行时可验证」
- 让迁移演进的种子数据可恢复，且不引入需要人工同步的硬编码副本

**Non-Goals:**

- 不接入 failsafe、不改 `ci.yml`
- 不改 `src/main/java`
- 不把 App 层测试改为纯单元测试
- 不采用 `@Transactional` 用例级回滚

## Decisions

### 决策 1：TRUNCATE 清数据，取代 flyway.clean() 重建 schema

**选择**：`TRUNCATE TABLE <每张待清空的 tb_ 表> RESTART IDENTITY CASCADE`。

**理由**：表结构在每个用例中完全一致，重建 schema 属于重复劳动。TRUNCATE 直接丢弃表存储、不写逐行日志，`CASCADE` 能一并处理仅有的 4 个外键（judge 表，`ON DELETE CASCADE`）。`RESTART IDENTITY` 让自增主键在用例间保持确定性。

**被否决的替代方案**：

| 方案 | 否决原因 |
|---|---|
| `DELETE FROM <每张表>` | 逐行删除并写 WAL，慢于 TRUNCATE；且需自行处理外键依赖顺序 |
| `@Transactional` 用例级回滚 | 会掩盖真实提交语义，无法覆盖需要观察提交结果的测试；与项目追求「真实、已提交环境」的初衷相悖 |
| 保留 `flyway.clean()` 但改为每类执行一次，用例间不清理 | 会让写入在用例间泄漏，隔离强度下降 |

### 决策 2：`tb_role` 定位为只读参考表，排除在清空之外

**选择**：TRUNCATE 的表清单排除 `tb_role`；同时把 3 个自造角色的测试类改为使用 4 个种子角色。

**理由**：
- 生产代码从不写 `tb_role`（`RoleRepository` 无 `save`、`RoleMapper` 生产侧只读、无创建角色入口），唯一写入方是迁移 V1。测试自造角色（`BATCH_ASSIGN_ROLE`、`ROLE_TEST_MEMBER`、`TEST_ROLE`）是在制造**生产不可能出现的状态**
- 不参与清空就不需要恢复，`tb_role` 的角色 ID 也因此稳定
- 若改为「清空 + 快照回填」，等于用一个恢复机制去兜住一批本不该存在的数据

**被否决的替代方案**：

| 方案 | 否决原因 |
|---|---|
| `tb_role` 参与清空 + 快照回填 | 模型上把只读表当作可写表；且需要额外机制维护一批不该出现的数据 |
| 保留 `tb_role` 不清理，但不改那 3 个测试类 | 21 处插入会跨用例泄漏（不再有人清理），必然失败 |

**代价**：需要修正 3 个测试类的角色来源。已核对语义等价——测试开始时 `tb_role_permission` 为空，种子角色天然就是这些测试想要的「存在但无任何权限绑定」的角色；`RolePermissionRepositoryImplIntegrationTest` 本身已有使用 `RoleFixture.roleId(...)` 的先例可照抄。

### 决策 3：只读性用运行时保护兜底

**选择**：迁移完成后快照 `tb_role` 内容；每个测试方法**结束时**比对快照与当前内容，不一致即判定该用例失败并提示「测试写入了只读参考表」。

**理由**：只读是约定，约定会被破坏。把检查放在 `@AfterEach` 而不是 `@BeforeEach`，是为了**把失败归因到真正违规的那个用例**——若放在 `@BeforeEach`，违规用例本身会通过，失败会出现在下一个无关用例上，排查成本高。

**被否决的替代方案**：

| 方案 | 否决原因 |
|---|---|
| 只在 spec 中约定，不加运行时检查 | 破坏只读后表现为「后续用例偶发失败」，与污染源无关联，排查困难 |
| 只比对行数 | 无法发现 UPDATE（行数不变但内容变了）。比对全量内容成本仅为每用例一次 4 行 SELECT |
| 在 `@BeforeEach` 比对 | 归因错误（见上） |

### 决策 4：`tb_direction_learning_step` 用「迁移后快照 + 清空后回填」

**选择**：迁移完成后 `SELECT *` 把该表内容存入内存快照；每个用例清空后按快照逐行 `INSERT` 回填。

**理由**：该表在生产中可写（`LearningPathAppService.createStep`），属可变表，必须清空。而其种子数据被 V26/V28 演进过——硬编码最终态会让测试代码持有一份会静默漂移的重复数据。快照让种子数据的唯一来源始终是迁移脚本。

**被否决的替代方案**：

| 方案 | 否决原因 |
|---|---|
| 硬编码 12 行到测试 Fixture | 需复制 V26/V28 演进后的值；迁移改动时静默漂移 |
| 硬编码 + 守卫测试校验一致性 | 守卫能发现漂移，但仍需维护重复数据，且失败晚于漂移引入 |
| 该表也排除在清空之外 | 测试会合法地向它写入数据（学习路径的增删改测试），排除后必然跨用例污染 |

### 决策 5：以 `DataSource` 身份判定「是否已迁移」，不用 `@TestInstance(PER_CLASS)`

**选择**：保持 JUnit 默认 `PER_METHOD`。以 `DataSource` 实例身份为键维护「已迁移数据源」集合与各自的种子快照；首次遇到某 `DataSource` 时执行一次迁移并快照，之后只做清空 + 回填。

**理由**：不同测试类因 `@MockitoBean` 组合不同会产生不同 Spring 上下文，进而是不同 Testcontainers 实例与不同数据库。迁移状态**必须按数据源隔离**，不能用 JVM 全局布尔值。

**被否决的替代方案**：

| 方案 | 否决原因 |
|---|---|
| `@TestInstance(PER_CLASS)` + 非静态 `@BeforeAll` | 会让测试实例跨用例复用，破坏「实例字段不跨用例泄漏」的现有隐式保证，影响全部 81 个类，风险与收益不成比例 |
| JVM 全局 `static boolean migrated` | 多上下文各有独立容器与数据库，全局标记会让第二个上下文误判、schema 为空 |
| `@BeforeAll` static + 静态注入 | Spring 不支持静态字段注入 |

### 决策 6：回填后修正自增序列

**选择**：回填后对 `tb_direction_learning_step` 执行 `setval(pg_get_serial_sequence(...), MAX(id))`。

**理由**：`TRUNCATE ... RESTART IDENTITY` 把序列重置为 1，而回填写入的是显式主键，Postgres 不会因此推进序列。不修正则后续插入会拿到已占用主键。

### 决策 7：权限相关测试 mock 掉权限扫描器

**选择**：`PermissionAppServiceImplIntegrationTest`、`PermissionRepositoryImplIntegrationTest`、`RolePermissionManageAppServiceImplIntegrationTest`、`RolePermissionRepositoryImplIntegrationTest` 四个类加 `@Import(TestSecurityConfig.class)`，其提供的 `@Primary` 空实现会取代真实扫描器。

**理由**：`tb_permission` 的内容由启动期 `PermissionScanner` 扫描 189 个注解写入，**不是迁移提供的**。让启动期代码往被测表写数据，会让测试无法控制该表内容。

**代价**：这 4 个类丧失「启动时校验权限标识全局唯一」的校验。另外 77 个 DB 层类仍跑真实扫描器，整体覆盖不丢。

## Risks / Trade-offs

- **存在未发现的种子依赖**：若某测试依赖除这两张表外的迁移数据，去掉逐用例迁移后会失败。→ 已用全文检索确认全部迁移中只有 2 条数据写入语句；实施时以「全量 DB 层 0 失败」作为最终验证，失败即说明有遗漏。
- **`tb_role` 只读化后，将来若有真实需求要增删角色**：会与只读定位冲突。→ 那应是一个有规格的功能变更（含权限、接口、界面），而不是靠测试悄悄插入。届时本决策需一并修订。
- **快照回填的通用性**：回填按 `SELECT *` 的列集合动态生成 INSERT，依赖同一查询内列顺序一致（同一 ResultSet 元数据，成立）。→ 实施时对 `tb_direction_learning_step` 验证列顺序与类型往返正确。
- **`@AfterEach` 执行顺序**：多个子类已有自己的 `@AfterEach`（如清 `UserCTX`）。→ 基类清理与保护检查使用与子类不同的方法名，两者并行执行；实施时抽查含 `@AfterEach` 的子类。
- **TRUNCATE 的锁**：TRUNCATE 需排他锁，当前测试串行执行无冲突。→ 若将来引入并行测试，本方案需重新评估。
- **首次迁移失败定位变难**：迁移只在首个用例前执行一次，失败会让整类用例报错且位置后移。→ 实施时确认报错信息仍指向 Flyway。

## Migration Plan

1. 改造 `DBIntegrationTest`：数据源键、迁移一次、`tb_role` 快照与保护、清空、`tb_direction_learning_step` 快照回填、序列修正
2. 修正 3 个自造角色的测试类
3. 为 4 个权限相关测试类加 `@Import(TestSecurityConfig.class)`
4. 抽样验证（含 `hasSize(6)` 的学习路径用例、写 `tb_role` 的用例、角色名查询）
5. 全量运行 DB 层测试，确认 0 失败且迁移次数等于类数
6. 全量运行后端测试，量化耗时

**回滚策略**：改动仅涉及测试基类与 5 个测试类，不产出部署物。`git revert` 即恢复逐用例 `flyway.clean()+migrate()`，无需数据迁移。

## Open Questions

- `RoleFixture.defaultRoleId(RoleType)` 硬编码角色 ID 1–4，其 Javadoc 声明「仅用于不依赖数据库的单元测试，集成测试请使用 `roleId(RoleMapper, RoleType)`」，但 `AdminUserAppServiceImplIntegrationTest`（3 处）与 `UserInfoAppServiceImplIntegrationTest`（2 处）这两个**集成测试**正在使用它。这违反项目测试规范「不要硬编码角色 ID」。本变更是否顺带修正？倾向修正，因为 `tb_role` 只读化后「角色 ID 稳定」这个事实会让这种硬编码更容易被误认为安全。
- 是否把「迁移执行次数等于测试类数」固化为自动化断言，而非仅实施时人工核对？
- `PermissionScanner` 上的 `@Order(100)` 是无效注解（不控制单例初始化顺序），其注释「在其他组件初始化之后执行」具有误导性。是否移除？该项与 `remove-permission-cache` 变更相关，需协调。
