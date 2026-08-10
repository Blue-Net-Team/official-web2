# TBD-RAG

基于标签驱动检索与渐进式披露的 RAG 系统。

## 核心理念

传统 RAG 直接在全量文档上做向量搜索，噪声大、相关性难以控制。TBD-RAG 将检索分为两个阶段：

1. **标签空间检索** — 先在标签（低成本的中间表示）上定位方向，再决定去哪里找文档
2. **文档空间检索** — 基于确认后的标签过滤，精准召回文档

每阶段内部都采用 **召回 + Rerank** 的两阶段检索模式，确保语义精准匹配。

## 系统架构

```mermaid
flowchart TD
    Q[Query] --> PD["预披露 (代码自动执行)"]

    subgraph PD_detail["[代码层] 预披露"]
        PD1["tag_generate(query)"]
        PD2["tag_search_detailed(标签) → 含score+docs数"]
        PD3["组装 enriched_input 注入 Agent"]
        PD1 --> PD2 --> PD3
    end

    PD --> PD_detail

    subgraph T1["Tool1: 标签检索 (max 3轮)"]
        T1I["输入: 预披露的初始标签"]
        T1A["1. 向量召回 top30 标签"]
        T1B["2. Reranker(query, tag) 精排"]
        T1C["3. 返回 top10 标签 + score + chunks_count"]
        T1D["4. Agent 判断是否继续扩展标签"]
        T1E["第3轮代码拦截: 已达上限，强制推进"]

        T1I --> T1A --> T1B --> T1C --> T1D
        T1D -->|继续扩展| T1A
        T1D -->|最多3轮| T1E
    end

    PD_detail --> T1

    subgraph SEL["Agent 选择标签子集"]
        SEL1["决策依据:"]
        SEL2["优先选 Rerank 分数 > 0.7 的标签"]
        SEL3["chunks_count > 50 的标签谨慎选择（可能太泛）"]
        SEL4["选中标签覆盖的文档总量控制在 50-100 篇"]
        SEL5["标签之间有包含关系时，只选更具体的"]

        SEL1 --> SEL2 --> SEL3 --> SEL4 --> SEL5
    end

    T1 --> SEL

    subgraph T2["Tool2: 分片检索 (双路搜索, max 2轮)"]
        T2I["输入: 选中的标签子集 + 原始 Query"]
        T2A1["路1: 标签向量搜 chunks"]
        T2A2["路2: tags 字段精确匹配"]
        T2B["合并去重"]
        T2C["Reranker(query, doc) 精排"]
        T2D["返回 top10 + score 分布"]
        T2E["Agent 判断是否需要更多文档"]

        T2I --> T2A1
        T2I --> T2A2
        T2A1 --> T2B
        T2A2 --> T2B
        T2B --> T2C --> T2D --> T2E
        T2E -->|score 普遍高| GEN[生成答案]
        T2E -->|score 普遍低| T2F["换标签/扩大范围"]
        T2F --> T1
        T2F --> T2A1
        T2F --> T2A2
    end

    SEL --> T2
    T2 --> GEN
```

### 混合可靠性策略

```
查询
  │
  ▼
┌──────────────────────────────────────┐
│ [代码层] 预执行:                     │
│ 1. tag_generate(query) → 生成初始标签 │
│ 2. tag_search_detailed(初始标签)      │
│    → 召回 top10 标签(含chunks_count)   │
│ 3. 将结果注入 Agent 的 user message    │
└──────────────┬───────────────────────┘
               ▼
┌──────────────────────────────────────┐
│ [LLM层] Agent 接收:                  │
│ "用户问: XXX                        │
│  初始标签检索结果: [标签列表]        │
│  请按两阶段流程工作..."               │
└──────────────┬───────────────────────┘
               ▼
         Agent 决策（更具确定性）:
         1. 是否扩展标签 → tag_search_detailed
         2. 选择哪些标签
         3. 按标签搜索 chunks → chunk_search_by_tags
         4. 生成答案
```

## 为什么标签也需要 Rerank

余弦相似度（双塔模型）捕捉的是主题相关性，不是任务相关性：

