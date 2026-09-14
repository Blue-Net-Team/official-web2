## Context

AI Service 的 `RagAgent`（LangGraph 状态图）通过 `ToolRegistry` 调用 `software_resource_search(query, direction)` 回答软件下载类问题。该工具把 `query` 原样作为后端 `keyword` 参数，后端执行单串 `LIKE '%keyword%'` 模糊匹配。

实际对话中出现了稳定复现的失败：用户问"嵌入式方向需要下载什么软件"时，Agent 生成 `query="Keil STM32 嵌入式 开发环境 烧录"`，整串无法命中 `name` / `category` / `description` 任一字段，返回"未找到匹配的软件资源"。

问题由两层缺陷叠加造成：

```
流程层：把"有哪些软件"（发现型问题）交给"查找型工具"，迫使 LLM 编造关键字串
工具层：缺少"按方向浏览"入口；匹配是整串 conjunctive substring，无分词、无容错、无降级
```

同时存在一个未被利用的资源：`docs/ai-knowledge-base/19-各方向所需软件及其下载相关说明.md` 已按方向给出团队推荐（含"考核指定"）的软件清单，恰好可以承担"该用哪些"的判定职责。

现状约束：

- 后端 `GET /api/v1/software-resources` 只提供**列表**接口（`direction` + `keyword` + 分页），**没有公开的按 id 查详情接口**。
- 后端 `SoftwareResourceRepositoryImpl.findActiveByDirection` 在传入方向时会自动并入 `GENERAL`：

  ```java
  List<SoftwareResourceDirection> directions = direction == null
          ? null
          : List.of(direction, SoftwareResourceDirection.GENERAL);
  ```

  因此 `direction=EMBEDDED` 的语义实际是 `EMBEDDED ∪ GENERAL`，跨方向通用软件（如 VSCode）不会漏。
- 数据库镜像为 `pgvector/pgvector:pg17`，数据量预期为几十条量级。
- 现有 spec：`openspec/specs/ai-software-resource-tool/spec.md`（工具契约）、`openspec/specs/ai-service-agent-workflow/spec.md`（状态图与轮次上限）。

## Goals / Non-Goals

**Goals:**

- 宽泛问题（"嵌入式要什么软件"）能返回"团队推荐的软件 + 下载链接"的合并结果。
- 具体问题（"SolidWorks 在哪下载"）能以单次调用返回下载链接。
- 资源库规模增长到上百甚至更多时，链路仍能工作而不把整表塞进 LLM 上下文。
- 容忍软件名写法差异（文档 `keil uvision5` vs 资源表 `Keil uVision5`）。

**Non-Goals:**

- 不改后端接口、不改数据库 schema、不引入 `pg_trgm` / `pgroonga` / `pg_search` 等扩展。
- 不把软件资源向量化进 RAG 知识库。
- 不做资源清单缓存 / 快照。
- 不新增面向运营的可维护别名表，也不在代码中内置任何软件别名数据。
- 不改动前端 `/resources` 页面行为。

## Decisions

### 1. 拆成"发现"与"查找"两条分支

**选择**：`SOFTWARE_DOWNLOAD` 意图下再分两支。

```
                  用户问软件
                       │
          ┌────────────┴─────────────┐
          │                          │
   点名了具体软件              只说方向 / 用途
   （"SolidWorks 在哪下载"）    （"嵌入式要什么软件"）
          │                          │
          ▼                          ▼
   lookup(names=[...])        ① list(direction)      取资源索引
                               ② chunk 检索软件清单文档
                               ③ LLM 从 chunk 抽软件名
                               ④ lookup(names=[...])  取下载链接
                               ⑤ 合并生成回答
```

**理由**：两类问题的信息需求完全不同。前者只需一次精确定位；后者需要"有哪些"（资源表）与"该用哪些"（知识库）两个来源做交集。混用会导致 LLM 用编造的关键字串去回答发现型问题。

**替代方案**：保持单工具，只把 `query` 改可选并加后端兜底。排除了——即使匹配变好，工具也无法回答"该用哪些软件"，仍然缺少团队推荐维度。

### 2. 资源索引与资源明细分层（"方案 3"）

**选择**：列表工具只返回 `name` / `category` / `direction`（轻量投影），明细工具按名返回完整字段（含 `externalUrl`）。

