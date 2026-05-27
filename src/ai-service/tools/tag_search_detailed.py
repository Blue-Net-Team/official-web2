from llm_providers import EmbeddingFactory, RerankerFactory
from retrieval.factory import VectorStoreFactory

from .base import TagSearchResult


def tag_search_detailed(query: str, top_k: int = 10) -> list[TagSearchResult]:
    """搜索标签，返回含 score + chunks_count 的详细结果。

    Args:
        query: 搜索查询文本。
        top_k: 返回结果数量，默认 10。

    Returns:
        标签搜索结果列表，包含名称、相关度分数和关联文档数。
    """
    embedding = EmbeddingFactory.create()
    vector = embedding.embed_texts([query])[0]

    vector_store = VectorStoreFactory.get()
    tag_records = vector_store.search_tags(vector, top_k=30)
    if not tag_records:
        return []

    tag_texts = [r.tag_name + " " + r.tag_description for r in tag_records]
    reranker = RerankerFactory.create()
    rerank_results = reranker.rerank(query, tag_texts, top_k=top_k)

    tag_map = {}
    for r in tag_records:
        tag_map[r.tag_name + " " + r.tag_description] = r

    results = []
    for rr in rerank_results:
        original = tag_records[rr.index]
        results.append(TagSearchResult(
            tag_name=original.tag_name,
            relevance_score=rr.relevance_score,
            chunks_count=original.chunks_count,
            tag_description=original.tag_description,
        ))

    return results
