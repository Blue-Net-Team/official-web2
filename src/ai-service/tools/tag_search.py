from dataclasses import dataclass

from llm_providers import EmbeddingFactory, RerankResult, RerankerFactory, LLMFactory
from retrieval.base import VectorStore

    
def tag_search(
    vector_store: VectorStore,
    query: str,
    top_k: int = 5,
) -> list[RerankResult]:
    """标签搜索。"""
    # 调用 LLM 进行向量编码
    embedding = EmbeddingFactory.create()
    vector = embedding.embed_texts([query])[0]
    
    results = vector_store.search_tags(
        vector,
        top_k=top_k,
    )
    
    # 重排
    reranker = RerankerFactory.create()
    rerank_results = reranker.rerank(
        query,
        [result.tag_name for result in results],
    )
    
    return rerank_results

def tag_generate(query: str) -> list[str]:
    """标签生成"""
    # 调用 LLM 生成标签
    llm = LLMFactory.create()
    system_prompt = """你需要根据用户输入的查询文字生成相关的标签，要求生成的标签用英文逗号分隔
如：
- 用户输入为“计算机视觉应该学习什么语言”，则生成的标签为“计算机视觉, 语言”
- 用户输入为“结构设计是干什么的？有什么相关的岗位”，则生成的标签为“结构设计, 岗位”
"""
    tags = llm.invoke([
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": query},
    ])
    return tags.split(", ")
