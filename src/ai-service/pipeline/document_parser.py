"""单文档解析流水线。

供 RabbitMQ 消费者和离线脚本共用。
"""

from __future__ import annotations

import math
from typing import Any

import httpx
from loguru import logger

from chunking import SemanticChunker
from llm_providers import EmbeddingFactory, LLMFactory, RerankerFactory
from retrieval import PgVectorStore, TagRecord
from retrieval.base import ChunkRecord

_log = logger.bind(module="document_parser")

# 全局模型实例（延迟初始化）
_llm = None
_embedding = None
_reranker = None
_chunker = None

SIMILARITY_THRESHOLD = 0.75
RERANKER_THRESHOLD = 0.90

TAG_GENERATION_PROMPT = """\
请为以下文本生成2-3个标签，用于信息检索分类。

【标签风格规范】
- 每个标签2-6个字，简短明确
- 标签之间不要有包含关系
- 不要生成过于宽泛的标签（如"技术"、"知识"）

【可选标签池】（严格优先从中选择，不匹配时才创建新标签）

主题标签（文本的核心主题）：
- 团队简介 — 仅限团队整体概况（名称、历史、定位、规模），不用于方向技术内容
- 招新 — 所有报名、加入、入队、招新流程相关内容（包括报名入口、招新时间、招新对象）
- 考核 — 所有考核、面试、选拔、三轮考核相关内容
- 比赛 — 所有参赛、赛事、获奖、比赛项目相关内容
- FAQ — 常见问题解答
- 学习资源 — 学习路径、教程、技能培训、做什么、适合谁、对口专业
- 就业 — 对口岗位、职业发展、毕业后方向
- 设备要求 — 电脑配置、硬件需求、实验室相关

方向标签（仅当文本明确讨论某方向的技术内容时才选）：
- 结构方向 — 机械设计、SolidWorks、三维建模相关内容
- 电控方向 — 嵌入式、STM32、电路设计、电机控制相关内容
- 视觉方向 — 计算机视觉、图像处理、OpenCV、AI算法相关内容

技术标签（文本明确提到的具体技术或工具，必须单独打出）：
- SolidWorks | STM32 | Python | OpenCV | Keil | C语言

【强制规则】
1. 如果文本明确提到了 SolidWorks、STM32、Python、OpenCV、Keil、C语言 中的任何一个，必须将其作为独立标签输出
2. 禁止"报名""加入我们""入队""招新对象""招新时间""咨询方式" → 统一用"招新"
3. 禁止"团队定位""团队氛围""团队规模" → 统一用"团队简介"
4. 禁止"获奖情况""参赛方向""组队建议" → 统一用"比赛"
5. 禁止"面试""选拔""方向选择" → 统一用"考核"
6. 禁止"使用指南""注意事项""时间投入""退队规则""成长节奏" → 统一用"FAQ"
7. 禁止"岗位""对口专业" → 统一用"就业"
8. 禁止"技术方向" → 方向类内容直接打具体方向标签

【已有标签】（优先复用）：
{existing_tags}

【重要规则】
- 方向类标签不是必选，通用内容（如团队简介、招新、FAQ）不需要打方向标签
- 方向详细介绍（如"结构方向做什么""学习路径"）的主题标签用"学习资源"，不要打"团队简介"
- 招新相关内容（报名、招新时间、招新对象）统一用"招新"标签
- 考核相关内容（面试、选拔）统一用"考核"标签
- 只输出标签本身，用英文逗号分隔，不要输出层级名称或解释

文本: {text}"""


def get_models():
    """获取或初始化全局模型实例。"""
    global _llm, _embedding, _reranker, _chunker
    if _llm is None:
        _llm = LLMFactory.create(provider="deepseek")
    if _embedding is None:
        _embedding = EmbeddingFactory.create(provider="siliconflow")
    if _reranker is None:
        _reranker = RerankerFactory.create()
    if _chunker is None:
        _chunker = SemanticChunker(_llm)
    return _llm, _embedding, _reranker, _chunker


