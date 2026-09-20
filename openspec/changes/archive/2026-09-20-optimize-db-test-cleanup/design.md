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
| `tb_role` 的 ID 是否可假定 | 否。V1 的 `INSERT INTO tb_role (name) VALUES (...)` 只写名称，`id` 由 `SERIAL` 生成；业务代码以 `name` 为查询条件，从不依赖 ID |
| `tb_role` 在生产中是否只读 | 是。`RoleRepository` 无 `save`，`RoleMapper` 生产调用全是读，无创建角色入口 |
| 种子数据是否被后续迁移改写 | 是。V26 改标题、V28 写 `sort_order`，最终态 ≠ V1 初始态 |
| 测试是否写入种子表 | 是，3 个类约 21 处 `roleMapper.insert(role)` |
| 外键约束 | 仅 4 个，集中于 judge 表并带 `ON DELETE CASCADE` |
| `tb_permission` 内容来源 | 启动期 `PermissionScanner` 扫描注解写入，**非迁移** |
| 表总数 | 43 张，全部 `tb_` 前缀 |

## Goals / Non-Goals

**Goals:**

- 把 DB 层用例的清理成本从约 1.6s/用例降到约 0.14s/用例（实测：TRUNCATE 111ms + 回填 27ms + 映射注入 1.7ms + 只读校验 2ms）
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

**推论：迁移次数等于 2 × 不同 `DataSource` 的数量，不等于测试类数，更不等于用例数。** 多个测试类只要共享同一 Spring 上下文就共享 `DataSource`，该数据源只在第一个类开始时重置一次，后续类的日志中不会再出现重置记录。每个数据源共出现 2 次 `Successfully applied 28 migrations`：1 次来自 Spring 启动时的自动迁移（`PermissionScanner` 需要 schema），1 次来自本基类的重置迁移；已实测 4 个共享上下文的测试类合计 2 次、8 个类（4 个上下文）合计 8 次。因此「迁移执行次数等于测试类数」既不是本设计的目标，也不是正确的不变量——它只是「每个测试类各自拥有独立上下文」这一特例下的巧合，不能用作验收断言。正确的验收方式是：**每个 `DataSource` 刚好出现 2 次迁移**；可操作化为「同一上下文的第二个测试类不再出现迁移日志」。

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

### 决策 8：角色一律按名称解析，删除硬编码角色 ID

**选择**：删除 `RoleFixture.defaultRoleId(RoleType)`（硬编码 SUPER_ADMIN→1、DIRECTION_ADMIN→2、MEMBER→3、CANDIDATE→4）。角色按名称解析走两条路径：

1. **显式查询**：`RoleFixture.roleId(roleMapper, RoleType.X)`（内部即 `roleMapper.selectByName`），或直接用 `roleRepository.findByName(...)`——用于有 `RoleMapper` / `RoleRepository` 的测试类。
2. **注入映射**：`DBIntegrationTest` 在迁移完成后读取 `tb_role` 建立「角色名 → 真实 ID」映射，通过 `RoleFixture.bindRoleIds(...)` 注入，`@AfterEach` 时 `unbindRoleIds()`。`UserFixture.Builder.withRoleType(RoleType)` 改为读取该映射。

**为什么保留 `withRoleType` 而不是删除它**：`UserFixture` 的 `member()` / `candidate()` / `directionAdmin()` / `superAdmin()` 都经由 `withRoleType`，而这些工厂方法被约 30 个测试类调用 55 次。删除它们会强制约 30 个文件改动，且多数测试类并没有 `RoleMapper` 可注入。保留 API、把解析方式从「硬编码」换成「注入映射」，用 4 个文件的改动达成同一目标。

**理由**：
- **角色 ID 在原理上不可假定。** V1 的 `INSERT INTO tb_role (name) VALUES (...)` 只写名称，`id` 由 `SERIAL` 生成。当前容器上恰好是 1–4，但那是执行顺序的副产物，不是契约。
- **业务代码本身也不按 ID 查找角色。** `RoleMapper.selectByName` 是唯一查询入口，`RoleRepository.findByName` 亦然。硬编码 ID 等于让测试依赖一个生产代码从不使用的字段。
- **`defaultRoleId` 无任何合法调用方。** 其 Javadoc 声明「仅用于不依赖数据库的单元测试」，但实测 10 处调用全部位于集成测试：`AdminUserAppServiceImplIntegrationTest`(3)、`UserInfoAppServiceImplIntegrationTest`(2)、`RoleRepositoryImplIntegrationTest`(2)，以及 `UserFixture.withRoleType` 被 `MemberRepositoryImplIntegrationTest` 通过 6 处调用间接触发，再加上 4 个工厂方法的内部调用。保留它只会让后续测试作者继续误用。

**被否决的替代方案**：

| 方案 | 否决原因 |
|---|---|
| 删除 `withRoleType`，工厂方法改为接收显式 `roleId` | 需机械修改约 30 个测试类 / 55 处调用，并给多数类新增 `RoleMapper` 注入，diff 与风险远超收益 |
| 保留 `defaultRoleId`，仅约定集成测试不调用 | 约定无法被静态检查；且方法本身无合法调用方，保留纯属负债 |
| 只修 `AdminUser` / `UserInfo` 等直接调用点 | `UserFixture` 工厂仍间接硬编码，未达成「集成测试不硬编码角色 ID」 |
| 在夹具中保留硬编码回退供单元测试使用 | 回退值仍是硬编码角色 ID，等于把问题移进夹具 |

