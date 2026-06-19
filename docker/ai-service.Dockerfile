# BlueNet AI Service Dockerfile
# 使用 uv 管理 Python 依赖，构建上下文应为项目根目录

# ---- Builder 阶段 ----
FROM ghcr.io/astral-sh/uv:python3.12-alpine AS builder

WORKDIR /app

ENV UV_COMPILE_BYTECODE=1
ENV UV_LINK_MODE=copy

# 复制依赖锁定文件（利用 Docker 缓存层）
COPY src/ai-service/pyproject.toml src/ai-service/uv.lock src/ai-service/uv.toml ./

# 安装生产依赖（不含项目本身）
RUN uv sync --frozen --no-install-project --no-dev

# 复制项目源码
COPY src/ai-service/ .

# 安装项目本身（editable 模式）
RUN uv sync --frozen --no-dev

# ---- Runtime 阶段 ----
FROM python:3.12-alpine

LABEL authors="IVEN"
LABEL description="BlueNet AI Service"

RUN apk add --no-cache curl

# 确保容器内使用 UTF-8 编码，避免中文请求/响应出现 400 或乱码
# Alpine + musl 原生支持 C.UTF-8，无需额外安装 locale 包
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV PYTHONUTF8=1

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# 从 builder 复制虚拟环境和源码（editable 安装依赖源码在 /app 路径下）
COPY --from=builder /app /app

ENV PATH="/app/.venv/bin:$PATH"

# 环境变量默认值（与 docker-compose 共享配置对齐）
ENV TBD_RAG_MILVUS_URI=http://milvus:19530
ENV TBD_RAG_MILVUS_TOKEN=
ENV TBD_RAG_PGVECTOR_URI=postgresql://postgres:postgres@database:5432/rag
ENV TBD_RAG_PGVECTOR_POOL_SIZE=10
ENV TBD_RAG_TAGS_COLLECTION_NAME=tags_collection
ENV TBD_RAG_CHUNKS_COLLECTION_NAME=chunks_collection
ENV TBD_RAG_DOCS_COLLECTION_NAME=docs_collection
ENV TBD_RAG_VECTOR_DIMENSION=1024
ENV TBD_RAG_VECTOR_INDEX_TYPE=HNSW
ENV TBD_RAG_VECTOR_METRIC_TYPE=COSINE
ENV TBD_RAG_VECTOR_STORE_BACKEND=milvus
ENV TBD_RAG_CHUNK_MAX_TOKENS=4000
ENV TBD_RAG_EMBEDDING_PROVIDER=siliconflow
ENV TBD_RAG_LLM_PROVIDER=siliconflow
ENV TBD_RAG_LLM_TEMPERATURE=0.7
ENV TBD_RAG_LLM_TIMEOUT=60
ENV TBD_RAG_RERANKER_PROVIDER=siliconflow

EXPOSE 8000

USER appuser

ENTRYPOINT ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
