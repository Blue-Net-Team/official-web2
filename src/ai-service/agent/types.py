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
        reasoning    - 模型思考过程
        tool_call    - 模型发起工具调用（含 tool_name / tool_args）
        tool_result  - 工具执行结果
        content      - 最终答案文本片段
        done         - 流式结束标记
        intent       - 意图判定结果（结构化，供轨迹采集与前端选用）
        pre_disclose - 预披露阶段的标签检索结果（结构化）
        prompt       - 完整 prompt 快照（仅供轨迹采集，不下发前端）

    ``round`` 为同一提问内的工具调用轮次序号（从 1 开始），仅对
    ``tool_call`` / ``tool_result`` 有意义。``blocked`` 标记该工具结果
    是被轮次上限拦截而非真实执行所得。
    """

    type: Literal[
        "reasoning",
        "tool_call",
        "tool_result",
        "content",
        "done",
        "intent",
        "pre_disclose",
        "prompt",
    ]
    content: str = ""
    tool_name: str | None = None
    tool_args: dict | None = field(default_factory=dict)
    round: int | None = None
    blocked: bool = False
    intent: str | None = None
    confidence: float | None = None
    action: str | None = None
    tags: list[str] | None = None
    hits: list[dict] | None = None
    prompt: list[dict] | None = None
