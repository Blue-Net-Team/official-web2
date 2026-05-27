"""模型提供商 — 支持 SiliconFlow / Ollama / DeepSeek / 本地模型。"""

from .base import EmbeddingProvider, LLMProvider, RerankResult, RerankerProvider

from .deepseek import DeepSeekLLM
from .factory import EmbeddingFactory, LLMFactory, RerankerFactory
from .ollama import OllamaEmbedding, OllamaLLM, OllamaReranker
from .siliconflow import SiliconFlowEmbedding, SiliconFlowLLM, SiliconFlowReranker

__all__ = [
    # 抽象基类
    "EmbeddingProvider",
    "RerankerProvider",
    "LLMProvider",
    "RerankResult",
    # 工厂
    "EmbeddingFactory",
    "LLMFactory",
    "RerankerFactory",
    # DeepSeek
    "DeepSeekLLM",
    # SiliconFlow
    "SiliconFlowEmbedding",
    "SiliconFlowReranker",
    "SiliconFlowLLM",
    # Ollama
    "OllamaEmbedding",
    "OllamaLLM",
    "OllamaReranker",
]
