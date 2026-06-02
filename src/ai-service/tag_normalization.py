"""标签同义词归一化模块。

原硬编码映射表已移除，现仅保留标签列表获取接口。
标签去重逻辑已迁移至 pipeline/load2db_pipeline.py 的 Reranker 动态归并机制。
"""

from __future__ import annotations


def get_canonical_tags() -> list[str]:
    """获取所有标准标签列表。

    原硬编码映射表已移除，本函数保留以保持向后兼容。
    实际标签库由数据库动态维护。
    """
    return []
