"""轨迹保留策略测试。

覆盖 ai-trace-capture 的"有界保留期"需求：
- 超过保留期的轨迹被清理
- 未过期数据不被误删
- 已无提问的孤儿会话一并清理
"""

from __future__ import annotations


def _insert_turn(store, conversation_id: str, seq: int, days_ago: int) -> None:
    """直接插入一条指定创建时间的轨迹，用于模拟过期数据。"""
    store._execute(
        """
        INSERT INTO tb_ai_turn
            (conversation_id, seq, user_input, answer, events, degraded, duration_ms, created_at)
        VALUES (%s, %s, %s, %s, '[]'::jsonb, FALSE, 0, NOW() - make_interval(days => %s))
        """,
        (conversation_id, seq, f"旧问题{seq}", "旧答案", days_ago),
    )


def _ensure_conversation(store, conversation_id: str) -> None:
    store._execute(
        "INSERT INTO tb_ai_conversation (id) VALUES (%s) ON CONFLICT (id) DO NOTHING",
        (conversation_id,),
    )


def test_expired_turns_are_purged_and_fresh_ones_kept(store_with_db):
    """WHEN 存在超过保留期的轨迹
    THEN 过期轨迹被删除，未过期轨迹保留。
    """
    store, conversation_id = store_with_db
    _ensure_conversation(store, conversation_id)
    _insert_turn(store, conversation_id, 1, days_ago=200)  # 过期
    _insert_turn(store, conversation_id, 2, days_ago=10)   # 未过期

    turns, _ = store.purge_expired(retention_days=180)

    assert turns == 1
    assert store.list_seqs(conversation_id) == [2]


def test_nothing_is_purged_within_retention_window(store_with_db):
    """WHEN 所有轨迹都在保留期内
    THEN 一条都不删除。
    """
    store, conversation_id = store_with_db
    _ensure_conversation(store, conversation_id)
    _insert_turn(store, conversation_id, 1, days_ago=1)
    _insert_turn(store, conversation_id, 2, days_ago=2)

    turns, conversations = store.purge_expired(retention_days=180)

    assert turns == 0
    assert conversations == 0
    assert store.list_seqs(conversation_id) == [1, 2]


def test_orphaned_conversation_is_removed(store_with_db):
    """WHEN 一个会话下的轨迹全部过期被清理
    THEN 该会话记录一并被删除。
    """
    store, conversation_id = store_with_db
    _ensure_conversation(store, conversation_id)
    _insert_turn(store, conversation_id, 1, days_ago=400)

    store.purge_expired(retention_days=180)

    assert store.get_conversation(conversation_id) is None


def test_conversation_survives_when_some_turns_remain(store_with_db):
    """WHEN 会话下仍有未过期轨迹
    THEN 会话记录保留。
    """
    store, conversation_id = store_with_db
    _ensure_conversation(store, conversation_id)
    _insert_turn(store, conversation_id, 1, days_ago=400)
    _insert_turn(store, conversation_id, 2, days_ago=5)

    store.purge_expired(retention_days=180)

    assert store.get_conversation(conversation_id) is not None