def cosine_similarity(a, b) -> float:
    """计算两个向量的余弦相似度。"""
    def _to_list(v):
        if isinstance(v, str):
            return [float(x) for x in v.strip("[]").split(",")]
        if hasattr(v, "tolist"):
            return v.tolist()
        return list(v)

    a = _to_list(a)
    b = _to_list(b)

    dot = sum(x * y for x, y in zip(a, b))
    norm_a = math.sqrt(sum(x * x for x in a))
    norm_b = math.sqrt(sum(x * x for x in b))
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return dot / (norm_a * norm_b)


def resolve_tag_with_reranker(
    new_tag: str,
    existing_tags: list[TagRecord],
    embedding,
    reranker,
) -> str:
    """判断新标签应归并到哪个已有标签，或保持独立。"""
    existing_names = {tag.tag_name for tag in existing_tags}
    if new_tag in existing_names:
        return new_tag

    new_vec = embedding.embed_texts([new_tag])[0]
    scored = []
    for tag in existing_tags:
        sim = cosine_similarity(new_vec, tag.tag_vector)
        scored.append((tag.tag_name, sim))

    scored.sort(key=lambda x: x[1], reverse=True)
    top5 = [name for name, sim in scored[:5] if sim >= SIMILARITY_THRESHOLD]

    if not top5:
        return new_tag

    rerank_results = reranker.rerank(new_tag, top5, top_k=1)

    if rerank_results and rerank_results[0].relevance_score >= RERANKER_THRESHOLD:
        matched = top5[rerank_results[0].index]
        _log.info(
            f"标签归并: '{new_tag}' -> '{matched}' "
            f"(reranker={rerank_results[0].relevance_score:.4f})"
        )
        return matched

    return new_tag


def generate_tags_for_chunk(chunk: str, existing_tags: list[str]) -> list[str]:
    """为单个 chunk 生成标签，最多返回3个。"""
    llm, _, _, _ = get_models()
    prompt = TAG_GENERATION_PROMPT.format(
        existing_tags=", ".join(existing_tags) if existing_tags else "（暂无）",
        text=chunk,
    )
    response = llm.invoke([{"role": "user", "content": prompt}])

    import re
    delimiters = re.compile(r'[,，\n]+')
    raw_tags = [t.strip() for t in delimiters.split(response.strip()) if t.strip()]

    seen = set()
    unique_tags = []
    for t in raw_tags:
        if t not in seen:
            seen.add(t)
            unique_tags.append(t)

    if len(unique_tags) > 3:
        unique_tags = unique_tags[:3]
    return unique_tags


def update_doc_status(doc_id: int, status: str, chunk_count: int = 0, error_message: str = "") -> bool:
    """更新文档解析状态。返回是否成功。"""
    try:
        with PgVectorStore() as store:
            store._execute(
                "UPDATE tb_rag_docs SET status = %s, chunk_count = %s, error_message = %s, updated_at = NOW() WHERE id = %s",
                (status, chunk_count, error_message, doc_id),
            )
        _log.info(f"文档状态更新: doc_id={doc_id}, status={status}, chunk_count={chunk_count}")
        return True
    except Exception as exc:
        _log.error(f"文档状态更新失败: doc_id={doc_id}, status={status}, error={exc}")
        return False


def check_doc_status(doc_id: int) -> str:
    """检查文档当前状态。"""
    with PgVectorStore() as store:
        rows = store._execute(
            "SELECT status FROM tb_rag_docs WHERE id = %s",
            (doc_id,),
            fetch=True,
        )
        if rows:
            return rows[0].get("status", "")
    return ""


def check_should_abort(doc_id: int) -> bool:
    """检查是否应该中止解析（canceling / canceled 状态）。

    即使状态更新失败，也返回 True 阻止继续解析，避免卡在中间状态。
    """
    try:
        status = check_doc_status(doc_id)
    except Exception as exc:
        _log.error(f"检查文档状态失败，保守中止: doc_id={doc_id}, error={exc}")
        return True

    if status == "canceled":
        return True
    if status == "canceling":
        update_doc_status(doc_id, "canceled")
        return True
    return False


