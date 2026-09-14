## Why

考生问"嵌入式方向需要下载什么软件"这类宽泛问题时，Agent 会把方向词与用途词拼接成 `"Keil STM32 嵌入式 开发环境 烧录"` 当作关键字传给 `software_resource_search`，而后端是整串 `LIKE '%...%'`，必然 0 命中，最终回答"未找到匹配的软件资源"。根因有两层：流程上把"有哪些软件"这个发现型问题交给了查找型工具；工具上既没有"按方向浏览"的入口，也缺乏对软件名的容错匹配。

## What Changes

- **BREAKING**（仅内部工具契约）：移除 `software_resource_search(query, direction)`，替换为两个职责清晰的工具：
  - `software_resource_index(direction)`：按方向返回资源**索引**（名称/分类/方向，不含描述与链接），供 Agent 判断"该方向有哪些软件"。
  - `software_resource_lookup(names, direction)`：按软件名列表返回**完整资源**（含下载地址），工具内部完成名称匹配并报告未命中项。
- `software_resource_index` 在工具内部自动翻页取全量，并对结果做轻量投影（丢弃 description / externalUrl），避免把整表重字段塞进 LLM 上下文。
- `software_resource_lookup` 接受名称**列表**并内部 fan-out 查询，同时对名称做本地容错匹配（归一化精确 → 双向子串 → 相似度），解决 `keil uvision5` / `SolidWorks` 等大小写与拼写差异；工具不内置别名表，同一软件的不同写法由 Agent 在同一次调用中作为多个候选传入。
- 明确软件类问题的两条分支流程：
  - **点名软件**（"SolidWorks 在哪下载"）→ 直接 `software_resource_lookup`。
  - **只给方向**（"嵌入式要什么软件"）→ `software_resource_index` → 知识库检索软件清单文档 → Agent 从 chunk 正文抽取软件名 → `software_resource_lookup` 取链接 → 合并回答。
- 回答合并规则：文档提到但资源库未收录的软件，输出"团队推荐 X，资源库暂未收录"；资源库有但文档未提到的条目不输出。
- 点名软件未命中时的别名引导：点名查询在考虑别名与相似写法后仍未命中时，输出资源库页面链接（已知方向为 `/resources?tab=<方向key>`，否则为 `/resources`）引导用户自行查找，而不是仅回复"未收录"。
- 知识库未检索到软件清单时的降级：直接返回资源库页面跳转链接 `/resources?tab=<方向>`，由用户自行查看。
- 为软件类工具增加调用上限（`index` ≤ 1 次、`lookup` ≤ 2 次），与现有 tag/chunk 轮次限制一致由状态图强制执行。
- 更新 system prompt：宽泛问题**禁止**编造关键字串，只传方向。

## Capabilities

### New Capabilities

- `ai-software-discovery-flow`: 软件类问题的两分支处理流程，包含方向浏览、知识库软件清单抽取、按名取链接、结果合并与降级策略。

### Modified Capabilities

- `ai-software-resource-tool`: 工具契约由单个 `software_resource_search` 改为 `software_resource_index` + `software_resource_lookup`；新增工具侧索引投影、名称容错匹配、按名批量查询与未命中上报。
- `ai-service-agent-workflow`: 状态图的按工具轮次限制新增 `software_resource_index` / `software_resource_lookup` 两条上限。

## Impact

- **AI Service**：`src/ai-service/tools/software_resource_search.py`（重写为 index/lookup 两个工具）、`tools/__init__.py`（工具注册）、`agent/prompts.py`（软件指引段重写）、`agent/graph.py`（新增轮次计数与上限）、`agent/agent.py`（初始状态字段）。
- **测试**：本次不为 AI Service 新增单元测试；仅同步清理已失效的 `src/ai-service/tests/tools/test_software_resource_search.py`（其主体 `software_resource_search` 已被移除）。
- **后端**：本次不改动。`/api/v1/software-resources` 现有 `direction` + `keyword` + 分页能力已满足需求；`direction` 查询天然包含 `GENERAL` 的既有行为被显式依赖。
- **数据库**：不改动。不引入 `pg_trgm` / `pgroonga` / `pg_search` 等扩展；模糊匹配放在 AI Service 侧（见 design.md 的 Future Work）。
- **无破坏性变更**：`software_resource_search` 仅注册在 AI Service 内部 ToolRegistry，不对外暴露 HTTP 接口，重命名不影响前端与后端契约。
