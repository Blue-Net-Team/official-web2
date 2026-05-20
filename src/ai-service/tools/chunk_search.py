from llm_providers import EmbeddingFactory, RerankResult, RerankerFactory
from retrieval.base import VectorStore


def chunk_search(
    vector_store: VectorStore,
    query: str,
    top_k: int = 5,
) -> list[RerankResult]:
    """分片搜索。"""
    # 调用 LLM 进行向量编码
    embedding = EmbeddingFactory.create()
    vector = embedding.embed_texts([query])[0]
    
    results = vector_store.search_chunks(
        vector,
        top_k=top_k,
    )
    
    # 重排
    reranker = RerankerFactory.create()
    rerank_results = reranker.rerank(
        query,
        [result.content for result in results],
    )
    
    return rerank_results