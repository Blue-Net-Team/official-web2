## MODIFIED Requirements

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
