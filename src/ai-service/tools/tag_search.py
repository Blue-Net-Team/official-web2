from llm_providers import EmbeddingFactory, LLMFactory, RerankResult, RerankerFactory
from retrieval.factory import VectorStoreFactory


def tag_search(query: str, top_k: int = 5) -> list[RerankResult]:
    """搜索与查询相关的知识标签。

    Args:
        query: 搜索查询文本。
        top_k: 返回结果数量，默认 5。

    Returns:
        重排序后的标签搜索结果列表。
    """
    embedding = EmbeddingFactory.create()
    vector = embedding.embed_texts([query])[0]

    vector_store = VectorStoreFactory.get()
    results = vector_store.search_tags(vector, top_k=top_k)

    reranker = RerankerFactory.create()
    rerank_results = reranker.rerank(query, [result.tag_name for result in results])

    return rerank_results


def tag_generate(query: str) -> list[str]:
    """根据查询文本生成相关标签。

    Args:
        query: 用户查询文本。

    Returns:
        生成的标签列表。
    """
    llm = LLMFactory.create()
    system_prompt = """你需要根据用户输入的查询文字生成相关的标签，要求生成的标签用英文逗号分隔
如：
- 用户输入为"计算机视觉应该学习什么语言"，则生成的标签为"计算机视觉, 语言"
- 用户输入为"结构设计是干什么的？有什么相关的岗位"，则生成的标签为"结构设计, 岗位"
"""
    tags = llm.invoke([
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": query},
    ])
    return [tag.strip() for tag in tags.split(",")]
