## Context

BlueNet 当前是 `src/backend` Spring Boot 主服务、`src/frontend` Next.js 前端、PostgreSQL、RabbitMQ、Redis、MinIO/OSS 的 monorepo。现有算法题能力已经包含答案提交、判题任务、用例结果和统一评判记录，但判题执行仍与主后端处在同一工程边界内，测试数据也不满足专业 OJ 的生成、隐藏、分类和多语言限时校准需求。

本变更将判题执行拆成 `src/judge-service` 独立 JAR。主后端继续管理用户、权限、考核、题目、答案、OSS 文件入口、RabbitMQ 投递、前端轮询 API 和所有 Flyway migration。Judge Service 只消费任务、执行不可信代码、生成测试数据、运行 benchmark、写入判题结果。

## Goals / Non-Goals

**Goals:**

- 在 `src/backend` 同级新增 `src/judge-service`，独立构建、独立部署、独立运行。
- 允许根 Maven 父工程统一 Java 21、Spring Boot 版本、Spotless、Compiler、Surefire 和依赖版本。
- 保持 Backend 是唯一 Flyway migration 执行方，Judge Service 禁用 Flyway。
- 用户提交代码继续存 DB，正式判题任务保存 `source_code` 快照。
- 判题资产复用主应用同一个 OSS 服务，使用独立 `bluenet-judge` bucket 存放 generator、标准解、manifest、生成后的 `.in/.out` 和可选日志。
- 后台不要求手写 manifest，也不要求逐条录入测试输入输出；Backend 根据后台配置生成 manifest，Judge Service 在沙箱中运行 generator 和标准解生成测试数据。
- 每题每语言维护独立资源限制；限制由对应语言标准解 benchmark 推导建议值，并由管理员确认后用于正式判题。
- 前端继续通过 Backend 轮询判题状态和结果。

**Non-Goals:**

- 不引入完整第三方 OJ 平台作为主系统替代。
- 不让 Judge Service 复用 Backend 的 mapper、entity、service 或权限上下文。
- 不保留测试数据历史版本；更新测试数据后当前题目使用最新配置和最新用例。
- 不在正式判题时运行标准解或将用户代码耗时与标准解实时对比。
- 不在第一阶段实现 SPJ、交互题、分布式调度优化或多数据版本回滚。

## Decisions

### Decision: Judge Service 独立 JAR，但共享构建规范

`src/judge-service` 与 `src/backend` 同级，拥有自己的 application 配置、RabbitMQ 消费者、窄 DB 写入层、OSS 客户端、沙箱执行器和判题流程。根 POM 只做构建和版本治理，不承载业务共享。

Alternatives considered:

- 完全独立仓库：边界更强，但当前开发和版本联动成本更高。
- Maven 多模块并共享 Backend mapper/entity：短期少写 SQL，但会让 Judge Service 依赖主业务模型、权限上下文和事务语义，削弱服务拆分价值。

### Decision: Backend 唯一负责数据库迁移

所有新表和表结构调整都通过 `src/backend/src/main/resources/db/migration` 新增 Flyway 版本迁移完成。Judge Service 显式配置 `spring.flyway.enabled=false`。

Alternatives considered:

- 两个服务分别管理迁移：需要独立 Flyway history 或严格部署编排，容易出现顺序和 ownership 冲突。
- Judge Service 自动建表：不符合当前项目迁移卫生要求，也不利于审计。

### Decision: OSS 保存判题资产，DB 保存索引和状态

用户答案代码存 DB；generator、标准解、manifest、生成的 `.in/.out` 和可选日志存同一 OSS 服务下的 `bluenet-judge` bucket。数据库保存 object key、hash、测试用例索引、语言限制和状态。

Alternatives considered:

- 将 generator 和标准解源码存 DB：单文件场景可行，但后续支持多文件、依赖和日志资产会变差。
- 将测试数据放本地目录：单机简单，但不符合本次要求，也不利于后续多 Worker 扩展。

### Decision: 测试用例由 generator 在沙箱中生成

管理员上传 generator 和各语言标准解，在后台配置测试用例的分类、生成参数、权重、隐藏性和样例标记。Backend 生成 manifest 并保存到 OSS，Judge Service 下载 manifest，在沙箱中运行 generator 生成 `.in`，再运行主标准解生成 `.out`。

