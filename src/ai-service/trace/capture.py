"""在 agent 事件流上做 tee。

一次分流：客户端可见事件照常下发；全部事件同时进入内存缓冲，
在流结束时（含客户端断流、中途异常）一次性落库。

落库是 best-effort —— 失败只记 warn，绝不影响对话。
"""

from __future__ import annotations

import time
from typing import Any, Iterator

from loguru import logger

from agent.types import StreamChunk

from .events import is_client_visible
from .recorder import TurnRecorder

_log = logger.bind(module="TraceCapture")


def run_traced_stream(
    agent: Any,
    user_input: str,
    conversation_id: str,
    store: Any | None,
) -> Iterator[StreamChunk]:
    """执行一次对话，转发客户端可见事件并采集完整轨迹。

    Args:
        agent: 具备 ``chat_stream(user_input)`` 的 RagAgent。
        user_input: 用户原始提问（未经预披露富化）。
        conversation_id: 会话标识。
        store: 具备 ``save(TraceRecord)`` 的存储；为 ``None`` 时关闭采集。

    Yields:
        客户端可见的 ``StreamChunk``。
    """
    recorder = TurnRecorder(user_input=user_input, conversation_id=conversation_id)
    started = time.monotonic()
    stream = agent.chat_stream(user_input)
    try:
        for chunk in stream:
            recorder.add(chunk)
            if is_client_visible(chunk.type):
                yield chunk
    finally:
        # 显式关闭内层生成器，确保下游异常/断流时也能走到这里
        close = getattr(stream, "close", None)
        if callable(close):
            close()
        _persist(recorder, store, int((time.monotonic() - started) * 1000))


def _persist(recorder: TurnRecorder, store: Any | None, duration_ms: int) -> None:
    """把缓冲写入存储。任何异常都不得影响对话。"""
    if store is None:
        return
    try:
        store.save(recorder.build(duration_ms=duration_ms))
    except Exception as exc:
        _log.warning(f"轨迹落库失败（不影响对话）: {exc}")
