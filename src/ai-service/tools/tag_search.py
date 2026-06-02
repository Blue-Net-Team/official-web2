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

    生成时会优先从数据库已有标签中选择，只有当查询明确不涉及已有标签时才生成新标签。

    Args:
        query: 用户查询文本。

    Returns:
        生成的标签列表。
    """
    llm = LLMFactory.create()
    vector_store = VectorStoreFactory.get()

    # 动态获取已有标签列表
    try:
        all_tags = vector_store.get_all_tags()
        existing_tag_names = sorted([t.tag_name for t in all_tags])
    except Exception:
        existing_tag_names = []

    tag_list_hint = ""
    if existing_tag_names:
        tag_list_hint = f"""
数据库中已有的标签列表（必须严格从中选择，禁止生成列表之外的标签）：
{', '.join(existing_tag_names)}

严格要求：
1. 只能从上述已有标签列表中选择，严禁生成不在列表中的新标签
2. 选择最相关的已有标签，1-3个即可
3. 不要为了覆盖查询而强行选择不相关的标签
4. 如果查询涉及多个概念，可以组合多个已有标签
5. 如果查询确实与所有已有标签无关，可以返回"无"

正确示例：
- 查询"怎么报名蓝网" -> "招新"（"招新"在列表中，"报名"不在，只选"招新"）
- 查询"结构设计学什么软件" -> "结构设计, SolidWorks"
- 查询"比赛获奖情况" -> "比赛"
"""

    system_prompt = f"""你需要根据用户输入的查询文字，从已有标签库中选择最相关的标签。

规则：
- 只能从已有标签列表中选择，严禁生成新标签
- 用英文逗号分隔多个标签
- 选择1-3个最相关的标签即可
{tag_list_hint}
"""
    tags = llm.invoke([
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": query},
    ])
    return [tag.strip() for tag in tags.split(",")]
