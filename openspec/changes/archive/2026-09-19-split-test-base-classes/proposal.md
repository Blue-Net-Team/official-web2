## Why

后端测试基类只有 `BaseIntegrationTest` 一个，它被同时当作「API 层测试基类」和「DB 层测试基类」使用，导致接口层测试被迫承担完整数据库开销。

**问题一：接口层测试不碰数据库，却要为数据库付全款。**

54 个 `*ControllerIntegrationTest` + 13 个 `*ControllerTest`（合计 67 个类、406 个用例）全部 `extends BaseIntegrationTest`。这些测试已经用 `@MockitoBean` mock 掉了 `XxxAppService` 与 `RequestConverter`/`ResponseConverter`，实测**零个类引用 MyBatis Mapper 或 Repository**：

```
grep -rn "import com.bluenet.web.infrastructure.*Mapper|domain.*Repository" \
  $(find src/test -name "*ControllerIntegrationTest.java")
→ 0 处命中
```

但 `BaseIntegrationTest` 是：

```java
@SpringBootTest
@Testcontainers                                   // 起 PostgreSQL + Redis + MinIO
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {
    @BeforeEach void setUp()    { flyway.clean(); flyway.migrate(); }   // 43 张表重建
    @AfterEach  void tearDown() { flyway.clean(); }                     // 再来一次
}
```

即每个用例执行 **2 次 `flyway.clean()` + 1 次 28 个迁移脚本**。实测开销约 3 秒/用例：

```
CollegeRepositoryImplIntegrationTest   9 tests  44.5s   上下文 18.6s → 2.88s/用例
HealthControllerIntegrationTest        2 tests  29.2s   上下文 21.8s → 3.70s/用例
```

406 个接口层用例 × 3s ≈ **20 分钟纯浪费在删表建表上**，被测逻辑一条 SQL 都用不到。

**实施后实测**（同一批 12 个类 / 57 个用例的 A/B 对比，仅切换基类）：

```
APIIntegrationTest（跳过迁移）  124s   迁移 0 次
DBIntegrationTest（保留迁移）  265s   迁移 64 次
→ 每个用例节省 2.47s，提速 2.1 倍
```

390 个接口层用例 × 2.47s ≈ **节省 16 分钟**。全量本地测试由约 64 分钟降至 **47 分钟**（2839s，1914 个用例全部通过）。

**问题二：部分接口层测试连带拖慢 CI。**

13 个 `*ControllerTest`（61 个用例）与 `QuestionContentJsonTest` 名字以 `Test` 结尾，**不被 surefire 排除**，因此它们既在 CI 里跑、又带着 Testcontainers + Flyway 的成本。其中 `QuestionContentJsonTest` 只用了 `ObjectMapper` 做 JSON 往返断言，是完全的单元测试，却因此每跑一次就要起 3 个容器。

**问题三：基类命名无法表达分层意图。**

`BaseIntegrationTest` 这个名字读起来是「所有集成测试的基类」，实际语义是「需要真实数据库的集成测试基类」。文档 `docs/03-开发指南/03-08-测试规范手册.md` §2.1 因此写出了自相矛盾的约定——同一条要求里既写「**必须 mock `XxxAppService`**……禁止使用真实 Repository、DomainService 或基础设施」，又写「测试类继承 `BaseIntegrationTest`」。

**为什么现在做**：`src/backend/pom.xml` 中 surefire 排除了 `**/*IntegrationTest.java`，131 个集成测试类在 CI 中从未运行。要把它们接回 CI，必须先让接口层测试脱离数据库开销，否则 CI 会新增约 40 分钟耗时，无法落地。

## What Changes