def download_file(download_url: str) -> str:
    """通过 HTTP GET 下载文件内容。"""
    _log.info(f"开始下载文件: {download_url[:80]}...")
    try:
        response = httpx.get(download_url, timeout=60.0, follow_redirects=True)
        response.raise_for_status()
        content = response.text
        _log.info(f"文件下载完成，大小={len(content)} 字符")
        return content
    except Exception as exc:
        _log.error(f"文件下载失败: {exc}")
        raise


def recalculate_tag_counts() -> None:
    """重新统计并更新所有标签的引用次数。"""
    _log.info("开始重新统计标签引用次数...")
    with PgVectorStore() as store:
        tag_counts: dict[str, int] = {}
        rows = store._execute(
            """
            SELECT tags FROM tb_rag_chunks
            WHERE tags IS NOT NULL AND array_length(tags, 1) > 0
            """,
            fetch=True,
        )
        for row in rows or []:
            for tag in row.get("tags") or []:
                tag = tag.strip()
                if tag:
                    tag_counts[tag] = tag_counts.get(tag, 0) + 1

        if tag_counts:
            store._execute("UPDATE tb_rag_tags SET chunks_count = 0")
            items = list(tag_counts.items())
            values = ", ".join(["(%s, %s)"] * len(items))
            params: list[Any] = []
            for tag, cnt in items:
                params.extend([tag, cnt])

            sql_update = f"""
                WITH counts AS (
                    SELECT v.tag_name, v.cnt::int
                    FROM (VALUES {values}) AS v(tag_name, cnt)
                )
                UPDATE tb_rag_tags t
                SET chunks_count = c.cnt
                FROM counts c
                WHERE t.tag_name = c.tag_name
            """
            store._execute(sql_update, tuple(params))
            _log.info(f"已更新 {len(tag_counts)} 个标签的引用次数")
        else:
            _log.warning("未统计到任何标签引用")


def ingest_chunks(doc_id: int, chunks: list[str], source: str = "knowledge") -> None:
    """将分段内容经过标签生成、归并、Embedding 后入库。

    Args:
        doc_id: 文档ID
        chunks: 分段文本列表
        source: 数据来源标识
    """
    llm, embedding, reranker, _ = get_models()

    # 查询已有标签
    with PgVectorStore() as store:
        existing_tag_records = store.get_all_tags()
        existing_names = {tag.tag_name for tag in existing_tag_records}

    # 收集所有 chunk 的原始标签
    chunk_tag_map: dict[str, list[str]] = {}
    all_tag_names: set[str] = set()

    for chunk in chunks:
        tags = generate_tags_for_chunk(chunk, existing_tags=list(all_tag_names | existing_names))
        chunk_tag_map[chunk] = tags
        all_tag_names.update(tags)
        _log.debug(f"生成标签: {tags}")

    # 动态归并新标签
    new_tag_names = list(all_tag_names - existing_names)
    merge_map: dict[str, str] = {}

    if new_tag_names and existing_tag_records:
        _log.info(f"开始动态归并 {len(new_tag_names)} 个新标签...")
        for tag_name in new_tag_names:
            resolved = resolve_tag_with_reranker(
                tag_name, existing_tag_records, embedding, reranker
            )
            if resolved != tag_name:
                merge_map[tag_name] = resolved

    if merge_map:
        merge_count = 0
        for chunk, tags in chunk_tag_map.items():
            seen = set()
            new_tags = []
            for t in tags:
                resolved = merge_map.get(t, t)
                if resolved != t:
                    merge_count += 1
                if resolved not in seen:
                    seen.add(resolved)
                    new_tags.append(resolved)
            chunk_tag_map[chunk] = new_tags

        _log.info(f"动态归并完成，共归并 {merge_count} 个标签引用")

        final_all_tags: set[str] = set()
        for tags in chunk_tag_map.values():
            final_all_tags.update(tags)
        new_tag_names = list(final_all_tags - existing_names)

    num_reused = len(all_tag_names) - len(new_tag_names)
    _log.info(f"复用 {num_reused} 个标签\t新标签数: {len(new_tag_names)}")

    # 插入新标签
    if new_tag_names:
        new_tag_embeddings = embedding.embed_texts(new_tag_names)
        tag_records = [
            TagRecord(tag_name=name, tag_vector=vec)
            for name, vec in zip(new_tag_names, new_tag_embeddings)
        ]
        with PgVectorStore() as store:
            store.insert_tags(tag_records)
            _log.info(f"存储 {len(tag_records)} 个新标签")

    # 插入所有 chunks
    chunk_records: list[ChunkRecord] = []
    for chunk, tags in chunk_tag_map.items():
        chunk_embedding = embedding.embed_texts([chunk])
        chunk_records.append(ChunkRecord(
            doc_id=doc_id,
            content=chunk,
            chunk_vector=chunk_embedding[0],
            tags=tags,
            source=source,
        ))

    # 批量插入
    batch_size = 50
    for i in range(0, len(chunk_records), batch_size):
        batch = chunk_records[i:i + batch_size]
        with PgVectorStore() as store:
            store.insert_chunks(batch)
        _log.info(f"已插入 chunks {i + 1}-{min(i + batch_size, len(chunk_records))}/{len(chunk_records)}")

    # 重新统计标签引用次数
    recalculate_tag_counts()


