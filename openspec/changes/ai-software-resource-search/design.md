## Context

软件资源库（`/resources`）已上线，数据维护在 `tb_software_resource`，支持按方向（计算机视觉 / 结构设计 / 嵌入式开发 / 通用）展示已启用的外部链接资源。AI Service（`src/ai-service`）采用 TBD-RAG 架构，通过 `RagAgent` 调用工具链（tag/chunk 检索）回答考生问题。目前 Agent 没有访问后端业务数据的途径，因此无法在对话中返回软件资源的下载地址。

## Goals / Non-Goals

**Goals:**
- 考生在 AI 对话中询问软件下载、安装工具、方向推荐软件时，Agent 能返回当前有效的资源名称、描述和外部下载地址。
- 返回的数据必须实时，与前端 `/resources` 页面看到的内容一致。
- 不引入数据同步或向量化的复杂度。

**Non-Goals:**
- 不把软件资源向量化进 RAG 知识库（本次不做）。
- 不改造现有 RAG 检索流程。
- 不新增后端管理功能或修改资源 CRUD 逻辑。
- 不做 AI Service 与后端之间的认证鉴权（复用已有公开接口）。

## Decisions

### 1. 采用工具调用而非向量化同步

**选择**: 在 AI Service 中新增一个 `software_resource_search` 工具，实时调用后端公开接口。

**理由**:
- 实现量最小：不需要消息队列、文档解析、chunk 入库等流水线。
- 数据实时：下载地址直接来自数据库，无同步延迟。
- 与现有 RAG 流程解耦：LLM 可以在合适的时机主动调用，不侵占 tag/chunk 检索轮次的关键路径。

**替代方案**: 向量化方案（每个资源作为 chunk 入库）。更自然融入 RAG，但需要扩展 RabbitMQ 消息、AI Service 消费者和 chunk 生成逻辑，且存在同步延迟。本次排除。

### 2. 关键字搜索放在后端 SQL 层

**选择**: 后端 `/api/v1/software-resources` 新增 `keyword` 参数，对 `name`、`category`、`description` 做 `LOWER(...) LIKE` 模糊匹配。

**理由**:
- AI Service 不需要拉取全量资源做本地过滤，节省带宽和内存。
- 后端可利用数据库索引（后续如数据量大可补充 `pg_trgm` 或搜索索引）。
- 分页语义保持统一。

### 3. 工具接受中文方向标签

**选择**: `software_resource_search` 的 `direction` 参数同时支持中文标签（如"视觉方向"）和后端枚举值（如 `COMPUTER_VISION`）。

**理由**:
- LLM 更容易从用户自然语言中抽取中文方向。
- 减少 Agent 侧的转换负担，提高触发准确率。

### 4. 配置项使用 `TBD_RAG_BACKEND_API_URL`

**选择**: 在 AI Service 的 `Settings` 中新增 `BACKEND_API_URL`，环境变量名保持 `TBD_RAG_` 前缀。

**理由**:
- 与现有配置体系一致。
- Docker 内使用服务名 `http://api-service:8080`，本地开发使用 `http://localhost:8080`。

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| LLM 未能识别软件相关问题 | 考生问"我要下 SolidWorks"时 Agent 不调用工具 | 工具描述和 system prompt 明确触发条件；观察日志后微调 prompt |
| 后端服务不可用时工具报错 | AI 回答"软件资源服务暂不可用" | 工具内 catch HTTP 异常，返回友好提示 |
| 关键词太泛返回大量结果 | 回答冗长或超出 token 限制 | 工具默认限制 `size=20`，结果按方向分组格式化 |
| 公开接口被 AI Service 高频调用 | 少量增加后端 QPS | 资源库数据量小且查询简单，当前架构可承受；后续可限速 |

## Migration Plan

1. 部署后端新版本（包含 `keyword` 参数的接口）。
2. 在 `docker/.env` 中补充 `TBD_RAG_BACKEND_API_URL`。
3. 部署 AI Service 新版本（包含工具和配置）。
4. 无需数据迁移；已有资源立即可被查询。
5. 回滚：单独回滚任一服务不影响另一方，只是 AI Service 调用失败时会降级提示。

## Open Questions

- 是否需要为 `keyword` 搜索添加 `pg_trgm` 索引以支持更大规模数据？（当前数据量小，暂不添加）
- 是否需要支持按分类精确筛选？（当前通过 `keyword` 已能覆盖，暂不单独加参）
