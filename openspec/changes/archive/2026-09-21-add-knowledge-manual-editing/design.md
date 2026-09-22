# Design: add-knowledge-manual-editing

## Context

知识库链路：Java API Service 负责上传/管理（写 `tb_files`、`tb_rag_docs`，向 `knowledge` 交换机发布消息）；ai-service (Python, FastAPI + aio-pika) 消费消息，直连 PostgreSQL 完成语义分段、LLM 打标签、embedding、写 `tb_rag_chunks`/`tb_rag_tags`。向量只在 ai-service 生成。分片与标签目前按**名字**耦合：`tb_rag_chunks.tags` 为 text 数组，重命名标签需逐分片 `array_replace`，等价于应用层维护外键。项目规范：不使用物理外键、数据物理删除、所有接口 `@RequiresPermission` + `ResponseMessage`。

## Goals / Non-Goals

**Goals:**
- 分片可编辑（内容 + 存量标签增删），保存即见、向量化后台完成
- 文档可换附件（ID 不变），触发全量重解析
- 标签页完整 CRUD，新建/重命名触发标签向量重算，删除自动解除关联
- 分片-标签关系规范化为中间表 `tb_rag_chunk_tags`
- 消费端对"事务未提交导致的目标行缺失"具备有限重试能力

**Non-Goals:**
- 分片拆分/合并（chunk 数量不变，留作后续）
- 源 Markdown 文件的自动回写（源文件永不自动修改）
- re-parse 对手工编辑的保护（已确认接受覆盖，不加 edited 标记）
- 旧附件的立即清理（孤儿化，凌晨任务回收）
- 标签的 reranker 自动归并扩展到人工新建（管理员输入即意图，不自动归并）

## Decisions

### D1: 中间表 `tb_rag_chunk_tags` 替代 tags 数组

分片-标签多对多关系用 `(chunk_id, tag_id)` 中间表表达，无物理外键，应用层维护（符合项目规范）。`recalculate_tag_counts` 改为对中间表 `GROUP BY tag_id`；检索过滤（`get_chunks_by_tags`、`_build_tag_filter_sql`）改为 JOIN。

- **备选：bigint[] 数组字段** — 关系仍揉在行内，GIN 索引与约束表达力弱，放弃。
- **备选：维持 name 引用 + array_replace** — 重命名涟漪 + 应用层外键负担，放弃。

### D2: 消息在数据库事务内投递，消费端兜重试

保留现有"publish 在 `@Transactional` 方法内"的做法：投递失败 → 异常 → 事务回滚，不产生孤儿记录（生产者侧强一致，刻意设计）。作为交换，消费端必须容忍"消息先于事务提交到达"：

```
check_doc_status 等查询目标行不存在时:
  sleep 2s 重查, 最多 5 次 (~10s 窗口)
  仍不存在 → 确认为脏数据, 记录日志后丢弃 (nack 不重回队列)
```

重试逻辑下沉到 ai-service 的查询封装处，三条消息（parse / re-embed / tag-upsert）共用。

- **备选：afterCommit 发布** — 投递失败时 DB 已提交，产生卡 PENDING 的孤儿，需额外巡检兜底，放弃。
- **备选：Outbox 表 + relay** — 两者兼得但引入 relay 进程，对本场景过重，放弃。

### D3: 向量化的单一事实源在 ai-service

所有 embedding（分片内容向量、标签名向量）只在 ai-service 生成。Java 侧接口只写标量字段，随后发消息：

| 消息 | routing key → 队列 | 消费行为 |
|------|--------------------|----------|
| parse (现有) | `parse` → `knowledge.parse` | 整文档：下载→分段→标签(名)→写 tags 拿 id→写 junction→embed→写 chunks |
| re-embed (新增) | `re-embed` → `knowledge.reembed` | embed content → UPDATE chunk_vector → recalc counts；消息含 `{chunkId}` |
| tag-upsert (新增) | `tag-upsert` → `knowledge.tag_upsert` | embed tag_name → UPDATE/INSERT tag_vector；消息含 `{tagId}` |

tag-upsert 对 INSERT 路径（新建标签）与 UPDATE 路径（重命名）统一处理：消费时按 id 取 tag_name 重新 embed 并 upsert。

### D4: 分片编辑 = 同步写库 + 乐观展示

