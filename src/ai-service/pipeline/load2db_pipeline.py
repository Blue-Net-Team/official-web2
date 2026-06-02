import asyncio
import math
import os
from typing import Any
from chunking import SemanticChunker
from llm_providers import EmbeddingFactory, LLMFactory, RerankerFactory
from retrieval import PgVectorStore, TagRecord
from loguru import logger
from retrieval.base import ChunkRecord

_log = logger.bind(module="load2db_pipeline")

DOC_FOLDER_PATH = "..\\..\\docs\\ai-knowledge-base"
# 语言模型，用于标签生成
llm = LLMFactory.create(provider="deepseek")
# 嵌入模型
embedding = EmbeddingFactory.create(provider="siliconflow")
# 重排序模型
reranker = RerankerFactory.create()
# 分段器
chunker = SemanticChunker(llm)

SIMILARITY_THRESHOLD = 0.75  # Embedding 粗筛阈值（降低到0.75让更多候选进入Reranker精排）
RERANKER_THRESHOLD = 0.90    # Reranker 精排阈值

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


# 加载文档路径
docs = []
for root, _, files in os.walk(DOC_FOLDER_PATH):
    for f in files:
        if f.lower().endswith(".md"):
            docs.append(os.path.join(root, f))


def _cosine_similarity(a, b) -> float:
    """计算两个向量的余弦相似度。

    兼容 list、numpy.ndarray、字符串等多种输入格式。
    """
    # 统一转换为 list[float]
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


def _resolve_tag_with_reranker(
    new_tag: str,
    existing_tags: list[TagRecord],
    embedding,
    reranker,
) -> str:
    """判断新标签应归并到哪个已有标签，或保持独立。

    流程：
    1. 精确匹配检查
    2. Embedding 粗筛 Top-5
    3. Reranker 精排
    4. 阈值判断

    Returns:
        最终应使用的标签名（归并后的已有标签名，或保持原样的新标签名）
    """
    # 1. 快速路径：精确匹配
    existing_names = {tag.tag_name for tag in existing_tags}
    if new_tag in existing_names:
        return new_tag

    # 2. 粗筛：计算新标签与所有已有标签的 embedding 余弦相似度
    new_vec = embedding.embed_texts([new_tag])[0]
    scored = []
    for tag in existing_tags:
        sim = _cosine_similarity(new_vec, tag.tag_vector)
        scored.append((tag.tag_name, sim))

    # 按相似度排序，取 Top-5
    scored.sort(key=lambda x: x[1], reverse=True)
    top5 = [name for name, sim in scored[:5] if sim >= SIMILARITY_THRESHOLD]

    if not top5:
        _log.debug(f"标签粗筛无候选: '{new_tag}' (best_sim={scored[0][1]:.4f})")
        return new_tag

    # 3. 精排：Reranker 判断新标签与 Top-5 候选的同义程度
    rerank_results = reranker.rerank(new_tag, top5, top_k=1)

    # 4. 阈值判断
    if rerank_results and rerank_results[0].relevance_score >= RERANKER_THRESHOLD:
        matched = top5[rerank_results[0].index]
        _log.info(
            f"标签归并: '{new_tag}' -> '{matched}' "
            f"(reranker={rerank_results[0].relevance_score:.4f}, "
            f"emb_sim={scored[0][1]:.4f})"
        )
        return matched

    best_reranker_str = f"{rerank_results[0].relevance_score:.4f}" if rerank_results else "N/A"
    _log.debug(
        f"标签保持独立: '{new_tag}' "
        f"(best_reranker={best_reranker_str}, "
        f"best_emb={scored[0][1]:.4f})"
    )
    return new_tag


async def _read_and_chunk(doc: str) -> list[str]:
    """异步读取单个文档并进行语义分段。"""
    loop = asyncio.get_running_loop()
    # 文件 I/O 在线程池中执行，避免阻塞事件循环
    raw_text = await loop.run_in_executor(None, _read_file, doc)
    # chunker.split 是 CPU/LLM 密集型操作，同样放到线程池
    chunks = await loop.run_in_executor(None, chunker.split, raw_text)
    _log.info(f"文档 {doc} 分片为 {len(chunks)} 个分段")
    return chunks


def _read_file(doc: str) -> str:
    """同步读取文件内容。"""
    with open(doc, "r", encoding="utf-8") as f:
        return f.read()


