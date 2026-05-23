"""VectorStore 工厂，支持配置驱动单例复用。"""

from __future__ import annotations

from typing import ClassVar

from loguru import logger

from setting import settings
from retrieval.base import VectorStore

_log = logger.bind(module="VectorStoreFactory")


class VectorStoreFactory:
    """VectorStore 工厂。

    根据配置自动创建并缓存 VectorStore 实例，供多个组件复用。
    单例模式：首次调用 get() 时创建，后续直接返回缓存实例。
    """

    _instance: ClassVar[VectorStore | None] = None

    @classmethod
    def get(cls) -> VectorStore:
        """获取 VectorStore 实例（单例，首次创建后复用）。"""
        if cls._instance is not None:
            return cls._instance

        backend = settings.VECTOR_STORE_BACKEND.lower()
        _log.info(f"初始化 VectorStore 后端: {backend}")

        if backend == "milvus":
            from retrieval.milvus_store import MilvusStore

            cls._instance = MilvusStore()
        elif backend == "pgsql":
            from retrieval.pgvector_store import PgVectorStore

            cls._instance = PgVectorStore()
        else:
            raise ValueError(f"不支持的向量存储后端: {backend}")

        return cls._instance

    @classmethod
    def reset(cls) -> None:
        """重置单例缓存（主要用于测试）。"""
        if cls._instance is not None:
            cls._instance.close()
            cls._instance = None
