## Context

AI 服务（`src/ai-service`，Python + FastAPI + LangGraph）当前把对话状态放在两处内存里：`api/chat.py` 的模块级 `_conversations: dict[str, RagAgent]`，以及 `agent/agent.py` 里每个 agent 的 `MemorySaver` checkpointer。检索轨迹只写 stderr 日志（loguru）。服务重启后全部丢失，开发团队无法回看用户提问、检索过程与作答依据。

已有可复用的基础设施：

- `agent/graph.py` 通过 LangGraph 的 `StreamWriter` 外发 `reasoning` / `tool_call` / `tool_result` / `content` 四类自定义事件，**这份事件流本身就是一份完整的执行轨迹**
- AI 服务与后端共享同一个 Postgres，且已有先例：`tb_rag_*` 表由后端 Flyway 管 schema、AI 服务用裸 SQL 写入（`retrieval/pgvector_store.py`）
- 后端已有成熟 RBAC（`@RequiresPermission` + `PermissionScanner`）、`PageDTO` 分页约定、`ResponseMessage` 响应包裹
- 前端已有 admin 壳子（深色侧边栏 + 浅色/暗色内容区）、Ant Design 6、以及 `/admin/knowledge` 这样的数据管理页先例

硬约束：部署在 5 台 `2C2G` 服务器上（全舰队合计约 10 GiB 内存），且这些机器上已经跑着 Postgres、Redis、RabbitMQ、MinIO、后端、前端、ai-service。

## Goals / Non-Goals

**Goals:**

- 让每个用户提问的执行轨迹（意图判定 → 预披露检索 → 每轮思考与工具调用 → 最终答案）持久化，可回看
- 保存"模型实际收到的完整 prompt"，使"为什么它没看到那段资料"这类问题可被回答
- 提供会话列表 / 会话详情 / 分析面板三个只读视图，服务于产品分析与知识库迭代决策
- 让会话边界可靠（`conversation_id` 服务端权威）
- 零新增容器、零新增服务

**Non-Goals:**

- **不做实时监控 / 告警 / APM。** 这是事后回看工具，不处理流式指标与告警。
- **不替换或升级 LangGraph checkpointer。** `MemorySaver` 重启丢会话上下文的问题依然存在；本次只保证"轨迹可回看"，不保证"会话上下文可恢复"。
- **不做用户级归因。** 不记录提问者身份，只到会话粒度。因此不需要改动鉴权链路，前端仍可直连 AI 服务。
- **不做工作队列。** 不引入 `handled` / `handle_note` 之类的处理状态，后台严格只读。
- **不修 RAG 检索链路本身。** 工具结果中的正文截断（`[:300]`）、标签覆盖不足等问题在本次仅被"观测"，不被修改。
- **不做知识库缺口页。** 缺口判断由人工浏览会话列表得出，不构成一个独立的数据视图。

## Decisions

### D1. 自研后台，复用现有 Postgres —— 不引入现成 LLM 可观测框架

**决定**：自建采集 + 自建查询后台，数据落在现有 Postgres 的新表里。

**理由**：主流方案在此环境下不可行或不划算。

| 方案 | 结论 |
|---|---|
| Langfuse v3 自托管 | 官方最低配置需 Web + Worker + Postgres + Redis + **ClickHouse** + MinIO，合计约 **25.5 GiB**。本项目全舰队总内存约 10 GiB —— 把 5 台机器清空都装不下，且 ClickHouse 单独就要 8 GiB |
| Phoenix (Arize) | 单容器可起，但内存需求不设上限，会吃满一台 2C2G；许可是 Elastic License 2.0（非 OSI）；偏向单人调试工具，无多用户/RBAC |
| Opik | 后端 + Postgres + Redis + ClickHouse（可选），同样超出预算 |
| LangSmith 自托管 | 仅企业版提供，非免费 |

此外，目标受众（开发团队做产品分析与知识库反哺）恰好是这些框架**最不覆盖**的部分——它们擅长单条 trace 的瀑布图排障，不擅长"按意图聚合提问分布"这类产品分析。项目本身也已有自研可观测先例（`api-statistics`、`backend-audit-logging`、`backend-request-logging`）。

**放弃的替代方案**：Langfuse（装不下）；托管 SaaS（学生提问数据出境，且团队无预算）。

### D2. 采集方式是"tee 既有 SSE 事件流"，不是新增埋点

**决定**：在 `api/chat.py` 的 SSE 事件生成器出口处做一次分流。

