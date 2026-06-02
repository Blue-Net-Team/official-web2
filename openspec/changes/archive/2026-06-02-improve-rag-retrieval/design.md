## Context

当前 AI Service 的 RagAgent 采用两阶段自主检索架构：
1. **标签探索阶段**：Agent 调用 `tag_search_detailed` 找相关标签（最多4轮）
2. **分片检索阶段**：Agent 调用 `chunk_search_by_tags` 用标签搜分片（最多3轮）

测试暴露三个关键问题：
- `tag_generate` 生成的标签与数据库标签库不匹配（命中率 25%），导致 `chunk_search_by_tags` 的精确匹配路径失效
- `tag_search_detailed` 返回的 top-k 混入噪声标签（如"比赛"查询返回 STM32），浪费 Agent 轮次
- 交叉查询"招新考核考什么内容"因标签均不在库，精确匹配完全失效，平均分仅 0.036

## Goals / Non-Goals

**Goals:**
- 提升标签生成命中率，让生成的标签尽可能落在数据库已有标签上
- 淘汰噪声标签，让 Agent 拿到的标签列表更干净
- 让 Agent 能自主判断检索质量，在标签失效时主动启用兜底策略
- 保持 Agent 自主检索架构，不在工具内部封装决策逻辑

**Non-Goals:**
- 不改嵌入模型或向量数据库底层
- 不改 chunk 分段策略或知识库内容
- 不引入新的外部依赖
- 不改 Agent 两阶段工作流的核心结构

## Decisions

### Decision 1: tag_generate 用 Prompt 约束而非代码映射

**选择**：在 `tag_generate` 的 system prompt 中加入"已有标签"列表，让 LLM 生成时优先选择。

**替代方案**：生成后用 embedding 做代码级映射到已有标签。

**理由**：
- Prompt 约束更简单，无额外 embedding 调用开销
- LLM（deepseek-v4-flash）有指令遵循能力，能较好遵守标签约束
- 保留 LLM 处理不在库中新概念的灵活性
- 代码映射需要新增 `tag_exists()` 接口和 embedding 比对逻辑，复杂度更高

### Decision 2: chunk_search_by_tags 返回诊断文本而非结构化数据

**选择**：在返回字符串中增加【检索诊断】段落，用自然语言描述标签命中情况。

**替代方案**：返回 JSON 结构化诊断数据。

**理由**：
- Agent（LLM）能直接阅读自然语言并做出判断，无需解析 JSON
- 保持现有工具返回类型（str）不变，减少接口变更
- 文本诊断对调试也友好，人类可读

### Decision 3: 兜底策略放在 system prompt 而非工具内部

**选择**：在 Agent system prompt 中明确指导"当诊断显示标签未命中时，调用 chunk_search 兜底"。

**替代方案**：在 `chunk_search_by_tags` 内部自动触发 `chunk_search`。

**理由**：
- 保持 Agent 自主决策，符合用户"保持 agent 自主检索"的要求
- 工具职责单一，`chunk_search_by_tags` 只做标签检索，`chunk_search` 做语义检索
- Agent 可以综合考虑多轮结果后决定是否兜底，工具层无法做这种跨轮次判断

### Decision 4: tag_search_detailed 用动态相对阈值而非固定阈值

**选择**：`threshold = max(MIN_SCORE, top_score * RATIO)`，其中 MIN_SCORE=0.005, RATIO=0.1。

**替代方案**：固定绝对阈值（如 0.01）。

**理由**：
- Reranker 分数分布因查询而异，固定阈值可能过严或过松
- 相对阈值能自适应：高分查询过滤更严，低分查询保留更多
- 保底机制确保至少保留第一名，避免全部过滤导致 Agent 无标签可用

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| Prompt 约束后 LLM 仍生成不在库的标签 | 诊断信息会明确告知 Agent"标签未命中"，Agent 可自主兜底 |
| 阈值过滤过于严格，遗漏边缘相关标签 | RATIO=0.1 较宽松，MIN_SCORE=0.005 保底，且 Agent 可多轮扩展 |
| Agent 不理解诊断信息，不触发兜底 | system prompt 明确指导兜底条件，且预披露阶段已展示诊断格式 |
| chunk_search 兜底增加 LLM token 消耗 | 兜底限制最多1轮，且只在标签完全失效时触发 |

## Migration Plan

1. 修改 `tag_search.py` Prompt（无破坏性变更）
2. 修改 `tag_search_detailed.py` 加阈值过滤（返回数量可能减少，但质量提升）
3. 修改 `chunk_search_by_tags.py` 加诊断段落（纯新增信息，不影响原有解析）
4. 修改 `agent/prompts.py` system prompt（新增兜底指导，不改变现有流程）
5. 重新运行测试脚本验证6个典型问题的检索效果

无需数据库迁移或数据清理。

## Open Questions

- `tag_generate` Prompt 中已有标签列表是硬编码还是动态查询？当前16个标签量小可硬编码，后续增长需考虑动态加载。
- 诊断信息中的中文表述是否足够让 LLM 理解并做出正确决策？需要测试验证。
