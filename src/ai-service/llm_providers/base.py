"""模型提供商抽象基类。"""

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Iterator, Literal


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


@dataclass
class StreamEvent:
    """LLM 流式事件。

    由 ``LLMProvider.stream_with_tools()`` 逐片段 yield，统一不同 provider 的流式输出。

    type 取值：
        reasoning - 模型思考过程片段，``delta`` 为本次新增的文本
        content   - 模型生成内容片段，``delta`` 为本次新增的文本
        tool_call - 完整工具调用事件（provider 内部已聚合完成），
                    包含 ``tool_call_id`` / ``tool_name`` / ``tool_args``
        done      - 当前轮次流式结束标记
    """

    type: Literal["reasoning", "content", "tool_call", "done"]
    delta: str = ""
    tool_call_id: str | None = None
    tool_name: str | None = None
    tool_args: dict | None = None


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
    def stream_with_tools(self, messages: list[dict], tools: list[dict]) -> Iterator[StreamEvent]:
        """支持 function calling 的流式调用。

        逐片段 yield ``StreamEvent``：
        - ``reasoning`` / ``content`` 为文本增量片段
        - ``tool_call`` 必须是 provider 内部聚合完整后的工具调用事件，
          包含 ``tool_name`` 和 ``tool_args``
        - ``done`` 表示当前轮次流式输出结束且没有更多 tool_call

        调用方执行工具后，将结果追加到 messages，继续调用本方法进入下一轮。

        Args:
            messages: OpenAI 格式消息列表。
            tools: OpenAI function calling 格式的工具定义列表。
        """
        ...