```
graph.stream(stream_mode="custom")
        │
        ▼
  _event_generator
        ├──▶ yield SSE ──────▶ 前端气泡（行为完全不变）
        └──▶ buffer.append() ─▶ finally 一次性落库
```

**理由**：
- 事件流已经包含 `reasoning` / `tool_call` / `tool_result` / `content`，即为完整轨迹，无需新增埋点体系
- 改动集中在一个函数，不侵入 `agent/` 的业务逻辑
- 后续新增事件类型时采集层自动跟随，不需要同步改两处

**放弃的替代方案**：
- 在 `agent/graph.py` 各节点内埋点 —— 侵入业务逻辑，且**意图分类发生在图外**（`RagAgent.chat_stream` 里、图调用之前），节点埋点覆盖不到
- 使用 LangChain callback / Langfuse CallbackHandler —— `llm_providers/deepseek.py` 直接用裸 `openai` SDK（不是 LangChain 封装），callback 对它不生效；三个 provider 中只有 SiliconFlow / Ollama 走 LangChain

### D3. 落库时机：内存缓冲 + `finally` 一次性写入

**决定**：事件累积在内存 buffer，生成器结束时在 `finally` 中一次性写库。

**理由**：
- 避免每条 delta 都打一次数据库（LLM 的 reasoning delta 非常碎，逐条写会造成严重写放大）
- **客户端中途断开会触发 `GeneratorExit`，`finally` 仍会执行**，这是"断流也落残 trace"的唯一可靠位置
- 异常处理只需一处，天然满足"采集失败不影响对话"

**放弃的替代方案**：边流边写（写放大）；丢到后台线程异步写（断流场景下难以保证完整性，且引入并发状态）。

### D4. 数据模型：两张表 + `events` JSONB，不抽派生标量列

**决定**：

```
tb_ai_conversation             tb_ai_turn（每行 = 一个用户提问）
  id                             id
  created_at                     conversation_id
  last_active_at                 seq                会话内序号
                                 user_input          用户原话
                                 answer              最终答案
                                 prompt    jsonb     完整 prompt 快照
                                 events    jsonb     原始事件流（唯一真相）
                                 degraded  bool
                                 duration_ms
                                 created_at
```

**理由**：
- 规模极小：万级提问、单条含 prompt 约 30 KB，压缩后约 80 MB，无需额外分析库
- `events` JSONB 是唯一真相，不存在"派生字段与事实不一致"的可能
- 列表页需要按意图筛选/展示时，直接 `events->0->>'intent'` 提取即可，Postgres 在几千行规模下毫无压力
- **逃生通道**：若将来聚合查询变慢，用 Postgres 生成列即可加上可索引的字段，**不需要改 Python**：

```sql
ALTER TABLE tb_ai_turn ADD COLUMN intent varchar(64)
  GENERATED ALWAYS AS (jsonb_path_query_first(events, '$[*] ? (@.type == "intent")')->>'intent') STORED;
```

**放弃的替代方案**：
- 每种事件类型一张表 —— 严重过度设计
- 应用层抽 `intent` / `action` / `max_rerank_score` 等标量列 —— 后两者需要解析工具结果里的中文文本（`score 分布: 最高 0.7912`）才能得到，不可靠；且引入双写不一致风险

### D5. 完整 prompt 单独一列，不塞进 `events`

**决定**：`tb_ai_turn.prompt` 独立 JSONB 列，存图执行结束时 `state["messages"]` 的快照。

**理由**：`prompt` 是**某一刻的快照**，`events` 是**按序发生的事件**。若把 prompt 作为 `{"type":"prompt"}` 事件塞进 events，详情页渲染时间线时必须特判跳过它，语义上也不属于"发生过的事"。

**注意**：`pre_disclose_node` 会把最后一条 user message **原地替换**为 enriched 版本（含检索结果），因此快照里的 user message 是富化后的。用户原话必须从 `req.message` 单独取出存 `user_input`。

### D6. `conversation_id` 收归 AI 服务，SSE 首帧下发

**决定**：服务端生成并作为流的第一个事件下发；前端不再 `generateId()`。

```
前端                       AI 服务
  │  POST /chat/stream        │
  │  （不带 conversation_id）  │
  ├──────────────────────────▶│  生成 id
  │  data: {type:"conversation_id", id}   ← 首帧
  │◀──────────────────────────┤
  │  data: {type:"reasoning"...}          ← 其余事件
  │◀──────────────────────────┤
  存进 state，后续请求带上
```