`PUT /chunks/{id}` 在 Java 侧同步 UPDATE `content` 并重建该分片的 junction 关联（先校验全部 tagId 存在），事务内发布 re-embed 消息后返回。前端立即刷新显示新内容，向量更新在后台静默完成，不做完成态轮询。

### D5: 删除标签 = 硬删除 + 自动解除关联

`DELETE /tags/{id}`：删除 `tb_rag_tags` 行 + 删除 junction 中该 tag 的全部行（应用层两步，同事务），随后异步重算受影响标签的 counts（可复用 re-embed 之外的轻量消息或直接由 Java 触发一次 counts 重算消息；实现时倾向复用 tag-upsert 消息附带 `recount=true` 标志，避免新增第四类消息）。删除前响应中携带引用计数供前端确认弹窗展示。

### D6: 换附件走"存文件 → 换 fileId → reparse"

`POST /docs/{id}/file`：校验 doc 状态非解析中 → `saveFile` 存新文件 → `doc.fileId = 新id; markForReparse()` → 事务内发布 `parse` 消息（reparse=true）。ai-service 侧复用现有 reparse 路径（先 DELETE 旧 chunks 再解析）。旧 OSS 文件成为孤儿，由凌晨清理任务回收（`orphan-file-cleanup` 既有能力）。

### D7: 分片列表的标签展示

`GET /docs/{id}/chunks` 返回 `tagIds`，由前端用标签池（`GET /tags`，数据量小）映射为名称展示。避免后端每次 JOIN；标签池页本身已有分页，实现时若池变大再改为后端聚合。

### D8: 分片向量状态指示（`vector_status`）

`tb_rag_chunks` 新增 `vector_status` 枚举列（`'synced'` / `'embedding'`）。编辑分片时 Java 在事务内置 `'embedding'`；ai-service re-embed 成功后置 `'synced'`；parse 流水线嵌入完成后插入的 chunk 直接为 `'synced'`。前端分片卡片据此显示"向量化中"徽标（spinner），并通过 3 秒轮询分片列表感知完成。重新上传/重解析沿用现有"先 DELETE 旧分片再解析"语义，期间分片页为空态，空态文案随 doc 状态显示"解析中"。

## Risks / Trade-offs

- [消费端重试窗口（~10s）内事务仍未提交] → 消息丢弃；由于 publish 在事务内，回滚即代表消息不该存在，丢弃是正确的；剩余场景是 commit 极慢，可用 nack-requeue 一次作为最后兜底（实现时决定）
- [迁移期间新旧代码并存] → 迁移脚本与代码切换在同一发布窗口内完成；存量数据量小（高校团队知识库），脚本成本可控
- [tag-upsert 消息在标签被删除后到达] → 消费时查不到 tag 行 → 命中 D2 重试 → 仍无则丢弃（删除与改名竞态的最终一致性可接受）
- [re-embed 在 reparse 进行中到达] → Java 侧编辑接口已用 doc 状态守卫（PENDING/PARSING/CANCELING 返回 409）拦截；残余竞态由消费端"chunk 不存在则丢弃"兜底
- [chunks 表废弃 tags 字段] → ai-service 与 Java 所有读写路径需同窗口切换；在迁移脚本中保留旧字段一个版本周期后再物理删除

## Migration Plan

1. 建 `tb_rag_chunk_tags` 表（chunk_id, tag_id，联合主键，各自普通索引）
2. 一次性迁移脚本：遍历 `tb_rag_chunks.tags` 数组 → 按 tag_name 查 `tb_rag_tags.id` → INSERT junction（名字无对应 tag 的行跳过并记日志）
3. 发布 Java + ai-service + 前端（同一窗口，新旧字段双写过渡期可省略——读路径全部切换到 junction 后再发布）
4. 观察一个版本周期后删除 `tb_rag_chunks.tags` 字段

回滚：junction 表为新增，回滚代码后旧 tags 数组仍在（步骤 4 之前），读路径回退无损。

## Open Questions

- 分片编辑是否需要记录操作审计（谁改了哪个分片）？现有 `backend-audit-logging` 能力是否覆盖管理端操作，实现时确认
- `GET /tags` 当前为分页接口；标签池作为选择器数据源时是否需要不分页的全量接口，前端实现时评估
