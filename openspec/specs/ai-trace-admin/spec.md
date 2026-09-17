## ADDED Requirements

### Requirement: Conversation list is paginated and grouped by conversation

后台 SHALL 提供一个按会话组织的分页列表接口。

#### Scenario: Default pagination
- **WHEN** 客户端请求会话列表且未指定分页参数
- **THEN** SHALL 返回第 1 页、每页 20 条
- **AND** 按会话最后活跃时间倒序排列

#### Scenario: Response shape follows the project envelope
- **WHEN** 客户端请求会话列表
- **THEN** SHALL 返回 `ResponseMessage<PageDTO<ConversationDTO>>`
- **AND** MUST NOT 返回 Spring Data 的 `Page<T>`

#### Scenario: Conversations embed their questions
- **WHEN** 返回会话列表
- **THEN** 每个会话 SHALL 内嵌其下的提问摘要列表
- **AND** 每条提问摘要 SHALL 至少包含提问文本、意图、动作、轮次、耗时
- **AND** 单个会话内联的提问数量 SHALL 有上限，超出部分以剩余数量表示

#### Scenario: Message count reflects user questions only
- **WHEN** 返回会话的 `messageCount`
- **THEN** 该值 SHALL 等于该会话下的用户提问数量
- **AND** MUST NOT 把助手回复计入

### Requirement: Conversation list supports filtering

列表 SHALL 支持按时间范围、意图、动作与关键词筛选。

#### Scenario: Filter by intent
- **WHEN** 客户端指定意图筛选条件
- **THEN** SHALL 只返回包含该意图提问的会话
- **AND** 返回的会话内嵌提问 SHALL 只保留匹配该意图的条目

#### Scenario: Filter by action
- **WHEN** 客户端指定动作筛选条件（检索 / 拒答 / 直接回复）
- **THEN** SHALL 只返回包含该动作提问的会话

#### Scenario: Keyword search over user input
- **WHEN** 客户端指定关键词
- **THEN** SHALL 对用户原始提问文本执行模糊匹配
- **AND** 匹配 SHALL NOT 作用于预披露富化后的文本

#### Scenario: Filter by time range
- **WHEN** 客户端指定时间范围
- **THEN** SHALL 按会话最后活跃时间过滤

### Requirement: Conversation detail returns the raw event sequence

详情接口 SHALL 返回指定会话的完整原始执行事件序列，MUST NOT 对事件内容做派生、聚合或改写。

#### Scenario: Detail returns ordered events per question
- **WHEN** 客户端请求某个会话的详情
- **THEN** SHALL 按会话内序号返回全部提问记录
- **AND** 每条记录 SHALL 按发生顺序返回其完整事件序列
- **AND** 事件类型 SHALL 至少覆盖意图判定、预披露、思考、工具调用、工具结果、最终答案

#### Scenario: Tool call arguments and results are returned verbatim
- **WHEN** 返回工具调用与工具结果事件
- **THEN** SHALL 返回采集时记录的原始参数与原始结果文本
- **AND** MUST NOT 在读取层重新格式化或截断

#### Scenario: Prompt snapshot is returned when present
- **WHEN** 该提问存在完整 prompt 快照
- **THEN** 详情 SHALL 返回该快照
- **AND** 快照缺失时 SHALL 以明确的空值表示，而非报错

#### Scenario: Detail of a non-existent conversation
- **WHEN** 客户端请求不存在的会话 ID
- **THEN** SHALL 返回 null 数据与成功状态码
- **AND** MUST NOT 抛出 500

### Requirement: Statistics use different aggregation units for trend and distribution

统计接口 SHALL 对趋势类指标按会话聚合、对分布类指标按提问聚合。

#### Scenario: Trend is aggregated by conversation
- **WHEN** 客户端请求会话量趋势
- **THEN** SHALL 按时间桶返回该区间内新增的会话数量
- **AND** 时间桶粒度 SHALL 按查询周期自适应（24 小时按小时、7 天与 30 天按天）

#### Scenario: Intent distribution is aggregated by question
- **WHEN** 客户端请求意图分布
- **THEN** SHALL 按提问统计各意图的出现次数
- **AND** SHALL NOT 按会话统计

#### Scenario: Action distribution is aggregated by question
- **WHEN** 客户端请求动作分布
- **THEN** SHALL 按提问统计检索、拒答、直接回复三类动作的次数与占比

#### Scenario: Fallback retrieval rate is reported
- **WHEN** 客户端请求概览指标
- **THEN** SHALL 返回兜底语义检索的触发比例
- **AND** 该比例 SHALL 以触发兜底的提问数除以总提问数计算

#### Scenario: Tool usage distribution is reported
- **WHEN** 客户端请求检索工具使用情况
- **THEN** SHALL 按工具名返回调用次数降序排列
- **AND** 兜底语义检索 SHALL 在结果中可被识别

#### Scenario: Refusal reasons are reported
- **WHEN** 客户端请求拒答原因分布
- **THEN** SHALL 按被拦截的意图返回次数降序排列

### Requirement: Trace admin is read-only

后台接口 SHALL 只提供读取能力，MUST NOT 提供任何写入、修改或删除轨迹数据的接口。

#### Scenario: Only read endpoints are exposed
- **WHEN** 检查 `AdminAiTraceController` 暴露的路由
- **THEN** 全部路由 SHALL 使用 GET 方法

#### Scenario: No state is written back from the admin UI
- **WHEN** 管理员在任意后台页面操作
- **THEN** MUST NOT 产生对轨迹数据的写操作
- **AND** 页面上的操作 SHALL 限于筛选、分页、跳转到详情与跳转到知识库管理

### Requirement: Trace admin enforces permission control

所有轨迹后台接口 SHALL 声明 `@RequiresPermission` 并指定全局唯一的权限标识。

#### Scenario: Permission identifiers are unique
- **WHEN** 应用启动并执行 `PermissionScanner` 扫描
- **THEN** 四个权限标识 `ai-trace:list`、`ai-trace:detail`、`ai-trace:statistics`、`ai-trace:gap` SHALL 全部全局唯一
- **AND** 应用 SHALL NOT 因权限重复而启动失败

#### Scenario: Unauthenticated access is rejected
- **WHEN** 未登录用户请求任意轨迹后台接口
- **THEN** SHALL 被拒绝且不返回任何轨迹数据

#### Scenario: Unauthorized access is rejected
- **WHEN** 已登录但缺少对应权限的用户请求轨迹后台接口
- **THEN** SHALL 返回权限不足的响应
- **AND** MUST NOT 返回轨迹数据

### Requirement: Empty and error states are handled explicitly

后台页面 SHALL 对无数据与请求失败给出明确状态，MUST NOT 呈现空白或静默失败。

#### Scenario: No conversations in range
- **WHEN** 筛选区间内没有任何会话
- **THEN** 列表 SHALL 展示空状态提示
- **AND** SHALL NOT 展示空表格骨架

#### Scenario: Statistics with no data
- **WHEN** 统计区间内没有任何提问
- **THEN** 统计接口 SHALL 返回零值而非报错
- **AND** 页面 SHALL 能区分"零值"与"请求失败"

#### Scenario: Request failure is visible
- **WHEN** 任一后台接口请求失败
- **THEN** 页面 SHALL 展示可重试的错误提示