**理由**：本后台以**会话**为单位组织数据。前端 `useAiChat` 用 `generateId()` 生成 id 并只存在 React state 里，**页面一刷新就换新 id**，同一个对话会在列表里裂成多个只有一条提问的碎片会话。会话边界的准确性是这套数据模型的地基，必须先修。

**放弃的替代方案**：前端把 id 持久化到 `localStorage` —— 仍无法处理多标签页与多设备，且把会话身份变成了客户端状态。

### D7. 分工：Python 只写，Java 只读

**决定**：两端共享同一个 Postgres；AI 服务只写轨迹表，后端只读并通过 `@RequiresPermission` 暴露。

```
ai-service (Python)              api-service (Java)              frontend
  写 events + prompt   ──▶  tb_ai_*  ──▶  只读 + RBAC  ──▶  /admin/ai-traces 三页
  （数据在这里）                （Flyway 管 schema）  （权限在这里）
```

**理由**：
- 数据天然产生在 Python 侧（事件流、意图判定、轮次计数都是服务端内存里的东西）
- 权限体系完整地长在 Java 侧，CLAUDE.md 明确要求"所有 REST 接口必须使用 `@RequiresPermission`"。让 Python 再自建一套鉴权是重复建设
- 与 `knowledge-base-management` 的既有先例完全同构（`tb_rag_*` 就是后端管 schema、AI 服务裸 SQL 写、后端读表出后台）
- 加容器数为 0

**放弃的替代方案**：AI 服务直接暴露查询接口 —— 需要第二套鉴权、需要前端再配一个 base URL、违反项目铁律。

### D8. 意图判定从"中文散文"升级为结构化事件

**决定**：新增结构化 `intent` 事件（同时**保留**原有的中文 reasoning 文案，前端展示不变）。

**理由**：当前 `agent/agent.py` 把意图结果只以一句中文写进 reasoning：

```python
content=f"\n[意图识别] 判定为 {intent_result.intent}，进入知识库检索。\n"
```

这句话人看得懂，但机器没法 `GROUP BY`。分析面板的意图分布、动作分布、拒答原因 TOP 3 全部依赖它。同样地，**预披露阶段的标签命中完全不在事件流里**（被拼进了 enriched user message），而它是"AI 怎么找到标签"的第一步，知识库反哺时信息量很大。

**放弃的替代方案**：读取时用正则从中文句子里抠出意图 —— 脆弱、不可测、改文案即失效。

### D9. 统计口径分流：趋势按会话，分布按提问

**决定**：

| 指标 | 聚合单位 | 回答的问题 |
|---|---|---|
| 会话量趋势 | 会话 | 每天有多少人来问 |
| 意图分布 / 动作分布 / 拒答原因 / 工具使用 / 兜底率 | 提问 | 问题的构成是什么 |

**理由**：两种口径回答的是不同问题，强行统一会丢失信息。趋势用会话数才能反映"访问量"；分布用提问数才能反映"问题结构"。

### D10. 「消息数」= 用户提问数，读时聚合不冗余存储

**决定**：列表的「消息数」列 = 该会话下 `tb_ai_turn` 的行数（只算用户侧，不计助手回复）。

**理由**：口径由用户确定。既然等价于 `COUNT(*)`，就不需要在会话表上冗余一列，避免双写。

> 副作用：该指标与分析面板的「用户提问」是同一个量，但用了两个名字。见 Open Questions。

### D11. 数据保留 180 天 + 孤儿会话清理

**决定**：定时任务清理超过保留期的 `tb_ai_turn`，并删除已无提问的 `tb_ai_conversation`。保留期可通过环境变量配置。

**理由**：容量上并非必需（万级提问约 80 MB），但学生的原始提问不应被无限期留档。清理任务可以复用 `main.py` 生命周期里已有的僵尸任务清理模式。

### D12. 前端严格只读，且不做缺口页

**决定**：三个页面（会话列表 / 会话详情 / 分析面板），无任何写操作，无处理状态字段。

**理由**：用户明确选择纯只读。知识库缺口的判断是人工浏览会话列表后得出的结论，不是一个系统状态，因此不构成独立页面（早期原型中的缺口页已删除）。

## 数据访问 SQL（已在真实数据上验证）

读取层需要从 `events` JSONB 中提取意图与工具信息。下列查询已于 2026-09-16 在 `db_blue_net` 的真实采集数据上逐条跑通，Java 层直接沿用。

### 意图与动作提取