```
Query: "如何优化 LSTM 的梯度消失问题"

向量检索标签 TopK (余弦相似度):
  深度学习   0.91  ← 主题相关但太泛
  RNN        0.89  ← 方向对
  梯度消失   0.85  ← 精准命中，但排名反而靠后
  优化算法   0.83  ← "优化"语义匹配了，但说的是参数优化器
  LSTM       0.82  ← 和 RNN 有包含关系
```

Cross-Encoder Reranker 将 (query, tag) 作为联合输入，能捕捉上下文交互，重排后 "梯度消失" 应排在最前。

## 数据模型

### Milvus Collections / PostgreSQL 表

#### 标签集合 (tags_collection / tb_rag_tags)

| 字段 | 类型 | 说明 |
|------|------|------|
| tag_id / id | INT64 (PK) / BIGSERIAL | 标签 ID |
| tag_name | VARCHAR | 标签名称 |
| tag_vector | FLOAT_VECTOR / VECTOR | 标签描述的 embedding |
| tag_description | VARCHAR | 标签描述文本 |
| chunks_count | INT32 | 关联的 chunk 数量 |

#### 切片集合 (chunks_collection / tb_rag_chunks)

| 字段 | 类型 | 说明 |
|------|------|------|
| chunk_id / id | INT64 (PK) / BIGSERIAL | 切片 ID |
| doc_id | INT64 / BIGINT | 可选，关联的文档 ID |
| doc_vector | FLOAT_VECTOR / VECTOR | 切片 embedding |
| title | VARCHAR | 切片标题 |
| content | TEXT | 切片内容 |
| tags | ARRAY\<VARCHAR\> / VARCHAR[] | 关联标签列表 |
| source | VARCHAR | 来源 |
| metadata | JSON / JSONB | 扩展元数据 |

#### 文档集合 (docs_collection / tb_rag_docs)

| 字段 | 类型 | 说明 |
|------|------|------|
| doc_id / id | INT64 (PK) / BIGSERIAL | 文档 ID |
| doc_vector | FLOAT_VECTOR / VECTOR | 整篇文档的 embedding |
| title | VARCHAR | 文档标题 |
| content | TEXT | 文档全文 |
| source | VARCHAR | 来源 |
| metadata | JSON / JSONB | 扩展元数据 |

### 索引

- `tag_vector`: HNSW / IVF_FLAT，用于标签向量召回
- `doc_vector` (chunks): HNSW / IVF_FLAT，用于切片向量召回
- `doc_vector` (docs): HNSW / IVF_FLAT，用于文档级向量召回

## 分片搜索的双路策略

`chunk_search_by_tags` 采用双路搜索，增强召回覆盖率：

| 路径 | 搜索方式 | 覆盖场景 |
|------|----------|----------|
| 标签向量搜 chunks | `embed(tag_name)` → `search_chunks(tag_vector)` | 语义上关于该标签的 chunk（即使未打该标签） |
| tags 字段精确匹配 | `get_chunks_by_tags([tag_list])` → `WHERE tags && ARRAY[...]` | 显式标注了这些标签的 chunk（即使向量距离远） |

两路合并去重后，用 Reranker(query, texts) 统一精排，保证最终结果与用户问题最相关。

## 基础设施

### 向量存储后端

支持两种后端，通过环境变量 `TBD_RAG_VECTOR_STORE_BACKEND` 切换：

| 后端 | 配置值 | 说明 |
|------|--------|------|
| Milvus | `milvus` | 独立的向量数据库服务，需 Docker Compose 启动 |
| PostgreSQL + pgvector | `pgsql` | 由主 API 服务的 Flyway 管理 DDL，本模块只做 DML |

### Docker Compose (Milvus + 依赖)

```bash
# 启动所有服务
docker compose up -d

# 查看状态
docker compose ps

# 查看日志
docker compose logs -f milvus

# 停止
docker compose down
```

| 服务 | 端口 | 说明 |
|------|------|------|
| Milvus | 19530 (gRPC), 9091 (Metrics) | 向量数据库 |
| MinIO | 9002 (API), 9003 (Console) | Milvus 对象存储后端 |
| etcd | 2379 (内部) | Milvus 元数据存储 |

MinIO Console: http://localhost:9003 (minioadmin / minioadmin)

### 连接信息

```python
from pymilvus import MilvusClient

client = MilvusClient(uri="http://localhost:19530")
```

## Agent 架构

### 文件结构