**代价**：
- 注入映射是静态可变状态，依赖测试串行执行（项目当前未开启 JUnit 并行：无 `junit-platform.properties`，`surefire` 也未配置 `parallel`）。
- 不访问数据库的纯单元测试无法使用 `member()` / `candidate()` 等工厂；`RoleFixture.roleIdFor` 会抛出明确异常，提示改用 `withRoleId(Long)`。`AssessmentAnswerDomainServiceImplTest` 的 2 处已按此调整。
- `RoleRepositoryImplIntegrationTest` 的 `findById` 用例原本用 `defaultRoleId(SUPER_ADMIN)` 作为入参，改为先按名称解析真实 ID 再 `findById`，该用例从「单次查询」变为「两次查询」——但这恰好是它应当验证的语义：按名称得到 ID，再按 ID 取回同一角色。

## Risks / Trade-offs

- **存在未发现的种子依赖**：若某测试依赖除这两张表外的迁移数据，去掉逐用例迁移后会失败。→ 已用全文检索确认全部迁移中只有 2 条数据写入语句；实施时以「全量 DB 层 0 失败」作为最终验证，失败即说明有遗漏。
- **`tb_role` 只读化后，将来若有真实需求要增删角色**：会与只读定位冲突。→ 那应是一个有规格的功能变更（含权限、接口、界面），而不是靠测试悄悄插入。届时本决策需一并修订。
- **快照回填的通用性**：回填按 `SELECT *` 的列集合动态生成 INSERT，依赖同一查询内列顺序一致（同一 ResultSet 元数据，成立）。→ 实施时对 `tb_direction_learning_step` 验证列顺序与类型往返正确。
- **`@AfterEach` 执行顺序**：多个子类已有自己的 `@AfterEach`（如清 `UserCTX`）。→ 基类清理与保护检查使用与子类不同的方法名，两者并行执行；实施时抽查含 `@AfterEach` 的子类。
- **删除 `defaultRoleId` 的连锁影响**：`UserFixture` 的 4 个工厂方法经由 `withRoleType` 间接依赖它，而这些工厂被约 30 个测试类调用 55 次。→ 保留工厂与 `withRoleType`，改为读取 `DBIntegrationTest` 注入的「角色名 → 真实 ID」映射（见决策 8），因此调用点无需改动。
- **注入映射依赖串行执行**：`RoleFixture` 的角色映射是静态可变状态。→ 项目当前未开启 JUnit 并行（无 `junit-platform.properties`，`surefire` 未配置 `parallel`）；若将来开启并行，需改为按数据源隔离或 ThreadLocal。
- **TRUNCATE 的锁**：TRUNCATE 需排他锁，当前测试串行执行无冲突。→ 若将来引入并行测试，本方案需重新评估。
- **首次迁移失败定位变难**：迁移只在首个用例前执行一次，失败会让整类用例报错且位置后移。→ 实施时确认报错信息仍指向 Flyway。

## Migration Plan

1. 改造 `DBIntegrationTest`：数据源键、迁移一次、`tb_role` 快照与保护、清空、`tb_direction_learning_step` 快照回填、序列修正
2. 修正 3 个自造角色的测试类
3. 为 4 个权限相关测试类加 `@Import(TestSecurityConfig.class)`
4. 抽样验证（含 `hasSize(6)` 的学习路径用例、写 `tb_role` 的用例、角色名查询）
5. 删除 `RoleFixture.defaultRoleId`，新增注入式角色映射，并把 `UserFixture.withRoleType` 改为读取映射；修正 4 个硬编码角色 ID 的测试类与 1 个纯单元测试
6. 全量运行 DB 层测试，确认 0 失败，且每个 `DataSource` 刚好出现 2 次迁移（1 次 Spring 启动 + 1 次基类重置；同一上下文内的第二个测试类不再出现迁移日志）
7. 全量运行后端测试，量化耗时

**回滚策略**：改动仅涉及测试基类与 5 个测试类，不产出部署物。`git revert` 即恢复逐用例 `flyway.clean()+migrate()`，无需数据迁移。

## Open Questions

- 无阻塞项。此前三项待定项均已确定：
  - `RoleFixture.defaultRoleId` 硬编码角色 ID → **采纳修正**：删除 `defaultRoleId`，`UserFixture.withRoleType` 改为读取迁移后注入的映射（决策 8）
  - 是否把「迁移执行次数等于测试类数」固化为断言 → **该不变量本身是错的**，正确表述为「每个 `DataSource` 刚好 2 次迁移」（决策 5）
  - `PermissionScanner` 的 `@Order(100)` → **已由代码侧移除**，本变更无需处理
- 后续可优化项（不阻塞本变更）：实测单用例清理开销中 **TRUNCATE 占约 78%（111ms / 142ms）**，种子回填仅 27ms。若需进一步提速，可评估「按用例只清空实际被写入的表」或去掉 `CASCADE`（全库仅 4 个外键且均带 `ON DELETE CASCADE`，多表 TRUNCATE 本身已能覆盖）。收益上限约为再省 0.1s/用例。
