# backend-test-base-layering Specification

## Purpose

约定后端集成测试基类的分层规则：按「被测代码是否访问真实数据库」选择基类，而非按测试类名是否包含 `IntegrationTest`。接口层基类跳过 Flyway 迁移以消除无意义的 schema 重建开销，数据库层基类保留完整迁移以保证持久化断言与迁移种子数据可用。

本能力由变更 `split-test-base-classes` 引入，起因是接口层测试长期为数据库开销付费（每个用例执行 2 次 `flyway.clean()` 加 1 次 28 个迁移脚本），叠加 131 个集成测试类被 surefire 排除，导致集成测试始终无法接入 CI。
## Requirements
### Requirement: 测试基类按数据库依赖分层选择

后端测试类 SHALL 依据「被测代码是否访问真实数据库」选择基类，而不是依据测试类名是否包含 `IntegrationTest`：

- 不访问真实数据库的测试（API 层契约测试、纯 JSON/转换逻辑测试）MUST 继承 `APIIntegrationTest`，或作为纯单元测试不继承任何 Spring 上下文基类
- 访问真实数据库的测试（Application Service 编排、RepositoryImpl 持久化）MUST 继承 `DBIntegrationTest`

基类命名 MUST 直接表达其分层职责：`APIIntegrationTest` 表示接口层测试，`DBIntegrationTest` 表示需要真实数据库的测试。

#### Scenario: 接口层测试类选择 API 层基类
- **WHEN** 一个测试类使用 `@MockitoBean` 替换 `XxxAppService` 与 `RequestConverter`/`ResponseConverter`，且不引用任何 `Mapper` 或 `Repository`
- **THEN** 该测试类 SHALL 继承 `APIIntegrationTest`
- **THEN** 该测试类 SHALL NOT 继承 `DBIntegrationTest`

#### Scenario: 持久化测试类选择 DB 层基类
- **WHEN** 一个测试类通过 `@Autowired` 注入真实 `Repository` 或 `Mapper`，或断言数据库中的持久化结果
- **THEN** 该测试类 SHALL 继承 `DBIntegrationTest`

#### Scenario: 纯单元测试不继承集成测试基类
- **WHEN** 一个测试类只依赖 `ObjectMapper` 等普通 Spring Bean 或 `new` 出来的对象，不访问数据库、不发起 HTTP 请求
- **THEN** 该测试类 SHALL NOT 继承 `APIIntegrationTest` 或 `DBIntegrationTest`
- **THEN** 该测试类 SHALL 以纯 JUnit 5 方式编写，或在确实需要 `ObjectMapper` 时仅使用 `@ExtendWith` 等轻量方式获取

#### Scenario: 基类重命名后不存在旧名称引用
- **WHEN** 重命名完成后的代码库被检索
- **THEN** `src/test` 目录下 SHALL NOT 存在 `BaseIntegrationTest` 的引用
- **THEN** `src/test` 目录下 SHALL NOT 存在 `BaseWebTest` 的引用

### Requirement: API 层基类不执行数据库迁移

`APIIntegrationTest` SHALL 为接口层测试提供 Spring Web 上下文，但 MUST NOT 执行 Flyway 数据库迁移。理由是接口层测试已 mock 应用层，不产生任何 SQL，迁移所建立的 schema 对被测逻辑无意义。

由于迁移被跳过，数据库 schema 为空，`APIIntegrationTest` MUST 同时替换掉启动期访问数据库的组件，否则 Spring 上下文无法启动。启动期访问数据库的组件为：

- `MessageTemplateRegistry`（`@PostConstruct` 查询消息模板表）
- `SystemUserInitializer`（`CommandLineRunner` 访问用户表）

`APIIntegrationTest` MUST 保留 Testcontainers 提供的 `DataSource`，以保证全部 `RepositoryImpl` Bean 能被正常构造（`@MockitoBean` 只替换单个 Bean，不阻止其他 `RepositoryImpl` 被实例化）。

