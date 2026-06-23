## Context

当前 `src/ai-service/agent/agent.py` 中的 `RagAgent` 用手写循环（`_run_two_stage_loop`）实现了多轮工具调用：预披露 → 标签检索 → 分片检索 → 兜底语义搜索 → 生成答案。该实现虽然工作正常，但存在以下问题：

- **状态机隐含在代码中**：阶段跳转、轮次限制、工具调用与结果回写混在一起，新增阶段或调整顺序成本高。
- **会话状态与 Agent 强耦合**：`Conversation` 维护消息列表，轮次计数器在 `RagAgent` 实例变量中，难以持久化或恢复。
- **流式输出与逻辑耦合**：`_run_two_stage_loop` 同时负责工具调用循环和最终答案生成，`chat_stream` 需要重复实现类似逻辑。
- **同步与流式路径重复**：`chat()` 与 `chat_stream()` 分别维护各自的工具调用循环，存在行为不一致风险（例如流式路径未把带 tool_calls 的 assistant 消息写入 `self.conversation`，可能导致同一会话下混用两种方式时上下文丢失）。

`langgraph` 提供了基于 `StateGraph` 的状态机框架，支持循环、条件边、持久化（checkpointer）和流式观测，与当前场景高度契合。

## Goals / Non-Goals

**Goals：**

- 将 `RagAgent` 的两阶段检索流程显式建模为 `langgraph.StateGraph`。
- 保留现有 `LLMProvider` 抽象，通过适配函数将 `invoke_with_tools` / `stream_with_tools` 接入 LangGraph 节点。
- 保持 `/ai/v1/chat/stream` 的 SSE 事件协议不变，前端无感知。
- 保持 `/ai/v1/chat` 非流式接口的响应格式不变。
- **统一同步与流式实现路径**：两者复用同一个 LangGraph 状态图，只在最终输出层区分完整响应与 SSE 事件流。
- 新增针对状态图节点流转的单元测试。

**Non-Goals：**

- 不替换 `ToolRegistry` 为 LangGraph 的 `@tool` 体系。
- 不将 `DeepSeekLLM`、`SiliconFlowLLM` 等改为 LangChain `BaseChatModel`。
- 不引入 Redis/Postgres checkpointer（仍使用内存会话）。
- 不改前端代码、类型、API 路径或 SSE 字段。
- 不改变 `rag-retrieval`、`ai-service-streaming-reasoning` 等已有规范定义的外部行为。

## Decisions

### 1. 使用 `langgraph.StateGraph` 替代手写循环

**选择**：将 `RagAgent` 重构为显式状态图，包含 `pre_disclose`、`agent`、`tool_executor` 节点与 `END` 终止点，以及阶段跳转的条件边。

**理由**：
- 阶段流转可视化，便于后续增加“人工确认”、“重试”等节点。
- LangGraph 的 `checkpointer` 天然支持状态持久化，为后续多会话历史打下基础。
- 非流式和流式可以复用同一个 graph，只是调用入口不同。

**替代方案**：保持手写循环并抽取辅助类。已否决，因为无法解决状态隐式耦合和持久化扩展问题。

### 2. 保留 `LLMProvider` 抽象，通过适配函数接入 LangGraph

**选择**：不将现有 LLM 提供者全部迁移为 LangChain `BaseChatModel`，而是在 graph 节点中统一调用 `LLMProvider.stream_with_tools` / `stream`，把事件累积为 assistant / tool 消息；流式调用时再通过 `StreamWriter` 外发 SSE 事件。

**理由**：
- 现有 `LLMProvider` 已支持 `reasoning_content`，DeepSeek 的推理流是关键用户体验，迁移到 LangChain ChatModel 会引入大量兼容工作。
- 当前 `ToolRegistry` 返回字符串结果，与 LangGraph `ToolNode` 的消息格式不完全一致，保留现有调用链可以减少改造范围。

**替代方案**：全量迁移到 LangChain ChatModel + LangGraph `ToolNode`。已否决，因为会引入 reasoning_content 丢失、工具返回格式重写、provider 行为变化等风险。

### 3. 保持现有 SSE 契约不变

**选择**：`api/chat.py` 中的 `/chat/stream` 继续输出 `{type, content, tool_name, tool_args}` 格式的事件。

**理由**：
- 前端 `apis/services/ai-chat.service.ts:14` 和 `hooks/useAiChat.ts:93` 已围绕该契约完整实现，改动会带来回归测试成本。
- 保持契约不变意味着 LangGraph 只是后端实现细节，不影响外部接口。

**替代方案**：直接暴露 LangGraph 原生 `stream_mode="messages"` 事件。已否决，因为前端必须重写解析和状态分发逻辑。

### 4. 状态图节点职责划分

