## 1. 清理硬编码同义词机制

- [x] 1.1 删除 `tag_normalization.py` 中的 `_TAG_SYNONYMS` 映射表及相关函数
- [x] 1.2 修改 `tools/tag_search_detailed.py`，删除 `expand_tag` 调用及导入
- [x] 1.3 修改 `tools/chunk_search_by_tags.py`，删除 `expand_tags` 调用及导入
- [x] 1.4 检查 `tools/__init__.py` 并清理相关导入（无相关导入需要清理）

## 2. 改进入库 Prompt 和标签生成

- [x] 2.1 修改 `TAG_GENERATION_PROMPT`，增加标签分层规范（方向/主题/技术三层）
- [x] 2.2 修改 `_generate_tags_for_chunk()`，从数据库读取全部已有标签注入 Prompt，删除 `normalize_tags` 调用
- [x] 2.3 修复标签解析器，支持中文逗号、换行符分隔，并添加去重逻辑
- [x] 2.4 收紧 Prompt，增加【禁止生成的标签】列表，强制同义标签统一

## 3. 实现 Reranker 动态归并

- [x] 3.1 在 `load2db_pipeline.py` 中新增 `_resolve_tag_with_reranker()` 函数（embedding 粗筛 Top-5 + Reranker 精排，阈值 0.90）
- [x] 3.2 在 `load2db_pipeline.py` 中新增 `_cosine_similarity()` 辅助函数，兼容字符串/ndarray/list 格式
- [x] 3.3 修改 `load2db_pipeline()` 主流程，在阶段二之后插入动态归并阶段
- [x] 3.4 用归并后的标签更新 `chunk_tag_map`，重新计算需要入库的新标签
- [x] 3.5 修复同批次 embedding 归并误杀技术标签的问题（禁用高风险同批次归并，仅保留跨批次 Reranker 精排）
- [x] 3.6 修复 f-string 条件格式化和向量类型兼容性 bug

## 4. 验证与测试

- [x] 4.1 运行 `load2db_pipeline.py` 完整入库，观察日志中的归并记录
- [x] 4.2 检查 `tb_rag_tags` 最终标签数量，确认无语义重复标签（从 37 个降至 16 个）
- [x] 4.3 运行 `tag_search_detailed` 测试，确认检索正常工作
- [x] 4.4 运行 `chunk_search_by_tags` 测试，确认双路搜索正常工作
- [x] 4.5 人工抽查边界 case：确认 STM32/Python/SolidWorks 等标签未被误判，招新类标签已统一

## 5. 迭代优化（额外）

- [x] 5.1 分析标签质量问题：低频噪声、同义分散、FAQ 过度使用
- [x] 5.2 多轮迭代调试 Prompt 和归并逻辑
- [x] 5.3 最终验证：16 个标签，无重复/无空标签/无格式异常，技术标签正确识别
