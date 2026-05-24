"""更新标签引用次数（chunks_count）。

用法:
    uv run python -m pipeline.update_tag_counts

逻辑：
    1. 读取 tb_rag_chunks 表中所有 chunk 的 tags 数组
    2. 统计每个标签出现的次数
    3. 将统计结果回写到 tb_rag_tags.chunks_count
"""

from __future__ import annotations

from loguru import logger

from retrieval import PgVectorStore

_log = logger.bind(module="UpdateTagCounts")


CHUNKS_TABLE = "tb_rag_chunks"
TAGS_TABLE = "tb_rag_tags"


def _count_tags(store: PgVectorStore) -> dict[str, int]:
    """从 chunks 表中统计每个标签的引用次数。"""
    sql = f"""
        SELECT tags FROM {CHUNKS_TABLE}
        WHERE tags IS NOT NULL AND array_length(tags, 1) > 0
    """
    rows = store._execute(sql, fetch=True)
    if not rows:
        return {}

    counts: dict[str, int] = {}
    for row in rows:
        tag_list = row.get("tags") or []
        for tag in tag_list:
            tag = tag.strip()
            if tag:
                counts[tag] = counts.get(tag, 0) + 1

    return counts


def _update_tag_counts(store: PgVectorStore, counts: dict[str, int]) -> int:
    """将统计结果回写到 tags 表，返回更新的行数。"""
    if not counts:
        _log.warning("没有标签引用数据，跳过更新")
        return 0

    updated = 0
    # 先全部置 0，再按实际统计值更新
    store._execute(f"UPDATE {TAGS_TABLE} SET chunks_count = 0")

    # 逐条更新（避免 SQL 注入，使用参数化）
    for tag_name, cnt in counts.items():
        sql = f"""
            UPDATE {TAGS_TABLE}
            SET chunks_count = %s
            WHERE tag_name = %s
        """
        result = store._execute(sql, (cnt, tag_name), fetch=False)
        # psycopg cursor.rowcount 在 execute 后可通过 cursor 获取，
        # 但 _execute 封装后不方便拿到。这里改为用 RETURNING 来判断。
        # 或者更简单：先查询确认标签存在
        updated += 1

    return updated


def _update_tag_counts_batch(store: PgVectorStore, counts: dict[str, int]) -> int:
    """批量更新 chunks_count，使用 VALUES ... 作为临时表。"""
    if not counts:
        _log.warning("没有标签引用数据，跳过更新")
        return 0

    # 先全部置 0
    store._execute(f"UPDATE {TAGS_TABLE} SET chunks_count = 0")

    # 构建 (tag_name, count) 列表用于批量更新
    items = list(counts.items())
    values = ", ".join([f"(%s, %s)"] * len(items))
    params = []
    for tag, cnt in items:
        params.extend([tag, cnt])

    sql = f"""
        WITH counts AS (
            SELECT v.tag_name, v.cnt::int
            FROM (VALUES {values}) AS v(tag_name, cnt)
        )
        UPDATE {TAGS_TABLE} t
        SET chunks_count = c.cnt
        FROM counts c
        WHERE t.tag_name = c.tag_name
    """
    store._execute(sql, tuple(params))

    # 查询实际更新的行数
    result = store._execute(
        f"SELECT COUNT(*) AS cnt FROM {TAGS_TABLE} WHERE chunks_count > 0",
        fetch=True,
    )
    return result[0]["cnt"] if result else 0


def main() -> None:
    _log.info("开始更新标签引用次数...")

    with PgVectorStore() as store:
        # 1. 统计
        counts = _count_tags(store)
        _log.info(f"共统计到 {len(counts)} 个标签有引用")

        if not counts:
            return

        # 打印前 10 个
        sorted_counts = sorted(counts.items(), key=lambda x: x[1], reverse=True)
        for tag, cnt in sorted_counts[:10]:
            _log.info(f"  {tag}: {cnt}")
        if len(sorted_counts) > 10:
            _log.info(f"  ... 共 {len(sorted_counts)} 个标签")

        # 2. 回写
        updated = _update_tag_counts_batch(store, counts)
        _log.info(f"已更新 {updated} 个标签的 chunks_count")

    _log.info("标签引用次数更新完成")


if __name__ == "__main__":
    main()