该清单 SHALL 随生产代码变化而维护：任何新增的、在 Spring 上下文启动阶段访问数据库的 `@Component` 都必须补入清单；任何被删除或改造为不再于启动期访问数据库的组件都必须从清单中移除。

#### Scenario: 接口层测试不触发数据库迁移
- **WHEN** 运行任意继承 `APIIntegrationTest` 的测试类
- **THEN** 测试日志中 SHALL NOT 出现 `Successfully applied 28 migrations`
- **THEN** 测试日志中 SHALL NOT 出现 `Successfully cleaned schema`

#### Scenario: 接口层测试仍然可以断言 HTTP 契约
- **WHEN** 一个继承 `APIIntegrationTest` 的 Controller 测试通过 `MockMvc` 发起请求并断言状态码、`ResponseMessage` 结构与权限校验结果
- **THEN** 断言 SHALL 全部通过
- **THEN** 权限断言 SHALL 基于 `@WithSecurityPrincipal` 声明的 `permissions` 生效，而不依赖数据库中的权限记录

#### Scenario: 跳过迁移后启动期组件不会导致上下文失败
- **WHEN** Spring 上下文在 `spring.flyway.enabled=false` 且 schema 为空的条件下启动
- **THEN** `MessageTemplateRegistry`、`SystemUserInitializer` SHALL 已被测试替身替换
- **THEN** Spring 上下文 SHALL 启动成功

#### Scenario: 缺少启动期组件替身时上下文启动失败
- **WHEN** 一个继承 `APIIntegrationTest` 的测试类移除了启动期查库组件的替身
- **THEN** Spring 上下文 SHALL 启动失败并报告无法获取数据库连接或表不存在

#### Scenario: 启动期组件清单已移除不再查库的组件
- **WHEN** 检查 `APIIntegrationTest` 中声明的测试替身
- **THEN** 清单中 SHALL NOT 包含 `PermissionCache`
- **THEN** 该断言 SHALL 在权限数据改为认证时直查持久层之后持续成立

### Requirement: DB 层测试基类保留完整迁移与隔离

`DBIntegrationTest` SHALL 为需要真实数据库的测试提供真实 PostgreSQL 实例与完整 Flyway 迁移，并 MUST 保证用例之间数据互不污染。

迁移 MUST 在每个数据源上恰好执行一次，且 MUST NOT 在每个测试方法中重复执行。多个测试类共享同一 Spring 上下文时共享同一数据源，此时迁移 MUST 只发生一次——迁移次数等于不同数据源的数量，MUST NOT 假定其等于测试类数。`DBIntegrationTest` MUST NOT 关闭 Flyway，原因是 Application Service 与 RepositoryImpl 测试断言依赖真实 schema 与迁移插入的种子数据。

用例之间的数据隔离 SHALL 通过清空数据实现（`TRUNCATE ... RESTART IDENTITY CASCADE`），MUST NOT 通过逐用例重建 schema 实现——表结构在每个用例中完全相同，drop 并重建 schema 是达成数据隔离最重的方式。

#### Scenario: DB 层测试执行完整迁移
- **WHEN** 运行任意继承 `DBIntegrationTest` 的测试类
- **THEN** 数据库 SHALL 已应用全部 Flyway 迁移脚本
- **THEN** 迁移插入的种子数据 SHALL 存在

#### Scenario: 迁移在每个数据源上只执行一次
- **WHEN** 运行一个包含多个测试方法的 `DBIntegrationTest` 子类
- **THEN** 该测试类对应的数据库 SHALL 只执行一次 Flyway 迁移
- **THEN** 该测试类日志中的迁移执行次数 SHALL 至多为 1，而不等于测试方法数

#### Scenario: 共享数据源的测试类不重复迁移
- **WHEN** 两个 `DBIntegrationTest` 子类共享同一 Spring 上下文与同一数据源并先后执行
- **THEN** 该数据源 SHALL 只执行一次 Flyway 迁移
- **THEN** 第二个测试类 SHALL 能读到迁移插入的种子数据

