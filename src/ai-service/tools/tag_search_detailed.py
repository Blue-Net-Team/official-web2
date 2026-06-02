from llm_providers import EmbeddingFactory, RerankerFactory
from retrieval.factory import VectorStoreFactory

from .base import TagSearchResult


def tag_search_detailed(query: str, top_k: int = 10) -> list[TagSearchResult]:
    """搜索标签，返回含 score + chunks_count 的详细结果。

    直接对查询文本做 embedding，在向量数据库中搜索最相关标签，
    经 Reranker 重排序后返回。

    Args:
        query: 搜索查询文本。
        top_k: 返回结果数量，默认 10。

    Returns:
        标签搜索结果列表，包含名称、相关度分数和关联文档数。
    """
    embedding = EmbeddingFactory.create()
    vector = embedding.embed_texts([query])[0]

    vector_store = VectorStoreFactory.get()
    results = vector_store.search_tags(vector, top_k=top_k)

    if not results:
        return []

    tag_texts = [r.tag_name + " " + r.tag_description for r in results]
    reranker = RerankerFactory.create()
    rerank_results = reranker.rerank(query, tag_texts, top_k=top_k)

    results_out = []
    for rr in rerank_results:
        original = results[rr.index]
        results_out.append(TagSearchResult(
            tag_name=original.tag_name,
            relevance_score=rr.relevance_score,
            chunks_count=original.chunks_count,
            tag_description=original.tag_description,
        ))

    # 动态阈值过滤：淘汰低相关度噪声标签
    MIN_SCORE = 0.005
    RATIO = 0.15
    if results_out:
        top_score = max(r.relevance_score for r in results_out)
        threshold = max(MIN_SCORE, top_score * RATIO)
        filtered = [r for r in results_out if r.relevance_score >= threshold]
        # 保底：至少保留分数最高的一个标签
        if not filtered:
            filtered = [results_out[0]]
        return filtered

    return results_out
