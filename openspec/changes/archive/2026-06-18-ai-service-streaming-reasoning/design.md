## Context

当前 `ai-service` 的流式对话接口 `/ai/v1/chat/stream` 分为两个阶段：

1. **Phase 1（工具调用循环）**：调用 `LLMProvider.invoke_with_tools()` 非流式获取完整响应，然后一次性 yield `reasoning`，再 yield `tool_call` / `tool_result`。
2. **Phase 2（最终答案）**：调用 `LLMProvider.stream()` 流式获取 `content` 片段。

这导致 `reasoning` 和 `content` 的用户体验不一致：前者是"突然出现一大段"，后者是"逐字打字机效果"。

`LLMProvider` 基类虽然已声明 `stream_with_tools()`，但三个实现（DeepSeek、SiliconFlow、Ollama）都是内部聚合完整结果后一次性 yield 一个 `LLMResponse`，并没有真正流式。

## Goals / Non-Goals

**Goals:**
- 让 DeepSeek provider 在 Phase 1 输出流式 `reasoning` 片段，与 Phase 2 的 `content` 体验一致。
- 统一 `LLMProvider.stream_with_tools()` 接口语义，使上层 Agent 可以以统一方式消费事件流。
- 保持 SiliconFlow 和 Ollama provider 的当前行为不变，但适配新的统一接口。
- 保持 `/ai/v1/chat/stream` 的 SSE 协议对现有客户端完全兼容。

**Non-Goals:**
- 不改非流式接口 `/ai/v1/chat/` 的行为。
- 不改造 SiliconFlow 和 Ollama 去真正实现流式 reasoning（保持现有非流式聚合）。
- 不新增前端页面或修改前端消费逻辑。
- 不改变工具调用的业务逻辑或可用工具集合。

## Decisions

### 1. 引入 `StreamEvent` 作为统一的流式事件抽象

**决定**：新增一个 `StreamEvent` dataclass，包含 `type`、`delta`、`tool_name`、`tool_args`。

**理由**：
- `LLMResponse` 不适合表示流式 delta，因为它表示"完整响应"。
- 统一事件抽象让 Agent 层无需关心具体 provider 的流式细节。
- `tool_call` 仍然需要 provider 内部聚合完整后再发出，因此 `StreamEvent.tool_call` 表示"已聚合完成的工具调用"。

```
┌─────────────────────────────────────────────┐
│  LLMProvider.stream_with_tools()            │
│  ─────────────────────────────────────      │
│  yields StreamEvent                         │
│    ├─ type="reasoning", delta="..."         │
│    ├─ type="content",   delta="..."         │
│    ├─ type="tool_call", tool_name, tool_args│
│    └─ type="done"                           │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  RagAgent.chat_stream()                     │
│  ─────────────────────────────────────      │
│  consumes StreamEvent                       │
│    ├─ reasoning ──▶ yield StreamChunk       │
│    ├─ content   ──▶ accumulate (Phase 1)    │
│    ├─ tool_call ──▶ yield + execute tool    │
│    └─ done      ──▶ enter Phase 2           │
└─────────────────────────────────────────────┘
```

### 2. Provider 内部聚合 tool_call，Agent 不处理片段

**决定**：`StreamEvent` 中的 `tool_call` 必须是已聚合完整的工具调用，片段聚合逻辑由各 provider 实现。

**理由**：
- OpenAI 流式 function calling 的 `tool_calls` 是按字段分片的（id、name、arguments 分多次推送），聚合逻辑与 provider 强相关。
- Agent 层只关心"何时执行哪个工具"，不应该了解 OpenAI 的片段协议。
- 这样 SiliconFlow / Ollama 即使不真正流式，也可以一次性 yield 完整 `tool_call`。

### 3. Phase 1 的 `content` 累积但不展示

**决定**：在工具调用循环阶段，如果 provider 返回 `content` 事件，Agent 累积到本地变量用于构建 assistant message，但不 yield 给客户端。

**理由**：
- 当前架构下，最终答案由 Phase 2 的 `stream()` 专门负责输出，避免重复输出或事件类型混乱。
- Phase 1 的 `content` 通常是模型在解释自己的工具调用意图，不是最终答案。
- 保持 SSE 协议简洁：`content` 事件只在 Phase 2 出现（未来如需在 Phase 1 展示思考中的 content，可再扩展）。

### 4. 每轮结束时把完整 reasoning_content 写入 messages

**决定**：Agent 在每轮工具调用结束时，将本轮累积的 `full_reasoning` 和 `full_content` 写入 assistant message 的 `reasoning_content` 和 `content` 字段，再追加 tool message。

**理由**：
- DeepSeek 需要把历史 reasoning_content 传给模型，否则多轮工具调用时模型看不到自己的思考过程。
- 该逻辑与当前非流式实现一致，只是 reasoning_content 从 `LLMResponse` 改由 Agent 累积。

### 5. SiliconFlow / Ollama 做薄适配，不改内部实现

**决定**：SiliconFlow 和 Ollama 的 `stream_with_tools()` 内部继续调用现有的 `invoke_with_tools()` 聚合完整结果，然后把结果包装成 `StreamEvent` 序列。

**理由**：
- 用户明确要求这次不改造 SiliconFlow 和 Ollama。
- 统一接口后，未来升级它们只需要替换内部实现，Agent 层不用动。
- 风险最小，不影响现有运行行为。

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| DeepSeek 流式 tool_call 聚合边界处理错误，导致参数不完整 | 增加单元测试覆盖单工具调用、多工具调用、参数分片等场景；在生产环境用真实请求验证 |
| `finish_reason` 不是可靠的 tool_call 完成信号 | 同时检测流结束和已聚合的 tool_call 缓冲，确保最后一轮也能正确发出事件 |
| Phase 1 的 `content` 被误展示给用户 | Agent 层明确区分：Phase 1 只 yield `reasoning`，`content` 累积到本地变量；Phase 2 才 yield `content` |
| SiliconFlow / Ollama 适配后接口行为变化 | 保留现有非流式聚合逻辑，仅做事件包装；通过单元测试确保返回的事件序列与之前等价 |
| 消息历史中 reasoning_content 丢失或重复 | 在 Agent 中每轮结束时统一写入 assistant message；添加测试验证多轮对话的消息结构 |

## Migration Plan

- **部署**：直接更新 `ai-service` 镜像并重启容器即可，无需数据库迁移或配置变更。
- **回滚**：如果发现问题，回退到上一个 `bluenet-ai-service` 镜像版本。
- **验证**：调用 `POST /ai/v1/chat/stream`，检查 `reasoning` 事件是否为多个片段，且 `tool_call` / `tool_result` / `content` / `done` 行为正常。

## Open Questions

- 当前前端没有 AI 客服页面，未来前端实现时是否需要为 reasoning 设计独立的 UI 区域（例如可折叠的思考过程），还是与 content 统一展示？
- 是否需要把 Phase 1 的 `content` 也展示给用户（例如模型解释自己为什么要调用工具）？目前决定不展示，但可后续讨论。