#### Scenario: DB 层测试用例之间数据隔离
- **WHEN** 同一个 `DBIntegrationTest` 子类中先后执行两个会写入数据的用例
- **THEN** 第二个用例开始前 SHALL 无法读取到第一个用例写入的数据

### Requirement: 接口层测试依赖边界

继承 `APIIntegrationTest` 的测试类 MUST NOT 依赖真实的应用层实现、仓储层实现或外部基础设施。其目的是使接口层测试只验证 HTTP 契约、权限注解生效、参数校验与统一响应格式。

#### Scenario: 接口层测试不注入真实仓储
- **WHEN** 检查所有继承 `APIIntegrationTest` 的测试类的导入与注入声明
- **THEN** 这些类 SHALL NOT 出现 `com.bluenet.web.infrastructure.repository.*Mapper` 的导入
- **THEN** 这些类 SHALL NOT 出现 `com.bluenet.web.domain.*Repository` 的导入

#### Scenario: 接口层测试替换应用层协作者
- **WHEN** 检查所有继承 `APIIntegrationTest` 的测试类
- **THEN** 每个类 SHALL 使用 `@MockitoBean` 替换其被测 Controller 所依赖的 `XxxAppService`
- **THEN** 每个类 SHALL 使用 `@MockitoBean` 替换该 Controller 使用的 `RequestConverter` 与 `ResponseConverter`

#### Scenario: 安全上下文在用例之间清理
- **WHEN** 一个继承 `APIIntegrationTest` 的测试用例执行完毕
- **THEN** 测试基类 SHALL 调用 `UserCTX.clear()` 清理安全上下文
- **THEN** 后续用例 SHALL NOT 读取到前一个用例残留的用户身份或权限

### Requirement: 角色必须按名称解析，禁止硬编码角色 ID

测试 MUST NOT 硬编码角色 ID。`tb_role.id` 由 `SERIAL` 生成，而迁移 V1 的插入语句只提供角色名称（`INSERT INTO tb_role (name) VALUES (...)`），因此角色 ID 在原理上不可假定为 1–4；生产代码也不按 ID 查找角色（`RoleMapper.selectByName` 是唯一查询入口）。测试 MUST 通过角色名称解析角色 ID（`RoleFixture.roleId(roleMapper, RoleType.X)` 内部即 `selectByName`，或直接使用 `RoleRepository.findByName`）。

测试夹具 MUST NOT 提供硬编码角色 ID 的路径：`RoleFixture` MUST NOT 保留 `defaultRoleId(RoleType)`。`UserFixture.Builder.withRoleType(RoleType)` 与依赖它的 `member()` / `candidate()` / `directionAdmin()` / `superAdmin()` MAY 保留，但 MUST 从迁移后注入的「角色名 → 真实 ID」映射解析，MUST NOT 回退到硬编码值；未注入映射时（典型为纯单元测试）MUST 抛出明确异常，提示改用 `withRoleId(Long)`。该映射由 `DBIntegrationTest` 在迁移完成后建立并在每个用例结束时清除。

#### Scenario: 集成测试通过名称解析角色
- **WHEN** 一个 `DBIntegrationTest` 子类需要某个角色的 ID
- **THEN** 它 SHALL 通过角色名称查询获得该 ID
- **THEN** 它 SHALL NOT 假设该 ID 等于 1、2、3 或 4

#### Scenario: 测试夹具不提供硬编码角色 ID
- **WHEN** 检索 `src/test` 中的测试夹具
- **THEN** `RoleFixture` SHALL NOT 存在 `defaultRoleId(RoleType)` 方法
- **THEN** `UserFixture.Builder.withRoleType` SHALL 从注入映射解析角色 ID，且 `src/test` 中 SHALL NOT 存在硬编码 1–4 作为角色 ID 的字面量

