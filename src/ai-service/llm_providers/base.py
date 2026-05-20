"""模型提供商抽象基类。"""

from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import Iterator


@dataclass
class RerankResult:
    """Rerank 结果。"""

    index: int
    relevance_score: float
    text: str


class EmbeddingProvider(ABC):
    """Embedding 提供者抽象基类。"""

    @abstractmethod
    def embed_texts(self, texts: list[str]) -> list[list[float]]:
        """将文本列表转换为向量。"""
        ...


class RerankerProvider(ABC):
    """Reranker 提供者抽象基类。"""

    @abstractmethod
    def rerank(self, query: str, documents: list[str], top_k: int = 10) -> list[RerankResult]:
        """对文档列表进行重排序。"""
        ...


class LLMProvider(ABC):
    """LLM 提供者抽象基类。"""

    @abstractmethod
    def invoke(self, messages: list[dict]) -> str:
        """调用 LLM 并返回文本响应。

        Args:
            messages: OpenAI 格式消息列表，如 
            [
                {"role": "system", "content": "你是助手"},
                {"role": "user", "content": "你好"},
                {"role": "ai", "content": "你好"},
            ]
        """
        ...

    @abstractmethod
    def stream(self, messages: list[dict]) -> Iterator[str]:
        """流式调用 LLM 并逐块返回文本。

        Args:
            messages: OpenAI 格式消息列表。

        Yields:
            每次生成的文本片段。
        """
        ...
