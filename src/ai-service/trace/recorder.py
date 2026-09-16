"""单轮提问的轨迹缓冲。"""

from __future__ import annotations

from dataclasses import dataclass

from agent.types import StreamChunk

from .events import EVENT_CONTENT, EVENT_PROMPT, chunk_to_event


@dataclass
class TraceRecord:
    """一条待落库的提问轨迹。"""

    conversation_id: str
    user_input: str
    answer: str
    prompt: list[dict] | None
    events: list[dict]
    degraded: bool
    duration_ms: int


class TurnRecorder:
    """累积单个提问的事件流，并构建可落库的记录。

    只做内存缓冲，不接触数据库 —— 便于单独测试采集语义。
    """

    def __init__(self, user_input: str, conversation_id: str) -> None:
        self.user_input = user_input
        self.conversation_id = conversation_id
        self._events: list[dict] = []
        self._answer_parts: list[str] = []
        self._prompt: list[dict] | None = None
        self._saw_done = False

    def add(self, chunk: StreamChunk) -> None:
        """采集一个事件。

        ``prompt`` 快照单独持有（落库到独立列），``done`` 只用于判定完整性。
        """
        if chunk.type == EVENT_PROMPT:
            self._prompt = chunk.prompt
            return
        if chunk.type == "done":
            self._saw_done = True
            return
        if chunk.type == EVENT_CONTENT:
            self._answer_parts.append(chunk.content or "")

        event = chunk_to_event(chunk)
        if event is not None:
            self._events.append(event)

    @property
    def degraded(self) -> bool:
        """未见到 ``done`` 事件即视为不完整（客户端断流或中途异常）。"""
        return not self._saw_done

    @property
    def events(self) -> list[dict]:
        return list(self._events)

    def build(self, duration_ms: int) -> TraceRecord:
        """构建待落库记录。"""
        return TraceRecord(
            conversation_id=self.conversation_id,
            user_input=self.user_input,
            answer="".join(self._answer_parts),
            prompt=self._prompt,
            events=list(self._events),
            degraded=self.degraded,
            duration_ms=duration_ms,
        )