**理由**：小数据量下"一次返回全量（含链接）"完全够用，但一旦库里上百条，完整字段（描述 + URL）会把上下文撑爆。分层后：

```
第1步 list:   300 条 × ~30 字符  ≈ 9K 字符      轻
第2步 doc:    筛选出推荐 6 个
第3步 lookup:   6 条 × ~200 字符 ≈ 1.2K 字符    重但极少
```

**替代方案**：
- *一次全量返回*：简单，但数据量上升后上下文与延迟不可控。
- *截断 + 上报剩余条数*：单独使用会漏答案——被文档推荐但排在截断线之后的条目永远取不到。
- *新增公开 `GET /software-resources/{id}` 接口*：语义最干净，但需要改后端；而 `lookup` 用"索引里的规范名"回查列表接口即可命中，无需新接口。

### 3. 工具侧投影，后端零改动

**选择**：`software_resource_list` 内部翻页取全量，然后在工具内丢弃 `description` / `externalUrl` 再返回给 LLM。

**理由**：省的是 **LLM 上下文**，不是带宽。AI Service 与后端同内网，多传几个字段无所谓；但进入 LLM 上下文后每一条都要反复计价。因此无需后端新增投影接口。

### 4. 用两个工具，而不是一个工具带 mode 参数

**选择**：注册 `software_resource_list(direction)` 与 `software_resource_lookup(names, direction)` 两个工具。

**理由**：两者参数形态与用途差异大（一个按方向、无必填参数；一个必须给名称列表）。分成两个工具后，LLM 的工具选择不需要额外推理"该不该传 query"，触发更稳定。

**替代方案**：单工具 + `mode` 参数。排除了——参数组合的语义歧义（`mode=index` 时 `query` 该不该给？）比多一个工具描述的维护成本更高。

### 5. 移除 `software_resource_search`，由 `lookup` 统一承接

**选择**：删除 `software_resource_search(query, direction)`。分支 A 与方案 3 的第 3 步都由 `software_resource_lookup(names, direction)` 承担。

**理由**：`lookup` 天然覆盖两者（单名 = 分支 A，多名 = 第 3 步），避免保留一个语义重复的旧工具。该工具仅在 AI Service 内部注册，不构成对外破坏性变更。

### 6. 名称容错匹配放在 AI Service 本地

**选择**：`lookup` 内部按顺序尝试 归一化精确 → 双向子串 → 相似度阈值，匹配对象是**已经拉取到本地的候选集**。

**理由**：
- 方案 3 的第 1 步已经把该方向的清单拉进进程，再发一次 SQL 模糊查询等于把自己手里的答案绕圈问回去。
- 大小写、空格、子串、拼写容错在 Python 里表达力远超 SQL `LIKE`。
- 完全不触碰数据库扩展与镜像。

**替代方案**（记录为 Future Work，不在本次实现）：

| 方案 | 能力 | 为何不选 |
|---|---|---|
| `pg_trgm`（contrib，镜像已带） | GIN 加速 LIKE + `similarity()` 打分 + 拼写容错 | 数据量小时收益为零；保留为数据上涨后的首选退路 |
| `pg_bigm` / `pgroonga` | CJK 全文检索更好 | 需自定义镜像；本场景匹配对象是专有名词而非中文长句 |
| `ParadeDB / pg_search` | 真 BM25，最接近 ES | 数据量不匹配，运维成本高 |
| `zhparser` / `pg_jieba` | 中文分词 | 本场景不需要分词；PG16+ 编译有坑 |

### 7. 别名不落数据，由 Agent 提供多写法候选

**选择**：工具内不维护任何别名表。`lookup` 的 `names` 接受名称列表，Agent 在不确定资源库写法时，根据自身世界知识在同一次调用中传入多个候选（如 `["vscode", "VS Code", "Visual Studio Code"]`）。

**理由**：
- 硬编码 `_ALIASES` 会腐烂：软件名与简称持续变化，代码里的表无法被使用方维护，漏项时表现为静默不命中。
- 别名本质是语义知识，模型比一张静态表更擅长，且不增加维护成本。
- `lookup` 已支持批量名称，候选写法不需要额外的 LLM 回合。
- 所有候选均未命中时已有确定降级路径（说明未找到 + 给出资源库页面链接），漏项不会变成静默失败。

