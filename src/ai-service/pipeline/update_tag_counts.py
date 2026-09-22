"""更新标签引用次数（chunks_count）。

用法:
    uv run python -m pipeline.update_tag_counts

逻辑：
    1. 按 tb_rag_chunk_tags 中间表 GROUP BY tag_id 统计引用次数
    2. 将统计结果回写到 tb_rag_tags.chunks_count（无引用的标签置 0）
"""

from __future__ import annotations

from loguru import logger

from retrieval import PgVectorStore

_log = logger.bind(module="UpdateTagCounts")

TAGS_TABLE = "tb_rag_tags"
CHUNK_TAGS_TABLE = "tb_rag_chunk_tags"


def _update_tag_counts(store: PgVectorStore) -> int:
    """基于中间表重算全部标签的引用计数，返回有引用的标签数。"""
    store._execute(
        f"""
        UPDATE {TAGS_TABLE} t
        SET chunks_count = COALESCE(cnt.c, 0)
        FROM (
            SELECT tag_id, COUNT(*) AS c
            FROM {CHUNK_TAGS_TABLE}
            GROUP BY tag_id
        ) cnt
        WHERE t.id = cnt.tag_id
        """
    )
    store._execute(
        f"""
        UPDATE {TAGS_TABLE} t
        SET chunks_count = 0
        WHERE NOT EXISTS (
            SELECT 1 FROM {CHUNK_TAGS_TABLE} ct WHERE ct.tag_id = t.id
        )
        """
    )
    result = store._execute(
        f"SELECT COUNT(*) AS cnt FROM {TAGS_TABLE} WHERE chunks_count > 0",
        fetch=True,
    )
    return result[0]["cnt"] if result else 0


def main() -> None:
    _log.info("开始更新标签引用次数...")

    with PgVectorStore() as store:
        updated = _update_tag_counts(store)
        _log.info(f"已更新标签引用计数，当前有引用的标签共 {updated} 个")

    _log.info("标签引用次数更新完成")


if __name__ == "__main__":
    main()
