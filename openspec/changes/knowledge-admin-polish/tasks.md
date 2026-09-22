# Tasks: knowledge-admin-polish

## 1. 标签向量化状态机（后端）

- [ ] 1.1 DDL：`tb_rag_tags` 新增 `vector_status varchar(16) NOT NULL DEFAULT 'synced'`（写入数据库迁移脚本/SQL 记录）
- [ ] 1.2 `KnowledgeTagDO` 加 `vectorStatus` 字段（复用 `ChunkVectorStatus` 枚举，MyBatis-Plus 类型转换）
- [ ] 1.3 `KnowledgeTag` 领域实体加 `vectorStatus`：`create()` 初始化为 `EMBEDDING`，`reconstruct()` 支持该字段，新增 `markSynced()`；converter 同步转换
- [ ] 1.4 `KnowledgeTagListItemResponseDTO` 加 `vectorStatus` 字段，converter 透传
- [ ] 1.5 `KnowledgeBaseAppServiceImpl.updateTag()`：重命名同事务置 `EMBEDDING`（创建路径 1.3 已覆盖）；补单元测试验证创建/重命名后置 `embedding`
- [ ] 1.6 标签创建/重命名集成测试：断言响应含 `vectorStatus=embedding`，DB 行状态正确

## 2. 删除分段（后端，TDD）

- [x] 2.1 先写 `deleteChunk` 应用服务测试：成功删除（断言删 chunk + 删关联 + `chunk_count - 1`）、解析中 409、chunk 不存在 404
- [x] 2.2 `KnowledgeChunkRepository`/`Mapper` 加 `deleteById`；`KnowledgeDocRepository`/`Mapper` 加 `decrementChunkCount(docId)`（SQL `chunk_count = chunk_count - 1`）
- [x] 2.3 `KnowledgeBaseAppService` 接口加 `deleteChunk(Long chunkId)` 并实现（校验文档状态 → 删关联 → 删分片 → 递减计数，单事务）
- [x] 2.4 `AdminKnowledgeDocController` 加 `DELETE /chunks/{id}`，`@RequiresPermission(value="knowledge:chunk:delete", name="删除知识库分段", access=PROTECTED)`，全局确认权限标识唯一
- [x] 2.5 集成测试：Controller 全链路（204/200 成功、409、404、无权限 403）

## 3. ai-service 协调（仓库外）

- [ ] 3.1 ai-service 消费 `tag-upsert` 完成嵌入后，UPDATE 语句加写 `vector_status='synced'`（向 ai-service 维护者提交需求/PR，本仓库记录部署依赖）
- [ ] 3.2 部署顺序确认：ai-service 先于或同步于后端上线

## 4. 前端

- [x] 4.1 `admin/knowledge/docs/page.tsx`：`STATUS_MAP.COMPLETED.label` 改为「已就绪」
- [ ] 4.2 `knowledge.service.ts` 新增 `deleteChunk(id)`；`KnowledgeTagDTO` 加 `vectorStatus: 'synced' | 'embedding'`
- [x] 4.3 chunks 页：删除按钮（`isAdmin`）+ Popconfirm + 请求期间按钮 loading spinner，成功后刷新列表与文档状态
- [ ] 4.4 tags 页：行内 `embedding` 状态显示 spinning「向量化中」Tag；存在 embedding 时 3s 静默轮询列表直至全部 synced
- [ ] 4.5 创建标签成功后保持现有 spinner 提示并触发轮询（若创建响应未含状态则以列表轮询为准）

## 5. 验证与收尾

- [ ] 5.1 `./mvnw clean compile package` 编译打包，重建 `bluenet-api-service:latest` 镜像并运行
- [ ] 5.2 Playwright E2E：文档列表「已就绪」文案；分段删除（Popconfirm + spinner + 列表消失 + 分段数 -1）；改标签名 → 出现「向量化中」spinner → 轮询至消失
- [ ] 5.3 按提交规范分 commit（`fix:`/`feat:`，必要时 `ref #<issue>`），不合并 PR
