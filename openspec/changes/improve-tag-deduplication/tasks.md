## 1. 清理硬编码同义词机制

- [ ] 1.1 删除 `tag_normalization.py` 中的 `_TAG_SYNONYMS` 映射表及相关函数
- [ ] 1.2 修改 `tools/tag_search_detailed.py`，删除 `expand_tag` 调用及导入
- [ ] 1.3 修改 `tools/chunk_search_by_tags.py`，删除 `expand_tags` 调用及导入
- [ ] 1.4 检查 `tools/__init__.py` 并清理相关导入

## 2. 改进入库 Prompt 和标签生成

- [ ] 2.1 修改 `TAG_GENERATION_PROMPT`，增加标签分层规范（方向/主题/技术三层）
- [ ] 2.2 修改 `_generate_tags_for_chunk()`，从数据库读取全部已有标签注入 Prompt
- [ ] 2.3 运行入库测试，观察 LLM 生成标签的质量变化

## 3. 实现 Reranker 动态归并

- [ ] 3.1 在 `load2db_pipeline.py` 中新增 `_merge_tags_by_embedding()` 函数（embedding 粗筛 Top-5）
- [ ] 3.2 在 `load2db_pipeline.py` 中新增 `_resolve_tag_with_reranker()` 函数（Reranker 精排，阈值 0.90）
- [ ] 3.3 修改 `load2db_pipeline()` 主流程，在阶段二之后插入动态归并阶段
- [ ] 3.4 用归并后的标签更新 `chunk_tag_map`，重新计算需要入库的新标签

## 4. 验证与测试

- [ ] 4.1 运行 `load2db_pipeline.py` 完整入库，观察日志中的归并记录
- [ ] 4.2 检查 `tb_rag_tags` 最终标签数量，确认无语义重复标签
- [ ] 4.3 运行 `tag_search_detailed` 测试，确认检索正常工作
- [ ] 4.4 运行 `chunk_search_by_tags` 测试，确认双路搜索正常工作
- [ ] 4.5 人工抽查几个边界 case（如 "SolidWorks" 是否被误判为 "结构方向"）
