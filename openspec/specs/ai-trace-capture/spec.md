## ADDED Requirements

### Requirement: Capture must never block or fail the chat path

轨迹采集 SHALL 以 best-effort 方式执行：写入失败、序列化失败或数据库不可用时，MUST NOT 影响用户的对话结果。

#### Scenario: Persistence failure still returns the full answer
- **WHEN** 落库过程抛出异常（数据库不可用、表不存在、序列化失败等）
- **THEN** 服务端 SHALL 记录一条 warn 日志
- **AND** 客户端 SHALL 仍然收到完整且未受损的 SSE 事件流与最终答案
- **AND** 服务端 MUST NOT 向客户端返回错误

#### Scenario: Capture failure does not abort the stream
- **WHEN** 采集层在流式过程中发生异常
- **THEN** SSE 生成器 SHALL 继续向外发送后续事件
- **AND** 后续事件的数量与内容 SHALL NOT 因采集异常而改变

### Requirement: Every user question produces exactly one trace record

无论意图分类的结果是检索、拒答还是直接回复，每个用户提问 SHALL 产生且仅产生一条轨迹记录。

#### Scenario: Retrieval question is recorded
- **WHEN** 意图分类结果为 `RETRIEVE` 且状态图执行完成
- **THEN** SHALL 产生一条轨迹记录，包含完整的执行事件序列与最终答案

#### Scenario: Refused question is recorded
- **WHEN** 意图分类结果为 `REFUSE` 且未进入状态图
- **THEN** SHALL 仍然产生一条轨迹记录，包含意图判定、拒绝话术
- **AND** 该记录的事件序列中 MUST NOT 包含任何检索类工具调用事件

#### Scenario: Direct reply question is recorded
- **WHEN** 意图分类结果为 `DIRECT`（含 `CLARIFY`）
- **THEN** SHALL 仍然产生一条轨迹记录，包含意图判定与直接回复内容

#### Scenario: Classification failure is recorded as clarification
- **WHEN** 意图分类抛出异常或无法解析出结果
- **THEN** SHALL 产生一条轨迹记录，其动作为澄清
- **AND** 记录 SHALL 保留分类失败的事实，以便区分"用户问题模糊"与"分类器故障"

### Requirement: Structured intent event replaces prose reasoning

意图判定结果 SHALL 以结构化事件外发，MUST NOT 只以自然语言文案的形式存在于 reasoning 事件中。

#### Scenario: Intent event carries machine-readable fields
- **WHEN** 意图分类产生结果
- **THEN** SHALL 外发一个结构化意图事件，至少包含 `intent`、`confidence`、`action` 三个字段
- **AND** 该事件的字段值 SHALL 与分类器返回值一致

#### Scenario: Existing prose reasoning is preserved
- **WHEN** 意图判定结果被结构化外发
- **THEN** 原有的中文说明文案 SHALL 继续以 reasoning 事件外发
- **AND** 前端展示行为 SHALL NOT 因此改变

### Requirement: Pre-disclosure retrieval results are emitted as an event

`pre_disclose` 节点执行的标签检索结果 SHALL 外发为结构化事件，MUST NOT 只作为文本拼接进用户消息。

#### Scenario: Pre-disclosure tag hits are captured
- **WHEN** `pre_disclose` 节点完成 `tag_generate` 与 `tag_search_detailed`
- **THEN** SHALL 外发一个预披露事件，包含自动生成的标签列表
- **AND** 包含每个命中标签的名称、相关度分数与关联分段数

#### Scenario: Pre-disclosure failure is captured as empty
- **WHEN** 标签生成或标签检索抛出异常
- **THEN** SHALL 仍然外发预披露事件，其命中列表为空
- **AND** 轨迹记录 SHALL 能够区分"没有命中"与"未执行检索"

### Requirement: Tool round counter is emitted with each tool execution

每轮工具执行 SHALL 携带该提问内的轮次序号，使轨迹能够还原 `agent ⇄ tool_executor` 的循环结构。

