## Context

后端测试基类现状：`src/test/java/com/bluenet/web/BaseIntegrationTest.java` 是唯一的集成测试基类，被 146 个测试类继承。它的定义为：

```java
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)     // PostgreSQL + Redis + MinIO
public abstract class BaseIntegrationTest {
    @Autowired private Flyway flyway;
    @BeforeEach void setUp()    { flyway.clean(); flyway.migrate(); }   // 43 张表重建 + 28 个迁移
    @AfterEach  void tearDown() { flyway.clean(); }
}
```

继承它的 146 个类按实际依赖分为三类（已逐一核查导入与注入声明）：

| 层次 | 类数 | 用例数 | 是否访问真实数据库 | 现状 |
|------|------|--------|--------------------|------|
| API 层：`*ControllerIntegrationTest` | 54 | 345 | 否（`@MockitoBean` 替换 AppService） | 支付 3 容器 + 每用例 2 次迁移 |
| API 层：`*ControllerTest` | 13 | 61 | 否 | 同上，且**在 CI 中运行** |
| DB 层：其他 `*IntegrationTest` | 77 | 726 | 是 | 合理 |
| DB 层：`MessageTemplateRegistryPersistenceTest` | 1 | — | 是 | 合理，且在 CI 中运行 |
| 纯单元测试：`QuestionContentJsonTest` | 1 | 3 | 否（仅用 `ObjectMapper`） | 支付 3 容器，且**在 CI 中运行** |

实测开销（同一 JVM 内，上下文启动耗时已从 surefire 报告的类耗时中扣除）：

```
CollegeRepositoryImplIntegrationTest   9 tests  44.5s   上下文 18.6s → 2.88s/用例
HealthControllerIntegrationTest        2 tests  29.2s   上下文 21.8s → 3.70s/用例
```

每用例约 3 秒中，`flyway.clean()` 约 0.25s、`flyway.migrate()` 约 1.1s，其余为用例自身的 Spring 上下文内开销。这 3 秒对 406 个 API 层用例完全无用。

约束：

- `@MockitoBean` 只替换单个 Bean，**不阻止**其他 `@Component` / `@Repository` 被实例化。因此即使 mock 掉 AppService，54 个 Controller 与 43 个 `RepositoryImpl` 仍会被构造，构造需要 `SqlSessionFactory` → `DataSource`。
- 启动期存在三个主动访问数据库的组件：`PermissionCache`（`@PostConstruct`）、`MessageTemplateRegistry`（`@PostConstruct`）、`SystemUserInitializer`（`CommandLineRunner`）。
- `src/backend/pom.xml` 的 surefire 配置排除了 `**/*IntegrationTest.java`，131 个集成测试类在 CI 中从未运行。将集成测试接回 CI 是后续独立变更，但必须先完成本变更以控制耗时。

## Goals / Non-Goals

**Goals:**

- 新增接口层专用基类 `APIIntegrationTest`，使接口层测试不再执行 Flyway 迁移
- 将 `BaseIntegrationTest` 重命名为 `DBIntegrationTest`，使基类名称表达分层语义
- 将 146 个既存测试类按实际数据库依赖重新归类
- 消除 `docs/03-开发指南/03-08-测试规范手册.md` §2.1 中「继承带 DB 的基类」与「禁止使用真实 Repository」的自相矛盾

**Non-Goals:**

- 不接入 `maven-failsafe-plugin`、不修改 `.github/workflows/ci.yml`（后续独立变更）
- 不将 DB 层清理策略改为 TRUNCATE（后续独立优化，涉及 `tb_role` 种子数据保留）
- 不修改 `src/main/java` 任何代码
- 不改变 CI 当前的测试覆盖范围。surefire 按类名 `**/*IntegrationTest.java` 排除，54 个 `*ControllerIntegrationTest` 与 77 个非 Controller `*IntegrationTest` 仍然不进 CI；本变更只降低它们在本地的耗时，并顺带加速 CI 中已有的 13 个 `*ControllerTest` 与 `QuestionContentJsonTest`

