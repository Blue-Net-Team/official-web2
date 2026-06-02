from typing import Any

from llm_providers import EmbeddingFactory, RerankerFactory
from retrieval.factory import VectorStoreFactory


def _get_content(record: Any) -> str:
    if isinstance(record, dict):
        return record.get("content", "")
    return record.content


def _get_source(record: Any) -> str:
    if isinstance(record, dict):
        return record.get("source", "")
    return record.source


def _get_title(record: Any) -> str:
    if isinstance(record, dict):
        return record.get("title", "")
    return record.title


def chunk_search_by_tags(query: str, tags: str, top_k: int = 10) -> str:
    """按标签过滤搜索文本分片，返回含 score 分布的格式化结果。

    采用双路搜索策略：
    1. 标签语义向量搜 chunks — 对每个标签名称做 embedding，用标签语义向量搜索 chunks
    2. 标签字段精确匹配 — 直接匹配 chunks.tags 数组字段
    两路结果合并去重后，经 Reranker 精排返回。

    Args:
        query: 搜索查询文本（用于最终 Rerank）。
        tags: 逗号分隔的标签列表，如 "LSTM, 梯度消失"。
        top_k: 返回结果数量，默认 10。

    Returns:
        格式化的搜索结果字符串，包含 score 分布和各结果详情。
    """
    raw_tags = [t.strip() for t in tags.split(",") if t.strip()]
    if not raw_tags:
        return "错误: 未提供有效标签"

    # 直接使用原始标签，不再做同义词扩展
    tag_list = raw_tags

    embedding = EmbeddingFactory.create()
    vector_store = VectorStoreFactory.get()

    seen = set()
    unique_texts = []
    unique_records = []

    def _dedup(content: str, record: Any) -> bool:
        if content not in seen:
            seen.add(content)
            unique_texts.append(content)
            unique_records.append(record)
            return True
        return False

    tag_embeddings = embedding.embed_texts(tag_list)
    for tag_vec in tag_embeddings:
        tag_vec_results = vector_store.search_chunks(tag_vec, top_k=top_k)
        for r in tag_vec_results:
            _dedup(_get_content(r), r)

    tag_records = vector_store.get_chunks_by_tags(tag_list, limit=top_k * 2)
    for r in tag_records:
        _dedup(_get_content(r), r)

    if not unique_texts:
        return "未找到匹配的文本分片"

    reranker = RerankerFactory.create()
    rerank_results = reranker.rerank(query, unique_texts, top_k=top_k)

    if not rerank_results:
        return "重排序后无有效结果"

    scores = [r.relevance_score for r in rerank_results]
    avg_score = sum(scores) / len(scores)
    max_score = max(scores)
    min_score = min(scores)

    lines = [f"共 {len(rerank_results)} 条结果"]
    lines.append(f"score 分布: 最高 {max_score:.4f}, 最低 {min_score:.4f}, 平均 {avg_score:.4f}\n")
    for i, rr in enumerate(rerank_results, 1):
        original = unique_records[rr.index]
        text_preview = _get_content(original)[:300] + "..." if len(_get_content(original)) > 300 else _get_content(original)
        source = f" [{_get_source(original)}]" if _get_source(original) else ""
        lines.append(f"[{i}] score={rr.relevance_score:.4f}{source}{text_preview}")

    return "\n".join(lines)
