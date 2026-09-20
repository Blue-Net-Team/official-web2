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

`DBIntegrationTest` MUST NOT 关闭 Flyway，原因是 Application Service 与 RepositoryImpl 测试断言依赖真实 schema 与迁移插入的种子数据（如 `tb_role` 中的角色记录）。

#### Scenario: DB 层测试执行完整迁移
- **WHEN** 运行任意继承 `DBIntegrationTest` 的测试类
- **THEN** 数据库 SHALL 已应用全部 Flyway 迁移脚本
- **THEN** 迁移插入的种子数据（`tb_role`、`tb_direction_learning_step`）SHALL 存在

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

