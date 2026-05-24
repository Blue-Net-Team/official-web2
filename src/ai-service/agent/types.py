"""Agent 响应类型定义。"""

from dataclasses import dataclass


@dataclass
class AgentResponse:
    """Agent 非流式响应。"""

    content: str
    reasoning: str = ""


@dataclass
class StreamChunk:
    """Agent 流式响应片段。"""

    content: str = ""
    reasoning: str = ""
