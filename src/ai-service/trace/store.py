"""轨迹持久化（写入端）。

只执行 DML：表结构由后端 Flyway 迁移管理（与 ``tb_rag_*`` 同一模式）。
AI 服务是唯一写入方，后端只读。
"""

from __future__ import annotations

from typing import Any

from loguru import logger
from psycopg.types.json import Jsonb

from setting import settings

from .recorder import TraceRecord

_log = logger.bind(module="TraceStore")


class TraceStore:
    """AI 对话轨迹的写入端。"""

    def __init__(self, uri: str | None = None, pool_size: int | None = None) -> None:
        """建立 PostgreSQL 连接池。

        Args:
            uri: 连接串，默认取 ``settings.TRACE_DB_URI``，为空时复用 ``PGVECTOR_URI``。
            pool_size: 连接池上限。
        """
        from psycopg_pool import ConnectionPool

        self._uri = uri or settings.TRACE_DB_URI or settings.PGVECTOR_URI
        self._pool: Any = ConnectionPool(
            conninfo=self._uri,
            min_size=1,
            max_size=pool_size or settings.PGVECTOR_POOL_SIZE,
            kwargs={"autocommit": False},
        )
        self._pool.wait()
        _log.info("轨迹存储连接池已建立")

    def close(self) -> None:
        if self._pool is not None:
            self._pool.close()
            self._pool = None

    # ------------------------------------------------------------------
    # 内部 SQL 辅助
    # ------------------------------------------------------------------

    def _execute(self, sql: str, params: tuple | None = None) -> None:
        with self._pool.connection() as conn:
            with conn.cursor() as cur:
                cur.execute(sql, params)
            conn.commit()

    def _query(self, sql: str, params: tuple | None = None) -> list[dict]:
        with self._pool.connection() as conn:
            with conn.cursor() as cur:
                cur.execute(sql, params)
                columns = [desc[0] for desc in cur.description] if cur.description else []
                return [dict(zip(columns, row)) for row in cur.fetchall()]

    # ------------------------------------------------------------------
    # 写入
    # ------------------------------------------------------------------

    def save(self, record: TraceRecord) -> None:
        """落库一条提问轨迹，并维护所属会话。

        会话 upsert 会对该行加锁，从而把同一会话内的 ``seq`` 分配串行化，
        避免并发写入时出现重复序号。
        """
        with self._pool.connection() as conn:
            with conn.cursor() as cur:
                cur.execute(
                    """
                    INSERT INTO tb_ai_conversation (id, created_at, last_active_at)
                    VALUES (%s, NOW(), NOW())
                    ON CONFLICT (id) DO UPDATE SET last_active_at = NOW()
                    """,
                    (record.conversation_id,),
                )
                cur.execute(
                    """
                    INSERT INTO tb_ai_turn
                        (conversation_id, seq, user_input, answer, prompt, events,
                         degraded, duration_ms)
                    VALUES (
                        %s,
                        (SELECT COALESCE(MAX(seq), 0) + 1
                           FROM tb_ai_turn WHERE conversation_id = %s),
                        %s, %s, %s, %s, %s, %s
                    )
                    """,
                    (
                        record.conversation_id,
                        record.conversation_id,
                        record.user_input,
                        record.answer,
                        Jsonb(record.prompt) if record.prompt is not None else None,
                        Jsonb(record.events),
                        record.degraded,
                        record.duration_ms,
                    ),
                )
            conn.commit()

    # ------------------------------------------------------------------
    # 读取辅助（供测试与运维排查）
    # ------------------------------------------------------------------

    def list_seqs(self, conversation_id: str) -> list[int]:
        """返回某会话下的序号列表，按升序。"""
        rows = self._query(
            "SELECT seq FROM tb_ai_turn WHERE conversation_id = %s ORDER BY seq",
            (conversation_id,),
        )
        return [row["seq"] for row in rows]

    def get_conversation(self, conversation_id: str) -> dict | None:
        """返回会话行，不存在时返回 ``None``。"""
        rows = self._query(
            "SELECT id, created_at, last_active_at FROM tb_ai_conversation WHERE id = %s",
            (conversation_id,),
        )
        return rows[0] if rows else None

    # ------------------------------------------------------------------
    # 保留策略
    # ------------------------------------------------------------------

    # 轨迹数据全量保留：不提供删除或过期清理能力。
    # 数据量可控（万级提问约 270 MB），保留完整历史以支持跨招新季的问题趋势对比。
    # 决策与理由见 openspec/changes/add-ai-trace-admin/design.md 的 D11。


# ---------------------------------------------------------------------------
# 全局默认存储
# ---------------------------------------------------------------------------

_default_store: TraceStore | None = None
_default_store_ready = False


def get_trace_store() -> TraceStore | None:
    """获取全局轨迹存储（惰性单例）。

    初始化失败或采集已关闭时返回 ``None`` —— 调用方应把 ``None`` 视为“不采集”，
    而非错误。
    """
    global _default_store, _default_store_ready
    if not settings.TRACE_ENABLED:
        return None
    if not _default_store_ready:
        _default_store_ready = True
        try:
            _default_store = TraceStore()
        except Exception as exc:
            _log.warning(f"轨迹存储初始化失败，采集已禁用: {exc}")
            _default_store = None
    return _default_store


def reset_trace_store() -> None:
    """重置单例缓存（主要供测试使用）。"""
    global _default_store, _default_store_ready
    if _default_store is not None:
        _default_store.close()
    _default_store = None
    _default_store_ready = False
