"""SSE 流帧序列测试。

覆盖 ai-service-streaming-reasoning 的 delta 需求：
- 首帧为 conversation_id，且先于任何其他事件
- 带既有 conversation_id 时原样回显
- 拒答/直接回复路径同样下发首帧
- 内部结构化事件不下发客户端
"""

from __future__ import annotations

import json

from agent.types import StreamChunk
from api.chat import build_sse_stream


class FakeAgent:
    def __init__(self, chunks: list[StreamChunk]):
        self._chunks = chunks

    def chat_stream(self, user_input: str):
        yield from self._chunks


def _parse(frame: str) -> dict:
    """从 SSE 帧中解析出 JSON 负载。"""
    assert frame.startswith("data: "), f"非法 SSE 帧: {frame!r}"
    return json.loads(frame.removeprefix("data: ").strip())


def _retrieve_chunks() -> list[StreamChunk]:
    return [
        StreamChunk(type="intent", intent="REGISTRATION", confidence=0.9, action="RETRIEVE"),
        StreamChunk(type="pre_disclose", tags=["报名"], hits=[]),
        StreamChunk(type="content", content="请填写报名表。"),
        StreamChunk(type="prompt", prompt=[{"role": "system", "content": "sys"}]),
        StreamChunk(type="done"),
    ]


def test_first_frame_is_conversation_id():
    """WHEN 请求不带 conversation_id
    THEN 首帧为 conversation_id 事件，且先于任何其他事件。
    """
    frames = list(build_sse_stream(FakeAgent(_retrieve_chunks()), "怎么报名", "c-new", None))

    first = _parse(frames[0])
    assert first["type"] == "conversation_id"
    assert first["conversation_id"] == "c-new"

    # 首帧之后不再出现 conversation_id
    assert all(_parse(f)["type"] != "conversation_id" for f in frames[1:])


def test_existing_conversation_id_is_echoed_unchanged():
    """WHEN 请求带既有 conversation_id
    THEN 首帧原样回显该标识。
    """
    frames = list(build_sse_stream(FakeAgent(_retrieve_chunks()), "继续问", "c-existing", None))
    assert _parse(frames[0])["conversation_id"] == "c-existing"


def test_first_frame_emitted_for_direct_reply_path():
    """WHEN 提问被直接回复（未进入状态图）
    THEN 仍然先下发 conversation_id 首帧。
    """
    chunks = [
        StreamChunk(type="intent", intent="GREETING", confidence=0.98, action="DIRECT"),
        StreamChunk(type="content", content="你好！"),
        StreamChunk(type="done"),
    ]
    frames = list(build_sse_stream(FakeAgent(chunks), "你好", "c-greet", None))

    assert _parse(frames[0])["type"] == "conversation_id"
    assert _parse(frames[-1])["type"] == "done"


def test_first_frame_emitted_for_refusal_path():
    """WHEN 提问被拒答
    THEN 仍然先下发 conversation_id 首帧。
    """
    chunks = [
        StreamChunk(type="intent", intent="BLOCKED_CODING", confidence=0.95, action="REFUSE"),
        StreamChunk(type="content", content="抱歉。"),
        StreamChunk(type="done"),
    ]
    frames = list(build_sse_stream(FakeAgent(chunks), "写个快排", "c-refuse", None))

    assert _parse(frames[0])["type"] == "conversation_id"


def test_internal_events_are_not_sent_to_client():
    """WHEN 事件流包含内部结构化事件
    THEN intent / pre_disclose / prompt 不出现在 SSE 帧中。
    """
    frames = list(build_sse_stream(FakeAgent(_retrieve_chunks()), "怎么报名", "c1", None))
    types = [_parse(f)["type"] for f in frames]

    assert "intent" not in types
    assert "pre_disclose" not in types
    assert "prompt" not in types
    assert "conversation_id" in types
    assert "content" in types
    assert "done" in types


def test_existing_event_fields_are_preserved():
    """WHEN 转发既有事件类型
    THEN 字段名保持 type / content / tool_name / tool_args 不变。
    """
    chunks = [
        StreamChunk(type="reasoning", content="思考"),
        StreamChunk(type="tool_call", tool_name="chunk_search", tool_args={"query": "x"}, round=1),
        StreamChunk(type="tool_result", tool_name="chunk_search", content="结果", round=1),
        StreamChunk(type="content", content="答案"),
        StreamChunk(type="done"),
    ]
    frames = list(build_sse_stream(FakeAgent(chunks), "问题", "c1", None))
    payloads = [_parse(f) for f in frames[1:]]

    assert payloads[0]["type"] == "reasoning"
    assert payloads[0]["content"] == "思考"
    assert payloads[1]["type"] == "tool_call"
    assert payloads[1]["tool_name"] == "chunk_search"
    assert payloads[1]["tool_args"] == {"query": "x"}
    assert payloads[2]["type"] == "tool_result"
    assert payloads[2]["content"] == "结果"
    assert payloads[3]["type"] == "content"
    assert payloads[4]["type"] == "done"
