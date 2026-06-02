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

    # --- 检索诊断：检查标签在库情况 ---
    try:
        all_existing_tags = vector_store.get_all_tags()
        existing_tag_names = {t.tag_name for t in all_existing_tags}
    except Exception:
        existing_tag_names = set()

    in_library_tags = [t for t in tag_list if t in existing_tag_names]
    not_in_library_tags = [t for t in tag_list if t not in existing_tag_names]

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

    # --- 构建检索诊断段落 ---
    diag_lines = ["\n【检索诊断】"]
    diag_lines.append(f"传入标签: {', '.join(tag_list)}")
    diag_lines.append(f"在库标签 ({len(in_library_tags)}个): {', '.join(in_library_tags) if in_library_tags else '无'}")
    diag_lines.append(f"不在库标签 ({len(not_in_library_tags)}个): {', '.join(not_in_library_tags) if not_in_library_tags else '无'}")
    diag_lines.append(f"精确匹配召回: {len(tag_records)} 条分片")

    if not in_library_tags:
        diag_lines.append("⚠️ 标签均不在库中，精确匹配路径失效。建议：使用 chunk_search(query) 直接进行语义搜索。")
    elif not_in_library_tags:
        diag_lines.append(f"提示: 部分标签不在库中 ({', '.join(not_in_library_tags)})，仅使用在库标签进行检索。")

    diag_text = "\n".join(diag_lines)

    if not unique_texts:
        return f"未找到匹配的文本分片{diag_text}"

    reranker = RerankerFactory.create()
    rerank_results = reranker.rerank(query, unique_texts, top_k=top_k)

    if not rerank_results:
        return f"重排序后无有效结果{diag_text}"

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

    lines.append(diag_text)
    return "\n".join(lines)