def _cleanup_chunks(doc_id: int) -> None:
    """清除指定文档的所有分段。"""
    try:
        with PgVectorStore() as store:
            store._execute("DELETE FROM tb_rag_chunks WHERE doc_id = %s", (doc_id,))
        _log.info(f"已清除分段: doc_id={doc_id}")
    except Exception as exc:
        _log.error(f"清除分段失败: doc_id={doc_id}, error={exc}")


def parse_single_document(doc_id: int, file_id: int, download_url: str, reparse: bool = False) -> None:
    """解析单个文档并入库。

    Args:
        doc_id: 文档ID
        file_id: 文件ID
        download_url: 预签名下载URL
        reparse: 是否为重新解析（会清除旧分段）
    """
    _, _, _, chunker = get_models()

    # 检查取消状态（入口处即处理，防止僵尸任务继续）
    if check_should_abort(doc_id):
        _log.info(f"文档 doc_id={doc_id} 已被取消，跳过解析")
        if reparse:
            _cleanup_chunks(doc_id)
        return

    try:
        # 1. 更新状态为解析中
        update_doc_status(doc_id, "parsing")

        # 2. 如果是重新解析，清除旧分段
        if reparse:
            _log.info(f"清除旧分段: doc_id={doc_id}")
            _cleanup_chunks(doc_id)

        # 3. 检查取消状态
        if check_should_abort(doc_id):
            _cleanup_chunks(doc_id)
            return

        # 4. 下载文件
        raw_text = download_file(download_url)

        # 5. 检查取消状态
        if check_should_abort(doc_id):
            _cleanup_chunks(doc_id)
            return

        # 6. 语义分段
        _log.info(f"开始语义分段: doc_id={doc_id}")
        chunks = chunker.split(raw_text)
        _log.info(f"分段完成: doc_id={doc_id}, 共 {len(chunks)} 段")

        # 7. 检查取消状态
        if check_should_abort(doc_id):
            _cleanup_chunks(doc_id)
            return

        # 8. 入库
        ingest_chunks(doc_id, chunks)

        # 9. 最终检查：完成后如果被要求取消，回滚已入库的 chunk
        if check_should_abort(doc_id):
            _cleanup_chunks(doc_id)
            return

        # 10. 更新文档状态为已完成
        update_doc_status(doc_id, "completed", len(chunks))
        _log.info(f"文档解析完成: doc_id={doc_id}, chunks={len(chunks)}")

    except Exception as exc:
        _log.error(f"文档解析失败: doc_id={doc_id}, error={exc}")
        # 尽最大努力更新失败状态，不阻塞异常传播
        update_doc_status(doc_id, "failed", 0, str(exc))
        raise
