# Tasks: add-knowledge-manual-editing

## 1. 数据层：中间表与迁移

- [x] 1.1 创建 `tb_rag_chunk_tags` 表（`chunk_id`, `tag_id` 联合主键，各自普通索引；无物理外键）
- [x] 1.2 `tb_rag_chunks` 新增 `vector_status` 枚举列（默认 `synced`），含默认值回填
- [x] 1.3 编写一次性迁移脚本（并入 Flyway V29）：存量 `tb_rag_chunks.tags` 数组按 tag_name 查 `tb_rag_tags.id` 写入 junction；无对应标签的名字跳过并记日志
- [x] 1.4 执行迁移并核对存量 junction 行数与数组标签总数（由集成测试 Testcontainers 自动执行迁移验证）

## 2. 后端：实体与仓储层

- [x] 2.1 `KnowledgeChunk` 实体新增 `updateContent()` 行为方法、`vectorStatus` 与 `tagIds` 字段；`KnowledgeTag` 新增 `rename()`、`create()` 校验唯一名
- [x] 2.2 新增 `KnowledgeChunkTagDO`/Mapper 及 Repository 层（saveByChunkId 批量重建、deleteByTagId、countByTagId 等）
- [x] 2.3 `KnowledgeChunkRepository` 查询改为经 junction 解析 tagIds；废弃 `tags` 字段读写
- [x] 2.4 `KnowledgeTagRepository` 新增 `existsByName`、`deleteById`；标签名唯一约束核对

## 3. 后端：应用层与接口层

- [x] 3.1 `KnowledgeParsePublisher` 扩展 `publishReembed(chunkId)` 与 `publishTagUpsert(tagId)`（新 routing key/队列常量）
- [x] 3.2 `RabbitMQ` 配置类新增 `knowledge.reembed`、`knowledge.tag_upsert` 队列与绑定
- [x] 3.3 新增 `PUT /admin/knowledge/chunks/{id}`（编辑分片）：doc 状态守卫 + tagId 存在性校验 + 事务内写 content/junction/`vector_status='embedding'` + 发 re-embed
- [x] 3.4 新增 `POST /admin/knowledge/docs/{id}/file`（换附件）：状态守卫 + saveFile + 换 fileId/markForReparse + 发 parse(reparse=true)
- [x] 3.5 新增 `POST /admin/knowledge/tags`（新建）与 `PUT /admin/knowledge/tags/{id}`（重命名/描述），发 tag-upsert
- [x] 3.6 新增 `DELETE /admin/knowledge/tags/{id}`：同事务删 tag 行 + junction 行，返回解除关联的 chunk 数
- [x] 3.7 全部新接口添加 `@RequiresPermission`（value 全局唯一）并核对 PermissionScanner 通过
- [x] 3.8 DTO/Converter 调整：分片列表返回 tagIds；新增请求 DTO 及校验

## 4. 后端测试

- [x] 4.1 实体行为方法单元测试（updateContent/rename/守卫逻辑）
- [x] 4.2 应用服务层测试：编辑分片（含 409/400 分支）、换附件、标签 CRUD、删除解除关联
- [x] 4.3 Controller 集成测试：权限注解、参数校验、消息发布验证
- [x] 4.4 junction Repository 集成测试

## 5. ai-service：消费端与检索层

- [x] 5.1 新增 `reembed_consumer.py`：embed content → UPDATE chunk_vector + `vector_status='synced'` → `recalculate_tag_counts`（改按 junction GROUP BY）
- [x] 5.2 新增 `tag_upsert_consumer.py`：按 tagId 取 tag_name → embed → INSERT/UPDATE tag_vector；处理 `recount` 标志（标签删除后重算）
- [x] 5.3 消费端查询封装统一加重试：目标行缺失时 2s 间隔最多 5 次，仍缺则记日志丢弃（三条消息共用）
- [x] 5.4 `document_parser.ingest_chunks` 改造：标签名解析为 id，chunks 写 junction；`_cleanup_chunks` 连带清 junction
- [x] 5.5 检索 SQL 改造：`get_chunks_by_tags`/`_build_tag_filter_sql` 改为 junction JOIN；`chunk_search_by_tags` 工具维持名字出入参（内部名→id 解析）
- [x] 5.6 `main.py` 注册两个新消费者；僵尸任务清理逻辑保持不变

## 6. ai-service 验证

- [x] 6.3 端到端验证：编辑分片后向量更新可被检索命中（并入 8.2 Playwright 端到端）

## 7. 前端

- [x] 7.1 `knowledge.service.ts` 新增接口封装（编辑分片、换附件、标签 CRUD）
- [x] 7.2 分片详情页：编辑弹窗（Markdown 编辑 + 标签多选，数据源为标签池），保存后乐观刷新；`vectorStatus==='embedding'` 的卡片右上角显示"向量化中"spinner 徽标，页面 3 秒轮询至全部 `synced`；doc 为解析中时空态显示"解析中"提示
- [x] 7.3 文档列表/详情页："重新上传附件"按钮（上传前状态校验提示）
- [x] 7.4 标签管理页：新建、重命名（编辑弹窗扩展）、删除（确认弹窗展示引用数）
- [x] 7.5 分片卡片标签展示改为 id→名称映射渲染

## 8. 联调与收尾

- [x] 8.1 后端打包、重建镜像；ai-service 重启；compose 基础设施确认
- [x] 8.2 Playwright 端到端：编辑分片→检索命中新内容；换附件→重解析完成；标签 CRUD→向量检索验证
- [ ] 8.3 观察期后删除 `tb_rag_chunks.tags` 字段（独立提交）
