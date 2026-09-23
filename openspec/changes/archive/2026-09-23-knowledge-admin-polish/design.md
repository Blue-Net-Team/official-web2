# Design: knowledge-admin-polish

## Context

知识库管理后台由本仓库（Spring Boot 后端 + Next.js 管理前端）和仓库外的 ai-service（RabbitMQ 消费者，负责分块/嵌入）组成。当前状态：

- 文档列表 `COMPLETED` 显示"已完成"，实际语义是"分段就绪可检索"。
- 分段只有 `PUT /chunks/{id}`（编辑），无删除能力；文档删除已级联删分片（`KnowledgeBaseAppServiceImpl.deleteDocument`）。
- `tb_rag_tags` 无 `vector_status` 列；创建/重命名标签发布 `tag-upsert` 后由 ai-service 异步嵌入 `tag_vector`，前端无从感知进度。chunk 侧已有同模式（`tb_rag_chunks.vector_status` + `ChunkVectorStatus` 枚举 + 前端轮询）。
- 嵌入文本仅标签名（spec 明确），改描述不触发重嵌入，现有行为正确。

## Goals / Non-Goals

**Goals:**
- 文档状态标签文案改为"已就绪"。
- 提供单分片删除（事务内删分片 + 关联 + `chunk_count - 1`），解析中禁止。
- 标签向量化状态机与 chunk 对齐：建/改名置 `embedding`，ai-service 完成后置 `synced`，前端 spinner + 3s 轮询。

**Non-Goals:**
- 不改 ai-service 的嵌入算法或嵌入文本构成。
- 不引入软删除、分片恢复、批量删除。
- 不改 `COMPLETED` 状态机语义，仅改前端文案。
- 改标签描述不触发重嵌入（维持现状）。

## Decisions

### D1: 删除分段走单事务同步删除，不发消息

删除一个分片是单行操作，代价极小，直接同事务完成：

```
DELETE /chunks/{id}
  1. 加载 chunk → 404 if missing
  2. 加载 doc → doc.status ∈ {PENDING,PARSING,CANCELING} → 409
  3. DELETE tb_rag_chunk_tags WHERE chunk_id = ?   （复用 deleteByChunkId）
  4. DELETE tb_rag_chunks WHERE id = ?              （chunk_vector 随行删除）
  5. UPDATE tb_rag_docs SET chunk_count = chunk_count - 1 WHERE id = ?
```

- 备选：发 MQ 让 ai-service 删 → 拒绝。删除无 AI 参与，引入异步反而制造"删除后短暂可查"的一致性问题。
- 文档先被删导致 chunk 404：符合 spec，代价可接受（用户已确认）。
- `chunk_count` 用 SQL 原子递减（`chunk_count - 1`），不回读文档对象再 save，避免并发编辑时的丢失更新。

### D2: 标签 vector_status 完全复刻 chunk 模式

```
新建标签:  vector_status = 'embedding' ──tag-upsert──▶ ai-service 嵌入 ──▶ 'synced'
重命名:    vector_status = 'embedding' ──tag-upsert──▶ ai-service 嵌入 ──▶ 'synced'
存量行:    DDL 加列 default 'synced'，无需回填
```

- 枚举复用现有 `ChunkVectorStatus`（`synced`/`embedding` 值一致），Java 侧直接复用该枚举映射 tag 字段；不新建枚举，避免同义双枚举。
- 备选：新建 `TagVectorStatus` → 拒绝，值域完全相同，纯重复。
- DTO：`KnowledgeTagListItemResponseDTO` 加 `vectorStatus`，converter 透传。

### D3: 前端轮询复用 chunks 页既有模式

- tags 页：任一 `vectorStatus === 'embedding'` 时 3s 静默轮询 `listTags`，全部 `synced` 停止。与 chunks 页（`fetchChunks(true)` 模式）一致。
- 文案变更：仅 `STATUS_MAP.COMPLETED.label`。
- 删除分段：Popconfirm（与标签删除交互一致）+ 按钮 `loading` spinner（用户明确要求）+ 成功后 `fetchChunks()` 非静默刷新（列表人数少，直接转圈即可）。

## Risks / Trade-offs

- **[ai-service 不写回 vector_status → spinner 永不消失]** → 发布顺序必须 ai-service 先上线（或同步上线）。ai-service 改动极小：嵌入 tag 成功的 UPDATE 语句多加一个 `vector_status='synced'`。灰度期间若 spinner 不消失，重命名一次即可自愈——回滚策略：回滚后端+DDL 列保留无害。
- **[删除分段与解析回写并发]** → 解析中已用 409 禁止；COMPLETED 后 ai-service 不再写该文档分片，无并发写。
- **[chunk_count 递减与重解析并发]** → 重解析（reparse）会先由 ai-service 清旧分片并整体回写 `chunk_count`，期间文档状态为 PENDING/PARSING，删除分段已被 409 挡住，无交叉。
- **[存量 tag 行状态]** → DDL default 'synced' 假设存量 tag 向量已生成（现状确如此，因为创建即嵌入）。若个别行向量实际缺失，仅影响 UI 不显示 spinner，无功能回退。

## Migration Plan

1. ai-service：tag-upsert 消费逻辑写回 `vector_status='synced'` → 部署。
2. 后端：DDL `ALTER TABLE tb_rag_tags ADD COLUMN vector_status varchar(16) NOT NULL DEFAULT 'synced';` → 部署新后端（新代码读新列）。
3. 前端：三个页面改动 → 部署。
4. 回滚：回滚后端/前端镜像即可；`vector_status` 列保留不影响旧代码（旧代码不读该列）。

## Open Questions

（无）
