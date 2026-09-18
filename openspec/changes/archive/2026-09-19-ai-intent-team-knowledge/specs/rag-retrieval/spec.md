# Delta: rag-retrieval

## ADDED Requirements

### Requirement: 空召回兜底回答
RAG system prompt SHALL 包含空召回兜底规则：当所有检索轮次（标签检索、分片检索、兜底语义搜索）均未获得与问题相关的分片时，Agent MUST 诚实告知用户知识库中未找到相关信息，并引导用户通过招新群或管理端咨询，禁止编造答案。

#### Scenario: 全轮次空召回时诚实告知
- **WHEN** 标签检索、分片检索与兜底语义搜索结束后，Agent 手中没有任何相关分片
- **THEN** Agent SHALL 生成"未在知识库中找到相关信息"的说明
- **AND** 给出引导建议（如关注招新群或联系团队管理端）
- **AND** 不输出任何编造的团队信息

#### Scenario: 空召回不触发额外检索
- **WHEN** 检索轮次已全部用尽且结果为空
- **THEN** Agent MUST 立即生成兜底回复
- **AND** 不再发起任何新的工具调用
