"""向量存储抽象基类、数据模型与异常体系。

所有存储引擎无关的结构定义在此模块，供上层统一导入。
"""

from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Any


# ---------------------------------------------------------------------------
# 异常体系
# ---------------------------------------------------------------------------


class VectorStoreError(Exception):
    """向量存储操作基础异常。"""

    pass


class CollectionNotFoundError(VectorStoreError):
    """集合/表不存在。"""

    pass


class IndexNotReadyError(VectorStoreError):
    """索引未就绪。"""

    pass


class ConnectionError(VectorStoreError):
    """连接失败。"""

    pass


# ---------------------------------------------------------------------------
# 数据模型
# ---------------------------------------------------------------------------


@dataclass
class TagRecord:
    """标签记录。"""

    tag_name: str
    tag_vector: list[float]
    id: int | None = None
    tag_description: str = ""
    chunks_count: int = 0


@dataclass
class ChunkRecord:
    """分段记录。"""

    doc_vector: list[float]
    id: int | None = None
    doc_id: int | None = None
    title: str = ""
    content: str = ""
    tags: list[str] | None = None
    source: str = ""
    metadata: dict | None = None


@dataclass
class DocRecord:
    """文档记录。"""

    doc_vector: list[float]
    id: int | None = None
    title: str = ""
    content: str = ""
    source: str = ""
    metadata: dict | None = None


# ---------------------------------------------------------------------------
# 抽象基类
# ---------------------------------------------------------------------------


class VectorStore(ABC):
    """向量存储抽象基类。

    所有底层存储引擎（Milvus、PgVector 等）必须实现此接口，
    以保证上层业务代码零改动切换后端。
    """

    # ------------------------------------------------------------------
    # 生命周期
    # ------------------------------------------------------------------

    def close(self) -> None:
        """关闭存储连接。"""
        pass

    def __enter__(self) -> VectorStore:
        return self

    def __exit__(self, *args: Any) -> None:
        self.close()

    def load_collection(self, name: str) -> None:
        """将集合加载到内存（Milvus 需要，PgVector 空操作）。"""
        pass

    # ------------------------------------------------------------------
    # Collection/Table 管理
    # ------------------------------------------------------------------

    @abstractmethod
    def collection_exists(self, name: str) -> bool:
        """检查集合/表是否存在。"""
        ...

    @abstractmethod
    def list_collections(self) -> list[str]:
        """列出所有集合/表名称。"""
        ...

    @abstractmethod
    def drop_collection(self, name: str) -> None:
        """删除集合/表。"""
        ...

    @abstractmethod
    def create_tags_collection(
        self, dimension: int | None = None, overwrite: bool = False
    ) -> None:
        """创建 tags 集合/表。"""
        ...

    @abstractmethod
    def create_chunks_collection(
        self, dimension: int | None = None, overwrite: bool = False
    ) -> None:
        """创建 chunks 集合/表。"""
        ...

    @abstractmethod
    def create_docs_collection(
        self, dimension: int | None = None, overwrite: bool = False
    ) -> None:
        """创建 docs 集合/表。"""
        ...

    # ------------------------------------------------------------------
    # 索引管理
    # ------------------------------------------------------------------

    @abstractmethod
    def create_vector_index(
        self,
        collection_name: str,
        field_name: str = "",
        index_type: str | None = None,
        metric: str | None = None,
    ) -> None:
        """为向量字段创建索引。"""
        ...

    @abstractmethod
    def create_scalar_index(self, collection_name: str, field_name: str) -> None:
        """为标量字段创建索引。"""
        ...

    @abstractmethod
    def has_index(self, collection_name: str, index_name: str = "") -> bool:
        """检查集合/表是否已有索引。"""
        ...

    @abstractmethod
    def drop_index(self, collection_name: str, index_name: str = "") -> None:
        """删除集合/表的索引。"""
        ...

    # ------------------------------------------------------------------
    # 数据写入
    # ------------------------------------------------------------------

    @abstractmethod
    def insert_tags(self, data: list[TagRecord]) -> dict:
        """批量插入标签数据。"""
        ...

    @abstractmethod
    def upsert_tags(self, data: list[TagRecord]) -> dict:
        """批量覆盖插入标签数据（存在则更新）。"""
        ...

    @abstractmethod
    def insert_chunks(self, data: list[ChunkRecord]) -> dict:
        """批量插入分段数据。"""
        ...

    @abstractmethod
    def upsert_chunks(self, data: list[ChunkRecord]) -> dict:
        """批量覆盖插入分段数据（存在则更新）。"""
        ...

    @abstractmethod
    def insert_docs(self, data: list[DocRecord]) -> dict:
        """批量插入文档数据。"""
        ...

    @abstractmethod
    def upsert_docs(self, data: list[DocRecord]) -> dict:
        """批量覆盖插入文档数据（存在则更新）。"""
        ...

    # ------------------------------------------------------------------
    # 数据查询
    # ------------------------------------------------------------------

    @abstractmethod
    def search_tags(
        self,
        vector: list[float],
        top_k: int = 30,
        filters: str | None = None,
        output_fields: list[str] | None = None,
    ) -> list[TagRecord]:
        """在 tags 集合/表中执行向量搜索。"""
        ...

    @abstractmethod
    def search_chunks(
        self,
        vector: list[float],
        top_k: int = 100,
        tag_filter: list[str] | None = None,
        output_fields: list[str] | None = None,
    ) -> list[ChunkRecord]:
        """在 chunks 集合/表中执行向量搜索，支持标签过滤。"""
        ...

    @abstractmethod
    def search_docs(
        self,
        vector: list[float],
        top_k: int = 100,
        filters: str | None = None,
        output_fields: list[str] | None = None,
    ) -> list[DocRecord]:
        """在 docs 集合/表中执行向量搜索。"""
        ...

    @abstractmethod
    def get_tags_by_ids(self, tag_ids: list[int]) -> list[TagRecord]:
        """根据 tag_id 列表精确查询标签。"""
        ...
        
    @abstractmethod
    def get_all_tags(self) -> list[TagRecord]:
        """获取所有标签。"""
        ...

    @abstractmethod
    def get_chunks_by_ids(self, chunk_ids: list[int]) -> list[ChunkRecord]:
        """根据 chunk_id 列表精确查询分段。"""
        ...

    @abstractmethod
    def get_docs_by_ids(self, doc_ids: list[int]) -> list[DocRecord]:
        """根据 doc_id 列表精确查询文档。"""
        ...

    @abstractmethod
    def get_chunks_by_tags(
        self,
        tags: list[str],
        limit: int = 50,
    ) -> list[ChunkRecord]:
        """根据标签列表精确查询 chunks（纯标签匹配，非向量搜索）。"""
        ...

    @abstractmethod
    def query_with_filter(
        self,
        collection_name: str,
        filter_expr: str,
        output_fields: list[str] | None = None,
        limit: int = 1000,
        offset: int = 0,
    ) -> list[DocRecord]:
        """使用标量过滤条件查询数据（非向量搜索）。"""
        ...

    # ------------------------------------------------------------------
    # 数据统计
    # ------------------------------------------------------------------

    @abstractmethod
    def get_collection_stats(self, name: str) -> dict:
        """获取集合/表统计信息。"""
        ...

    @abstractmethod
    def count_entities(self, name: str) -> int:
        """获取集合/表中的实体数量。"""
        ...