```
agent/
├── __init__.py       # 导出 RagAgent、Conversation
├── agent.py          # RagAgent 主类：预披露 + 两阶段工具循环
├── prompts.py        # 两阶段工作流系统提示词
└── conversation.py   # 对话历史管理（消息列表、Token 控制）
```

### Agent 工作流程

```
chat("LSTM梯度消失怎么解决？")
  │
  ├─ 1. _pre_disclosure()
  │     ├── tag_generate() → LLM 生成初始标签
  │     └── tag_search_detailed() → 检索实际存在的标签
  │
  ├─ 2. 组装 enriched_input 注入消息
  │
  ├─ 3. _run_two_stage_loop()
  │     ├── LLM 决策：标签是否充分？
  │     ├── 需要扩展 → tag_search_detailed() （代码层限制最多3轮）
  │     ├── 选择标签子集 → LLM 输出标签列表
  │     └── 按标签搜 chunk → chunk_search_by_tags() （代码层限制最多2轮）
  │
  └─ 4. LLM 基于检索结果生成最终回答
```

### 可靠性保障

| 保障层 | 说明 |
|--------|------|
| 预披露 | tag_generate + tag_search_detailed 由代码自动执行，结果确定 |
| 代码轮次限制 | tag 最多 3 轮、chunk 最多 2 轮，超出则注入提示语强制推进 |
| 清晰的 system prompt | 包含具体阈值（score > 0.8, < 0.6）、docs 数限制（> 50 谨慎） |
| 结构化的工具结果 | tag 结果含 score + docs 数，chunk 结果含 score 分布 |

### 调用方式

```python
from agent import RagAgent

# 同步对话
agent = RagAgent()
answer = agent.chat("计算机视觉应该学习什么语言？")
print(answer)

# 流式对话（适合 HTTP SSE）
for chunk in agent.chat_stream("计算机视觉应该学习什么语言？"):
    print(chunk, end="", flush=True)

# 多轮对话（自动保持上下文）
agent = RagAgent()
print(agent.chat("什么是LSTM？"))
print(agent.chat("它的梯度消失怎么解决？"))  # 自动带上历史消息

# 重置对话
agent.reset_conversation()

# 自定义参数
agent = RagAgent(
    provider="siliconflow",
    model="deepseek-ai/DeepSeek-V3",
    temperature=0.3,
)
```

## 项目结构

```
ai-service/
├── config.py                       # 全局配置（Pydantic Settings）
├── pyproject.toml                  # 项目元数据与依赖
│
├── agent/                          # Agent 编排层
│   ├── agent.py                    # RagAgent 主逻辑
│   ├── prompts.py                  # 系统提示词管理
│   └── conversation.py             # 对话历史管理
│
├── llm_providers/                  # 模型提供商抽象层
│   ├── base.py                     # EmbeddingProvider, RerankerProvider, LLMProvider
│   ├── factory.py                  # EmbeddingFactory, LLMFactory, RerankerFactory
│   ├── siliconflow.py              # 硅基流动 API (embedding + rerank + llm)
│   ├── deepseek.py                 # DeepSeek 官方 API (llm)
│   └── ollama.py                   # Ollama 本地 (embedding + rerank + llm)
│
├── retrieval/                      # 向量存储层
│   ├── base.py                     # VectorStore 抽象基类 + 数据模型
│   ├── factory.py                  # VectorStoreFactory（单例，Milvus / PgVector 切换）
│   ├── milvus_store.py             # Milvus 向量数据库实现
│   └── pgvector_store.py           # PostgreSQL + pgvector 实现
│
├── tools/                          # Agent 可调用的工具
│   ├── base.py                     # ToolDefinition、TagSearchResult 数据类
│   ├── registry.py                 # ToolRegistry 注册表
│   ├── chunk_search.py             # 通用分片搜索（向量召回 + Rerank）
│   ├── tag_search.py               # 标签搜索 + 标签生成
│   ├── tag_search_detailed.py      # 增强标签搜索（含 score + chunks_count）
│   └── chunk_search_by_tags.py     # 双路分片搜索（标签向量 + 字段匹配）
│
├── chunking/                       # 文本分片
│   ├── factory.py                  # ChunkerFactory
│   └── semantic_chunker.py         # 基于 LLM 的语义主题分段器
│
├── pipeline/                       # 数据导入流水线
│   ├── loader.py                   # Loader 基类 + MsmarcoLoader + WordLoader
│   └── load2db_pipeline.py         # 完整的数据导入 Pipeline（CLI 入口）
│
└── tests/                          # 测试
```

