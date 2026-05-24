"""Agent 响应类型定义。"""

from dataclasses import dataclass, field
from typing import Literal


@dataclass
class AgentResponse:
    """Agent 非流式响应。"""

    content: str
    reasoning: str = ""


@dataclass
class StreamChunk:
    """Agent 流式响应片段。

    type 取值：
        reasoning   - 模型思考过程
        tool_call   - 模型发起工具调用（含 tool_name / tool_args）
        tool_result - 工具执行结果
        content     - 最终答案文本片段
        done        - 流式结束标记
    """

    type: Literal["reasoning", "tool_call", "tool_result", "content", "done"]
    content: str = ""
    tool_name: str | None = None
    tool_args: dict | None = field(default_factory=dict)
