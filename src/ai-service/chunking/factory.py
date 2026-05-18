"""SemanticChunker 工厂，支持单例复用。"""

import os
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

        chunker = cls._create()
        cls._instance = chunker
        return chunker

    @classmethod
    def reset(cls) -> None:
        """重置单例缓存（主要用于测试）。"""
        cls._instance = None

    @classmethod
    def _create(cls) -> SemanticChunker | None:
        """根据配置创建 SemanticChunker，失败返回 None。"""
        provider = settings.CHUNK_LLM_PROVIDER.lower()
        model = settings.CHUNK_LLM_MODEL
        base_url = settings.CHUNK_LLM_BASE_URL
        api_key = settings.CHUNK_LLM_API_KEY

        if not api_key:
            if provider == "deepseek":
                api_key = os.environ.get("DEEPSEEK_API_KEY", "")
            elif provider == "siliconflow":
                api_key = os.environ.get("SILICONFLOW_API_KEY", "")

        if not api_key:
            logger.warning("未配置分片 LLM 的 API Key，回退到段落简单分片")
            return None

        llm = cls._build_llm(provider, api_key, model, base_url)
        if llm is None:
            return None

        return SemanticChunker(
            llm=llm,
            max_tokens_per_chunk=settings.CHUNK_MAX_TOKENS,
        )

    @classmethod
    def _build_llm(
        cls,
        provider: str,
        api_key: str,
        model: str,
        base_url: str,
    ) -> LLMProvider | None:
        """根据提供商构建 LLM 实例。"""
        try:
            if provider == "deepseek":
                from llm_providers.deepseek import DeepSeekLLM

                llm = DeepSeekLLM(api_key=api_key, model=model, temperature=0)
                if base_url:
                    llm._llm.base_url = base_url
                return llm

            if provider == "siliconflow":
                from llm_providers.siliconflow import SiliconFlowLLM

                llm = SiliconFlowLLM(api_key=api_key, model=model, temperature=0)
                if base_url:
                    llm._llm.base_url = base_url
                return llm

            logger.warning(f"不支持的分片 LLM 提供商: {provider}，回退到段落简单分片")
            return None
        except Exception as exc:
            logger.warning(f"创建分片 LLM 失败: {exc}，回退到段落简单分片")
            return None
