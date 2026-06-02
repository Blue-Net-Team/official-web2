## 1. 标签生成优化

- [x] 1.1 修改 `tools/tag_search.py` 中 `tag_generate` 的 system prompt，加入已有标签约束
- [x] 1.2 验证 tag_generate 对 6 个典型问题的输出是否优先返回已有标签

## 2. 标签检索阈值过滤

- [x] 2.1 修改 `tools/tag_search_detailed.py`，在返回前增加动态阈值过滤逻辑
- [x] 2.2 验证"比赛"查询不再返回 STM32、团队简介等噪声标签
- [x] 2.3 验证极端查询下至少保留第一名标签（保底机制）

## 3. 分片检索诊断信息

- [x] 3.1 修改 `tools/chunk_search_by_tags.py`，在返回字符串中增加【检索诊断】段落
- [x] 3.2 诊断信息包含：传入标签列表、在库标签列表、不在库标签列表、精确匹配召回数量
- [x] 3.3 当标签均不在库时，诊断信息建议"使用 chunk_search 直接搜索"
- [x] 3.4 验证问题6"招新考核考什么内容"的诊断信息正确标识标签在库状态

## 4. Agent 兜底策略

- [x] 4.1 修改 `agent/prompts.py` 中 `TAG_RETRIEVAL_SYSTEM_PROMPT`，加入兜底策略指导
- [x] 4.2 兜底指导内容：当 chunk_search_by_tags 诊断显示标签未命中时，调用 chunk_search(query)
- [x] 4.3 兜底指导内容：chunk_search 最多调用 1 轮
- [x] 4.4 验证 chunk_search 工具已在 ToolRegistry 注册且 Agent 可调用

## 5. 端到端验证

- [x] 5.1 运行测试脚本验证 6 个典型问题的检索质量
- [x] 5.2 验证问题6"招新考核考什么内容"标签路径平均分从 0.036 提升到 0.4122（第一轮，超过 0.3 目标）
- [x] 5.3 验证所有查询无噪声标签混入
- [x] 5.4 清理临时测试脚本