#### Scenario: 未注入映射时夹具拒绝解析角色
- **WHEN** 一个不访问数据库的纯单元测试调用 `UserFixture.member(studentId)`
- **THEN** 该调用 SHALL 抛出明确异常，提示改用 `withRoleId(Long)` 显式指定

#### Scenario: 按名称解析得到的角色可被按 ID 取回
- **WHEN** 通过角色名称查询得到角色 ID，再按该 ID 调用 `RoleRepository.findById`
- **THEN** 取回的 SHALL 是同一个角色，且其名称与查询时一致

### Requirement: 只读参考表不参与数据清空且受运行时保护

`tb_role` SHALL 被定位为只读参考表：其内容由数据库迁移写入，此后 MUST NOT 被任何运行期代码或测试修改。生产代码 SHALL NOT 提供创建或修改角色的能力。

`DBIntegrationTest` MUST NOT 把 `tb_role` 纳入清空范围。原因是清空后无法在不重复迁移的前提下恢复其内容，而迁移按数据源只执行一次。

为守住只读性，`DBIntegrationTest` MUST 在迁移完成后快照 `tb_role` 的内容，并在每个测试方法结束时比对快照与当前内容。不一致时 MUST 判定该测试方法失败，并明确提示「测试写入了只读参考表」，而非把污染留到后续用例造成难以定位的失败。

#### Scenario: 清空范围排除只读参考表
- **WHEN** 一个 `DBIntegrationTest` 子类的用例执行完毕并触发数据清空
- **THEN** `tb_role` SHALL NOT 被清空
- **THEN** `tb_role` SHALL 仍包含 SUPER_ADMIN、DIRECTION_ADMIN、MEMBER、CANDIDATE 四个角色

#### Scenario: 只读参考表的内容在迁移后保持稳定
- **WHEN** 依次执行同一个 `DBIntegrationTest` 子类的多个用例
- **THEN** 每个用例开始时 `tb_role` 的内容 SHALL 与迁移完成时快照的内容一致

#### Scenario: 测试写入只读参考表时立即失败
- **WHEN** 某个测试方法向 `tb_role` 插入、更新或删除数据
- **THEN** 该测试方法 SHALL 失败
- **THEN** 失败信息 SHALL 明确指出有测试写入了只读参考表
- **THEN** 该写入 SHALL NOT 泄漏到后续用例

#### Scenario: 角色名查询始终可用
- **WHEN** 任意 `DBIntegrationTest` 子类通过 `RoleFixture.roleId(roleMapper, RoleType.MEMBER)` 获取角色 ID
- **THEN** 查询 SHALL 返回非空结果

### Requirement: 可变种子表通过迁移快照恢复

`tb_direction_learning_step` 在生产中可被创建（`LearningPathAppService.createStep`），属于可变表，因此 MUST 参与数据清空。其种子数据（迁移 V1 写入的 12 行）MUST 在每个用例清空后被恢复。

恢复方式 SHALL 为：在迁移完成后对该表的内容做内存快照，清空后按快照逐行回填，并修正自增序列。种子数据 MUST NOT 在测试代码中硬编码——迁移会改写种子数据（V26 更新标题、V28 写入 `sort_order`），硬编码会产生静默漂移且漂移无告警。

#### Scenario: 可变种子表被清空后恢复
- **WHEN** 一个用例向 `tb_direction_learning_step` 写入数据，随后执行下一个用例
- **THEN** 下一个用例 SHALL 无法读取到前一个用例写入的数据
- **THEN** 该表中 `computer_vision` 方向的行数 SHALL 为 4，且标题与 `sort_order` SHALL 为迁移演进后的最终值

#### Scenario: 依赖种子数据的既有断言保持通过
- **WHEN** `LearningPathAppServiceImplIntegrationTest` 的用例自建 2 行后查询该方向的学习路径
- **THEN** 结果 SHALL 为 6 行（4 行种子 + 2 行自建）

#### Scenario: 回填后自增序列可用
- **WHEN** 一个用例在种子数据回填后向该表插入新行
- **THEN** 插入 SHALL 成功且不产生主键冲突

