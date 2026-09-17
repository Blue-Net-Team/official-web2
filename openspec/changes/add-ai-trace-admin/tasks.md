## 1. 数据层（Flyway 迁移）

- [x] 1.1 编写 `V27__add_ai_trace_tables.sql`：建 `tb_ai_conversation`（`id` / `created_at` / `last_active_at`），补表与列注释
- [x] 1.2 同一迁移建 `tb_ai_turn`（`id` / `conversation_id` / `seq` / `user_input` / `answer` / `prompt` / `events` / `degraded` / `duration_ms` / `created_at`），为 `conversation_id`、`created_at` 建索引，不使用物理外键
- [x] 1.3 启动 compose 基础设施并执行迁移，确认两表结构与索引正确
- [x] 1.4 更新 [docs/05-参考手册/05-01-数据库设计.md](docs/05-参考手册/05-01-数据库设计.md) 补充两张表的说明

## 2. 采集层：结构化事件（Python，测试先行）

- [x] 2.1 新增失败测试：意图判定结果以结构化事件外发，含 `intent` / `confidence` / `action` 三个字段
- [x] 2.2 新增失败测试：预披露事件包含自动生成的标签列表与命中标签（名称 / 相关度分数 / 关联分段数）
- [x] 2.3 新增失败测试：标签生成或检索失败时仍外发预披露事件，且命中列表为空（可与"未执行检索"区分）
- [x] 2.4 新增失败测试：同一提问内工具调用与工具结果事件携带从 1 开始递增的轮次序号
- [x] 2.5 新增失败测试：轮次达上限被代码拦截时，返回可被识别为拦截结果而非真实工具执行结果
- [x] 2.6 新建 `trace/` 模块，定义结构化事件的类型与字段，使 2.1–2.5 通过
- [x] 2.7 在 `agent/agent.py` 把意图判定改为结构化外发，同时保留原有中文 reasoning 文案
- [x] 2.8 在 `agent/graph.py` 的 `pre_disclose_node` 外发预披露事件
- [x] 2.9 在 `tool_executor_node` 为工具事件附加轮次序号
- [x] 2.10 回归确认前端展示行为未变（`reasoning` / `tool_call` / `tool_result` / `content` / `done` 语义不变）

## 3. 采集层：轨迹落库（Python，测试先行）

- [x] 3.1 新增失败测试：一次完整检索对话落库一条记录，含完整事件序列与最终答案
- [x] 3.2 新增失败测试：`REFUSE` / `DIRECT` / `CLARIFY` 三类提问同样各落一条记录，且拒答记录不含检索类工具调用
- [x] 3.3 新增失败测试：客户端中途断开时落库已采集事件并标记 `degraded`
- [x] 3.4 新增失败测试：落库异常时对话结果完整、不向客户端返回错误、只记 warn
- [x] 3.5 新增失败测试：完整 prompt 快照被保存，且 `user_input` 不含预披露拼接的内容
- [x] 3.6 新增失败测试：同一会话内 `seq` 从 1 开始递增，会话 `created_at` / `last_active_at` 正确维护
- [x] 3.7 实现内存事件缓冲 + `finally` 一次性写入，使 3.1–3.6 通过
- [x] 3.8 实现会话创建与最后活跃时间更新
- [x] 3.9 在 `setting.py` 增加采集开关与轨迹库连接配置项

## 4. conversation_id 服务端权威化

- [x] 4.1 新增失败测试：`/ai/v1/chat/stream` 首帧为 `conversation_id` 事件，且先于任何其他事件
- [x] 4.2 新增失败测试：带既有 `conversation_id` 时首帧原样回显；拒答与直接回复路径同样下发首帧
- [x] 4.3 在 `api/chat.py` 实现首帧下发，使 4.1–4.2 通过
- [x] 4.4 `useAiChat` 改为不发送 `conversation_id`，从流首帧读取并存进 state
- [x] 4.5 `ai-chat.service.ts` 的 SSE 解析确保忽略未知事件类型
- [x] 4.6 手工验证：同一页面多轮追问复用同一 id；刷新页面后才产生新会话

## 5. 后端只读接口（Java，TDD）

