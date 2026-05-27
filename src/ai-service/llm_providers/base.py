"""模型提供商抽象基类。"""

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Iterator


@dataclass
class RerankResult:
    """Rerank 结果。"""

    index: int
    relevance_score: float
    text: str


@dataclass
class LLMResponse:
    """LLM 响应，包含文本内容和可选的工具调用。"""

    content: str
    tool_calls: list[dict] = field(default_factory=list)
    reasoning_content: str = ""


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
    def invoke_with_tools(self, messages: list[dict], tools: list[dict]) -> LLMResponse:
        """支持 function calling 的调用，返回文本 + tool_calls。

        Args:
            messages: OpenAI 格式消息列表。
            tools: OpenAI function calling 格式的工具定义列表。
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

    @abstractmethod
    def stream_with_tools(self, messages: list[dict], tools: list[dict]) -> Iterator[LLMResponse]:
        """支持 function calling 的流式调用。

        每轮对话 yield 一个 LLMResponse：
        - 如果 LLM 输出文本，yield LLMResponse(content="文本", tool_calls=[])
        - 如果 LLM 发起 tool_calls，yield LLMResponse(content="", tool_calls=[...])
        - 调用方执行工具后，将结果追加到 messages，继续 stream

        Args:
            messages: OpenAI 格式消息列表。
            tools: OpenAI function calling 格式的工具定义列表。
        """
        ...
