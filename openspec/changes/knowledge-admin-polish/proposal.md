# Proposal: knowledge-admin-polish

## Why

知识库管理后台有三个体验缺口：(1) 文档列表的 `COMPLETED` 状态标签"已完成"措辞不准确（该状态实际含义是分段已就绪可供检索）；(2) 分段详情页只能编辑不能删除，管理员无法清理脏数据分段；(3) 标签改名后后台会异步重新生成标签向量，但前端没有任何状态反馈，管理员不知道向量何时可用。

## What Changes

- 前端文档列表状态文案：`COMPLETED` 标签从"已完成"改为"已就绪"（纯文案，纯前端）。
- 新增删除分段能力：
  - 后端新增 `DELETE /chunks/{id}` 接口，权限标识 `knowledge:chunk:delete`（PROTECTED）。
  - 文档处于 `PENDING`/`PARSING`/`CANCELING` 状态时禁止删除分段（否则解析回写失败、浪费 token），返回 `409 Conflict`。
  - 删除时同事务删除分片行（向量随列删除）、`tb_rag_chunk_tags` 关联，并将 `tb_rag_docs.chunk_count` 减 1。
  - 若文档已不存在（级联删除竞态），删除分段失败视为可接受代价。
  - 前端分段页增加删除按钮，使用 Popconfirm 确认（与标签删除一致），删除请求期间按钮显示 spinner，成功后刷新列表。
- 标签向量化完整状态机（与 chunk 的 `vector_status` 同模式）：
  - `tb_rag_tags` 新增 `vector_status` 列（`synced`/`embedding`）。
  - 创建标签与重命名标签时同事务置 `embedding`，发布 `tag-upsert` 消息。
  - ai-service 消费 `tag-upsert` 完成嵌入后置 `synced`。
  - 标签列表/详情 DTO 暴露 `vectorStatus`。
  - 前端标签页对 `embedding` 状态的标签显示"向量化中" spinner 标签，并以 3 秒轮询直至全部 `synced`（复用 chunks 页轮询模式）。

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `knowledge-chunk-editing`: 新增"管理员可删除分段"需求（删除接口、解析中禁止、chunk_count 同步、事务边界）。
- `knowledge-base-management`: 标签管理需求扩展——标签增加向量化状态机（创建/重命名置 `embedding`，ai-service 完成后置 `synced`），标签列表响应暴露 `vectorStatus`。

## Impact

- **后端**：`AdminKnowledgeDocController` 新增删除分段端点；`KnowledgeBaseAppService` 新增 `deleteChunk`；`KnowledgeChunk`/`KnowledgeDoc` 领域行为；`tb_kag_chunk_tag` 复用 `deleteByChunkId`；`tb_rag_tags` 加列（DDL）；标签 DTO/实体/DO 加 `vectorStatus`。
- **前端**：`admin/knowledge/docs/page.tsx`（文案）；`admin/knowledge/docs/[docId]/chunks/page.tsx`（删除按钮 + Popconfirm + spinner）；`admin/knowledge/tags/page.tsx`（向量化中标签 + 轮询）；`knowledge.service.ts` 新增 `deleteChunk`，`KnowledgeTagDTO` 加 `vectorStatus`。
- **ai-service（仓库外）**：消费 `tag-upsert` 完成后需将 `tb_rag_tags.vector_status` 置 `synced`（与 chunk re-embed 写回模式一致）；否则前端 spinner 不会消失。**发布需协调 ai-service 先上线或同步上线。**
- **数据库**：`tb_rag_tags` 新增 `vector_status varchar(16) not null default 'synced'`（存量行默认 synced，无需回填）。
- **权限**：新增权限标识 `knowledge:chunk:delete`，需全局唯一，启动时由 `PermissionScanner` 校验。
