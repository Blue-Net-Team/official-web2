import asyncio
import os

from chunking import SemanticChunker
from loguru import logger
from pipeline.document_parser import get_models, ingest_chunks
from retrieval import PgVectorStore
from retrieval.base import DocRecord

_log = logger.bind(module="load2db_pipeline")

DOC_FOLDER_PATH = "..\\..\\docs\\ai-knowledge-base"


def _read_file(doc: str) -> str:
    """同步读取文件内容。"""
    with open(doc, "r", encoding="utf-8") as f:
        return f.read()


async def _read_and_chunk(doc: str, chunker: SemanticChunker) -> list[str]:
    """异步读取单个文档并进行语义分段。"""
    loop = asyncio.get_running_loop()
    raw_text = await loop.run_in_executor(None, _read_file, doc)
    chunks = await loop.run_in_executor(None, chunker.split, raw_text)
    _log.info(f"文档 {doc} 分片为 {len(chunks)} 个分段")
    return chunks


async def _load_all_chunks(chunker: SemanticChunker) -> dict[str, list[str]]:
    """并发读取所有文档并分段，返回文件名到 chunks 的映射。"""
    docs = []
    for root, _, files in os.walk(DOC_FOLDER_PATH):
        for f in files:
            if f.lower().endswith(".md"):
                docs.append(os.path.join(root, f))

    tasks = [_read_and_chunk(doc, chunker) for doc in docs]
    results = await asyncio.gather(*tasks)
    return {doc: chunks for doc, chunks in zip(docs, results)}


def _create_doc_record(title: str, source: str = "knowledge") -> int:
    """在 tb_rag_docs 中创建文档记录，返回 doc_id。"""
    with PgVectorStore() as store:
        result = store.insert_docs([DocRecord(title=title, source=source)])
        doc_id = result["ids"][0] if result.get("ids") else None
        if doc_id is None:
            # 尝试查询刚插入的记录
            rows = store._execute(
                "SELECT id FROM tb_rag_docs WHERE title = %s ORDER BY id DESC LIMIT 1",
                (title,),
                fetch=True,
            )
            doc_id = rows[0]["id"] if rows else None
        _log.info(f"创建文档记录: doc_id={doc_id}, title={title}")
        return doc_id


async def load2db_pipeline():
    """离线批量导入本地 Markdown 文件到知识库。"""
    _, _, _, chunker = get_models()

    # 阶段一：并发读取所有文档并分段
    doc_chunks_map = await _load_all_chunks(chunker)

    # 阶段二：逐个文档入库（复用 document_parser 的核心逻辑）
    for doc_path, chunks in doc_chunks_map.items():
        if not chunks:
            _log.warning(f"文档 {doc_path} 无内容，跳过")
            continue

        title = os.path.basename(doc_path)
        doc_id = _create_doc_record(title)
        if doc_id is None:
            _log.error(f"创建文档记录失败: {doc_path}")
            continue

        _log.info(f"开始入库文档: {doc_path}, doc_id={doc_id}, chunks={len(chunks)}")
        ingest_chunks(doc_id, chunks, source="knowledge")
        _log.info(f"文档入库完成: {doc_path}, doc_id={doc_id}")

    _log.info("所有文档入库完成")


def main():
    asyncio.run(load2db_pipeline())


if __name__ == "__main__":
    main()
