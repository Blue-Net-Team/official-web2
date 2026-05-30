from llm_providers import EmbeddingFactory, RerankerFactory
from retrieval.factory import VectorStoreFactory
from tag_normalization import expand_tag

from .base import TagSearchResult


def tag_search_detailed(query: str, top_k: int = 10) -> list[TagSearchResult]:
    """搜索标签，返回含 score + chunks_count 的详细结果。

    支持同义词扩展：查询"嵌入式"时会同时匹配"电控方向"及其同义词。

    Args:
        query: 搜索查询文本。
        top_k: 返回结果数量，默认 10。

    Returns:
        标签搜索结果列表，包含名称、相关度分数和关联文档数。
    """
    embedding = EmbeddingFactory.create()

    # 同义词扩展：将查询词扩展为其等价词组，分别 embedding 后合并搜索
    expanded_queries = expand_tag(query)
    vectors = embedding.embed_texts(expanded_queries)

    vector_store = VectorStoreFactory.get()
    seen_tag_ids: set[int] = set()
    tag_records: list = []

    # 对每个扩展查询执行向量搜索，合并去重
    for vec in vectors:
        batch = vector_store.search_tags(vec, top_k=30)
        for r in batch:
            if r.id not in seen_tag_ids:
                seen_tag_ids.add(r.id)
                tag_records.append(r)

    if not tag_records:
        return []

    tag_texts = [r.tag_name + " " + r.tag_description for r in tag_records]
    reranker = RerankerFactory.create()
    rerank_results = reranker.rerank(query, tag_texts, top_k=top_k)

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