Alternatives considered:

- 上传静态测试数据包：可控但仍要求出题人手动产出完整 `.in/.out` 包，不符合希望服务器自动生成数据的方向。
- 后台逐条填写输入输出：只适合极小题目，不适合复杂度卡点和大数据。

### Decision: 标准解用于生成输出和推导语言限制

每个支持语言上传一份标准解。Judge Service 多次运行每种语言标准解，统计 p95/max 耗时和内存，按 `roundUp(max(p95 * marginMultiplier, p95 + minExtraMs), roundToMs)` 生成建议时限，管理员确认后写入 `tb_judge_language_limit`。

正式判题只运行用户代码，并按 `question_id + language` 读取已确认的时间、内存和输出限制。

Alternatives considered:

- 所有语言共享一个 time limit：会导致 C++ 错复杂度可过或 Python 正解过不了。
- 使用慢解对照作为必需流程：能提升出题质量，但会增加第一版复杂度；本变更不强制。

### Decision: 不保留测试数据历史版本

当前题目只保留一套可用测试数据。更新测试数据时替换当前 config、test cases 和 language limits；旧 OSS prefix 可在替换成功后清理。

Alternatives considered:

- dataset version 模型：适合比赛追溯和历史重判复现，但当前需求明确不保留旧测试用例。

## Risks / Trade-offs

- [Risk] 不保留测试数据历史版本会导致历史结果无法严格复现。→ Mitigation: 明确业务语义为“重判使用最新数据”，并在管理端提示更新测试数据的影响。
- [Risk] generator 或标准解本身可能死循环、写爆输出或访问网络。→ Mitigation: generator、标准解和用户提交代码全部通过同一类沙箱限制运行。
- [Risk] 标准解 benchmark 受机器负载影响导致时限过紧。→ Mitigation: 多次运行、使用 p95/max、加入倍率和最小额外时间，并要求管理员确认。
- [Risk] Judge Service 直接写主库可能误触业务表。→ Mitigation: 只实现窄写入接口，限定可写表和字段范围，不复用 Backend mapper/service。
- [Risk] OSS 当前数据替换中途失败可能产生不一致。→ Mitigation: 使用 staging prefix 先生成和校验，DB 替换成功后再切换 current key 并清理旧文件。
- [Risk] 不同语言运行环境差异影响公平性。→ Mitigation: 每题声明支持语言，每语言独立标准解和资源限制；无法稳定支持的语言不开放。

## Migration Plan

1. 新增根 POM 或调整现有构建，使 `backend` 和 `judge-service` 共享 Java 21、Spotless 和插件版本，但不共享业务代码。
2. 新增 `src/judge-service` 骨架，禁用 Flyway，配置 PostgreSQL、RabbitMQ、共享 OSS 连接信息、判题 bucket 和沙箱运行参数。
3. 在 Backend 新增 Flyway migration，创建测试数据配置、标准解、测试用例和语言限制相关表；保留现有答案、判题任务、用例结果和评判表。
4. 在 Backend 新增管理入口：上传 generator/标准解、配置测试用例、生成 manifest、写 OSS 和创建数据生成任务。
5. 在 Judge Service 实现数据生成任务：下载 manifest、沙箱运行 generator 和标准解、上传 `.in/.out`、写测试用例和 benchmark 结果。
6. 在 Backend 增加管理员确认语言限制和发布当前测试数据配置的流程。
7. 在 Judge Service 实现正式判题任务消费：读取 job、test cases 和 language limit，下载 `.in/.out`，沙箱运行用户代码，写 case result、job 状态和 assessment judgement。
8. 保持前端轮询 Backend API；必要时只调整展示字段，不让前端直接访问 Judge Service 或 `bluenet-judge` bucket。

Rollback strategy:

- 部署初期保留旧判题路径的开关或禁止算法题正式提交，直到新 Judge Service 验证通过。
- 如 Judge Service 不可用，Backend 仍可创建或保留 PENDING/FAILED_REVIEW_REQUIRED 状态，不产生错误的自动评判结果。
- 数据结构由新增迁移创建；回滚应用时不修改历史迁移，必要时通过新迁移禁用或清理新配置。
