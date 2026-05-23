import re
from abc import ABC, abstractmethod
from itertools import islice
from pathlib import Path
from typing import override

from docx import Document
from loguru import logger

from chunking.factory import ChunkerFactory
from chunking.semantic_chunker import SemanticChunker


class Loader(ABC):
    """
    数据集加载器
    """
    def __init__(self, file_path: str):
        self.file_path = file_path

    @abstractmethod
    def load(self) -> list[tuple[int, str]]:
        """从文件加载数据。

        Returns:
            list[tuple[int, str]]: (id, chunk) 列表
        """
        raise NotImplementedError("load method not implemented")


class MsmarcoLoader(Loader):
    """MS MARCO collection.tsv 加载器。"""

    def __init__(self, file_path: str, max_docs: int = 0, offset: int = 0):
        super().__init__(file_path)
        self.max_docs = max_docs
        self.offset = offset

    @override
    def load(self):
        """MS MARCO collection.tsv数据加载器。

        Returns:
            list[tuple[int, str]]: (pid, passage) 列表
        """
        results: list[tuple[int, str]] = []
        stop = None if self.max_docs <= 0 else self.offset + self.max_docs
        with open(self.file_path, encoding="utf-8") as f:
            for line in islice(f, self.offset, stop):
                line = line.strip()
                if not line:
                    continue
                parts = line.split("\t", 1)
                if len(parts) < 2:
                    continue
                pid = int(parts[0])
                passage = parts[1]
                results.append((pid, passage))
        return results


class WordLoader(Loader):
    """Word 文档加载器，基于 LLM 语义主题自动分片。

    当 chunker 为 None 时，自动根据配置创建 SemanticChunker：
    1. 优先读取 TBD_RAG_CHUNK_LLM_* 系列环境变量
    2. 若未配置，回退到主业务 LLM 的默认配置（如 DEEPSEEK_API_KEY）
    3. 若仍无法创建，回退到按段落简单分片
    """

    def __init__(
        self,
        file_path: str,
        chunker: SemanticChunker | None = None,
    ):
        super().__init__(file_path)
        self.chunker = chunker

    @staticmethod
    def _read_word_text(file_path: str) -> str:
        """读取 Word 文档全文。"""
        doc = Document(file_path)
        paragraphs = [p.text.strip() for p in doc.paragraphs if p.text.strip()]
        return "\n\n".join(paragraphs)

    @override
    def load(self) -> list[tuple[int, str]]:
        """读取 Word 文档并基于 LLM 语义分片。

        Returns:
            list[tuple[int, str]]: (chunk_id, chunk_text) 列表，id 从 1 开始递增
        """
        text = self._read_word_text(self.file_path)
        if not text:
            logger.warning(f"Word 文档为空或读取失败: {self.file_path}")
            return []

        chunker = self.chunker
        if chunker is None:
            chunker = ChunkerFactory.get()

        if chunker is not None:
            logger.info("使用 LLM 语义分片")
            chunks = chunker.split(text)
        else:
            logger.info("使用段落简单分片")
            chunks = [p for p in text.split("\n\n") if p.strip()]

        return [(i + 1, chunk) for i, chunk in enumerate(chunks)]


class MarkdownLoader(Loader):
    """Markdown 知识库文档加载器。

    递归扫描指定目录下所有 .md 文件，按二级标题(##)分片。
    每个 chunk 保留完整的标题层级上下文，适合 RAG 检索。
    """

    def __init__(self, dir_path: str):
        super().__init__(dir_path)
        self.dir_path = Path(dir_path)

    @staticmethod
    def _find_markdown_files(dir_path: Path) -> list[Path]:
        """递归查找目录下所有 .md 文件，按路径排序。"""
        return sorted(dir_path.rglob("*.md"))

    @staticmethod
    def _split_by_headers(text: str) -> list[str]:
        """按二级标题(##)分片，保留标题在 chunk 中。

        如果文本中没有二级标题，则整个文本作为一个 chunk。
        一级标题(#)及其后的导言内容会作为第一个 chunk。
        """
        text = text.replace("\r\n", "\n")
        # 正向前瞻：在 ## 前分割，但不消耗 ##
        parts = re.split(r"(?=^## )", text, flags=re.MULTILINE)
        chunks: list[str] = []
        for part in parts:
            part = part.strip()
            if part:
                chunks.append(part)
        return chunks

    @override
    def load(self) -> list[tuple[int, str]]:
        """加载所有 markdown 文件并分片。

        Returns:
            list[tuple[int, str]]: (chunk_id, chunk_text) 列表，id 从 1 开始递增
        """
        md_files = self._find_markdown_files(self.dir_path)
        if not md_files:
            logger.warning(f"未找到 markdown 文件: {self.dir_path}")
            return []

        results: list[tuple[int, str]] = []
        chunk_id = 1

        for file_path in md_files:
            try:
                with open(file_path, encoding="utf-8") as f:
                    text = f.read()
            except Exception as exc:
                logger.warning(f"读取文件失败 {file_path}: {exc}")
                continue

            if not text.strip():
                continue

            chunks = self._split_by_headers(text)
            for chunk in chunks:
                chunk = chunk.strip()
                if chunk:
                    results.append((chunk_id, chunk))
                    chunk_id += 1

        logger.info(
            f"MarkdownLoader: {len(md_files)} 个文件 → {len(results)} 个 chunks"
        )
        return results