- 新增 `APIIntegrationTest` 基类（接口层专用）：保留 Testcontainers 以提供 `DataSource`，但**关闭 Flyway 迁移**，并 mock 掉启动期访问数据库的组件
- **BREAKING** 将 `BaseIntegrationTest` 重命名为 `DBIntegrationTest`，明确其「需要真实数据库 + 完整迁移」的语义
- 64 个接口层测试类（54 个 `*ControllerIntegrationTest` + 13 个 `*ControllerTest` - 3 个实测需要数据库的类）改继承 `APIIntegrationTest`
- 实施中发现 3 个原判为接口层的类实际访问数据库，改归 DB 层：`AdminAiTraceControllerIntegrationTest`（用 `JdbcTemplate` 直接写 `tb_ai_conversation` / `tb_ai_turn`）、`FileDownloadControllerIntegrationTest` 与 `FileUploadControllerIntegrationTest`（注入真实 `FileAppService` 并调用写库方法）
- 78 个 DB 层测试类（77 个非 Controller `*IntegrationTest` + `MessageTemplateRegistryPersistenceTest`）改继承 `DBIntegrationTest`
- `QuestionContentJsonTest` 移除基类继承，降为纯单元测试（仅需 `ObjectMapper`）
- 更新 `docs/03-开发指南/03-08-测试规范手册.md` §2.1，消除「继承带 DB 的基类」与「禁止使用真实 Repository」之间的矛盾，并补充两类基类的选用规则

本次变更**不包含**（明确非目标）：

- 接入 `maven-failsafe-plugin`、修改 `.github/workflows/ci.yml` 让集成测试在 CI 运行（需在本变更落地后独立进行）
- 将 DB 层的清理策略由 `flyway.clean()+migrate()` 改为 TRUNCATE（独立优化，涉及 `tb_role` / `tb_direction_learning_step` 种子数据保留问题）
- 修改任何 production 代码（`src/main/java` 零改动）；`PermissionCache` / `MessageTemplateRegistry` 的 eager 加载改为 lazy 属于生产行为变更，另行评估
- 改用 `@WebMvcTest` 切片测试或彻底移除 Testcontainers。实测「假 DataSource + 关迁移 + mock 启动期组件」方案上下文启动 30.6s，反而慢于现方案的 21.8s（JDBC 连接重试阻塞），故不采用

## Capabilities

### New Capabilities

- `backend-test-base-layering`: 定义后端测试基类的分层规则——按「是否访问真实数据库」而非「是否名为集成测试」选择基类，规定 API 层基类不得执行数据库迁移，并规定各层测试的依赖边界

### Modified Capabilities

无。`backend-testing` 现有需求（测试角色一致性、测试环境校验）不受本次变更影响，本次仅新增基类分层约定。

## Impact

- 测试代码：新增 `src/test/java/com/bluenet/web/APIIntegrationTest.java`；重命名 `BaseIntegrationTest.java` → `DBIntegrationTest.java`；146 个既存测试类中的 145 个需调整继承声明（**实际结果：64 改 API 层、81 改 DB 层、`QuestionContentJsonTest` 去掉基类**）
- 测试资源：`APIIntegrationTest` 需在类级别设置 `spring.flyway.enabled=false`
- 文档：`docs/03-开发指南/03-08-测试规范手册.md` §2.1 Controller 测试契约、§2.2 / §2.4 基类名称引用
- 生产代码：无改动
- CI 配置：无改动（但本变更是后续接入 failsafe 的前置条件）
- 实测收益：
  - 接口层：390 个用例不再执行迁移，每用例节省 2.47s，合计约 **16 分钟**
  - `QuestionContentJsonTest` 降为纯单元测试：**约 30s → 0.608s**（10 个用例）
  - 全量本地测试：**约 64 分钟 → 47 分钟**（1914 个用例，0 失败）
  - CI 路径（`./mvnw -B test`）：844 个用例，0 失败，239s；迁移仅 7 次（全部来自唯一的 DB 层类），13 个 `*ControllerTest` 已不再触发迁移
- 风险：接口层基类关闭迁移后数据库 **schema 为空**，因此必须 mock 启动期查库组件，否则上下文启动失败
