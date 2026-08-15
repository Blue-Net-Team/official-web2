## Why

当前 AI 客服对所有问题都会进入 RAG 检索流程，但用户可能询问考核具体内容、代码实现、技术答疑、系统部署细节或密钥等敏感/无关问题。这些问题不应检索知识库，也不应被回答。需要在运行时增加意图识别与安全围栏，只让"报名、考核流程、实验室介绍、软件下载"四类问题进入检索，其余问题直接拒绝或简短回复。

## What Changes

- 在 `src/ai-service` 中新增 `IntentClassifier`，基于单独配置的 LLM 对用户输入进行结构化意图分类。
- 在 `RagAgent.chat()` 和 `RagAgent.chat_stream()` 入口处增加意图闸门：
  - `RETRIEVE`：允许的问题进入现有 LangGraph RAG 流程。
  - `REFUSE`：被拦截的问题不检索，由 LLM 生成礼貌拒绝话术并返回。
  - `DIRECT`：问候/闲聊类问题不检索，由 LLM 生成简短直接回复并返回。
- 新增分类器配置项（`INTENT_LLM_PROVIDER`、`INTENT_LLM_MODEL`、`INTENT_LLM_TEMPERATURE`），支持独立选择分类模型。
- 使用 LangChain `with_structured_output`（Pydantic）实现 JSON 结构化输出，解析失败时按"体验优先"策略降级为询问用户重新描述，而非直接拒绝。
- 在分类 prompt 中嵌入 few-shot 示例，减少"考核流程"与"考核内容"等边界 case 的误判。
- 新增单元测试覆盖 allowed / blocked / direct / 边界 / 解析失败等场景。

## Capabilities

### New Capabilities
- `ai-intent-recognition`：AI 服务运行时意图识别与安全围栏，决定用户请求是否进入 RAG 检索、直接回复或拒绝。

### Modified Capabilities
- `ai-service-agent-workflow`：`RagAgent` 在调用 LangGraph 之前增加意图分类闸门；被拦截的请求不再进入 `pre_disclose` / `agent` / `tool_executor` 工作流，但 SSE 事件协议保持兼容。

## Impact

- **代码**：`src/ai-service/agent/` 新增 `intent.py`；修改 `agent.py`、`prompts.py`、`types.py`；`setting.py` 增加配置项。
- **API**：`/ai/v1/chat` 和 `/ai/v1/chat/stream` 的请求/响应字段不变；被拦截请求仍返回标准 `content` + `done` 事件序列。
- **依赖**：不新增第三方依赖，复用现有 `langchain-openai` 的 `with_structured_output`。
- **配置**：需要新增 `TBD_RAG_INTENT_LLM_PROVIDER`、`TBD_RAG_INTENT_LLM_MODEL` 等环境变量；默认空值表示复用主 LLM。
- **性能**：每条请求增加一次轻量 LLM 分类调用，被拒绝请求跳过 RAG 检索，整体成本取决于拦截比例。