#### Scenario: Round number increments per agent decision
- **WHEN** 状态图在同一提问内完成一次 agent 决策并执行工具
- **THEN** 对应的工具调用与工具结果事件 SHALL 携带递增的轮次序号
- **AND** 轮次序号 SHALL 在同一提问内从 1 开始连续递增

#### Scenario: Round limit messages are distinguishable
- **WHEN** 某个阶段的轮次已达上限、工具被代码拦截而返回上限提示
- **THEN** 该事件 SHALL 可被识别为拦截结果而非真实工具执行结果

### Requirement: Full prompt snapshot is persisted

SHALL 保存状态图执行结束时组装完成的完整消息列表，以便还原模型实际收到的上下文。

#### Scenario: Snapshot contains the assembled message list
- **WHEN** 状态图执行结束
- **THEN** SHALL 保存该时刻的完整消息列表快照
- **AND** 快照 SHALL 包含系统提示、预披露富化后的用户消息、每轮的助手消息与工具结果消息

#### Scenario: Original user input is stored separately from the enriched message
- **WHEN** 保存轨迹记录
- **THEN** SHALL 单独保存用户的原始提问文本
- **AND** 该字段 SHALL NOT 包含预披露阶段拼接的检索结果
- **AND** 快照中的用户消息 SHALL 保留富化后的版本

#### Scenario: Prompt snapshot is optional for non-retrieval questions
- **WHEN** 意图分类结果为 `REFUSE` 或 `DIRECT`（未执行状态图）
- **THEN** 记录 SHALL 被标记为不含 prompt 快照
- **AND** 该字段为空 MUST NOT 被视为记录不完整

### Requirement: Abandoned streams still produce a partial trace

客户端中途断开连接时，SHALL 仍然落库已采集到的部分轨迹，并标记该记录为不完整。

#### Scenario: Client disconnects mid-stream
- **WHEN** 客户端在 SSE 流未结束时断开连接
- **THEN** SHALL 落库已采集的事件序列
- **AND** 该记录 SHALL 被标记为 degraded
- **AND** 该记录 SHALL 保留已生成的最终答案（若非空）

#### Scenario: Degraded flag is queryable
- **WHEN** 管理员查询会话列表
- **THEN** 不完整的记录 SHALL 可被识别，且 SHALL NOT 被静默丢弃

### Requirement: Traces are ordered within a conversation

同一会话内的多条轨迹 SHALL 具有稳定的先后顺序，可用于还原对话时间线。

#### Scenario: Sequence number assigned per conversation
- **WHEN** 一个会话内的用户提问被依次记录
- **THEN** 每条记录 SHALL 带有会话内递增的序号
- **AND** 序号 SHALL 从 1 开始

#### Scenario: Conversation lifecycle is tracked
- **WHEN** 一个新会话产生第一条轨迹
- **THEN** SHALL 记录该会话的创建时间
- **AND** 后续每次记录 SHALL 更新该会话的最后活跃时间

### Requirement: Trace data is retained indefinitely

轨迹数据 SHALL 全量保留，系统 MUST NOT 提供自动删除、过期清理或归档能力。

保留完整历史是为了支持跨招新季（年度周期）的问题趋势对比。实测存储开销可控：
单轮检索型提问约 27 KB，万级提问约 270 MB。

#### Scenario: No automatic deletion happens
- **WHEN** 轨迹记录已存在任意长时间
- **THEN** 系统 MUST NOT 自动删除该记录
- **AND** 服务启动与运行期间 MUST NOT 调度任何轨迹清理任务

#### Scenario: No retention configuration exists
- **WHEN** 检查轨迹采集的配置项
- **THEN** MUST NOT 存在保留期、保留天数或清理相关的配置项

#### Scenario: Storage growth stays within a single database
- **WHEN** 轨迹累积到万级提问规模
- **THEN** 存储占用 SHALL 仍在单个 Postgres 实例的可承受范围内
