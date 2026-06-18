## 1. Provider 接口与基类改造

- [x] 1.1 在 `src/ai-service/llm_providers/base.py` 中新增 `StreamEvent` dataclass，包含 `type`（`reasoning` / `content` / `tool_call` / `done`）、`delta`、`tool_name`、`tool_args` 字段。
- [x] 1.2 修改 `LLMProvider.stream_with_tools()` 抽象方法签名，返回类型改为 `Iterator[StreamEvent]`，并更新 docstring 说明事件语义（`tool_call` 由 provider 内部聚合完整后发出）。

## 2. DeepSeek Provider 流式实现

- [x] 2.1 在 `src/ai-service/llm_providers/deepseek.py` 的 `DeepSeekLLM.stream_with_tools()` 中，使用 OpenAI 流式调用并逐 chunk 处理 `delta.reasoning_content`、`delta.content`、`delta.tool_calls`。
- [x] 2.2 实现流式 `tool_calls` 聚合逻辑：按 `index` 聚合 id、name、arguments，在 `finish_reason == "tool_calls"` 或流结束时发出完整 `tool_call` 事件。
- [x] 2.3 确保 `reasoning` 和 `content` 以 `delta` 形式实时 yield，`done` 事件在流正常结束时发出。

## 3. SiliconFlow 与 Ollama Provider 兼容适配

- [x] 3.1 在 `src/ai-service/llm_providers/siliconflow.py` 中重写 `stream_with_tools()`：内部复用现有 `invoke_with_tools()` 聚合完整结果，然后包装为 `StreamEvent` 序列（`reasoning` → `content` → `tool_call` → `done`）。
- [x] 3.2 在 `src/ai-service/llm_providers/ollama.py` 中做同样的接口适配，保持现有非流式行为不变。

## 4. Agent 层流式消费改造

- [x] 4.1 修改 `src/ai-service/agent/agent.py` 的 `chat_stream()` 方法，Phase 1 工具调用循环改用 `self._llm.stream_with_tools()`。
- [x] 4.2 对 `StreamEvent` 进行分类处理：`reasoning` 实时 yield，`content` 累积到本地变量不展示，`tool_call` 写入 assistant message 后 yield 并执行工具、`yield tool_result`。
- [x] 4.3 每轮工具调用结束时，将本轮累积的 `full_reasoning` 和 `full_content` 正确写入 messages 历史，并追加 tool message。
- [x] 4.4 处理 `done` 事件：无 tool_call 时进入 Phase 2 最终答案流式输出。

## 5. 单元测试

- [x] 5.1 为 `DeepSeekLLM.stream_with_tools()` 编写测试：模拟 OpenAI 流式返回 `reasoning_content` 片段，验证 `reasoning` 事件按片段 yield。
- [x] 5.2 为 `DeepSeekLLM.stream_with_tools()` 编写测试：模拟 function calling 分片，验证 `tool_call` 事件在参数完整后一次性发出。
- [x] 5.3 为 `RagAgent.chat_stream()` 编写测试：验证 `reasoning` → `tool_call` → `tool_result` → `content` → `done` 的事件序列正确。
- [x] 5.4 为 SiliconFlow 和 Ollama 的 `stream_with_tools()` 编写测试：验证接口适配后事件序列与原有非流式行为等价。

## 6. 集成验证

- [x] 6.1 本地重新构建 `bluenet-ai-service` 镜像并启动容器。
- [x] 6.2 调用 `POST /ai/v1/chat/stream`，观察 `reasoning` 事件是否为多个片段，且 `tool_call` / `tool_result` / `content` / `done` 行为正常。
- [x] 6.3 调用 `POST /ai/v1/chat/` 非流式接口，确认返回结构和行为无变化。