## Decisions

### 决策 1：保留 Testcontainers，只关闭 Flyway

`APIIntegrationTest` 保留 `@Testcontainers` 与 `TestcontainersConfiguration`，仅通过 `spring.flyway.enabled=false` 跳过迁移，并替换启动期查库组件。

**为什么**：`@SpringBootTest` 会实例化全部 43 个 `RepositoryImpl`，每个都需要 `SqlSessionFactory` → `DataSource`。若移除容器，上下文无法启动。

**已实测的三个备选方案（探针代码已删除，结论保留）：**

| 方案 | 结果 | 结论 |
|------|------|------|
| 排除 `DataSourceAutoConfiguration` + `MybatisPlusAutoConfiguration` | 上下文启动失败：`AchievementRepositoryImpl → No qualifying bean 'AchievementMapper'` | 不可行 |
| 假 `DataSource`（不可达 URL + `initialization-fail-timeout=-1`）+ 关 Flyway | 上下文启动 **30.6s**，慢于现方案 21.8s | 不可行。JDBC 连接重试阻塞的成本高于起容器（镜像已缓存） |
| `@WebMvcTest(controllers = X.class)` 切片 | 需依次补齐 `CsrfTokenFilter → CsrfTokenService → CookieProperties`、`JwtAuthenticationFilter → JwtUtil / AuthTokenService / UserRepository / FailAuthEntryPoint / CookieProperties / PermissionCache / RoleTypeResolver`，依赖链最终回到 `DataSource` | 不可行。要么为每个类维护 6+ 个环境性 mock，要么仍需 `DataSource` |
| **保留容器 + 关 Flyway（采纳）** | 上下文启动 13.8s（温态，同 JVM 背靠背对比），迁移执行 0 次，断言全部通过 | 采纳 |

选择依据：容器镜像本地已缓存，起容器成本（数秒）低于任何「假装有数据库」方案的额外成本。

### 决策 2：基类命名为 `APIIntegrationTest` / `DBIntegrationTest`

**为什么**：两个名字直接表达分层职责。`APIIntegrationTest` 说明「接口层的集成测试」，`DBIntegrationTest` 说明「需要真实数据库的集成测试」，避免了 `BaseIntegrationTest` 那种「看起来是所有集成测试的基类」的歧义。

**与 surefire 排除规则的关系**：`src/backend/pom.xml` 排除的是 `**/*IntegrationTest.java`。两个新基类名都以 `IntegrationTest` 结尾，但基类是抽象类，本就不该被 surefire 收集，因此无影响。真正决定 CI 覆盖范围的是**具体测试类的类名**，本变更不改类名，故 CI 覆盖范围不变。

### 决策 3：在基类中以 `@MockitoBean` 替换启动期查库组件

`APIIntegrationTest` 在类级别声明：

```java
@MockitoBean private PermissionCache permissionCache;              // @PostConstruct 查权限表
@MockitoBean private MessageTemplateRegistry messageTemplateRegistry; // @PostConstruct 查模板表
@MockitoBean private SystemUserInitializer systemUserInitializer;   // CommandLineRunner 查用户表
```

**为什么用测试替身而不是改 production**：将 `PermissionCache` / `MessageTemplateRegistry` 的 `@PostConstruct` 改为懒加载虽然能同时改善生产环境的启动健壮性（数据库短暂不可用时不至于启动失败），但属于生产行为变更，需要重新定义「权限缓存加载失败」的语义（fail-closed 还是 fail-open），风险与本次变更不同量级，故拆分为独立评估。

**代价**：`src/main/java` 每新增一个启动期访问数据库的组件，`APIIntegrationTest` 就需要补一个替身；否则上下文启动失败（失败是显式的、可定位的，不会静默）。

### 决策 4：直接重命名，不做双基类过渡