```sql
jsonb_path_query_first(events, '$[*] ? (@.type == "intent")')->>'intent'  AS intent
jsonb_path_query_first(events, '$[*] ? (@.type == "intent")')->>'action'  AS action
```

### 提问内的工具轮次（`tool_call.round` 的最大值）

```sql
(SELECT COALESCE(MAX((e->>'round')::int), 0)
   FROM jsonb_array_elements(events) e
  WHERE e->>'type' = 'tool_call') AS tool_rounds
```

### 是否触发兜底语义检索

```sql
(SELECT count(*)
   FROM jsonb_array_elements(events) e
  WHERE e->>'type' = 'tool_call' AND e->>'tool_name' = 'chunk_search') AS fallback_calls
```

### 检索工具使用分布

```sql
SELECT e->>'tool_name' AS tool, count(*) AS calls
  FROM tb_ai_turn t, jsonb_array_elements(t.events) e
 WHERE e->>'type' = 'tool_call'
 GROUP BY 1 ORDER BY 2 DESC
```

### 意图 / 动作分布（按提问）

```sql
SELECT jsonb_path_query_first(events, '$[*] ? (@.type == "intent")')->>'intent' AS intent,
       count(*) AS cnt
  FROM tb_ai_turn GROUP BY 1 ORDER BY 2 DESC
```

### 会话量趋势（按会话，用 `generate_series` 补零）

沿用 `AuditMapper.selectTrends` 的既有写法：以 `generate_series` 生成时间桶，
对 `tb_ai_conversation.created_at` 做 `date_trunc` 聚合后左连接，保证无数据的桶也返回 0。

### 筛选谓词

列表的四类筛选均以 `EXISTS` 子查询实现，避免 `JOIN` 造成的行放大：

```sql
AND (:intent IS NULL OR EXISTS (
      SELECT 1 FROM tb_ai_turn t2 WHERE t2.conversation_id = c.id
        AND jsonb_path_query_first(t2.events, '$[*] ? (@.type == "intent")')->>'intent' = :intent))
```

关键词筛选作用于 `tb_ai_turn.user_input`（不能用富化后的消息），用 `ILIKE`。

## Risks / Trade-offs

- **[prompt 快照体积]** 已实测（2026-09-16，一次真实检索对话）：

  | 轮次类型 | `events` | `prompt` | 合计 |
  |---|---|---|---|
  | 检索型 | 15.5 KB（1431 个事件） | 12.0 KB | **≈ 27 KB** |
  | 拒答 / 直接回复 | 0.7–1.1 KB | 0 B（NULL） | ≈ 1 KB |

  按 27 KB/轮估算，万级提问 ≈ 270 MB，在预算内。

  **意外发现**：单个检索轮的 1431 个事件里，绝大多数是 **reasoning 逐字增量碎片**（DeepSeek 流式输出），它们贡献了 `events` 体积的主体。若日后需要压缩，**合并相邻 reasoning 片段**是最有效的单一手段（可减少约 90% 的事件条数）。系统提示只占 prompt 的约 1/6，不值得单独优化。

- **[同步 LLM 调用阻塞事件循环]** 流式端点虽为 `async def`，但消费的是同步生成器，LLM/嵌入调用会阻塞 uvicorn 事件循环 —— 并发请求实际串行化。实测单个检索轮耗时 23.7 s（其中嵌入调用占 15 s）。→ 本次**不修**（属于既有行为），但它是后台的“数据来源可靠性”隐忧：如果并发上去了，轨迹记录会跟着变慢。

- **[SSE 协议新增首帧事件]** 旧前端可能把未知事件类型当错误处理 → 缓解：前后端同仓同发布；spec 已明确要求客户端忽略未知 `type`。

- **[`MemorySaver` 重启丢会话上下文]** 落库后历史可回看，但会话内多轮上下文仍会断（同一 `thread_id` 的 `MemorySaver` 状态随进程消失）→ 本次不解决，属于 Non-Goals。表现为：用户刷新/重启后继续同一 `conversation_id` 提问时，模型看不到前文，但后台仍能看到完整历史。

- **[意图判定在图外]** 拒答与直接回复不经过状态图 → 采集层必须在 `RagAgent.chat_stream` 层面覆盖三类动作，不能只挂在图的事件流上。spec 已要求每个提问都产生一条记录。

- **[工具结果中的正文截断]** `chunk_search_by_tags` 把 chunk 正文截到 300 字符、`_format_rerank_results` 截到 200 字符后再送入 LLM。后台忠实展示"LLM 实际看到的内容"，但这不等于完整 chunk → 本次**只观测不修改**，见 Open Questions。

