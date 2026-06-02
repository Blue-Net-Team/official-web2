## Why

当前 AI 客服（RagAgent）的知识库内容通过离线脚本 `load2db_pipeline.py` 从本地 markdown 文件直接入库，管理平台没有可视化的文档管理能力。管理员无法上传新文档、查看已有分片、管理标签，知识库维护完全依赖手动操作和脚本执行，效率低且容易出错。

## What Changes

- **新增管理平台菜单**："知识库管理"菜单组，包含文档管理、标签管理两个子页面
- **新增后端接口**：API Service 提供知识库文档 CRUD、标签管理、解析状态查询等接口
- **新增 RabbitMQ 队列**：`knowledge.parse` 队列，用于异步分发文档解析任务
- **AI Service 新增消费者**：接收 RabbitMQ 消息，执行文档下载、分片、向量化、入库
- **AI Service 解析逻辑重构**：将现有 `load2db_pipeline.py` 的批量离线导入逻辑拆分为可复用的单文档解析函数，供消费者和离线脚本共用
- **数据库变更**：`tb_rag_docs` 增加 `status`、`chunk_count`、`error_message`、`created_at`、`updated_at` 字段

## Capabilities

### New Capabilities

- `knowledge-base-management`: 管理平台的知识库文档上传、查看、删除，以及标签管理
- `knowledge-doc-parse`: 文档异步解析、分片、向量化入库的完整流水线

### Modified Capabilities

- 无现有 spec 需要修改

## Impact

| 模块 | 影响 |
|------|------|
| `src/backend` | 新增 Controller、AppService、Repository、枚举、Flyway 迁移、RabbitMQ 配置 |
| `src/ai-service` | 新增 RabbitMQ 消费者、单文档解析函数、环境变量配置 |
| `src/frontend` | 新增 Admin 菜单组、3 个管理页面 |
| `docker/.env` | 新增 AI Service 的 RabbitMQ 环境变量 |
