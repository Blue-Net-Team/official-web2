## 1. 依赖与基础设施

- [x] 1.1 在 `src/ai-service/pyproject.toml` 添加 `langgraph` 依赖，并锁定与现有 `langchain>=1.2.15` 兼容的版本范围
- [x] 1.2 更新 `src/ai-service/uv.lock`（或等效 lock 文件），确保 CI 可复现安装
- [x] 1.3 确认现有 `llm_providers` 和 `tools` 的接口无需修改即可接入 LangGraph

## 2. LangGraph 状态图实现

- [x] 2.1 新建 `src/ai-service/agent/graph.py`，定义 `AgentState` TypedDict（含 messages、tag_rounds、chunk_rounds、fallback_rounds、final_content、final_reasoning）
- [x] 2.2 实现 `pre_disclose_node`：调用 `tag_generate` + `tag_search_detailed`，将结果注入首条用户消息
- [x] 2.3 实现 `agent_node`：统一读取 `LLMProvider.stream_with_tools` 事件流，构建含 tool_calls 和 reasoning_content 的 assistant 消息；**当本轮无 tool_call 时，直接将 content 作为最终答案写入 state 并结束工作流**，避免额外调用生成节点导致空 assistant 消息干扰模型
- [x] 2.4 实现 `tool_executor_node`：通过 `ToolRegistry.execute` 执行工具，流式模式下外发 `tool_result` 事件，更新轮次计数并将结果写回 state.messages
- [x] 2.5 ~~实现 `generate_node`~~ 已移除：`agent_node` 在无工具调用时直接生成最终答案，简化状态图并修复答案质量
- [x] 2.6 实现条件边路由：`should_continue` 检查 tool_calls 是否已补充对应 tool 消息，决定继续工具循环还是结束工作流
- [x] 2.7 实现 `build_rag_graph()` 工厂函数，组合节点与边；`RagAgent` 编译时配置 `MemorySaver` checkpointer

## 3. Agent 包装类与接口适配

- [x] 3.1 在 `src/ai-service/agent/agent.py` 中重写 `RagAgent` 类，暴露 `chat()` 和 `chat_stream()` 方法（注：未新建 `rag_graph_agent.py`，直接在原文件中替换实现）
- [x] 3.2 在 `chat()` 中调用 `graph.invoke()`，返回 `AgentResponse(content, reasoning)`
- [x] 3.3 在 `chat_stream()` 中调用 `graph.stream(stream_mode="custom")`，将 LangGraph writer 事件转译为现有 SSE 事件格式（reasoning/tool_call/tool_result/content/done）
- [x] 3.4 确保 `chat()` 与 `chat_stream()` 使用同一个 `build_rag_graph()` 实例和节点定义，避免同步/流式逻辑分叉
- [x] 3.5 实现 `reset_conversation()`：清除 `Conversation` 历史（LangGraph 线程状态随新调用自动推进）
- [x] 3.6 保持 `api/chat.py` 的接口签名与导入不变，内部 `RagAgent` 已替换为基于 LangGraph 的实现

## 4. 单元测试

> 按用户要求，本阶段删除新增的单测文件，改为直接启动服务做真实验证。

- [x] 4.1 ~~新增 `src/ai-service/tests/agent/test_graph.py`~~ 已删除
- [x] 4.2 ~~测试标签搜索达到 4 轮后强制进入生成阶段~~ 已删除
- [x] 4.3 ~~测试分片搜索达到 3 轮后强制进入生成阶段~~ 已删除
- [x] 4.4 ~~测试 `chunk_search` 兜底搜索达到 1 轮后强制进入生成阶段~~ 已删除
- [x] 4.5 ~~测试 reasoning_content 在工具调用轮次中透传并写回 conversation~~ 已删除
- [x] 4.6 ~~测试无工具调用时直接生成最终答案~~ 已删除
- [x] 4.7 ~~测试 `chat()` 与 `chat_stream()` 对同一输入产生等价的工具调用序列和最终答案~~ 已删除

## 5. 集成验证

- [x] 5.1 启动 ai-service 与基础基础设施（PostgreSQL、Redis、MinIO、RabbitMQ）
  - 已停止旧版 `bluenet-ai-service` 容器
  - 已用 `uv run uvicorn main:app` 直连现有 Docker 基础设施启动新版 ai-service
  - `GET /ai/v1/health` 返回 `{"status":"ok"}`
- [x] 5.2 使用 curl 调用 `POST /ai/v1/chat/stream`，确认事件序列包含 reasoning、tool_call、tool_result、content、done
  - 已验证：流式事件序列完整，含 reasoning → tool_call → tool_result → content → done
- [x] 5.3 使用 curl 调用 `POST /ai/v1/chat`，确认响应字段 `reply`、`reasoning`、`conversation_id` 与旧实现一致
  - 已验证：返回 `{reply, reasoning, conversation_id}`
- [x] 5.4 启动前端开发服务，验证浮窗 AI 客服的 reasoning 展开、tool 卡片、Markdown 答案显示正常
  - 已验证：浮窗可正常展开思考过程、显示 tool 卡片、流式输出答案
  - 修复了 `should_continue` 在工具达到上限时直接跳 generate 导致的 "insufficient tool messages" 错误
  - **修复了最终答案质量**：移除独立 `generate_node`，改为在 `agent_node` 无工具调用时直接输出最终答案，避免模型看到空内容 assistant 消息而输出元话语
  - 截图验证："蓝网团队是什么？" 与 "怎么报名" 均返回结构化的正式回答
- [x] 5.5 验证多轮对话：同一 `conversation_id` 连续发送消息，上下文能正确累积
  - 已验证：前端连续提问 "蓝网团队是什么？" → "它主要做什么？"，第二句正确指代蓝网团队

## 6. 清理与归档

- [x] 6.1 旧 `RagAgent` 手写循环实现已替换为 LangGraph 版本，`agent/agent.py` 保留模块入口
- [x] 6.2 `RagAgent` 类名保持不变，`api/chat.py` 导入稳定（无需重命名步骤）
- [x] 6.3 运行 `ruff check` 和 `pytest`，无 lint 错误和测试失败（删除测试文件前已验证；graph.py 修复后再次 ruff 通过）
- [x] 6.4 外部接口与架构未变，无需更新文档
- [x] 6.5 按用户要求跳过 `/opsx:archive` 归档
