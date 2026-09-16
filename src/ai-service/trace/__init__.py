"""AI 对话轨迹采集。

把 ``RagAgent`` 的执行事件流采集下来并持久化，供管理后台回看。

设计要点：
- 采集是 best-effort：写入失败只记 warn，绝不影响对话
- 事件在内存中缓冲，流结束后（含客户端断流）一次性落库
"""

from .events import (
    CLIENT_EVENT_TYPES,
    EVENT_CONTENT,
    EVENT_CONVERSATION_ID,
    EVENT_INTENT,
    EVENT_PRE_DISCLOSE,
    EVENT_PROMPT,
    EVENT_REASONING,
    EVENT_TOOL_CALL,
    EVENT_TOOL_RESULT,
    PERSISTED_EVENT_TYPES,
    chunk_to_event,
    is_client_visible,
)
from .recorder import TraceRecord, TurnRecorder

__all__ = [
    "CLIENT_EVENT_TYPES",
    "EVENT_CONTENT",
    "EVENT_CONVERSATION_ID",
    "EVENT_INTENT",
    "EVENT_PRE_DISCLOSE",
    "EVENT_PROMPT",
    "EVENT_REASONING",
    "EVENT_TOOL_CALL",
    "EVENT_TOOL_RESULT",
    "PERSISTED_EVENT_TYPES",
    "TraceRecord",
    "TurnRecorder",
    "chunk_to_event",
    "is_client_visible",
]
