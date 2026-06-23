## Why

当前 `ai-service/agent/agent.py` 中的 `RagAgent` 用手写循环实现了两阶段检索（标签检索 → 分片检索 → 兜底搜索）和工具调用轮次限制。随着检索阶段、工具数量和会话状态管理需求的增加，这种手写状态机越来越难以维护、扩展和可视化。引入 `langgraph.StateGraph` 可以将阶段流转、轮次限制、会话持久化等关注点标准化，同时保留现有 SSE 契约，避免前端改动。

## What Changes

- 新增基于 `langgraph.StateGraph` 的 Agent 工作流，替代 `RagAgent` 中的手写 `_run_two_stage_loop` 循环。
- 保留现有的工具注册表（`ToolRegistry`）和 LLM 提供者抽象（`LLMProvider`），通过轻量适配层接入 LangGraph。
- 保持 `/ai/v1/chat/stream` 的 SSE 事件格式完全不变：`reasoning`、`tool_call`、`tool_result`、`content`、`done`、`error`。
- 将 `Conversation` 与 LangGraph 的 checkpointer 机制对齐，为后续会话持久化打下基础（本次不启用 Redis，仍使用内存）。
- **统一同步与流式对话的实现路径**：`chat()` 与 `chat_stream()` 复用同一个 LangGraph 状态图，消除当前两条独立循环路径可能产生的逻辑差异。
- 添加 `langgraph` 依赖，并补充针对新状态图的单元测试。
- **前端无需修改**；验证时只需确认浮窗对话、流式输出、reasoning、tool 卡片表现与现状一致。

## Capabilities

### New Capabilities

- `ai-service-agent-workflow`: 基于 LangGraph StateGraph 的 RAG Agent 多轮检索工作流，包含预披露、标签扩展、分片检索、兜底搜索、最终生成等节点及阶段跳转条件。

### Modified Capabilities

- （无。本次为纯实现层重构，不改变 `rag-retrieval`、`ai-service-streaming-reasoning` 等已有规范定义的外部行为。）

## Impact

- **受影响代码**：`src/ai-service/agent/agent.py`、`src/ai-service/agent/conversation.py`、`src/ai-service/api/chat.py`。
- **依赖变更**：`src/ai-service/pyproject.toml` 新增 `langgraph`。
- **测试**：新增/更新 `src/ai-service/tests/agent/` 下的状态图流转测试。
- **接口契约**：`/ai/v1/chat` 与 `/ai/v1/chat/stream` 的请求/响应字段不变；前端 `apis/schema/ai-chat.dto.ts`、`apis/services/ai-chat.service.ts`、`hooks/useAiChat.ts` 无需改动。