async def _load_all_chunks() -> list[str]:
    """并发读取所有文档并分段，返回合并后的 chunk 列表。"""
    tasks = [_read_and_chunk(doc) for doc in docs]
    results = await asyncio.gather(*tasks)
    all_chunks: list[str] = []
    for chunks in results:
        all_chunks.extend(chunks)
    return all_chunks


def _generate_tags_for_chunk(chunk: str, existing_tags: list[str]) -> list[str]:
    """为单个 chunk 生成标签，优先复用已有标签，最多返回3个。"""
    prompt = TAG_GENERATION_PROMPT.format(
        existing_tags=", ".join(existing_tags) if existing_tags else "（暂无）",
        text=chunk,
    )
    response = llm.invoke([{"role": "user", "content": prompt}])

    # 支持英文逗号、中文逗号、换行符分隔
    import re
    delimiters = re.compile(r'[,，\n]+')
    raw_tags = [t.strip() for t in delimiters.split(response.strip()) if t.strip()]

    # 去重（保持顺序）
    seen = set()
    unique_tags = []
    for t in raw_tags:
        if t not in seen:
            seen.add(t)
            unique_tags.append(t)

    # 截断到最多3个标签
    if len(unique_tags) > 3:
        _log.debug(f"标签数量超标 {len(unique_tags)} 个，截断为前3个: {unique_tags[:3]}")
        unique_tags = unique_tags[:3]
    return unique_tags


async def load2db_pipeline():
    # 执行异步文档加载
    chunks = await _load_all_chunks()

    # 阶段一：查询已有标签
    with PgVectorStore() as store:
        existing_tag_records = store.get_all_tags()
        existing_names = {tag.tag_name for tag in existing_tag_records}

    # 阶段二：收集所有 chunk 的原始标签
    chunk_tag_map: dict[str, list[str]] = {}
    all_tag_names: set[str] = set()

    for chunk in chunks:
        tags = _generate_tags_for_chunk(chunk, existing_tags=list(all_tag_names | existing_names))
        chunk_tag_map[chunk] = tags
        all_tag_names.update(tags)
        _log.info(f"生成标签: {tags}")

    # 阶段三：动态归并新标签（仅跨批次，用 Reranker 精排）
    new_tag_names = list(all_tag_names - existing_names)
    merge_map: dict[str, str] = {}

    # 3a. 新标签与已有标签比对（Embedding 粗筛 + Reranker 精排）
    # 注：同批次内不做 embedding 归并，避免短文本 embedding 误判（如 STM32 被误归并到 FAQ）
    if new_tag_names and existing_tag_records:
        _log.info(f"开始动态归并 {len(new_tag_names)} 个新标签...")
        for tag_name in new_tag_names:
            resolved = _resolve_tag_with_reranker(
                tag_name, existing_tag_records, embedding, reranker
            )
            if resolved != tag_name:
                merge_map[tag_name] = resolved

    # 应用归并映射到 chunk_tag_map，并全局去重
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

        # 重新计算最终需要入库的新标签
        final_all_tags: set[str] = set()
        for tags in chunk_tag_map.values():
            final_all_tags.update(tags)
        new_tag_names = list(final_all_tags - existing_names)

    num_reused = len(all_tag_names) - len(new_tag_names)
    _log.info(f"复用 {num_reused} 个标签\t新标签数: {len(new_tag_names)}")

    # 阶段四：插入新标签
    if new_tag_names:
        new_tag_embeddings = embedding.embed_texts(new_tag_names)
        tag_records = [
            TagRecord(tag_name=name, tag_vector=vec)
            for name, vec in zip(new_tag_names, new_tag_embeddings)
        ]
        with PgVectorStore() as store:
            store.insert_tags(tag_records)
            _log.info(f"存储 {len(tag_records)} 个新标签")

    # 阶段五：插入所有 chunks
    for chunk, tags in chunk_tag_map.items():
        chunk_embedding = embedding.embed_texts([chunk])
        chunk_record = ChunkRecord(
            content=chunk, chunk_vector=chunk_embedding[0], tags=tags
        )
        with PgVectorStore() as store:
            store.insert_chunks([chunk_record])
            _log.info(f"存储 chunk 到 chunks，标签: {tags}")

    # 阶段六：统计并更新标签引用次数
    _log.info("开始统计并更新标签引用次数...")
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
            # 先清零
            store._execute("UPDATE tb_rag_tags SET chunks_count = 0")
            # 批量更新
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


def main():
    asyncio.run(load2db_pipeline())


if __name__ == "__main__":
    main()
# end main
