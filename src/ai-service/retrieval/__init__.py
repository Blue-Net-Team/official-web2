"""向量存储模块公共 API。

统一导出抽象基类、数据模型、异常体系、工厂与具体实现，
供上层（Pipeline、Service）直接导入使用。
"""

from retrieval.base import (
    ChunkRecord,
    CollectionNotFoundError,
    ConnectionError,
    DocRecord,
    IndexNotReadyError,
    TagRecord,
    VectorStore,
    VectorStoreError,
)
from retrieval.factory import VectorStoreFactory
from retrieval.milvus_store import MilvusStore
from retrieval.pgvector_store import PgVectorStore

__all__ = [
    # 抽象基类
    "VectorStore",
    # 工厂
    "VectorStoreFactory",
    # 具体实现
    "MilvusStore",
    "PgVectorStore",
    # 数据模型
    "TagRecord",
    "ChunkRecord",
    "DocRecord",
    # 异常体系
    "VectorStoreError",
    "CollectionNotFoundError",
    "IndexNotReadyError",
    "ConnectionError",
]
