## Why

DB 层测试的清理策略是 `flyway.clean() + flyway.migrate()`，挂在 `DBIntegrationTest` 的 `@BeforeEach` 与 `@AfterEach` 上，即**每个测试方法都 drop 并重建整个 schema**（43 张表、28 个迁移脚本）。

实测开销：`flyway.clean()` ≈ 0.25s（每用例 2 次）、`flyway.migrate()` ≈ 1.10s（每用例 1 次），合计 **约 1.60s/用例**。上一变更把接口层测试从迁移中解放后，全量本地测试仍有 47 分钟，其中执行了 **766 次迁移**；DB 层约 700 个用例，仅清理开销就占 **约 19 分钟**。

DB 层保留真实数据库本身是**正当的设计**：41 个 AppService 中 30 个带 `@Transactional`，事务边界就是被测对象，把仓储 mock 掉会让 `@Transactional` 空转。所以优化方向不是减少数据库使用，而是**换一种更轻的清理方式**——表结构在每个用例中完全一样，只有数据不同。

## What Changes

- `DBIntegrationTest` 改为**每个数据源只迁移一次**（按 `DataSource` 实例身份判定，共享同一 Spring 上下文的测试类共享数据源），不再逐用例重建 schema
- 用例之间的数据隔离改为 `TRUNCATE ... RESTART IDENTITY CASCADE` 清空全部 `tb_` 表
- **`tb_role` 确立为只读参考表**：数据由迁移 V1 写入，此后不再变更。它 MUST NOT 参与清空，测试 MUST NOT 写入它。经核查生产代码从不写 `tb_role`——`RoleRepository` 接口没有 `save`，`RoleMapper` 的生产侧调用全是 `selectById` / `selectByName` / `selectBatchIds`，应用层与接口层都没有「创建角色」的入口，唯一写入方是迁移
- **角色引用一律按名称解析，删除硬编码角色 ID 的夹具**：`tb_role.id` 是 `SERIAL`，V1 只插入 `name`（`INSERT INTO tb_role (name) VALUES (...)`），因此角色 ID **在原理上就不可假定**为 1–4；业务代码也从不按 ID 查找角色（`RoleRepository` / `RoleMapper` 以 `name` 为查询条件）。测试 MUST 通过名称解析角色 ID。`RoleFixture.defaultRoleId(RoleType)`（硬编码 1–4）MUST 删除——其 Javadoc 声称「仅用于不依赖数据库的单元测试」，但实测 10 处调用**全部位于集成测试**，无任何合法调用方。`UserFixture` 的 `member()` / `candidate()` / `directionAdmin()` / `superAdmin()` 与 `withRoleType(RoleType)` 被约 30 个测试类调用 55 次，MUST 保留 API 但改为从「角色名 → 真实 ID」映射解析；该映射由 `DBIntegrationTest` 在迁移完成后注入，不访问数据库的纯单元测试改用 `withRoleId(Long)` 显式指定
- 为只读表增加**运行时保护**：迁移后快照 `tb_role` 内容，每个用例结束时比对；不一致即判定该用例违规写入并失败，而非留到后续用例造成难以定位的污染
- `tb_direction_learning_step` 在生产中**可写**（`LearningPathAppService.createStep`），因此必须清空；其 12 行种子数据通过**迁移后快照 + 清空后回填**恢复，并修正自增序列
- 修正 9 个受影响的测试类：3 个不再自造角色、4 个 mock 掉权限扫描器、4 个不再硬编码角色 ID（其中 2 个同时需要多项改动，详见 Impact）；同步收缩测试夹具 `RoleFixture` 与 `UserFixture`

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

- `backend-test-base-layering`: 修改「DB 层测试基类保留完整迁移与隔离」。迁移时机由「每个用例」改为「每个数据源一次」（同一上下文的多个测试类共享，迁移次数不等于测试类数）；数据隔离手段由 `flyway.clean()` 重建 schema 改为清空数据；新增 `tb_role` 只读参考表的定位与运行时保护；新增「角色必须按名称解析、禁止硬编码角色 ID」；明确仅 `tb_direction_learning_step` 需要快照回填

## Impact

- 测试基类：`src/backend/src/test/java/com/bluenet/web/DBIntegrationTest.java`
- 测试夹具：`RoleFixture.java`（删除 `defaultRoleId`，新增注入式「角色名 → 真实 ID」映射）、`UserFixture.java`（`withRoleType` 改为读取注入映射）、`DBIntegrationTest.java`（每个用例注入/清除映射）、`AssessmentAnswerDomainServiceImplTest.java`（纯单元测试改用显式 `withRoleId`）
- 受影响测试类 **9 个**：