- [x] 5.1 编写 Repository 集成测试：按会话分页查询、内嵌提问、`messageCount` 等于用户提问数
- [x] 5.2 编写集成测试：时间范围 / 意图 / 动作 / 关键词四类筛选，关键词只作用于 `user_input`
- [x] 5.3 编写集成测试：会话详情返回完整原始事件序列与 prompt 快照；不存在的会话返回 null 而非 500
- [x] 5.4 编写集成测试：统计接口——趋势按会话、分布按提问、兜底检索率、工具使用分布、拒答原因分布；无数据时返回零值
- [x] 5.5 实现领域层实体与 Repository 接口（返回 `Optional<Aggregate>` / `Page<Aggregate>`）
- [x] 5.6 实现基础设施层 DO、Mapper、RepositoryImpl 与转换逻辑
- [x] 5.7 实现应用层 AppService，返回 DTO 与 `PageDTO`
- [x] 5.8 实现 `AdminAiTraceController`，四个 GET 接口，全部标注 `@RequiresPermission`（`ai-trace:list` / `ai-trace:detail` / `ai-trace:statistics` / `ai-trace:gap`）
- [x] 5.9 启动应用确认 `PermissionScanner` 未报权限标识重复
- [x] 5.10 编写权限测试：未登录与缺少权限的访问均被拒绝且不返回数据
- [x] 5.11 确认控制器未暴露任何写方法（无 POST / PUT / DELETE）

## 6. 前端三个页面

- [x] 6.1 新增 `ai-trace.service.ts`，封装三个界面实际使用的只读接口（列表 / 详情 / 统计）；`/gaps` 按设计决定不建页面，故不封装
- [x] 6.2 会话列表页：表格 + 分页 + 四类筛选，展开显示内嵌提问，轮次与消息数分列展示
- [x] 6.3 会话详情页：左栏提问导航 + 右栏原始事件流（意图 / 预披露 / 思考 / 工具调用 / 工具结果 / 最终答案），并可查看完整 prompt 快照
- [x] 6.4 分析面板：概览指标卡、意图分布、动作分布、会话量趋势、检索工具使用、拒答原因
- [x] 6.5 三个页面补齐加载态、空状态与可重试的错误提示
- [x] 6.6 `AdminNav` 侧边栏新增「AI 对话记录」分组（会话列表 / 分析面板）
- [x] 6.7 统一列表与分析面板的指标文案（「消息数」与「用户提问」二选一）

## 7. 保留策略（已取消）

- 决策变更：轨迹数据**全量保留**，不提供清理或归档。
- 原 7.1–7.3（过期清理、孤儿会话清理、清理任务测试）曾实现并验证通过，现已连同配置项一并移除。
- 原因：清理的收益为负 —— 容量本就无压力（万级提问约 270 MB），而短于一年的保留期会
让跨招新季的趋势对比断掉，损害核心用途。详见 `design.md` 的 D11。

## 8. 打包与端到端验证

- [x] 8.1 `cd src/backend && ./mvnw clean compile package` 编译打包后端产物
- [x] 8.2 构建并运行 `bluenet-api-service:latest` 镜像
- [x] 8.3 确认 compose 基础设施已启动（PostgreSQL / Redis / RabbitMQ / MinIO）
- [x] 8.4 检查 3000 端口占用情况，决定复用现有前端服务还是启动 dev
- [x] 8.5 Playwright 验证：发一次检索型对话 → 会话列表出现该会话 → 详情页能看到完整轨迹与 prompt
- [x] 8.6 Playwright 验证：发一次被拒答的问题 → 记录仍存在且事件序列中无检索类工具调用
- [x] 8.7 Playwright 验证：分析面板的意图与动作分布与真实数据一致

## 9. 收尾

- [x] 9.1 更新 [docs/05-参考手册/05-04-AI智能检索客服服务.md](docs/05-参考手册/05-04-AI智能检索客服服务.md) 补充采集层与后台说明
- [x] 9.2 记录决策：按项目约束**不改动** docker-compose.yml，采集开关在 Docker 部署下吃 Python 默认值（`TRACE_ENABLED=true`），已在 05-04 文档中说明
- [ ] 9.3 归档本次变更
