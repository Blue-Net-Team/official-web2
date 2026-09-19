## Why

DB 层测试的清理策略是 `flyway.clean() + flyway.migrate()`，挂在 `DBIntegrationTest` 的 `@BeforeEach` 与 `@AfterEach` 上，即**每个测试方法都 drop 并重建整个 schema**（43 张表、28 个迁移脚本）。

实测开销：`flyway.clean()` ≈ 0.25s（每用例 2 次）、`flyway.migrate()` ≈ 1.10s（每用例 1 次），合计 **约 1.60s/用例**。上一变更把接口层测试从迁移中解放后，全量本地测试仍有 47 分钟，其中执行了 **766 次迁移**；DB 层约 700 个用例，仅清理开销就占 **约 19 分钟**。

DB 层保留真实数据库本身是**正当的设计**：41 个 AppService 中 30 个带 `@Transactional`，事务边界就是被测对象，把仓储 mock 掉会让 `@Transactional` 空转。所以优化方向不是减少数据库使用，而是**换一种更轻的清理方式**——表结构在每个用例中完全一样，只有数据不同。

## What Changes

- `DBIntegrationTest` 改为**每个测试类只迁移一次**（按 `DataSource` 实例身份判定），不再逐用例重建 schema
- 用例之间的数据隔离改为 `TRUNCATE ... RESTART IDENTITY CASCADE` 清空全部 `tb_` 表
- **`tb_role` 确立为只读参考表**：数据由迁移 V1 写入，此后不再变更。它 MUST NOT 参与清空，测试 MUST NOT 写入它。经核查生产代码从不写 `tb_role`——`RoleRepository` 接口没有 `save`，`RoleMapper` 的生产侧调用全是 `selectById` / `selectByName` / `selectBatchIds`，应用层与接口层都没有「创建角色」的入口，唯一写入方是迁移
- 为只读表增加**运行时保护**：迁移后快照 `tb_role` 内容，每个用例结束时比对；不一致即判定该用例违规写入并失败，而非留到后续用例造成难以定位的污染
- `tb_direction_learning_step` 在生产中**可写**（`LearningPathAppService.createStep`），因此必须清空；其 12 行种子数据通过**迁移后快照 + 清空后回填**恢复，并修正自增序列
- 修正 5 个受影响的测试类：3 个不再自造角色，4 个 mock 掉权限扫描器（详见 Impact）

本次变更**不包含**（明确非目标）：

- 不接入 `maven-failsafe-plugin`、不修改 `.github/workflows/ci.yml`
- 不修改 `src/main/java` 任何代码
- 不修改任何测试的断言与业务语义；不改动接口层 `APIIntegrationTest`
- **不把 App 层测试改为纯单元测试**。App 层保留真实数据库是正当的（事务边界见 Why），可转换的只有 7 个非事务类 / 54 个用例，收益约 2.7 分钟，不值得混入本变更
- 不与 `remove-permission-cache` 合并（后者是独立的功能修复）

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `backend-test-base-layering`: 修改「DB 层测试基类保留完整迁移与隔离」。迁移时机由「每个用例」改为「每个测试类一次」；数据隔离手段由 `flyway.clean()` 重建 schema 改为清空数据；新增 `tb_role` 只读参考表的定位与运行时保护；明确仅 `tb_direction_learning_step` 需要快照回填

## Impact

- 测试基类：`src/backend/src/test/java/com/bluenet/web/DBIntegrationTest.java`
- 受影响测试类 **5 个**：

| 测试类 | 层次 | 改动 | 原因 |
|---|---|---|---|
| `RolePermissionRepositoryImplIntegrationTest` | Repository | 9 处 `createRole("X")` → `RoleFixture.roleId(roleMapper, RoleType.Y)` | `tb_role` 只读 |
| `RolePermissionManageAppServiceImplIntegrationTest` | App | 12 处 `createRole("ROLE_TEST_*")` → 种子角色 | `tb_role` 只读 |
| `UserRepositoryImplIntegrationTest` | Repository | 1 处 `RoleDO.builder().name("TEST_ROLE")` → `RoleFixture.roleId(...)` | `tb_role` 只读 |
| `PermissionAppServiceImplIntegrationTest` | App | 加 `@Import(TestSecurityConfig.class)` | mock 权限扫描器 |
| `PermissionRepositoryImplIntegrationTest` | Repository | 加 `@Import(TestSecurityConfig.class)` | mock 权限扫描器 |

> `RolePermissionRepositoryImplIntegrationTest` 与 `PermissionRepositoryImplIntegrationTest` 不重叠，但 `RolePermissionManageAppServiceImplIntegrationTest` 与 `RolePermissionRepositoryImplIntegrationTest` 需要**同时做两件事**（既改角色来源、又 mock 扫描器）。

- 生产代码：无改动
- 依赖：无新增（使用已有的 `JdbcTemplate` 与 `Flyway`）
- 预期收益：DB 层清理开销由约 1.6s/用例降至毫秒级，全量本地测试由 47 分钟降至约 28 分钟。TRUNCATE 与快照回填的实际耗时将在实施时实测，不以估算代替验证
- 风险：`tb_role` 只读化依赖「测试不再写入它」。运行时保护是本决策的兜底机制

## 实测勘查结论（本变更的设计依据）

| 勘查项 | 结论 |
|---|---|
| 有种子数据的表 | 仅 `tb_role`（4 行）、`tb_direction_learning_step`（12 行），均来自 `V1__init_schema.sql` |
| `tb_role` 在生产中是否只读 | **是**。`RoleRepository` 无 `save`；`RoleMapper` 生产调用全是读；应用层与接口层无创建角色入口 |
| 种子数据是否被后续迁移改写 | 是。V26 改写 `tb_direction_learning_step` 12 行中 9 行的标题、V28 写入 `sort_order`，最终态 ≠ V1 初始态 |
| 测试是否写入种子表 | 是，共 3 个类 / 约 21 处 `roleMapper.insert(role)`，角色名如 `BATCH_ASSIGN_ROLE`、`ROLE_TEST_MEMBER`、`TEST_ROLE` |
| 外键约束 | 全库仅 4 个，集中于 judge 表并带 `ON DELETE CASCADE`，`TRUNCATE ... CASCADE` 可处理 |
| 表总数 | 43 张，全部以 `tb_` 开头 |
| `tb_direction_learning_step` 种子是否被依赖 | **是**。`LearningPathAppServiceImplIntegrationTest` 断言 `hasSize(6)`＝4 行种子＋2 行自建 |
| `tb_permission` 的内容来源 | 启动期 `PermissionScanner` 扫描 189 个 `@RequiresPermission` 后写入，**非迁移**。故其集成测试应 mock 扫描器，自行控制表内容 |
| 测试中硬编码角色 ID 的违规 | `RoleFixture.defaultRoleId` 硬编码 1–4，其 Javadoc 声明「仅用于不依赖数据库的单元测试」，但 `AdminUserAppServiceImplIntegrationTest` 与 `UserInfoAppServiceImplIntegrationTest` 两个集成测试在用它（见 design 待定项） |
