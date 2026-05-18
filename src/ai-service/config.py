"""环境驱动的配置模块。

所有配置项优先从环境变量读取，其次从 .env 文件读取，最后使用默认值。
环境变量前缀为 TBD_RAG_，例如：
    TBD_RAG_MILVUS_URI=http://localhost:19530
"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """TBD-RAG 全局配置。"""

    # Milvus 连接
    MILVUS_URI: str = "http://localhost:19530"
    MILVUS_TOKEN: str = ""

    # Collection 名称
    TAGS_COLLECTION_NAME: str = "tags_collection"
    CHUNKS_COLLECTION_NAME: str = "chunks_collection"
    DOCS_COLLECTION_NAME: str = "docs_collection"

    # 向量维度
    VECTOR_DIMENSION: int = 1024

    # 索引配置
    VECTOR_INDEX_TYPE: str = "HNSW"
    VECTOR_METRIC_TYPE: str = "COSINE"

    # 向量存储后端配置
    VECTOR_STORE_BACKEND: str = "milvus"  # "milvus" | "pgsql"
    PGVECTOR_URI: str = "postgresql://user:pass@localhost:5432/db"
    PGVECTOR_POOL_SIZE: int = 10

    # 语义分片配置
    CHUNK_MAX_TOKENS: int = 4000
    CHUNK_LLM_PROVIDER: str = "deepseek"
    CHUNK_LLM_MODEL: str = "deepseek-v4-flash"
    CHUNK_LLM_BASE_URL: str = ""
    CHUNK_LLM_API_KEY: str = ""

    class Config:
        env_file = ".env"
        env_prefix = "TBD_RAG_"
        extra = "ignore"


settings = Settings()
