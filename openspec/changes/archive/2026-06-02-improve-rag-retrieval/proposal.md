## Why

AI Service 检索链路存在三个关键断层：标签生成与标签库不匹配（命中率仅 25%）、标签检索结果混入噪声标签（STM32 被错误关联到"比赛"查询）、交叉查询召回严重不足（"招新考核考什么"平均分仅 0.036）。这导致 Agent 无法有效获取知识库内容，回答质量不稳定。

## What Changes

- **改 tag_generate**：Prompt 中加入已有标签约束，让 LLM 生成标签时优先从库中选择，提升标签命中率
- **改 tag_search_detailed**：增加动态阈值过滤，淘汰低分噪声标签，只返回高相关标签给 Agent
- **改 chunk_search_by_tags**：返回结果中增加【检索诊断】段落，让 Agent 自主判断"标签是否命中库""精确匹配是否失效"
- **改 Agent system prompt**：加入兜底策略指导，当诊断显示标签未命中或召回不足时，Agent 自主调用 chunk_search 直接语义搜索
- 保持 Agent 自主检索架构不变，工具职责单一，不封装黑盒逻辑

## Capabilities

### New Capabilities
- `rag-retrieval`: AI Service RAG 检索链路，包含标签生成、标签检索、分片检索及兜底策略

### Modified Capabilities
- （无现有 spec 需要修改）

## Impact

- `src/ai-service/tools/tag_search.py` — tag_generate Prompt 改造
- `src/ai-service/tools/tag_search_detailed.py` — 阈值过滤逻辑
- `src/ai-service/tools/chunk_search_by_tags.py` — 诊断信息输出
- `src/ai-service/agent/prompts.py` — system prompt 加入兜底策略
