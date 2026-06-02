## Why

当前 AI Service 的标签系统依赖 `tag_normalization.py` 中的硬编码 `_TAG_SYNONYMS` 映射表实现同义词归一化和检索扩展。该映射表覆盖不全，LLM 生成的新变体标签无法处理，导致标签表膨胀形成"标签孤岛"——入库了但检索无法召回。同时，原来的同义词扩展只在检索层操作，没有在入库层预防同义标签的产生。

## What Changes

- **删除** `tag_normalization.py` 中的硬编码 `_TAG_SYNONYMS` 映射表及相关函数（`normalize_tag`、`normalize_tags`、`expand_tag`、`expand_tags`）
- **简化** `tools/tag_search_detailed.py` 和 `tools/chunk_search_by_tags.py`，移除同义词扩展调用，检索完全依赖 embedding 向量搜索
- **改进** `pipeline/load2db_pipeline.py` 的 `TAG_GENERATION_PROMPT`，加入标签分层规范（方向/主题/技术三层），引导 LLM 生成更一致的标签
- **新增** 入库阶段的标签动态归并机制：使用 embedding 粗筛 + Reranker 精排，语义相似度超过阈值的新标签自动归并到已有标签，防止同义标签重复入库

## Capabilities

### New Capabilities
<!-- 本次变更不涉及新的 spec 级能力，纯实现层改进 -->
- *本次变更为实现层重构，不引入新的 spec 级能力。*

### Modified Capabilities
<!-- 本次变更不涉及 spec 级行为变更，仅优化内部实现 -->
- *本次变更为内部实现优化，不修改现有 spec 的行为要求。*

## Impact

- **受影响文件**：
  - `src/ai-service/tag_normalization.py` — 删除硬编码映射表和函数
  - `src/ai-service/pipeline/load2db_pipeline.py` — Prompt 改进 + 新增 Reranker 归并逻辑
  - `src/ai-service/tools/tag_search_detailed.py` — 删除同义词扩展
  - `src/ai-service/tools/chunk_search_by_tags.py` — 删除同义词扩展
  - `src/ai-service/tools/__init__.py` — 清理相关导入
- **不影响**：检索 API 行为、数据库 Schema、前端接口