**替代方案**：
- *代码内置 `_ALIASES` 表*：实现简单，但需要发版维护，且与“先不动数据库”之外的又一处硬编码数据。
- *后端 `tb_software_resource` 加 `aliases` 列*：可运营维护，但本次明确不改数据库；保留为 Future Work。

### 8. 依赖后端"方向查询自动并入 GENERAL"的既有行为

**选择**：`software_resource_list(direction="EMBEDDED")` 不额外请求 GENERAL；直接依赖后端已实现的 `direction ∪ GENERAL` 语义。

**理由**：后端已有该行为（前端 `/resources` 各方向 tab 亦依赖它）。在 spec 中显式记录这一依赖，避免后端未来改动时静默破坏。

### 9. 调用上限

**选择**：状态图对 `software_resource_list` 限制 1 轮，对 `software_resource_lookup` 限制 2 轮，超限返回提示并要求基于已有结果作答。

**理由**：分支 B 正常只需 `list × 1 + lookup × 1`。"所有方向"通过省略 `direction` 一次拿全，因此 1 轮足够；`lookup` 留 1 轮纠错余量。

### 10. 结果合并与降级规则

点名软件未命中时，Agent 已用多种写法候选 + 工具的机械匹配（归一化、子串、相似度）排除过常见写法差异；仍然未命中则说明软件很可能确实不在资源库，或写法差异超出模型可猜测的范围。此时仅回复"未收录"既无帮助也无法自查，因此在 prompt 中要求输出资源库页面链接，把兜底动作交给用户。方向可知时给出对应 tab，否则给出资源库首页。

| 情况 | 输出 |
|---|---|
| 文档提到 + 资源库有 | 名称 + 说明 + `[名称](URL)` |
| 文档提到 + 资源库无 | "团队推荐 X，但资源库暂未收录" |
| 资源库有 + 文档未提 | 不输出 |
| 点名软件未命中 | "未找到，可能未收录或名称写法不同" + 资源库页面链接（`/resources?tab=<方向key>` 或 `/resources`） |
| chunk 检索不到软件清单文档 | 返回资源库跳转链接 `/resources?tab=<方向>` |
| 后端不可用 | 沿用现有降级话术（"软件资源服务暂不可用，请稍后重试"） |

## Risks / Trade-offs

- **文档 19 未被召回** → 预披露的 tag 流程主要面向报名/考核，软件类问题不保证命中该文档。缓解：prompt 中显式引导检索"各方向所需软件"分片并保留 `chunk_search` 兜底；即使最终失败也降级为 `/resources` 跳转链接，而非报"未找到"。
- **LLM 名称对齐出错**（文档 `AutoACD` → 索引 `AutoCAD`）→ 缓解：本地机械匹配多级回退 + Agent 多写法候选；未命中项显式上报，Agent 可据此补充候选或说明"未找到"并给出资源库链接，而不是静默丢失。
- **写法候选给不全** → 缓解：未命中名称已在返回文本中显式上报，Agent 可补一轮候选；最终仍有确定降级路径（说明未找到 + 资源库页面链接），不会静默失败。
- **lookup 内部 fan-out 放大后端 QPS** → 缓解：单次 `lookup` 最多接收有限个名称（建议 10 个），超出截断并提示；内部串行请求。
- **一次全量拉取在数据量剧增时退化为大响应** → 缓解：`list` 内部翻页设总量上限（如 500 条）并在超限时明确告知 Agent 结果不完整，由 Agent 改用 `lookup` 精确查询。
- **移除 `software_resource_search` 使既有测试失效** → 缓解：本次同步删除 `tests/tools/test_software_resource_search.py`（其测试主体已不存在）；本次不为 AI Service 新增测试。

## Migration Plan

1. 合并 AI Service 代码改动（工具、注册表、prompt、状态图）。
2. 无需后端发布、无需数据库迁移、无需环境变量变更。
3. 回滚：单独回滚 AI Service 镜像即可，前端与后端不受影响。

## Open Questions

- `lookup` 单次可接受的最大名称数量取 10 还是 20？（倾向 10，兼顾 round trip 与覆盖度）
- 相似度匹配的阈值与算法（`difflib.SequenceMatcher` vs `rapidfuzz`，是否需要引入新依赖）留待上线后根据实际未命中情况标定。
- 资源库页面跳转链接的 query 参数是否稳定为 `/resources?tab=<key>`（当前前端 `TABS` key 为 `general` / `computer_vision` / `structural_design` / `embedded`）——如前端调整需同步。
