import asyncio
import os
from chunking import SemanticChunker
from llm_providers import EmbeddingFactory, LLMFactory
from retrieval import PgVectorStore, TagRecord
from loguru import logger
from retrieval.base import ChunkRecord
_log = logger.bind(module="load2db_pipeline")

DOC_FOLDER_PATH = "..\\..\\docs\\ai-knowledge-base"
# 语言模型，用于标签生成
llm = LLMFactory.create(provider="deepseek")
# 嵌入模型
embedding = EmbeddingFactory.create(provider="siliconflow")
# 分段器
chunker = SemanticChunker(llm)
# 标签生成提示
TAG_GENERATION_PROMPT = """\
请为以下文本生成2-3个简短标签（中文标签），用于信息检索分类。
已有可用标签: {existing_tags}

如果以下文本的主题与已有标签匹配，请直接输出对应的已有标签名；
如果已有标签都不合适，再生成新的简短标签。
只输出标签，用英文逗号分隔，不要输出任何其他内容。

文本: {text}"""


# 加载文档路径
docs = []
for root, _, files in os.walk(DOC_FOLDER_PATH):
    for f in files:
        if f.lower().endswith(".md"):
            docs.append(os.path.join(root, f))


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
    """为单个 chunk 生成标签，复用已有标签。"""
    prompt = TAG_GENERATION_PROMPT.format(
        existing_tags=existing_tags, text=chunk
    )
    response = llm.invoke([{"role": "user", "content": prompt}])
    tags = [t.strip() for t in response.strip().split(",") if t.strip()]
    return tags


async def load2db_pipeline():
    # 执行异步文档加载
    chunks = await _load_all_chunks()

    # 阶段一：收集所有 chunk 的标签（全局去重）
    chunk_tag_map: dict[str, list[str]] = {}
    all_tag_names: set[str] = set()

    for chunk in chunks:
        tags = _generate_tags_for_chunk(chunk, existing_tags=list(all_tag_names))
        chunk_tag_map[chunk] = tags
        all_tag_names.update(tags)
        _log.info(f"生成标签: {tags}")

    # 阶段二：查询已有标签，筛选新标签
    with PgVectorStore() as store:
        existing_tag_records = store.get_all_tags()
        existing_names = {tag.tag_name for tag in existing_tag_records}

    new_tag_names = list(all_tag_names - existing_names)
    num_reused = len(all_tag_names) - len(new_tag_names)
    _log.info(f"复用 {num_reused} 个标签\t新标签数: {len(new_tag_names)}")

    # 阶段三：插入新标签
    if new_tag_names:
        new_tag_embeddings = embedding.embed_texts(new_tag_names)
        tag_records = [
            TagRecord(tag_name=name, tag_vector=vec)
            for name, vec in zip(new_tag_names, new_tag_embeddings)
        ]
        with PgVectorStore() as store:
            store.insert_tags(tag_records)
            _log.info(f"存储 {len(tag_records)} 个新标签")

    # 阶段四：插入所有 chunks
    for chunk, tags in chunk_tag_map.items():
        chunk_embedding = embedding.embed_texts([chunk])
        chunk_record = ChunkRecord(
            content=chunk, chunk_vector=chunk_embedding[0], tags=tags
        )
        with PgVectorStore() as store:
            store.insert_chunks([chunk_record])
            _log.info(f"存储 chunk 到 chunks，标签: {tags}")


if __name__ == "__main__":
    asyncio.run(load2db_pipeline())
# end main
