"""SemanticChunker 工厂，支持单例复用。"""

from typing import ClassVar

from loguru import logger

from chunking.semantic_chunker import SemanticChunker
from config import settings
from llm_providers.base import LLMProvider


class ChunkerFactory:
    """SemanticChunker 工厂。

    根据配置自动创建并缓存 SemanticChunker 实例，供多个 Loader 复用。
    单例模式：首次调用 get() 时创建，后续直接返回缓存实例。
    """

    _instance: ClassVar[SemanticChunker | None] = None

    @classmethod
    def get(cls) -> SemanticChunker | None:
        """获取 SemanticChunker 实例（单例，首次创建后复用）。"""
        if cls._instance is not None:
            return cls._instance

        chunker = cls.create()
        cls._instance = chunker
        return chunker

    @classmethod
    def reset(cls) -> None:
        """重置单例缓存（主要用于测试）。"""
        cls._instance = None

    @staticmethod
    def create(
        llm: LLMProvider | None = None,
        max_tokens_per_chunk: int = 0,
    ) -> SemanticChunker | None:
        """创建 SemanticChunker 实例。

        Args:
            llm: LLM 提供商实例。必须传入，未传入则抛出 ValueError。
            max_tokens_per_chunk: 每个 chunk 的最大 token 数，默认为 0，
                为 0 时从 settings.CHUNK_MAX_TOKENS 读取。

        Returns:
            SemanticChunker 实例，或 None（当 llm 为 None 时回退）。

        Raises:
            ValueError: 当 llm 为 None 时抛出。
        """
        if llm is None:
            raise ValueError("创建 SemanticChunker 必须传入 llm 参数")

        max_tokens = max_tokens_per_chunk or settings.CHUNK_MAX_TOKENS

        return SemanticChunker(
            llm=llm,
            max_tokens_per_chunk=max_tokens,
        )
