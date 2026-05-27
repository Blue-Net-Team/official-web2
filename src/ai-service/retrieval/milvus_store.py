"""Milvus 向量数据库实现。

封装所有 pymilvus 操作，为上层提供简洁、类型安全的 API。
实现 VectorStore 抽象接口，支持策略模式切换。
"""

from __future__ import annotations

import time
from typing import Any

from pymilvus import (
    CollectionSchema,
    DataType,
    FieldSchema,
    MilvusClient,
)

from loguru import logger

from setting import settings
from retrieval.base import (
    ChunkRecord,
    CollectionNotFoundError,
    ConnectionError,
    DocRecord,
    IndexNotReadyError,
    TagRecord,
    VectorStore,
)

_log = logger.bind(module="MilvusStore")


class MilvusStore(VectorStore):
    """Milvus 向量数据库操作封装。

    提供 Collection 管理、索引管理、数据写入、向量搜索、标量查询等功能。

    示例::

        store = MilvusStore()
        store.create_tags_collection(dimension=1024)
        store.create_chunks_collection(dimension=1024)
        # ... 插入、搜索 ...
        store.close()
    """

    def __init__(self, uri: str | None = None, token: str | None = None) -> None:
        """初始化 Milvus 连接。

        Args:
            uri: Milvus 服务地址，默认从 settings.MILVUS_URI 读取
            token: 认证令牌，默认从 settings.MILVUS_TOKEN 读取
        """
        self._uri = uri or settings.MILVUS_URI
        self._token = token or settings.MILVUS_TOKEN
        self._client: MilvusClient | None = None
        self._connect()

    # ------------------------------------------------------------------
    # 连接管理
    # ------------------------------------------------------------------

    def _connect(self) -> None:
        """建立 Milvus 连接。"""
        try:
            kwargs: dict[str, Any] = {"uri": self._uri}
            if self._token:
                kwargs["token"] = self._token
            self._client = MilvusClient(**kwargs)
        except Exception as exc:
            raise ConnectionError(f"无法连接到 Milvus ({self._uri}): {exc}") from exc

    def close(self) -> None:
        """关闭 Milvus 连接。"""
        if self._client is not None:
            self._client.close()
            self._client = None

    def __enter__(self) -> MilvusStore:
        return self

    def __exit__(self, *args: Any) -> None:
        self.close()

    # ------------------------------------------------------------------
    # Collection 生命周期
    # ------------------------------------------------------------------

    def collection_exists(self, name: str) -> bool:
        """检查集合是否存在。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")
        return self._client.has_collection(name)

    def list_collections(self) -> list[str]:
        """列出所有集合名称。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")
        return self._client.list_collections()

    def drop_collection(self, name: str) -> None:
        """删除集合。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")
        self._client.drop_collection(name)

    def load_collection(self, name: str) -> None:
        """将集合加载到内存，查询前必须调用。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        if not self.has_index(name):
            _log.warning(f"集合 {name} 尚未创建索引，开始创建索引")
            self.create_vector_index(name)

        self._client.load_collection(name)

    def create_tags_collection(
        self, dimension: int | None = None, overwrite: bool = False
    ) -> None:
        """创建 tags_collection。

        Args:
            dimension: 向量维度，默认从 settings.VECTOR_DIMENSION 读取
            overwrite: 是否覆盖已存在的集合
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        dim = dimension or settings.VECTOR_DIMENSION
        name = settings.TAGS_COLLECTION_NAME

        if self.collection_exists(name):
            if overwrite:
                self.drop_collection(name)
            else:
                return

        schema = CollectionSchema(
            fields=[
                FieldSchema(
                    name="tag_id", dtype=DataType.INT64, is_primary=True, auto_id=False
                ),
                FieldSchema(name="tag_name", dtype=DataType.VARCHAR, max_length=128),
                FieldSchema(
                    name="tag_vector", dtype=DataType.FLOAT_VECTOR, dim=dim
                ),
                FieldSchema(
                    name="tag_description", dtype=DataType.VARCHAR, max_length=512
                ),
                FieldSchema(name="chunks_count", dtype=DataType.INT32),
            ],
            description="TBD-RAG Tags Collection",
        )

        self._client.create_collection(collection_name=name, schema=schema)

    def create_chunks_collection(
        self, dimension: int | None = None, overwrite: bool = False
    ) -> None:
        """创建 chunks_collection。

        Args:
            dimension: 向量维度，默认从 settings.VECTOR_DIMENSION 读取
            overwrite: 是否覆盖已存在的集合
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        dim = dimension or settings.VECTOR_DIMENSION
        name = settings.CHUNKS_COLLECTION_NAME

        if self.collection_exists(name):
            if overwrite:
                self.drop_collection(name)
            else:
                return

        schema = CollectionSchema(
            fields=[
                FieldSchema(
                    name="chunk_id",
                    dtype=DataType.INT64,
                    is_primary=True,
                    auto_id=False,
                ),
                FieldSchema(
                    name="doc_id", dtype=DataType.INT64, nullable=True
                ),
                FieldSchema(
                    name="doc_vector", dtype=DataType.FLOAT_VECTOR, dim=dim
                ),
                FieldSchema(name="title", dtype=DataType.VARCHAR, max_length=512),
                FieldSchema(
                    name="content", dtype=DataType.VARCHAR, max_length=65535
                ),
                FieldSchema(
                    name="tags",
                    dtype=DataType.ARRAY,
                    element_type=DataType.VARCHAR,
                    max_length=128,
                    max_capacity=50,
                ),
                FieldSchema(name="source", dtype=DataType.VARCHAR, max_length=64),
                FieldSchema(name="metadata", dtype=DataType.JSON),
            ],
            description="TBD-RAG Chunks Collection",
        )

        self._client.create_collection(collection_name=name, schema=schema)

    def create_docs_collection(
        self, dimension: int | None = None, overwrite: bool = False
    ) -> None:
        """创建 docs_collection。

        Args:
            dimension: 向量维度，默认从 settings.VECTOR_DIMENSION 读取
            overwrite: 是否覆盖已存在的集合
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        dim = dimension or settings.VECTOR_DIMENSION
        name = settings.DOCS_COLLECTION_NAME

        if self.collection_exists(name):
            if overwrite:
                self.drop_collection(name)
            else:
                return

        schema = CollectionSchema(
            fields=[
                FieldSchema(
                    name="doc_id", dtype=DataType.INT64, is_primary=True, auto_id=False
                ),
                FieldSchema(
                    name="doc_vector", dtype=DataType.FLOAT_VECTOR, dim=dim
                ),
                FieldSchema(name="title", dtype=DataType.VARCHAR, max_length=512),
                FieldSchema(
                    name="content", dtype=DataType.VARCHAR, max_length=65535
                ),
                FieldSchema(name="source", dtype=DataType.VARCHAR, max_length=64),
                FieldSchema(name="metadata", dtype=DataType.JSON),
            ],
            description="TBD-RAG Docs Collection",
        )

        self._client.create_collection(collection_name=name, schema=schema)

    # ------------------------------------------------------------------
    # 索引管理
    # ------------------------------------------------------------------

    def create_vector_index(
        self,
        collection_name: str,
        field_name: str = "",
        index_type: str | None = None,
        metric: str | None = None,
    ) -> None:
        """为向量字段创建索引。

        Args:
            collection_name: 集合名
            field_name: 向量字段名，空字符串时自动推断
            index_type: 索引类型，默认 HNSW
            metric: 距离度量，默认 COSINE
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        if not self.collection_exists(collection_name):
            raise CollectionNotFoundError(f"集合不存在: {collection_name}")

        idx_type = index_type or settings.VECTOR_INDEX_TYPE
        metric_type = metric or settings.VECTOR_METRIC_TYPE

        # 自动推断向量字段名
        if not field_name:
            if collection_name == settings.TAGS_COLLECTION_NAME:
                field_name = "tag_vector"
            elif collection_name == settings.CHUNKS_COLLECTION_NAME:
                field_name = "doc_vector"
            elif collection_name == settings.DOCS_COLLECTION_NAME:
                field_name = "doc_vector"
            else:
                raise ValueError("无法自动推断向量字段名，请显式指定 field_name")

        index_params = self._client.prepare_index_params()
        index_params.add_index(
            field_name=field_name,
            index_type=idx_type,
            metric_type=metric_type,
            params={"M": 16, "efConstruction": 200} if idx_type == "HNSW" else {"nlist": 128},
        )
        self._client.create_index(
            collection_name=collection_name, index_params=index_params
        )

    def create_scalar_index(
        self, collection_name: str, field_name: str
    ) -> None:
        """为标量字段创建倒排索引。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        if not self.collection_exists(collection_name):
            raise CollectionNotFoundError(f"集合不存在: {collection_name}")

        index_params = self._client.prepare_index_params()
        index_params.add_index(field_name=field_name, index_type="INVERTED")
        self._client.create_index(
            collection_name=collection_name, index_params=index_params
        )

    def has_index(self, collection_name: str, index_name: str = "") -> bool:
        """检查集合是否已有索引。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        if not self.collection_exists(collection_name):
            raise CollectionNotFoundError(f"集合不存在: {collection_name}")

        try:
            indexes = self._client.list_indexes(collection_name)
            if index_name:
                return index_name in indexes
            return len(indexes) > 0
        except Exception:
            return False

    def drop_index(self, collection_name: str, index_name: str = "") -> None:
        """删除集合的索引。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        if not self.collection_exists(collection_name):
            raise CollectionNotFoundError(f"集合不存在: {collection_name}")

        self._client.drop_index(collection_name=collection_name, index_name=index_name)

    def _wait_for_index_ready(
        self, collection_name: str, timeout: int = 30
    ) -> None:
        """等待索引构建完成。"""
        start = time.time()
        while time.time() - start < timeout:
            try:
                indexes = self._client.list_indexes(collection_name)
                for idx_name in indexes:
                    info = self._client.describe_index(collection_name, index_name=idx_name)
                    if info.get("state") == "Finished":
                        return
            except Exception:
                pass
            time.sleep(0.5)
        raise IndexNotReadyError(
            f"索引在 {timeout}s 内未就绪: {collection_name}"
        )

    # ------------------------------------------------------------------
    # 数据写入
    # ------------------------------------------------------------------

    @staticmethod
    def _records_to_dicts(
        records: list[TagRecord] | list[ChunkRecord] | list[DocRecord]
    ) -> list[dict]:
        """将 dataclass 列表转换为字典列表。"""
        result: list[dict] = []
        for r in records:
            d: dict[str, Any] = {}
            for k, v in r.__dict__.items():
                if k == "tags" and v is None:
                    d[k] = []
                elif k == "doc_id" and v is None:
                    continue
                else:
                    d[k] = v
            result.append(d)
        return result

    def insert_tags(self, data: list[TagRecord]) -> dict:
        """批量插入标签数据，已存在则忽略。

        Returns:
            {"insert_count": int, "ids": list[int]}
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.TAGS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        # 过滤掉已存在的标签
        existing = self.query_with_filter(
            collection_name=name,
            filter_expr=f"tag_name in {[d.tag_name for d in data]}",
            output_fields=["tag_name"],
            limit=len(data),
        )
        existing_names = {r["tag_name"] for r in existing}
        rows = self._records_to_dicts([d for d in data if d.tag_name not in existing_names])

        if not rows:
            return {"insert_count": 0, "ids": []}

        result = self._client.insert(collection_name=name, data=rows)
        return {
            "insert_count": result["insert_count"],
            "ids": result["ids"],
        }

    def insert_chunks(self, data: list[ChunkRecord]) -> dict:
        """批量插入分段数据。

        Returns:
            {"insert_count": int, "ids": list[int]}
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.CHUNKS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        rows = self._records_to_dicts(data)
        result = self._client.insert(collection_name=name, data=rows)
        return {
            "insert_count": result["insert_count"],
            "ids": result["ids"],
        }

    def upsert_tags(self, data: list[TagRecord]) -> dict:
        """批量覆盖插入标签数据（存在则更新）。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.TAGS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        rows = self._records_to_dicts(data)
        result = self._client.upsert(collection_name=name, data=rows)
        return {
            "upsert_count": result["upsert_count"],
            "ids": result["ids"],
        }

    def upsert_chunks(self, data: list[ChunkRecord]) -> dict:
        """批量覆盖插入分段数据（存在则更新）。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.CHUNKS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        rows = self._records_to_dicts(data)
        result = self._client.upsert(collection_name=name, data=rows)
        return {
            "upsert_count": result["upsert_count"],
            "ids": result["ids"],
        }

    def insert_docs(self, data: list[DocRecord]) -> dict:
        """批量插入文档数据。

        Returns:
            {"insert_count": int, "ids": list[int]}
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.DOCS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        rows = self._records_to_dicts(data)
        result = self._client.insert(collection_name=name, data=rows)
        return {
            "insert_count": result["insert_count"],
            "ids": result["ids"],
        }

    def upsert_docs(self, data: list[DocRecord]) -> dict:
        """批量覆盖插入文档数据（存在则更新）。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.DOCS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        rows = self._records_to_dicts(data)
        result = self._client.upsert(collection_name=name, data=rows)
        return {
            "upsert_count": result["upsert_count"],
            "ids": result["ids"],
        }

    def search_docs(
        self,
        vector: list[float],
        top_k: int = 100,
        filters: str | None = None,
        output_fields: list[str] | None = None,
    ) -> list[dict]:
        """在 docs_collection 中执行向量搜索。

        Args:
            vector: 查询向量
            top_k: 返回结果数量
            filters: 标量过滤表达式，如 "source == 'msmarco'"
            output_fields: 需要返回的字段列表

        Returns:
            搜索结果列表
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.DOCS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        default_fields = ["doc_id", "title", "content", "source", "metadata"]
        fields = output_fields or default_fields

        result = self._client.search(
            collection_name=name,
            data=[vector],
            limit=top_k,
            filter=filters or "",
            output_fields=fields,
        )
        return self._flatten_search_result(result)

    def get_docs_by_ids(self, doc_ids: list[int]) -> list[dict]:
        """根据 doc_id 列表精确查询文档。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.DOCS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        return self._client.get(collection_name=name, ids=doc_ids)

    # ------------------------------------------------------------------
    # 数据查询
    # ------------------------------------------------------------------

    def search_tags(
        self,
        vector: list[float],
        top_k: int = 30,
        filters: str | None = None,
        output_fields: list[str] | None = None,
    ) -> list[dict]:
        """在 tags_collection 中执行向量搜索。

        Args:
            vector: 查询向量
            top_k: 返回结果数量
            filters: 标量过滤表达式，如 "chunks_count > 10"
            output_fields: 需要返回的字段列表

        Returns:
            搜索结果列表，每项包含 id, distance, 以及请求的 output_fields
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.TAGS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        default_fields = ["tag_id", "tag_name", "tag_description", "chunks_count"]
        fields = output_fields or default_fields

        result = self._client.search(
            collection_name=name,
            data=[vector],
            limit=top_k,
            filter=filters or "",
            output_fields=fields,
        )
        return self._flatten_search_result(result)

    def search_chunks(
        self,
        vector: list[float],
        top_k: int = 100,
        tag_filter: list[str] | None = None,
        output_fields: list[str] | None = None,
    ) -> list[dict]:
        """在 chunks_collection 中执行向量搜索，支持标签过滤。

        Args:
            vector: 查询向量
            top_k: 返回结果数量
            tag_filter: 标签过滤列表，如 ["深度学习", "NLP"]
            output_fields: 需要返回的字段列表

        Returns:
            搜索结果列表
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.CHUNKS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        filters = ""
        if tag_filter:
            filters = self._build_tag_filter_expr(tag_filter)

        default_fields = ["chunk_id", "title", "content", "tags", "source", "metadata"]
        fields = output_fields or default_fields

        result = self._client.search(
            collection_name=name,
            data=[vector],
            limit=top_k,
            filter=filters,
            output_fields=fields,
        )
        return self._flatten_search_result(result)

    def get_chunks_by_tags(self, tags: list[str], limit: int = 50) -> list[dict]:
        """根据标签列表精确查询 chunks（纯标签匹配，非向量搜索）。

        使用 Milvus query() + array_contains 过滤，不依赖向量相似度。
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.CHUNKS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        if not tags:
            return []

        filter_expr = self._build_tag_filter_expr(tags)
        fields = ["chunk_id", "title", "content", "tags", "source", "metadata"]

        return self._client.query(
            collection_name=name,
            filter=filter_expr,
            output_fields=fields,
            limit=limit,
        )

    @staticmethod
    def _flatten_search_result(raw: list[list[dict]]) -> list[dict]:
        """将 Milvus 嵌套搜索结果展平为列表。"""
        flattened: list[dict] = []
        for batch in raw:
            for item in batch:
                flat: dict[str, Any] = {
                    "id": item.get("id"),
                    "distance": item.get("distance"),
                }
                flat.update(item.get("entity", {}))
                flattened.append(flat)
        return flattened

    def get_tags_by_ids(self, tag_ids: list[int]) -> list[dict]:
        """根据 tag_id 列表精确查询标签。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.TAGS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        return self._client.get(collection_name=name, ids=tag_ids)

    def get_chunks_by_ids(self, chunk_ids: list[int]) -> list[dict]:
        """根据 chunk_id 列表精确查询分段。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        name = settings.CHUNKS_COLLECTION_NAME
        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        return self._client.get(collection_name=name, ids=chunk_ids)

    def query_with_filter(
        self,
        collection_name: str,
        filter_expr: str,
        output_fields: list[str] | None = None,
        limit: int = 1000,
        offset: int = 0,
    ) -> list[dict]:
        """使用标量过滤条件查询数据（非向量搜索）。

        Args:
            collection_name: 集合名
            filter_expr: 过滤表达式，如 "source == 'msmarco'"
            output_fields: 返回字段
            limit: 最大返回数量
            offset: 偏移量

        Returns:
            匹配的记录列表
        """
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        if not self.collection_exists(collection_name):
            raise CollectionNotFoundError(f"集合不存在: {collection_name}")

        return self._client.query(
            collection_name=collection_name,
            filter=filter_expr,
            output_fields=output_fields or ["*"],
            limit=limit,
            offset=offset,
        )

    # ------------------------------------------------------------------
    # 辅助方法
    # ------------------------------------------------------------------

    @staticmethod
    def _build_tag_filter_expr(tags: list[str]) -> str:
        """构建 ARRAY 字段的过滤表达式。

        示例:
            ["深度学习", "NLP"] -> 'array_contains(tags, "深度学习") or array_contains(tags, "NLP")'
        """
        if not tags:
            return ""
        conditions = [f'array_contains(tags, "{t}")' for t in tags]
        return " or ".join(conditions)

    # ------------------------------------------------------------------
    # 数据统计
    # ------------------------------------------------------------------

    def get_collection_stats(self, name: str) -> dict:
        """获取集合统计信息。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        return self._client.get_collection_stats(name)

    def count_entities(self, name: str) -> int:
        """获取集合中的实体数量。"""
        if self._client is None:
            raise ConnectionError("Milvus 未连接")

        if not self.collection_exists(name):
            raise CollectionNotFoundError(f"集合不存在: {name}")

        stats = self._client.get_collection_stats(name)
        return stats.get("row_count", 0)
