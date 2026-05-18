"""PostgreSQL + pgvector 向量存储实现。

Schema 管理（CREATE TABLE / CREATE INDEX）完全交由主 API 服务的 Flyway 迁移，
本模块仅负责 DML（CRUD、向量搜索、标量过滤）。

表结构假设（由 Flyway 创建）：
- tb_rag_tags   (tag_id PK, tag_name, tag_vector vector(dim), tag_description, chunks_count)
- tb_rag_chunks (chunk_id PK, doc_id, doc_vector vector(dim), title, content, tags[], source, metadata JSONB)
- tb_rag_docs   (doc_id PK, doc_vector vector(dim), title, content, source, metadata JSONB)
"""

from __future__ import annotations

import json
from typing import Any

from loguru import logger

from config import settings
from retrieval.base import (
    ChunkRecord,
    CollectionNotFoundError,
    ConnectionError,
    DocRecord,
    TagRecord,
    VectorStore,
)

_log = logger.bind(module="PgVectorStore")

# 表名映射（collection_name -> 实际表名）
_TABLE_MAP: dict[str, str] = {
    settings.TAGS_COLLECTION_NAME: "tb_rag_tags",
    settings.CHUNKS_COLLECTION_NAME: "tb_rag_chunks",
    settings.DOCS_COLLECTION_NAME: "tb_rag_docs",
}

# 反向映射（表名 -> collection_name）
_COLLECTION_MAP: dict[str, str] = {v: k for k, v in _TABLE_MAP.items()}

# 距离度量映射
_METRIC_OPS: dict[str, str] = {
    "COSINE": "<=>",
    "L2": "<->",
    "IP": "<#>",
}


def _get_table(name: str) -> str:
    """将 collection_name 映射为实际表名。"""
    if name in _TABLE_MAP:
        return _TABLE_MAP[name]
    # 允许直接传入表名
    if name in _COLLECTION_MAP:
        return name
    raise CollectionNotFoundError(f"未知的集合/表: {name}")


