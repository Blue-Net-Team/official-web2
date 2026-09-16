"""轨迹事件定义与转换。

把 Agent 外发的 ``StreamChunk`` 归一化为可持久化的扁平事件字典。

事件流是轨迹的唯一真相来源：读取层不抽派生列，需要按意图/动作筛选时
直接从事件的 JSONB 中提取（例如 ``events->0->>'intent'``）。
"""

from __future__ import annotations

from agent.types import StreamChunk

EVENT_CONVERSATION_ID = "conversation_id"
EVENT_INTENT = "intent"
EVENT_PRE_DISCLOSE = "pre_disclose"
EVENT_REASONING = "reasoning"
EVENT_TOOL_CALL = "tool_call"
EVENT_TOOL_RESULT = "tool_result"
EVENT_CONTENT = "content"
#: 完整 prompt 快照。只供采集层使用：写入独立列，不进入事件流，也不下发前端。
EVENT_PROMPT = "prompt"

#: 需要持久化的事件类型（``done`` 是流结束标记，不承载信息，不入库）
PERSISTED_EVENT_TYPES: frozenset[str] = frozenset({
    EVENT_INTENT,
    EVENT_PRE_DISCLOSE,
    EVENT_REASONING,
    EVENT_TOOL_CALL,
    EVENT_TOOL_RESULT,
    EVENT_CONTENT,
})

#: 需要原样转发给浏览器的类型。结构化事件（intent / pre_disclose）只供采集层使用，
#: 不下发前端，以保持 SSE 对客户端的语义稳定。
CLIENT_EVENT_TYPES: frozenset[str] = frozenset({
    EVENT_REASONING,
    EVENT_TOOL_CALL,
    EVENT_TOOL_RESULT,
    EVENT_CONTENT,
    "done",
    "error",
})


def chunk_to_event(chunk: StreamChunk) -> dict | None:
    """把 StreamChunk 转成扁平的事件字典。

    ``prompt`` 快照存入独立列，不属于事件流，因此返回 ``None``。

    Returns:
        事件字典；若该类型不需要进入事件流则返回 ``None``。
    """
    if chunk.type == EVENT_PROMPT:
        return None

    if chunk.type not in PERSISTED_EVENT_TYPES:
        return None

    event: dict = {"type": chunk.type}

    if chunk.type == EVENT_INTENT:
        event["intent"] = chunk.intent
        event["confidence"] = chunk.confidence
        event["action"] = chunk.action
    elif chunk.type == EVENT_PRE_DISCLOSE:
        event["tags"] = list(chunk.tags or [])
        event["hits"] = list(chunk.hits or [])
    elif chunk.type == EVENT_TOOL_CALL:
        event["tool_name"] = chunk.tool_name
        event["tool_args"] = dict(chunk.tool_args or {})
        event["round"] = chunk.round
    elif chunk.type == EVENT_TOOL_RESULT:
        event["tool_name"] = chunk.tool_name
        event["content"] = chunk.content
        event["round"] = chunk.round
        event["blocked"] = bool(chunk.blocked)
    else:
        event["content"] = chunk.content

    return event


def is_client_visible(chunk_type: str) -> bool:
    """该类型的事件是否应转发给浏览器。"""
    return chunk_type in CLIENT_EVENT_TYPES
