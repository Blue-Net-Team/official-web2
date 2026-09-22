# Proposal: add-knowledge-manual-editing

## Why

知识库内容目前完全由机器流水线产出（上传 → 语义分段 → 打标签 → 向量化），管理员只能查看、不能修正。实际使用中分段不合理、内容过时、标签不准确等问题无法人工介入；文档内容整体更新也只能删旧传新，导致文档 ID 变化、关联断裂。

## What Changes

- **新增分片编辑能力**：管理员可编辑单个分片的 Markdown 内容，并从现有标签池多选增删标签（不允许在分片编辑时新建标签）；保存后同步写库、异步重新向量化（乐观更新）
- **新增文档换附件能力**：同一文档可重新上传 `.md` 附件，文档 ID 不变，自动触发全量重新分段与向量化；旧 OSS 文件孤儿化，由凌晨清理任务回收
- **标签管理页补全 CRUD**：新增新建标签、重命名标签能力，删除标签时自动解除与所有分片的关联；标签向量化（新建/重命名触发）由 ai-service 异步完成
- **分片-标签关系规范化**：废弃 `tb_rag_chunks.tags` 文本数组，新增中间表 `tb_rag_chunk_tags(chunk_id, tag_id)`（无物理外键，应用层维护），重命名标签不再产生连锁更新
- **ai-service 消费端健壮性**：消费 parse / re-embed / tag-upsert 消息时，若目标行因生产端事务未提交而查询不到，执行有限次重试而非直接丢弃
- **MQ 拓扑扩展**：`knowledge` 交换机新增 `re-embed`、`tag-upsert` 两个 routing key 与对应队列

## Capabilities

### New Capabilities

- `knowledge-chunk-editing`: 分片内容编辑、标签手动关联/解除、异步重新向量化（re-embed 消息），以及文档级重新上传附件触发完整重解析

### Modified Capabilities

- `knowledge-base-management`: 标签管理从"仅改描述"扩展为完整 CRUD（新建/重命名/删除+自动解除关联）；分片列表的标签展示改为基于中间表解析；新增分片编辑与换附件的管理端接口

## Impact

- **后端 (Java)**：`AdminKnowledgeDocController` 新增 5 个接口（编辑分片、换附件、新建标签、重命名标签、删除标签）；新增 junction 表的 Mapper/Repository 层；`KnowledgeChunk`、`KnowledgeTag` 实体新增行为方法；`KnowledgeParsePublisher` 扩展发布 re-embed / tag-upsert 消息
- **ai-service (Python)**：新增 `reembed_consumer`、`tag_upsert_consumer` 两个消费者；`document_parser` 写库路径改为 junction 表；标签检索 SQL 改为 JOIN；消费端空行重试逻辑
- **前端**：分片详情页加编辑弹窗（Markdown 编辑 + 标签多选）；文档列表页加"重新上传附件"；标签管理页加新建/重命名/删除
- **数据库**：新增 `tb_rag_chunk_tags` 表；`tb_rag_chunks` 废弃 `tags` 字段（迁移后删除）；一次性数据迁移脚本（存量 tags 数组 → junction 行）
- **MQ**：`knowledge` 交换机新增 2 个队列与 routing key
- **兼容性注意**：`tb_rag_chunks.tags` 字段废弃对 ai-service 检索层是内部实现变更，`rag-retrieval` 对外工具行为不变
