"""PgVectorStore 标签向量写回的单元测试（不依赖真实数据库）。"""

from __future__ import annotations

from retrieval.pgvector_store import PgVectorStore


def _make_store() -> PgVectorStore:
    """构造不连接数据库的 store 实例（仅用于方法级 SQL 断言）。"""
    store = PgVectorStore.__new__(PgVectorStore)
    store._pool = object()  # 绕过 _execute 的连接检查（本测试会替换 _execute）
    return store


def test_update_tag_vector_marks_synced(monkeypatch):
    """更新标签向量时应同事务将 vector_status 置为 synced。"""
    store = _make_store()
    executed: list[tuple[str, tuple]] = []

    def fake_execute(sql, params=None, fetch=False):
        executed.append((sql, params))
        return [] if fetch else None

    monkeypatch.setattr(store, "_execute", fake_execute)

    store.update_tag_vector(7, [0.1, 0.2])

    assert len(executed) == 1
    sql, params = executed[0]
    assert "UPDATE tb_rag_tags" in sql
    assert "vector_status = 'synced'" in sql
    assert params == ([0.1, 0.2], 7)