一次性将 `BaseIntegrationTest` 重命名为 `DBIntegrationTest`，并同步修改全部继承声明，不保留旧基类。

**为什么**：保留旧名会让「新代码该继承哪个」长期含混，且 `BaseIntegrationTest` 与 `DBIntegrationTest` 语义重复。由于改动全部位于 `src/test`，不产出部署物，回滚成本仅为 `git revert`，因此不需要渐进过渡。

### 决策 5：`QuestionContentJsonTest` 降为纯单元测试

该类只使用 `ObjectMapper` 做 JSON 往返断言，移除 `extends BaseIntegrationTest`，改为不启动 Spring 上下文的纯 JUnit 测试。

**为什么**：它同时在 CI 中运行（类名以 `Test` 结尾不被排除），当前每跑一次要起 3 个容器 + 执行迁移。降为单元测试后其在 CI 中的耗时从数十秒降到毫秒级。

## Risks / Trade-offs

- **`APIIntegrationTest` 下写入被误用为 DB 断言**：迁移已关闭、schema 为空，若有人在 API 层测试里加数据库断言，测试会失败并报「表不存在」。→ 这是**期望的失败模式**：失败信息明确指向分层错误，且 `backend-test-base-layering` spec 已规定 API 层不得注入 Repository/Mapper。
- **`@MockitoBean` 数量影响 Spring 上下文缓存**：基类上新增 3 个替身会改变 context cache key，子类再叠加各自的替身会让 54 个 `*ControllerIntegrationTest` 各持有独立上下文，无法复用。→ 本变更不引入新的上下文数量（这些测试类原本就因各自 mock 不同 AppService 而无法复用），但需在验证时对比改动前后总耗时以确认无退化。
- **67 个类批量改动可能出现漏改或错改**：→ 分批进行，每批改完后以「全量 `mvn test` 编译通过 + 抽样类实际运行通过」验证；最终以全量测试运行核对失败数与改动前一致。
- **重命名遗漏引用**：`BaseIntegrationTest` 命名也匹配 pom 中的 surefire 排除规则，重命名后需确认 `pom.xml` 的 `<exclude>**/*IntegrationTest.java</exclude>` 仍能覆盖率 `DBIntegrationTest` 的子类。→ 两个新基类名均以 `IntegrationTest` 结尾，具体子类名未变，排除规则继续生效；验证步骤中包含「确认改动前后被 surefire 收集的测试类数量一致」。

## Migration Plan

1. 新增 `APIIntegrationTest`，先用单个 Controller 测试类验证：迁移执行 0 次、断言全部通过、上下文启动成功
2. 将 `BaseIntegrationTest` 重命名为 `DBIntegrationTest`，同步修改全部继承声明，编译通过
3. 将其余接口层测试类迁移到 `APIIntegrationTest`，DB 层测试类保持继承 `DBIntegrationTest`
4. `QuestionContentJsonTest` 移除基类继承
5. 更新 `docs/03-开发指南/03-08-测试规范手册.md`
6. 全量运行后端测试，对比改动前后的用例总数与失败数

**回滚策略**：全部改动位于 `src/test` 与文档，不产出部署物。如需回滚，`git revert` 即可回到单一 `BaseIntegrationTest` 的状态；无需数据迁移或服务重启。

## Open Questions

- 本次变更后是否立即推进「接入 `maven-failsafe-plugin` 让集成测试在 CI 运行」？该变更依赖本次落地，但会显著增加 CI 时长（即便迁移已跳过，131 个集成测试类仍各自启动含 Testcontainers 的 Spring 上下文），需要先测量全量集成测试在跳过迁移后的实际耗时再决定运行时机（PR / main / nightly）。
- DB 层清理策略是否改为 TRUNCATE？可再省可观时间，但需先确定 `tb_role` 与 `tb_direction_learning_step` 的种子数据处理方式（排除不清理 vs 清理后重插种子）。倾向「清理后重插」，语义更干净，但需在测试侧维护一份种子数据。