- **[开发环境配置漂移]** `docker/docker-compose.yml` 的 `PGVECTOR_URI` 默认值与 `src/ai-service/.env.example` 仍指向不存在的库 `rag`（实际库为 `db_blue_net`，生产由 `docker/.env` 覆盖）。已在本地 `.env` 修正以便开发与测试，**未改动受版本控制的默认值**（按项目维护者要求）。→ 后续建议由项目维护者在受控文件中统一修正。

- **[权限标识全局唯一]** `PermissionScanner` 遇到重复标识会直接让应用启动失败 → 已确认 `ai-trace:*` 四个标识与现有权限无冲突（任务 5.9 需在实现后再次验证）。

- **[grep 风险：AI 服务无鉴权]** AI 服务目前 `allow_origins=["*"]` 且 `/chat/*` 无鉴权。本次**不改变**这一点（因为不做用户级归因），轨迹数据里也不含用户身份，因此泄露面与现状持平。

## Migration Plan

部署顺序（每步都可独立回滚）：

1. **Flyway 迁移**建 `tb_ai_conversation` / `tb_ai_turn`；表为空不影响任何既有功能
2. **Python 采集层**上线 —— 写入失败只 warn，因此即使表未建好服务也能正常提供对话
3. **后端只读接口**上线 —— 无数据时返回空列表与零值统计，前端尚未接入
4. **前端 SSE 改造**（消费首帧 `conversation_id`）+ 采集层首帧改动，**需与服务端同批次发布**（这是唯一的契约变更点）
5. **前端三个页面**上线
6. **清理任务**启用

回滚策略：

- 步骤 2、3、5 各自独立可回滚，互不影响
- 步骤 4 若需回滚，前端恢复自行生成 id 即可，服务端首帧事件对旧前端是"未知类型"（按 spec 应被忽略）
- 步骤 1 的两张表即使保留也不会被读取，可择机删除

## Open Questions

1. **文案口径不统一** —— 列表列名是「消息数」，分析面板指标名是「用户提问」，两者数值完全相同。需要统一成一个词（建议统一为「用户提问」或统一为「消息数」）。

2. **系统提示在 prompt 快照中的固定开销** —— 每个提问都重复存约 2 KB 完全相同的系统提示。是否值得改为只存一份 + hash 引用？默认不做（牺牲快照保真度），待实测体积后再决定。

3. **chunk 正文截断是否在本次一并处理** —— `[:300]` / `[:200]` 的截断让 LLM 只看到 chunk 的前一部分，而 rerank 是按**完整正文**打分的，导致"排序依据被截掉"。

   **已实测（2026-09-16，`db_blue_net`）**：`tb_rag_chunks` 共 82 条，平均 148 字，p50 = 103，p90 = 295，最长 536。

   - 主路径 `chunk_search_by_tags`（截断 300）：仅 7 条（**8.5%**）会被截断，最长一条也只丢掉约 44%。**绝大多数情况下不截断**。
   - 兑底路径 `_format_rerank_results`（截断 200）：22 条（**27%**）会被截断。

   结论：该问题的影响远小于早期估算，**不构成阻塞**。本次只让它可观测。

4. **截断长度不一致** —— 三处不同的截断值：`chunk_search_by_tags` 正文 300、`registry._format_rerank_results` 正文 200、`tag_description` 60（两处）。若后续要治理，建议统一为一个可配置值；基于上一条实测，优先级不高。

6. **检索池规模偏小（新发现，待评估）** —— 全库仅 82 条 chunk、17 个标签。这意味着"标签驱动 + 两阶段 rerank"的检索链路实际是在一个极小的池子里运作，其设计收益（对比单次向量召回）可能远低于预期。属于 RAG 架构层面的问题，**不在本次范围**，但值得单独评估。

7. **配置漂移：`PGVECTOR_URI` 指向不存在的库** —— `docker/docker-compose.yml` 的默认值是 `postgresql://.../rag`，`src/ai-service/.env` 也是 `/rag`，而实际库是 `db_blue_net`（`docker/.env:140` 覆盖了 compose 默认值）。生产部署未被影响，但**本地直接跑 ai-service 会连到不存在的库**。建议本次一并修正配置默认值。

5. **详情页短会话的空白** —— 详情卡高度为撑满，短会话会在底部留出大片空白。当前判断是"真实会话通常更长，卡片需要内部滚动"，因此不做处理。若希望短内容收缩，需改为 `fit_content`。