class PgVectorStore(VectorStore):
    """PostgreSQL + pgvector 向量存储实现。

    所有 DDL 由 Flyway 迁移脚本管理，本类仅执行 DML。
    启动时假设表已存在，若不存在则抛出 CollectionNotFoundError。
    """

    def __init__(self, uri: str | None = None, pool_size: int | None = None) -> None:
        """初始化 PostgreSQL 连接池。

        Args:
            uri: PostgreSQL 连接 URI，默认从 settings.PGVECTOR_URI 读取
            pool_size: 连接池大小，默认从 settings.PGVECTOR_POOL_SIZE 读取
        """
        self._uri = uri or settings.PGVECTOR_URI
        self._pool_size = pool_size or settings.PGVECTOR_POOL_SIZE
        self._pool: Any | None = None
        self._connect()

    # ------------------------------------------------------------------
    # 连接管理
    # ------------------------------------------------------------------

    def _connect(self) -> None:
        """建立 PostgreSQL 连接池。"""
        try:
            from psycopg_pool import ConnectionPool

            self._pool = ConnectionPool(
                conninfo=self._uri,
                min_size=1,
                max_size=self._pool_size,
                kwargs={"autocommit": True},
            )
            # 预热连接池
            self._pool.wait()
            _log.info(f"PostgreSQL 连接池已建立: {self._uri}")
        except Exception as exc:
            raise ConnectionError(f"无法连接到 PostgreSQL ({self._uri}): {exc}") from exc

    def close(self) -> None:
        """关闭连接池。"""
        if self._pool is not None:
            self._pool.close()
            self._pool = None
            _log.info("PostgreSQL 连接池已关闭")

    def __enter__(self) -> PgVectorStore:
        return self

    def __exit__(self, *args: Any) -> None:
        self.close()

    def _execute(
        self, sql: str, params: tuple | list | None = None, fetch: bool = False
    ) -> list[dict] | None:
        """执行 SQL 语句。

        Args:
            sql: SQL 语句
            params: 参数
            fetch: 是否获取结果

        Returns:
            若 fetch=True 返回结果列表，否则返回 None
        """
        if self._pool is None:
            raise ConnectionError("PostgreSQL 未连接")

        with self._pool.connection() as conn:
            with conn.cursor() as cur:
                cur.execute(sql, params)
                if fetch:
                    columns = [desc[0] for desc in cur.description] if cur.description else []
                    rows = cur.fetchall()
                    return [dict(zip(columns, row)) for row in rows]
                return None

    def _check_table_exists(self, table: str) -> bool:
        """检查表是否存在。"""
        sql = """
            SELECT EXISTS (
                SELECT 1 FROM information_schema.tables
                WHERE table_schema = 'public' AND table_name = %s
            )
        """
        result = self._execute(sql, (table,), fetch=True)
        return result[0]["exists"] if result else False

    # ------------------------------------------------------------------
    # Collection/Table 管理
    # ------------------------------------------------------------------

    def collection_exists(self, name: str) -> bool:
        """检查集合/表是否存在。"""
        try:
            table = _get_table(name)
        except CollectionNotFoundError:
            return False
        return self._check_table_exists(table)

    def list_collections(self) -> list[str]:
        """列出所有集合名称（即 Flyway 创建的 tb_rag_* 表）。"""
        sql = """
            SELECT table_name FROM information_schema.tables
            WHERE table_schema = 'public' AND table_name LIKE 'tb_rag_%'
        """
        rows = self._execute(sql, fetch=True)
        collections: list[str] = []
        for row in rows or []:
            table = row.get("table_name", "")
            if table in _COLLECTION_MAP:
                collections.append(_COLLECTION_MAP[table])
        return collections

    def drop_collection(self, name: str) -> None:
        """删除集合/表（TRUNCATE，保留表结构）。

        由于 Schema 由 Flyway 管理，本方法仅清空数据，不删除表。
        """
        table = _get_table(name)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")
        self._execute(f"TRUNCATE TABLE {table} RESTART IDENTITY CASCADE")
        _log.info(f"已清空表: {table}")

    def create_tags_collection(
        self, dimension: int | None = None, overwrite: bool = False
    ) -> None:
        """创建 tags 集合/表。

        由于 Schema 由 Flyway 管理，本方法仅检查表是否存在。
        若 overwrite=True 则清空已有数据。
        """
        table = _get_table(settings.TAGS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(
                f"表 {table} 不存在，请先由主 API 服务执行 Flyway 迁移"
            )
        if overwrite:
            self.drop_collection(settings.TAGS_COLLECTION_NAME)

    def create_chunks_collection(
        self, dimension: int | None = None, overwrite: bool = False
    ) -> None:
        """创建 chunks 集合/表。"""
        table = _get_table(settings.CHUNKS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(
                f"表 {table} 不存在，请先由主 API 服务执行 Flyway 迁移"
            )
        if overwrite:
            self.drop_collection(settings.CHUNKS_COLLECTION_NAME)

    def create_docs_collection(
        self, dimension: int | None = None, overwrite: bool = False
    ) -> None:
        """创建 docs 集合/表。"""
        table = _get_table(settings.DOCS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(
                f"表 {table} 不存在，请先由主 API 服务执行 Flyway 迁移"
            )
        if overwrite:
            self.drop_collection(settings.DOCS_COLLECTION_NAME)

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

        由于索引由 Flyway 迁移管理，本方法仅检查索引是否存在，
        不存在时发出警告（建议通过 Flyway 补充索引）。
        """
        table = _get_table(collection_name)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        # 检查是否已有 HNSW 索引
        if self.has_index(collection_name):
            _log.info(f"{table} 向量索引已存在，跳过")
            return

        _log.warning(
            f"{table} 缺少向量索引，建议通过 Flyway 迁移添加。"
            f" 示例: CREATE INDEX idx_{table}_vector ON {table} USING hnsw (xxx_vector vector_cosine_ops);"
        )

    def create_scalar_index(self, collection_name: str, field_name: str) -> None:
        """为标量字段创建索引。

        标量索引由 Flyway 迁移管理，本方法仅做检查。
        """
        table = _get_table(collection_name)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        _log.info(f"标量索引由 Flyway 管理，跳过手动创建: {table}.{field_name}")

    def has_index(self, collection_name: str, index_name: str = "") -> bool:
        """检查集合/表是否已有索引。"""
        table = _get_table(collection_name)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        if index_name:
            sql = """
                SELECT EXISTS (
                    SELECT 1 FROM pg_indexes
                    WHERE schemaname = 'public' AND tablename = %s AND indexname = %s
                )
            """
            result = self._execute(sql, (table, index_name), fetch=True)
        else:
            sql = """
                SELECT EXISTS (
                    SELECT 1 FROM pg_indexes
                    WHERE schemaname = 'public' AND tablename = %s
                )
            """
            result = self._execute(sql, (table,), fetch=True)
        return result[0]["exists"] if result else False

    def drop_index(self, collection_name: str, index_name: str = "") -> None:
        """删除集合/表的索引。

        由于索引由 Flyway 管理，本方法默认不执行删除，仅记录日志。
        如需强制删除，请通过 Flyway 迁移或手动执行 DROP INDEX。
        """
        table = _get_table(collection_name)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        _log.warning(
            f"索引由 Flyway 管理，不建议在运行时删除。"
            f" 如需删除请手动执行: DROP INDEX IF EXISTS {index_name or 'idx_name'};"
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
                    d[k] = None
                elif k == "metadata" and v is not None:
                    d[k] = json.dumps(v)
                else:
                    d[k] = v
            result.append(d)
        return result

    def insert_tags(self, data: list[TagRecord]) -> dict:
        """批量插入标签数据。"""
        table = _get_table(settings.TAGS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        rows = self._records_to_dicts(data)
        if not rows:
            return {"insert_count": 0, "ids": []}

        sql = f"""
            INSERT INTO {table} (tag_id, tag_name, tag_vector, tag_description, chunks_count)
            VALUES (%(tag_id)s, %(tag_name)s, %(tag_vector)s, %(tag_description)s, %(chunks_count)s)
        """
        for row in rows:
            self._execute(sql, row)

        return {"insert_count": len(rows), "ids": [r["tag_id"] for r in rows]}

    def upsert_tags(self, data: list[TagRecord]) -> dict:
        """批量覆盖插入标签数据（存在则更新）。"""
        table = _get_table(settings.TAGS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        rows = self._records_to_dicts(data)
        if not rows:
            return {"upsert_count": 0, "ids": []}

        sql = f"""
            INSERT INTO {table} (tag_id, tag_name, tag_vector, tag_description, chunks_count)
            VALUES (%(tag_id)s, %(tag_name)s, %(tag_vector)s, %(tag_description)s, %(chunks_count)s)
            ON CONFLICT (tag_id) DO UPDATE SET
                tag_name = EXCLUDED.tag_name,
                tag_vector = EXCLUDED.tag_vector,
                tag_description = EXCLUDED.tag_description,
                chunks_count = EXCLUDED.chunks_count
        """
        for row in rows:
            self._execute(sql, row)

        return {"upsert_count": len(rows), "ids": [r["tag_id"] for r in rows]}

    def insert_chunks(self, data: list[ChunkRecord]) -> dict:
        """批量插入分段数据。"""
        table = _get_table(settings.CHUNKS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        rows = self._records_to_dicts(data)
        if not rows:
            return {"insert_count": 0, "ids": []}

        sql = f"""
            INSERT INTO {table} (chunk_id, doc_id, doc_vector, title, content, tags, source, metadata)
            VALUES (%(chunk_id)s, %(doc_id)s, %(doc_vector)s, %(title)s, %(content)s, %(tags)s, %(source)s, %(metadata)s)
        """
        for row in rows:
            self._execute(sql, row)

        return {"insert_count": len(rows), "ids": [r["chunk_id"] for r in rows]}

    def upsert_chunks(self, data: list[ChunkRecord]) -> dict:
        """批量覆盖插入分段数据（存在则更新）。"""
        table = _get_table(settings.CHUNKS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        rows = self._records_to_dicts(data)
        if not rows:
            return {"upsert_count": 0, "ids": []}

        sql = f"""
            INSERT INTO {table} (chunk_id, doc_id, doc_vector, title, content, tags, source, metadata)
            VALUES (%(chunk_id)s, %(doc_id)s, %(doc_vector)s, %(title)s, %(content)s, %(tags)s, %(source)s, %(metadata)s)
            ON CONFLICT (chunk_id) DO UPDATE SET
                doc_id = EXCLUDED.doc_id,
                doc_vector = EXCLUDED.doc_vector,
                title = EXCLUDED.title,
                content = EXCLUDED.content,
                tags = EXCLUDED.tags,
                source = EXCLUDED.source,
                metadata = EXCLUDED.metadata
        """
        for row in rows:
            self._execute(sql, row)

        return {"upsert_count": len(rows), "ids": [r["chunk_id"] for r in rows]}

    def insert_docs(self, data: list[DocRecord]) -> dict:
        """批量插入文档数据。"""
        table = _get_table(settings.DOCS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        rows = self._records_to_dicts(data)
        if not rows:
            return {"insert_count": 0, "ids": []}

        sql = f"""
            INSERT INTO {table} (doc_id, doc_vector, title, content, source, metadata)
            VALUES (%(doc_id)s, %(doc_vector)s, %(title)s, %(content)s, %(source)s, %(metadata)s)
        """
        for row in rows:
            self._execute(sql, row)

        return {"insert_count": len(rows), "ids": [r["doc_id"] for r in rows]}

    def upsert_docs(self, data: list[DocRecord]) -> dict:
        """批量覆盖插入文档数据（存在则更新）。"""
        table = _get_table(settings.DOCS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        rows = self._records_to_dicts(data)
        if not rows:
            return {"upsert_count": 0, "ids": []}

        sql = f"""
            INSERT INTO {table} (doc_id, doc_vector, title, content, source, metadata)
            VALUES (%(doc_id)s, %(doc_vector)s, %(title)s, %(content)s, %(source)s, %(metadata)s)
            ON CONFLICT (doc_id) DO UPDATE SET
                doc_vector = EXCLUDED.doc_vector,
                title = EXCLUDED.title,
                content = EXCLUDED.content,
                source = EXCLUDED.source,
                metadata = EXCLUDED.metadata
        """
        for row in rows:
            self._execute(sql, row)

        return {"upsert_count": len(rows), "ids": [r["doc_id"] for r in rows]}

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
        """在 tags 表中执行向量搜索。"""
        table = _get_table(settings.TAGS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        metric = settings.VECTOR_METRIC_TYPE
        op = _METRIC_OPS.get(metric, "<=>")

        default_fields = ["tag_id", "tag_name", "tag_description", "chunks_count"]
        fields = output_fields or default_fields
        select_cols = ", ".join(fields)

        where_clause = ""
        params: list[Any] = [vector]
        if filters:
            # 简单转换 Milvus 过滤表达式为 SQL WHERE
            where_clause = "WHERE " + self._convert_filter(filters)

        sql = f"""
            SELECT {select_cols}, tag_vector {op} %s AS distance
            FROM {table}
            {where_clause}
            ORDER BY tag_vector {op} %s
            LIMIT {top_k}
        """
        params.append(vector)
        rows = self._execute(sql, params, fetch=True)
        return self._format_search_result(rows or [], fields)

    def search_chunks(
        self,
        vector: list[float],
        top_k: int = 100,
        tag_filter: list[str] | None = None,
        output_fields: list[str] | None = None,
    ) -> list[dict]:
        """在 chunks 表中执行向量搜索，支持标签过滤。"""
        table = _get_table(settings.CHUNKS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        metric = settings.VECTOR_METRIC_TYPE
        op = _METRIC_OPS.get(metric, "<=>")

        default_fields = ["chunk_id", "doc_id", "title", "content", "tags", "source", "metadata"]
        fields = output_fields or default_fields
        select_cols = ", ".join(fields)

        conditions: list[str] = []
        params: list[Any] = [vector]

        if tag_filter:
            conditions.append(self._build_tag_filter_sql(tag_filter))

        where_clause = ""
        if conditions:
            where_clause = "WHERE " + " AND ".join(conditions)

        sql = f"""
            SELECT {select_cols}, doc_vector {op} %s AS distance
            FROM {table}
            {where_clause}
            ORDER BY doc_vector {op} %s
            LIMIT {top_k}
        """
        params.append(vector)
        rows = self._execute(sql, params, fetch=True)
        return self._format_search_result(rows or [], fields)

    def search_docs(
        self,
        vector: list[float],
        top_k: int = 100,
        filters: str | None = None,
        output_fields: list[str] | None = None,
    ) -> list[dict]:
        """在 docs 表中执行向量搜索。"""
        table = _get_table(settings.DOCS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        metric = settings.VECTOR_METRIC_TYPE
        op = _METRIC_OPS.get(metric, "<=>")

        default_fields = ["doc_id", "title", "content", "source", "metadata"]
        fields = output_fields or default_fields
        select_cols = ", ".join(fields)

        where_clause = ""
        params: list[Any] = [vector]
        if filters:
            where_clause = "WHERE " + self._convert_filter(filters)

        sql = f"""
            SELECT {select_cols}, doc_vector {op} %s AS distance
            FROM {table}
            {where_clause}
            ORDER BY doc_vector {op} %s
            LIMIT {top_k}
        """
        params.append(vector)
        rows = self._execute(sql, params, fetch=True)
        return self._format_search_result(rows or [], fields)

    @staticmethod
    def _format_search_result(rows: list[dict], output_fields: list[str]) -> list[dict]:
        """格式化搜索结果，与 Milvus 返回结构保持一致。"""
        result: list[dict] = []
        for row in rows:
            item: dict[str, Any] = {
                "id": row.get("tag_id") or row.get("chunk_id") or row.get("doc_id"),
                "distance": row.get("distance"),
            }
            for field in output_fields:
                if field in row:
                    item[field] = row[field]
            result.append(item)
        return result

    def get_tags_by_ids(self, tag_ids: list[int]) -> list[dict]:
        """根据 tag_id 列表精确查询标签。"""
        table = _get_table(settings.TAGS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        if not tag_ids:
            return []

        placeholders = ", ".join(["%s"] * len(tag_ids))
        sql = f"""
            SELECT tag_id, tag_name, tag_vector, tag_description, chunks_count
            FROM {table}
            WHERE tag_id IN ({placeholders})
        """
        return self._execute(sql, tuple(tag_ids), fetch=True) or []

    def get_chunks_by_ids(self, chunk_ids: list[int]) -> list[dict]:
        """根据 chunk_id 列表精确查询分段。"""
        table = _get_table(settings.CHUNKS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        if not chunk_ids:
            return []

        placeholders = ", ".join(["%s"] * len(chunk_ids))
        sql = f"""
            SELECT chunk_id, doc_id, doc_vector, title, content, tags, source, metadata
            FROM {table}
            WHERE chunk_id IN ({placeholders})
        """
        return self._execute(sql, tuple(chunk_ids), fetch=True) or []

    def get_docs_by_ids(self, doc_ids: list[int]) -> list[dict]:
        """根据 doc_id 列表精确查询文档。"""
        table = _get_table(settings.DOCS_COLLECTION_NAME)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        if not doc_ids:
            return []

        placeholders = ", ".join(["%s"] * len(doc_ids))
        sql = f"""
            SELECT doc_id, doc_vector, title, content, source, metadata
            FROM {table}
            WHERE doc_id IN ({placeholders})
        """
        return self._execute(sql, tuple(doc_ids), fetch=True) or []

    def query_with_filter(
        self,
        collection_name: str,
        filter_expr: str,
        output_fields: list[str] | None = None,
        limit: int = 1000,
        offset: int = 0,
    ) -> list[dict]:
        """使用标量过滤条件查询数据（非向量搜索）。"""
        table = _get_table(collection_name)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        fields = output_fields or ["*"]
        select_cols = ", ".join(fields) if fields != ["*"] else "*"

        where_clause = ""
        if filter_expr:
            where_clause = "WHERE " + self._convert_filter(filter_expr)

        sql = f"""
            SELECT {select_cols}
            FROM {table}
            {where_clause}
            LIMIT {limit}
            OFFSET {offset}
        """
        return self._execute(sql, fetch=True) or []

    # ------------------------------------------------------------------
    # 辅助方法
    # ------------------------------------------------------------------

    @staticmethod
    def _build_tag_filter_sql(tags: list[str]) -> str:
        """构建标签数组过滤 SQL。

        使用 PostgreSQL 数组操作符 &&（交集）实现。
        """
        if not tags:
            return ""
        escaped = [t.replace("'", "''") for t in tags]
        array_literal = ", ".join(f"'{t}'" for t in escaped)
        return f"tags && ARRAY[{array_literal}]::varchar[]"

    @staticmethod
    def _convert_filter(milvus_expr: str) -> str:
        """将 Milvus 过滤表达式简单转换为 PostgreSQL WHERE 子句。

        支持简单的 ==、!=、>、<、>=、<= 和 and/or 组合。
        复杂表达式可能需要手动调整。
        """
        import re

        sql = milvus_expr
        # 替换 == 为 =
        sql = sql.replace(" == ", " = ")
        # 替换 and/or 为 AND/OR
        sql = re.sub(r'\band\b', 'AND', sql, flags=re.IGNORECASE)
        sql = re.sub(r'\bor\b', 'OR', sql, flags=re.IGNORECASE)
        # 处理 array_contains
        sql = re.sub(
            r'array_contains\s*\(\s*(\w+)\s*,\s*"([^"]+)"\s*\)',
            r"\1 @> ARRAY['\2']::varchar[]",
            sql,
        )
        return sql

    # ------------------------------------------------------------------
    # 数据统计
    # ------------------------------------------------------------------

    def get_collection_stats(self, name: str) -> dict:
        """获取集合/表统计信息。"""
        table = _get_table(name)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        sql = """
            SELECT
                relname AS table_name,
                n_live_tup AS row_count,
                pg_size_pretty(pg_total_relation_size(%s)) AS total_size
            FROM pg_stat_user_tables
            WHERE relname = %s
        """
        rows = self._execute(sql, (table, table), fetch=True)
        if rows:
            return {
                "row_count": rows[0].get("row_count", 0),
                "total_size": rows[0].get("total_size", "0 bytes"),
            }
        return {"row_count": 0, "total_size": "0 bytes"}

    def count_entities(self, name: str) -> int:
        """获取集合/表中的实体数量。"""
        table = _get_table(name)
        if not self._check_table_exists(table):
            raise CollectionNotFoundError(f"表不存在: {table}")

        sql = f"SELECT COUNT(*) AS cnt FROM {table}"
        rows = self._execute(sql, fetch=True)
        return rows[0]["cnt"] if rows else 0
