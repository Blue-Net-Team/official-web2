## Why

当前 AI 客服流式接口 `/ai/v1/chat/stream` 的思考过程（`type: reasoning`）是一次性完整返回的，而最终答案（`type: content`）是逐字/逐 token 流式推送的，两者用户体验不一致。为了让前端能够以统一的打字机效果展示模型思考过程，需要让 reasoning 也按片段流式输出。

## What Changes

- 统一 `LLMProvider.stream_with_tools()` 的接口语义，从"聚合后一次性返回完整 `LLMResponse`"改为"逐片段 yield `StreamEvent`"，使 reasoning 可以像 content 一样流式输出。
- 在 DeepSeek provider 中真正实现流式 reasoning + 流式 tool_call 聚合。
- 在 SiliconFlow 和 Ollama provider 中做接口兼容适配：内部仍按现有非流式方式聚合完整结果，再包装成 `StreamEvent` 事件流输出，保持接口统一且不影响当前行为。
- 改造 `RagAgent.chat_stream()` 的 Phase 1 工具调用循环，使其消费新的 `stream_with_tools()` 事件流，实时 yield `reasoning` 片段，同时保持 `tool_call` / `tool_result` 一次性返回。
- 补充单元测试，覆盖 DeepSeek 流式 reasoning 分片、流式 tool_call 聚合、Agent 事件序列等场景。

## Capabilities

### New Capabilities

- `ai-service-streaming-reasoning`: AI 客服流式对话中 `reasoning` 事件按片段实时推送，与 `content` 保持一致的流式体验；`tool_call` / `tool_result` 仍保持一次性返回。

### Modified Capabilities

- 无现有 spec 级别的行为变更。SSE 协议的事件类型（`reasoning` / `tool_call` / `tool_result` / `content` / `done`）和字段结构保持不变，仅 `reasoning` 的推送粒度从"一段完整思考"变为"多个思考片段"。

## Impact

- **受影响代码**：`src/ai-service/llm_providers/base.py`、`deepseek.py`、`siliconflow.py`、`ollama.py`、`agent/agent.py` 及对应测试。
- **API 协议**：`/ai/v1/chat/stream` 的 SSE 输出格式不变，前端无需修改事件解析逻辑；未来前端实现 reasoning 打字机效果时可直接按片段追加。
- **运行时影响**：仅影响 AI 服务（`ai-service`），对 API 服务、前端、数据库无影响。
- **向后兼容**：SiliconFlow 和 Ollama provider 保持当前非流式行为，DeepSeek provider 升级为流式 reasoning。
