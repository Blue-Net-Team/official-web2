## Why

算法题配置中，书面用例、默认运行用例、Generator 和正式用例参数需要重复编写，配置繁琐且容易出现覆盖不完整或参数不一致的问题。现有 AI 客服又受到知识问答意图围栏限制，无法承担管理端的代码与结构化配置生成，因此需要一个独立的 AI 判题配置草稿能力。

## What Changes

- 在算法题编辑抽屉的「判题配置」页签增加单按钮 AI 补全入口，不引入配置 Modal，并支持新增尚未保存的题目。
- 使用当前表单中的题干、输入说明、输出说明和数据范围生成草稿，直接回填题面书面用例、默认运行用例、Python Generator 源码和正式测试用例配置。
- 标准解源码、主标准解语言、语言模板、Benchmark 参数和资源限制继续由出题人维护，AI 不修改这些字段。
- 与现有 AI 客服保持同构，由前端直接调用 AI Service 专用结构化生成接口，不增加 Backend 转发层，也不复用知识库聊天或意图围栏。
- AI 应用结果只更新前端未保存表单，不自动保存题目或判题配置，不自动发布测试数据生成任务；后续继续使用现有保存、测试数据生成、Benchmark 和资源限制确认流程。
- 对生成结果进行结构、枚举、用例编号、Generator 参数和默认值校验，并为重复生成、字段缺失、生成失败和超时提供明确反馈。

## Capabilities

### New Capabilities

- `ai-judge-config-drafting`: 管理端基于未保存算法题面生成并应用书面用例、默认运行用例、Generator 和正式用例配置草稿，包含权限保护、结构化输出、校验及非持久化回填行为。

### Modified Capabilities

无。现有考题保存、判题配置保存、测试数据生成和正式判题契约保持不变。

## Impact

- **Frontend**：`QuestionDrawer.tsx`、算法判题配置 API service 与 DTO，增加生成状态、覆盖确认和跨页签字段回填。
- **AI Service**：新增专用路由、请求/响应 Pydantic 模型、生成提示词、结构化输出解析与业务校验；不复用 `/chat/stream`。
- **Deployment**：前端沿用现有浏览器可访问的 AI Service 地址与独立超时控制；沿用现有 LLM provider 与密钥配置，不新增第三方服务。
- **Judge Service / Database**：无需修改表结构、Manifest、RabbitMQ 消息或判题执行逻辑。
