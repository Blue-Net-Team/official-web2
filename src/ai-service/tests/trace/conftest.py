"""轨迹采集测试的共享 fixture。"""

from __future__ import annotations

import uuid

import pytest

from setting import settings


@pytest.fixture
def store_with_db():
    """提供一个连接真实 PostgreSQL 的 TraceStore，并在结束后清理测试数据。

    数据库不可用或轨迹表不存在时跳过测试，避免在没有基础设施的环境里失败。
    """
    pytest.importorskip("psycopg")

    from trace.store import TraceStore

    uri = settings.TRACE_DB_URI or settings.PGVECTOR_URI
    try:
        store = TraceStore(uri=uri, pool_size=2)
    except Exception as exc:  # pragma: no cover - 环境依赖
        pytest.skip(f"PostgreSQL 不可用，跳过轨迹落库集成测试: {exc}")

    try:
        store._query("SELECT 1 FROM tb_ai_turn LIMIT 1")
    except Exception as exc:  # pragma: no cover - 环境依赖
        store.close()
        pytest.skip(f"轨迹表不存在，请先执行 Flyway 迁移: {exc}")

    conversation_id = f"test-{uuid.uuid4()}"
    try:
        yield store, conversation_id
    finally:
        try:
            store._execute("DELETE FROM tb_ai_turn WHERE conversation_id = %s", (conversation_id,))
            store._execute("DELETE FROM tb_ai_conversation WHERE id = %s", (conversation_id,))
        finally:
            store.close()