### 工具注册表

所有工具通过 `tools/__init__.py` 自动注册到 `ToolRegistry`，Agent 通过 `ToolRegistry.get_function_calling_specs()` 获取 OpenAI 兼容的 Function Calling 规格。

| 工具名称 | 功能 | 调用方 |
|----------|------|--------|
| `tag_search` | 简单标签搜索 | 通用 |
| `tag_generate` | LLM 生成标签 | 预披露阶段 |
| `tag_search_detailed` | 增强标签搜索（含 score + docs 数） | Agent 决策扩展 |
| `chunk_search` | 通用分片搜索 | 通用 |
| `chunk_search_by_tags` | 双路分片搜索（标签向量 + 字段匹配） | Agent 按标签检索 |

## 模型提供商

每个能力独立选 provider，随意组合：

| 能力 | 硅基流动 (API) | DeepSeek (API) | Ollama (本地) |
|------|---------------|----------------|--------------|
| Embedding | `BAAI/bge-m3` | - | `bge-m3` |
| Reranker | `BAAI/bge-reranker-v2-m3` | - | LLM 模拟打分 |
| LLM | `deepseek-ai/DeepSeek-V3` 等 | `deepseek-chat` / `deepseek-reasoner` | `qwen3:8b` 等 |

## 环境变量配置

通过 `.env` 文件或环境变量配置（前缀 `TBD_RAG_`）：

```env
# 向量存储后端
TBD_RAG_VECTOR_STORE_BACKEND=milvus      # milvus | pgsql

# Milvus 连接
TBD_RAG_MILVUS_URI=http://localhost:19530
TBD_RAG_MILVUS_TOKEN=

# PostgreSQL + pgvector
TBD_RAG_PGVECTOR_URI=postgresql://user:pass@localhost:5432/bluenet
TBD_RAG_PGVECTOR_POOL_SIZE=10

# Collection / 表名
TBD_RAG_TAGS_COLLECTION_NAME=tags_collection
TBD_RAG_CHUNKS_COLLECTION_NAME=chunks_collection
TBD_RAG_DOCS_COLLECTION_NAME=docs_collection
TBD_RAG_VECTOR_DIMENSION=1024
TBD_RAG_VECTOR_INDEX_TYPE=HNSW
TBD_RAG_VECTOR_METRIC_TYPE=COSINE

# Embedding 模型（硅基流动 / Ollama）
TBD_RAG_EMBEDDING_PROVIDER=siliconflow
TBD_RAG_EMBEDDING_API_KEY=sk-xxx
TBD_RAG_EMBEDDING_MODEL=

# LLM（硅基流动 / DeepSeek / Ollama）
TBD_RAG_LLM_PROVIDER=siliconflow
TBD_RAG_LLM_API_KEY=sk-xxx
TBD_RAG_LLM_MODEL=
TBD_RAG_LLM_BASE_URL=
TBD_RAG_LLM_TEMPERATURE=0.7
TBD_RAG_LLM_TIMEOUT=60

# Reranker（硅基流动 / Ollama）
TBD_RAG_RERANKER_PROVIDER=siliconflow
TBD_RAG_RERANKER_API_KEY=sk-xxx
TBD_RAG_RERANKER_MODEL=

# 语义分片
TBD_RAG_CHUNK_MAX_TOKENS=4000
TBD_RAG_CHUNK_LLM_PROVIDER=deepseek
TBD_RAG_CHUNK_LLM_MODEL=deepseek-v4-flash
```

## 技术栈

- **向量数据库**: Milvus 2.6 (Standalone) / PostgreSQL + pgvector
- **Embedding**: 硅基流动 API / Ollama 本地
- **Reranker**: 硅基流动 API / Ollama 本地
- **LLM**: 硅基流动 API / DeepSeek 官方 API / Ollama 本地
- **Agent 框架**: LangChain ChatOpenAI (Function Calling)
- **文本分片**: 基于 LLM 的语义主题分段
- **对象存储**: MinIO（Milvus 后端依赖）
- **元数据存储**: etcd（Milvus 后端依赖）