| 测试类 | 层次 | 改动 | 原因 |
|---|---|---|---|
| `RolePermissionRepositoryImplIntegrationTest` | Repository | 9 处 `createRole("X")` → `RoleFixture.roleId(roleMapper, RoleType.Y)`；加 `@Import(TestSecurityConfig.class)` | `tb_role` 只读；mock 权限扫描器 |
| `RolePermissionManageAppServiceImplIntegrationTest` | App | 12 处 `createRole("ROLE_TEST_*")` → 种子角色；加 `@Import(TestSecurityConfig.class)` | `tb_role` 只读；mock 权限扫描器 |
| `UserRepositoryImplIntegrationTest` | Repository | 1 处 `RoleDO.builder().name("TEST_ROLE")` → `RoleFixture.roleId(...)` | `tb_role` 只读 |
| `PermissionAppServiceImplIntegrationTest` | App | 加 `@Import(TestSecurityConfig.class)` | mock 权限扫描器 |
| `PermissionRepositoryImplIntegrationTest` | Repository | 加 `@Import(TestSecurityConfig.class)` | mock 权限扫描器 |
| `AdminUserAppServiceImplIntegrationTest` | App | 3 处 `RoleFixture.defaultRoleId(...)` → `RoleFixture.roleId(roleMapper, ...)` | 角色 ID 不可假定 |
| `UserInfoAppServiceImplIntegrationTest` | App | 2 处 `RoleFixture.defaultRoleId(...)` → 按名称解析 | 角色 ID 不可假定 |
| `RoleRepositoryImplIntegrationTest` | Repository | 2 处 `RoleFixture.defaultRoleId(...)` → `roleRepository.findByName(...)` 得到的 ID | 角色 ID 不可假定 |
| `MemberRepositoryImplIntegrationTest` | Repository | 6 处 `withRoleType(...)` → `withRoleId(RoleFixture.roleId(roleMapper, ...))` | 角色 ID 不可假定 |

> `RolePermissionRepositoryImplIntegrationTest` 与 `RolePermissionManageAppServiceImplIntegrationTest` 需要**同时做两件事**（既改角色来源、又 mock 扫描器）；其余 7 个类只需一项改动。
>
> 删除 `RoleFixture.defaultRoleId` 会让编译器直接暴露所有遗留调用点；`UserFixture` 的 4 个工厂方法与 `withRoleType` 通过注入映射继续工作，因此约 30 个测试类调用点无需改动。

- 生产代码：无改动
- 依赖：无新增（使用已有的 `JdbcTemplate` 与 `Flyway`）
- 预期收益（已实测，见下）：DB 层清理开销从约 1.6s/用例降到约 0.14s/用例（约 11×）；全量后端测试从 47.3 分钟降到 **19.9 分钟**（约 2.4×）
- 实测收益（本机，2026-09-20）：
  - 单用例清理开销均值（13 个用例）：**TRUNCATE 111ms + 种子回填 27ms + 角色映射注入 1.7ms + 只读校验 2ms ≈ 142ms**；旧的 `clean()+migrate()+clean()` 约 1.60s/用例
  - 全量后端测试：**1917 用例 / 1194s（19.9 分钟）**，基线 1914 用例 / 2839s（47.3 分钟），提速 2.38×
  - `application/service/impl`：423 用例，425s（基线 1209s）；`infrastructure/repository/impl`：281 用例，135s（基线 684s）
  - 迁移次数：**766 → 40**（20 个不同 `DataSource` × 2），远小于用例数
- 新的主要开销是 TRUNCATE（占单用例清理开销约 78%），后续如需进一步提速可单独优化（见 design 待定项），不影响本变更的收益结论
- 预期迁移次数：由 766 次（＝用例数）降至 **2 × 不同 `DataSource` 实例的数量**（每个数据源 1 次 Spring 启动迁移 + 1 次基类重置迁移）。已实测：4 个共享同一上下文的测试类合计仅 2 次迁移、8 个类（4 个上下文）合计 8 次。共享同一上下文的测试类合计只迁移一次，因此迁移次数**不等于测试类数**，也不应以其等于类数作为验收断言
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
| `tb_role` 的 ID 是否确定 | **否**。V1 的插入语句只写 `name`，`id` 由 `SERIAL` 生成，不可假定为 1–4；业务代码按 `name` 查询，从不依赖 ID |
| 测试中硬编码角色 ID 的违规 | `RoleFixture.defaultRoleId` 硬编码 1–4，其 Javadoc 声明「仅用于不依赖数据库的单元测试」，但实测 **10 处调用全部在集成测试**：`AdminUserAppServiceImplIntegrationTest`(3)、`UserInfoAppServiceImplIntegrationTest`(2)、`RoleRepositoryImplIntegrationTest`(2)、`UserFixture.withRoleType`(1 定义 + 被 `MemberRepositoryImplIntegrationTest` 通过 6 处调用间接触发)。无任何纯单元测试在用 |
