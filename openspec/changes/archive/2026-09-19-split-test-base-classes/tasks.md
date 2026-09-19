## 1. 基类重构

- [x] 1.1 新增 `src/test/java/com/bluenet/web/APIIntegrationTest.java`：注解组合 `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")` + `@Testcontainers` + `@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})`，并以 `@TestPropertySource(properties = "spring.flyway.enabled=false")` 关闭 Flyway
- [x] 1.2 在 `APIIntegrationTest` 中声明三个启动期查库组件的测试替身：`@MockitoBean PermissionCache`、`@MockitoBean MessageTemplateRegistry`、`@MockitoBean SystemUserInitializer`
- [x] 1.3 在 `APIIntegrationTest` 中提供 `@AfterEach` 调用 `UserCTX.clear()`，保证安全上下文在用例间清理
- [x] 1.4 为 `APIIntegrationTest` 补充 Javadoc，说明「为何保留 Testcontainers」「为何关闭 Flyway」「新增启动期查库组件时需同步补充替身」
- [x] 1.5 用单个 Controller 测试类（建议 `api/controller/v1/health/HealthControllerIntegrationTest`）临时继承 `APIIntegrationTest`，运行并确认：日志中无 `Successfully applied 28 migrations`、无 `Successfully cleaned schema`，且断言全部通过
- [x] 1.6 将 `src/test/java/com/bluenet/web/BaseIntegrationTest.java` 重命名为 `src/test/java/com/bluenet/web/DBIntegrationTest.java`，类名同步更新，并补充 Javadoc 说明其「真实数据库 + 完整迁移」语义
- [x] 1.7 全局替换 `extends BaseIntegrationTest` 为 `extends DBIntegrationTest`，同步修正各测试类的 import 语句
- [x] 1.8 执行 `./mvnw -B clean test-compile` 确认编译通过，且 `grep -rn "BaseIntegrationTest" src/test` 无残留引用

## 2. API 层测试类迁移

- [x] 2.1 迁移 `api/controller/v1/admin` 下的 31 个类（`*ControllerIntegrationTest` 与 `*ControllerTest`）改为继承 `APIIntegrationTest`
- [x] 2.2 迁移 `api/controller/v1/assessment` 下的 12 个类改为继承 `APIIntegrationTest`
- [x] 2.3 迁移 `api/controller/v1` 下其余 24 个类（`user` 3、`file` 2、`auth` 2、`algorithm` 2、`wps` 1、`venue` 1、`softwareresource` 1、`qrcode` 1、`member` 1、`learningpath` 1、`health` 1、`github` 1、`equipment` 1、`enrollment` 1、`enrollform` 1、`competition` 1、`college` 1、`bugreport` 1、`achievement` 1）改为继承 `APIIntegrationTest`
- [x] 2.4 核查迁移后的 67 个 API 层测试类：无 `infrastructure.repository.*Mapper` 与 `domain.*Repository` 的导入（`com.fasterxml.jackson.databind.ObjectMapper` 不在此列），且均以 `@MockitoBean` 替换被测 Controller 的 `XxxAppService` 与 `RequestConverter`/`ResponseConverter`
- [x] 2.5 运行 `api/controller/v1/admin` 与 `api/controller/v1/assessment` 两批测试，确认失败数与迁移前一致，且日志中无数据库迁移记录
- [x] 2.6 运行全部 API 层测试（`-Dtest='*ControllerIntegrationTest,*ControllerTest' -Dsurefire.excludes=`），确认失败数与迁移前一致，并记录总耗时以与迁移前对比

## 3. DB 层测试类迁移

- [x] 3.1 确认 78 个 DB 层测试类（77 个非 Controller `*IntegrationTest` + `MessageTemplateRegistryPersistenceTest`）已完成 `extends DBIntegrationTest` 的替换，且其 Flyway 迁移与 `@BeforeEach` 数据清理行为保持不变
- [x] 3.2 运行 `application/service/impl` 与 `infrastructure/repository/impl` 两个包的测试，确认失败数与迁移前一致，且迁移仍被正常执行（日志中出现 `Successfully applied 28 migrations`）
- [x] 3.3 抽样验证 `@WithSecurityPrincipal` 相关的角色查询仍可用：确认测试中 `roleMapper.selectByName("MEMBER")` 能取到角色，证明 `DBIntegrationTest` 的迁移与种子数据未被破坏

## 4. 纯单元测试修正

- [x] 4.1 移除 `src/test/java/com/bluenet/web/domain/model/vo/QuestionContentJsonTest.java` 的 `extends BaseIntegrationTest`（连同相关基类 import）
- [x] 4.2 将 `QuestionContentJsonTest` 改为不启动 Spring 上下文的纯 JUnit 5 测试：`ObjectMapper` 实例直接 `new` 或通过 `@ExtendWith` 轻量获取，不依赖 `@SpringBootTest` / `@Autowired`
- [x] 4.3 运行 `QuestionContentJsonTest`，确认 3 个用例全部通过且耗时降至毫秒级（不再出现容器启动日志）

## 5. 文档更新

- [x] 5.1 更新 `docs/03-开发指南/03-08-测试规范手册.md` §2.1 Controller 测试契约：将「测试类继承 `BaseIntegrationTest`」改为「测试类继承 `APIIntegrationTest`」，消除与「禁止使用真实 Repository」的矛盾
- [x] 5.2 更新同文档 §2.2 Application 集成测试契约、§2.4 RepositoryImpl 集成测试契约中的基类引用为 `DBIntegrationTest`
- [x] 5.3 在 §2 开头新增一小节「测试基类选用规则」，写明判定标准（是否访问真实数据库）、两类基类的开销差异、以及「新增启动期查库组件时需在 `APIIntegrationTest` 补充替身」的维护约定
- [x] 5.4 检查 `CLAUDE.md` 与其它文档中是否存在 `BaseIntegrationTest` 引用，如有则一并更新

## 6. 验证与收尾

- [x] 6.1 执行 `./mvnw -B clean test-compile`，确认全量编译通过
- [x] 6.2 检索确认无残留旧名称：`grep -rn "BaseIntegrationTest\|BaseWebTest" src/ docs/ CLAUDE.md` 无命中
- [x] 6.3 确认 surefire 收集的测试类数量与改动前一致（`pom.xml` 的 `**/*IntegrationTest.java` 排除规则仍覆盖率 `DBIntegrationTest` 子类，故 `*IntegrationTest` 仍不进 CI；`*ControllerTest` 与 `QuestionContentJsonTest` 仍在 CI 中运行但已加速）
- [x] 6.4 运行全量后端测试 `./mvnw -B test`，对比改动前后的用例总数与失败数，确认无新增失败
- [x] 6.5 在本地运行全量测试（含被 surefire 排除的集成测试），记录改动前后的总耗时，量化接口层迁移的实际收益
- [x] 6.6 更新 `proposal.md` 的预期收益段落，用实测数字替换「预计节省约 20 分钟」的估算
