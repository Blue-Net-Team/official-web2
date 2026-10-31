## 1. AI Service 契约与测试

- [x] 1.1 为草稿请求/响应 Pydantic 模型编写单元测试，覆盖题面必填与长度、四类输出数量、连续 `caseNo`、分类枚举、正式用例默认属性和 JSON 对象参数。
- [x] 1.2 为生成服务编写单元测试，使用 Mock LLM 覆盖结构化输出、JSON 文本回退、非 JSON 输出、业务校验失败和 provider 异常。
- [x] 1.3 为专用路由编写 API 测试，覆盖成功响应、非法请求和上游生成失败，验证不进入 RagAgent、RAG 检索或意图围栏。

## 2. AI Service 实现

- [x] 2.1 新增判题配置草稿请求/响应模型及共享业务校验，响应仅包含 `examples`、`runTestCases`、`generatorLanguage`、`generatorSource` 和 `testcases`。
- [x] 2.2 实现专用 Prompt 与生成服务，复用 LLMFactory，要求 Python Generator stdin/stdout 契约、固定随机种子、6～10 个覆盖不同场景的正式用例，并实现结构化输出及受控 JSON 回退。
- [x] 2.3 注册 `POST /ai/v1/judge-config-drafts` 非流式路由，统一转换模型及校验异常，运行 AI Service 单元测试与 lint。

## 3. Backend 测试先行

- [x] 3.1 编写草稿应用服务单元测试，Mock AI HTTP 客户端，验证题面转发、草稿返回以及全过程不写题目、判题配置、OSS 或 RabbitMQ。
- [x] 3.2 编写 AI Service HTTP 客户端测试，覆盖内部地址与超时配置、成功响应绑定、上游非 2xx、连接失败、读取超时和无效响应。
- [x] 3.3 编写管理端 Controller 集成测试，覆盖 `POST /api/v1/admin/judge/config-drafts` 的合法响应、请求校验、未认证/无权限拒绝和全局唯一权限扫描。

## 4. Backend 实现与配置

- [x] 4.1 增加 AI Service 内部地址、连接超时和读取超时配置及 HTTP 客户端基础设施实现，并补充本地与 Docker 配置示例。
- [x] 4.2 增加草稿 API DTO、应用服务与转换逻辑，以 `ResponseMessage` 返回临时草稿，不引入数据库迁移或持久化实体。
- [x] 4.3 增加管理端 Controller，使用全局唯一的 `judge-problem-config:draft-ai` 权限标识和 `PROTECTED` 访问级别，处理预期上游异常。
- [x] 4.4 运行 Backend 相关单元测试、集成测试和权限扫描验证，确认现有判题配置及生成任务测试无回归。

## 5. Frontend 测试与实现

- [x] 5.1 编写草稿映射和表单行为测试，覆盖必填检查、完整响应校验、JSON 参数格式化、非目标字段保留、覆盖取消和失败时不修改表单。
- [x] 5.2 增加草稿请求/响应 TypeScript DTO 和 `adminJudgeProblemConfigService` 的非持久化生成方法。
- [x] 5.3 在 `QuestionDrawer.tsx` 的「判题配置」页签增加仅限算法题新增/编辑态的 AI 按钮，实现 loading、防重复提交、缺失字段提示和轻量覆盖确认。
- [x] 5.4 实现成功后一次性白名单回填 `examples`、`runTestCases`、`generatorLanguage`、`generatorSource` 和 `testcases`，显示数量摘要，不切换页签、不自动保存或触发生成任务。
- [x] 5.5 运行 Frontend 相关测试、类型检查和 lint，确认题面预览、原有保存及判题配置表单行为无回归。

## 6. 文档与端到端验收

- [x] 6.1 更新算法判题指南和环境配置文档，说明 AI 与人工字段分工、生成后人工检查要求、Backend AI Service 配置及回滚方式。
- [x] 6.2 检查 LLM provider 与密钥环境变量是否已由用户配置，构建并运行 AI Service、Backend 和 Frontend 所需产物与容器，确认服务健康及权限扫描通过。
- [x] 6.3 检查 3000 端口占用并遵守现有前端服务约束，使用 Playwright 验证新题未保存生成、编辑题未保存修改生成、跨页签回填、标准解保留、覆盖取消/确认、失败不改表单及后续保存链路。
- [x] 6.4 使用确定性 Mock 草稿完成自动化端到端验收；若环境已配置真实 LLM，再进行一次人工冒烟并检查 Generator 与正式参数的一致性，不以真实模型输出作为稳定测试断言。

## 7. 直连架构调整

- [x] 7.1 按现有 AI 客服调用方式改为 Frontend 直连 AI Service，使用原生 fetch、150 秒 AbortController 超时和直接 JSON 响应。
- [x] 7.2 移除 Backend 草稿转发接口、重复 DTO、HTTP 客户端及专用配置，同步更新 OpenSpec 设计、规格和环境文档。
- [x] 7.3 运行 Frontend 类型检查、Backend 编译、AI Service 测试和真实直连冒烟，重新构建 Frontend 并验证容器调用链。