```
pre_disclose
    │
    ▼
agent ──(tool_call)──► tool_executor ──► agent (循环，直到无 tool_call)
    │
   END
```

- `pre_disclose`：调用 `tag_generate` 和 `tag_search_detailed`，把结果注入首条用户消息。
- `agent`：调用 `LLMProvider.stream_with_tools`，把 reasoning / tool_call 事件写回 state.messages；本轮无 tool_call 时，`content` 即为最终答案，写入 `final_content` 并结束工作流。流式调用时通过 `StreamWriter` 外发事件。
- `tool_executor`：调用 `ToolRegistry.execute`，更新轮次计数，将结果写回 state.messages；流式调用时外发 `tool_result` 事件。

**状态字段**：`messages: list[dict]`、`tag_rounds: int`、`chunk_rounds: int`、`fallback_rounds: int`、`final_content: str`、`final_reasoning: str`。

### 5. 会话管理仍使用内存 dict，但结构与 LangGraph checkpointer 对齐

**选择**：`api/chat.py` 中的 `_conversations: dict[str, RagAgent]` 保留，但 `RagAgent` 内部使用 LangGraph 的 `MemorySaver` 管理线程状态。

**理由**：
- 本次范围明确为后端重构，不引入 Redis 等外部依赖。
- 使用 `MemorySaver` 后，后续切换到 Redis checkpointer 只需改配置。

### 6. 同步与流式复用同一个状态图

**选择**：`chat()` 调用 `graph.invoke()`，`chat_stream()` 调用 `graph.stream(stream_mode="custom")`，两者使用完全相同的节点和条件边。

**理由**：
- 消除当前 `chat()` 与 `chat_stream()` 中重复的两阶段循环，避免未来修改时只改一边导致行为分叉。
- 工具调用轮次限制、兜底策略、reasoning 透传等逻辑只实现一次。
- LangGraph 的 `invoke` / `astream` 共享同一 `state`，天然保证同步和流式的中间状态一致。

**替代方案**：保留两个独立但共享工具函数的实现。已否决，因为无法根除状态回写路径的差异。

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|---|---|---|
| LangGraph 1.x 与现有 `langchain` 版本兼容问题 | 运行时报错或行为异常 | 在 `pyproject.toml` 中指定兼容版本；CI 中跑通 agent 单元测试后再合并 |
| reasoning_content 在状态图中丢失 | 前端无法展示思考过程 | 在 `agent` 节点中显式捕获 `reasoning_content` 并写入 state；测试覆盖 DeepSeek provider |
| 工具调用事件顺序与旧实现不一致 | 前端 tool 卡片显示错乱 | 保持 `tool_executor` 串行执行，确保 `tool_call` 后立即跟随 `tool_result`；E2E 验证 |
| 状态图调试复杂度高于手写循环 | 定位问题更困难 | 为每个节点添加详细日志；保留 `agent.py` 中 `_log` 的 module 绑定 |
| 流式输出中断后状态恢复 | 用户取消请求后再次发送，历史消息可能重复 | `MemorySaver` 按 `conversation_id` 维护线程状态，取消不影响已持久化的历史 |

## Migration Plan

1. **依赖准备**：在 `src/ai-service/pyproject.toml` 添加 `langgraph`（ resolved 到 `1.1.8`），更新 lock 文件。
2. **状态图实现**：新增 `agent/graph.py`，实现 `build_rag_graph()`；`agent` 节点在流式模式下通过 `StreamWriter` 外发事件，无 tool_call 时直接生成最终答案；`tool_executor` 节点执行工具并外发结果。
3. **Agent 重写**：在 `agent/agent.py` 中直接替换 `RagAgent` 实现为基于 LangGraph 的版本，`chat()` / `chat_stream()` 复用同一状态图；`api/chat.py` 导入与接口签名保持不变。
4. **单元测试**：新增 `tests/agent/test_graph.py`，覆盖轮次限制、reasoning 透传、同步/流式等价性等场景；更新 `tests/agent/test_agent_stream.py` 的预披露 mock 以保持测试速度。
5. **集成验证**：启动 ai-service，通过前端浮窗和直接 curl 测试 `/chat/stream`，对比旧实现的事件序列。
6. **清理**：`RagAgent` 类名与文件路径保持不变，无需重命名或删除文件；删除不再使用的旧 `_run_two_stage_loop` 等私有方法。
7. **回滚**：若生产验证失败，切换回 Git 历史中的旧实现。

## Open Questions

1. `langgraph` 的具体版本号需要与现有 `langchain` 1.x 兼容，是否允许我先锁定 `langgraph>=0.2.70,<0.3`？
2. 是否需要保留旧 `RagAgent` 一段时间作为功能开关，还是直接替换？
3. 单元测试中是否需要 mock LLM provider，还是使用 Ollama 本地模型跑端到端？
