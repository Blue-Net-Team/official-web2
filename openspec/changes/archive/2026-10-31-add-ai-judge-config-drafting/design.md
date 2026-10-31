## Context

算法题编辑集中在 `QuestionDrawer.tsx`，当前未保存表单同时包含题面与判题配置。Backend 已负责判题配置持久化，Judge Service 已负责 Generator 执行、标准解执行和 Benchmark；AI Service 已有统一 LLMFactory，但现有聊天链路带知识库检索和意图围栏，会拒绝算法解题及代码生成请求。

本变更横跨 Frontend 和 AI Service，涉及非确定性模型输出以及跨页签字段更新。生成动作必须支持尚未保存的新题，因此不能依赖 `questionId` 或现有判题配置保存路由。

## Goals / Non-Goals

**Goals:**

- 通过一个按钮将当前未保存题面转换为四类可编辑草稿：书面用例、默认运行用例、Python Generator 和正式用例配置。
- 使用专用结构化输出契约，保证返回内容能映射到现有前端表单。
- 与现有 AI 客服保持一致的直连调用方式，避免在 Backend 增加仅供转发的适配层。
- 应用草稿时不写数据库、OSS 或 RabbitMQ，并保留现有人工保存及判题工作流。
- 失败时不改变已有表单，重复生成时明确覆盖范围。

**Non-Goals:**

- 不生成、翻译或修改标准解源码及主标准解语言。
- 不生成语言模板、Benchmark 参数或资源限制。
- 不在沙箱中预执行 AI 代码，不做样例自动修复或多轮 Agent 工作流。
- 不改变 Manifest、测试资产生成、Benchmark、正式判题或数据库结构。
- 不复用知识库聊天、RAG 检索、对话轨迹或意图围栏。

## Decisions

### 1. 使用无题目 ID 的 AI 临时草稿接口

Frontend 直接调用 `POST /ai/v1/judge-config-drafts`，请求只包含当前题面字段；AI Service 返回临时草稿，不创建持久化资源。该设计同时支持新增与编辑，也明确表达“生成草稿”和“保存配置”是两个动作。

### 2. 与 AI 客服一致直连 AI Service

Frontend 复用 `AI_CHAT_BASE_URL`，通过原生 `fetch` 调用专用草稿接口，并使用 `AbortController` 设置 150 秒超时。AI Service 直接返回结构化 JSON，不经过 Backend DTO 转换或 `ResponseMessage` 二次包装。

Backend 代理方案会增加一层仅做请求转发的应用服务、HTTP 客户端和重复 DTO，且与现有 AI 客服的调用方式不一致，因此不采用。AI Service 的公网访问、CORS 与后续统一鉴权由 AI 服务边界集中处理。

### 3. AI Service 使用专用结构化生成链路

新增独立 API、Pydantic 请求/响应模型和专用 Prompt，直接通过 LLMFactory 获取模型。优先使用 LangChain 结构化输出；对不支持该能力的 provider 使用受控 JSON 文本解析回退，最终都必须经过同一 Pydantic 与业务规则校验，不进入 RagAgent。

响应契约包含：

- `examples`: 2～3 个题面样例，字段与现有 `AlgorithmExample` 对齐；
- `runTestCases`: 2～4 个小规模默认运行用例，字段与现有 `AlgorithmTestCase` 对齐；
- `generatorLanguage`: 固定为 `python`；
- `generatorSource`: 单个 Python 源码字符串；
- `testcases`: 6～10 个正式用例配置，`generatorArgs` 为 JSON 对象而非字符串。

标准解及 Benchmark 字段不进入 AI 响应契约，从类型边界防止误覆盖。

### 4. 在 AI Service 和 Frontend 分层校验

除 Pydantic 类型校验外，AI Service 必须验证：用例数量、`caseNo` 从 1 连续递增、分类属于现有枚举、权重为 1、正式用例 `hidden=true` 且 `sample=false`、`generatorArgs` 为对象、源码非空。Prompt 要求随机用例使用固定 seed，且 Generator 只从 stdin 读取参数、只向 stdout 写测试输入。模型输出不符合约束时自动修正重试一次。

Frontend 在应用表单前再次检查完整响应、用例数量、编号、分类和参数类型；请求题面设置非空校验，不完整或无效响应不进入表单。

### 5. 前端一次性回填，不做流式输出

「判题配置」页签右上角仅在算法题新增/编辑态显示 AI 按钮。点击后先检查题干、输入说明、输出说明和数据范围，再发送一次非流式请求；等待期间按钮 loading 并禁止重复提交。

成功后通过一次 `form.setFieldsValue` 原子更新 `examples`、`runTestCases`、`generatorLanguage`、`generatorSource` 和 `testcases`，其中正式用例参数转换为现有表单使用的格式化 JSON 字符串。不切换页签、不自动保存。成功消息汇总生成数量，并提示书面用例位于「题面信息」页签。

非流式方案比 SSE 更适合一次性结构化结果，也避免处理半截 JSON 或代码片段。

### 6. 覆盖前轻量确认，失败保留现场

若任一目标字段已有内容，使用 Popconfirm 等轻量确认，不引入 Modal；确认文案列出将覆盖的四类内容。请求失败、取消或校验失败时不修改任何目标字段。

只更新白名单字段，标准解、主标准解语言、语言模板、Benchmark 参数、资源限制和其他题面字段保持原值。

### 7. 测试以确定性替身替代真实模型

AI Service 单元测试覆盖结构化输出、JSON 回退、自动修复和业务校验；Frontend 测试覆盖必填检查、覆盖确认、字段映射、非目标字段保留和失败不改表单。端到端验证直接拦截 AI Service 草稿请求并返回可控响应，避免真实模型导致测试不稳定。

## Risks / Trade-offs

- **AI 书面答案或标准解语义可能错误** → 明确定位为草稿；保留人工标准解与人工审查，不在本变更中宣称自动验证正确性。
- **Generator、正式参数和题面可能不一致** → 单一结构化生成请求联合产出三部分，集中校验参数形状；后续仍由现有测试数据生成流程暴露运行错误。
- **模型返回合法 JSON 但代码不可运行** → 源码只进入未保存表单，现有沙箱生成流程承担编译/运行验证；失败时不覆盖数据库配置。
- **生成最大规模用例可能消耗过多资源** → 限制正式用例数量，Prompt 要求遵守题面约束并合理控制规模，现有 Judge 沙箱资源限制继续作为最终防线。
- **模型延迟或 provider 输出差异** → 使用非流式超时、明确错误反馈、结构化输出与 JSON 回退；测试固定 Mock 模型行为。
- **题面中的提示注入可能操纵生成内容** → 将题面视为不可信数据，限定输出 Schema、长度和业务规则；生成代码仍只能经人工保存后进入既有隔离沙箱。
- **浏览器直连依赖 AI Service 公网配置** → 与 AI 客服共用地址、CORS 和服务边界；Frontend 使用独立超时和严格响应校验，Backend 不承担转发。

## Migration Plan

1. 部署 AI Service 新路由，确认现有浏览器 AI Service 地址和 CORS 可用。
2. 发布 Frontend 直连按钮与独立超时控制，无需 Backend 配置或数据库迁移。
3. 回滚时先移除 Frontend 入口，再回滚 AI Service；由于草稿不持久化，不涉及数据清理。

## Open Questions

无。首版固定 Python Generator、无 Modal、直接回填、不生成标准解、不自动保存或触发判题，均已在需求探索中确定。
