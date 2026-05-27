from llm_providers import EmbeddingFactory, RerankResult, RerankerFactory
from retrieval.factory import VectorStoreFactory


def chunk_search(query: str, top_k: int = 5) -> list[RerankResult]:
    """在知识库中搜索与查询最相关的文本分片。

    Args:
        query: 搜索查询文本。
        top_k: 返回结果数量，默认 5。

    Returns:
        重排序后的搜索结果列表，按相关度降序排列。
    """
    embedding = EmbeddingFactory.create()
    vector = embedding.embed_texts([query])[0]

    vector_store = VectorStoreFactory.get()
    results = vector_store.search_chunks(vector, top_k=top_k)

    reranker = RerankerFactory.create()
    rerank_results = reranker.rerank(query, [result.content for result in results])

    return rerank_results
