## Why

AI 服务（`src/ai-service`）目前把用户提问、检索过程、模型思考全部留在内存里：会话存在 `api/chat.py` 的 `_conversations` 字典，图状态存在 LangGraph 的 `MemorySaver`，检索轨迹只写 stderr 日志。**服务一重启，这些数据全部消失**，开发团队无法回答"用户问了什么、AI 检索到了什么、为什么这么答"。

直接后果有两个：

1. **知识库迭代靠感觉** —— `docs/ai-knowledge-base/` 该补哪篇文档，没有真实提问分布和检索命中情况作为依据。
2. **检索链路不可观测** —— 标签覆盖不足、兜底语义检索被触发、答案质量差这些问题，目前只能靠用户口头反馈发现。

现在做的成本极低：`agent/graph.py` 已经通过 `StreamWriter` 把 `reasoning` / `tool_call` / `tool_result` / `content` 四类事件实时外发给前端，**这份事件流本身就是一份完整的执行轨迹**。采集层只需要在既有事件流上做一次 tee 并落库，不需要新建埋点体系。

## What Changes

### 采集层（Python）

- 在 `chat_stream` 的 SSE 生成器上做一次 tee：事件照常外发前端，同时累积到内存缓冲
- 生成器结束（含客户端中途断开）时一次性落库，异常只记 warn，**绝不阻塞或中断对话**
- 新增三类结构化事件，补上现有事件流缺失的信息：
  - 意图判定结果（`intent` / `confidence` / `action`）——目前只是一句中文 reasoning 文案，无法筛选
  - 预披露阶段的标签命中（`tag_generate` + `tag_search_detailed` 的结果）——目前被拼进 enriched user message，完全不在事件流里
  - 每轮工具调用的轮次计数——目前只存在于 `AgentState`，从不外发
- 额外保存**完整 prompt 快照**（图执行结束时 `state["messages"]` 的内容），用于回答"模型实际看到了什么"
- 用户原话单独带出（`pre_disclose_node` 会把 user message 原地替换成 enriched 版本，原始输入只存在于 `req.message`）

### 数据层

- 新增 `tb_ai_conversation` / `tb_ai_turn` 两张表，由后端 Flyway 管理 schema（与 `tb_rag_*` 同一模式，AI 服务只负责写入）
- 数据保留策略：全量保留 + 180 天清理任务

### 读取层（Java + Next.js，纯只读）

- 后端新增 `AdminAiTraceController`，4 个只读接口，全部标注 `@RequiresPermission`
- 分页统一返回 `PageDTO<T>`，响应统一 `ResponseMessage<T>`
- 前端新增 `/admin/ai-traces` 下三个页面：会话列表、会话详情、分析面板
- 侧边栏「AI 对话记录」分组（会话列表 / 分析面板）

### **BREAKING**: `conversation_id` 归属变更

- 由 AI 服务生成并回传，前端不再用 `generateId()` 生成
- `/ai/v1/chat/stream` 的 SSE 流新增**首帧** `{"type": "conversation_id", "conversation_id": "..."}` 事件
- 前端 `useAiChat` 需消费该事件

变更理由：本后台以**会话**为单位组织数据，而前端生成的 id 在页面刷新后就换了新的，"一个对话"的边界会被切碎，列表里会出现大量只有一条消息的碎片会话。会话边界准确性是这套数据模型的地基。

## Capabilities

### New Capabilities

- `ai-trace-capture`: AI 服务侧的对话轨迹采集与持久化 —— 结构化事件、完整 prompt 快照、tee 式落库、不阻塞对话、断流也落残 trace
- `ai-trace-admin`: 面向开发团队的只读轨迹查询后台 —— 会话列表 / 会话详情 / 分析面板三个视图，及其后端只读接口与权限控制

### Modified Capabilities

- `ai-service-streaming-reasoning`: SSE 协议新增首帧 `conversation_id` 事件类型
- `ai-service-agent-workflow`: `conversation_id` 由服务端生成并在流式响应中回传客户端

## Impact

### 代码

| 位置 | 改动 |
|---|---|
| `src/ai-service/api/chat.py` | SSE 生成器 tee + 落库 + 首帧 conversation_id 事件 |
| `src/ai-service/agent/agent.py` | 意图判定结果、轮次计数改为结构化事件外发 |
| `src/ai-service/agent/graph.py` | `pre_disclose_node` 外发标签命中事件 |
| `src/ai-service/trace/` (新增) | 事件缓冲、结构化事件定义、落库写入器 |
| `src/ai-service/setting.py` | 数据保留天数等配置项 |
| `src/backend` (新增) | Controller / AppService / Repository / Mapper + Flyway 迁移 |
| `src/frontend/src/app/admin/ai-traces/` (新增) | 三个页面 |
| `src/frontend/src/hooks/useAiChat.ts` | 消费 conversation_id 事件，不再自行生成 |
| `src/frontend/src/components/Admin/AdminNav/index.tsx` | 侧边栏新增「AI 对话记录」分组 |

### 接口

- 新增：`GET /api/v1/admin/ai-traces/conversations`、`/conversations/{id}`、`/statistics/*`（只读）
- 变更：`POST /ai/v1/chat/stream` —— 新增首帧 `conversation_id` 事件（向后兼容：未知事件类型可忽略）

### 数据库

- 新增表 `tb_ai_conversation`、`tb_ai_turn`；无物理外键；表名遵循 `tb_` 前缀约定
- 复用现有 Postgres 实例，**不引入任何新容器**（部署环境为 5 × 2C2G，自托管 Langfuse 等方案需 6 个容器、约 25 GiB 内存，不可行）

### 权限

- 新增 4 个全局唯一权限标识：`ai-trace:list`、`ai-trace:detail`、`ai-trace:statistics`、`ai-trace:gap`（已确认与现有权限无冲突）

### 风险

- 完整 prompt 快照数据量约 15–20 KB/提问，需在实现中确认增长速度
- AI 服务与后端共享 Postgres，写放大需控制在低水位（best-effort + 异步）
