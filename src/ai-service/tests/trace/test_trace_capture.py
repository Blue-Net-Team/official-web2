"""轨迹采集与落库测试。

覆盖 ai-trace-capture 规格中"落库"相关的需求：
- 每个提问（含拒答/直接回复/澄清）各落一条记录
- 拒答记录不含检索类工具调用
- 客户端断流落库并标记 degraded
- 落库失败不影响对话
- 完整 prompt 快照与用户原话分开保存

这些测试在实现 trace/recorder.py 与 trace/capture.py 之前应当全部失败。
"""

from __future__ import annotations

from dataclasses import dataclass, field

import pytest

from agent.types import StreamChunk
from trace.capture import run_traced_stream

# ---------------------------------------------------------------------------
# 测试替身
# ---------------------------------------------------------------------------


class FakeAgent:
    """按预设序列外发事件的假 agent。"""

    def __init__(self, chunks: list[StreamChunk], fail_at: int | None = None):
        self._chunks = chunks
        self._fail_at = fail_at
        self.closed = False

    def chat_stream(self, user_input: str):
        try:
            for i, chunk in enumerate(self._chunks):
                if self._fail_at is not None and i == self._fail_at:
                    raise RuntimeError("模拟 agent 异常")
                yield chunk
        finally:
            self.closed = True


@dataclass
class FakeStore:
    """记录 save 调用的假存储。"""

    saved: list = field(default_factory=list)
    raise_on_save: bool = False

    def save(self, record) -> None:
        if self.raise_on_save:
            raise RuntimeError("模拟落库失败")
        self.saved.append(record)


def _retrieve_turn() -> list[StreamChunk]:
    """一次正常的检索型对话事件序列。"""
    return [
        StreamChunk(type="intent", intent="SOFTWARE_DOWNLOAD", confidence=0.93, action="RETRIEVE"),
        StreamChunk(type="pre_disclose", tags=["视觉", "软件"], hits=[{"tag_name": "视觉", "score": 0.8, "chunks_count": 3}]),
        StreamChunk(type="reasoning", content="需要先查标签"),
        StreamChunk(type="tool_call", tool_name="chunk_search_by_tags", tool_args={"query": "CUDA"}, round=1),
        StreamChunk(type="tool_result", tool_name="chunk_search_by_tags", content="命中 3 条", round=1),
        StreamChunk(type="content", content="视觉方向需要"),
        StreamChunk(type="content", content="安装 CUDA 11.8。"),
        StreamChunk(type="prompt", prompt=[{"role": "system", "content": "sys"}]),
        StreamChunk(type="done"),
    ]


# ---------------------------------------------------------------------------
# 3.1 完整检索对话落库
# ---------------------------------------------------------------------------


def test_retrieve_turn_is_recorded_once_with_events_and_answer():
    """WHEN 一次完整检索对话正常结束
    THEN 落库一条记录，含完整事件序列与最终答案，且未标记 degraded。
    """
    store = FakeStore()
    list(run_traced_stream(FakeAgent(_retrieve_turn()), "视觉方向要装什么软件", "c1", store))

    assert len(store.saved) == 1
    rec = store.saved[0]
    assert rec.conversation_id == "c1"
    assert rec.user_input == "视觉方向要装什么软件"
    assert rec.answer == "视觉方向需要安装 CUDA 11.8。"
    assert rec.degraded is False
    assert [e["type"] for e in rec.events] == [
        "intent",
        "pre_disclose",
        "reasoning",
        "tool_call",
        "tool_result",
        "content",
        "content",
    ]


def test_internal_events_are_not_forwarded_to_client():
    """WHEN 事件流包含内部结构化事件
    THEN intent / pre_disclose / prompt 不下发客户端，其余原样转发。
    """
    store = FakeStore()
    forwarded = list(run_traced_stream(FakeAgent(_retrieve_turn()), "问题", "c1", store))

    forwarded_types = [c.type for c in forwarded]
    assert "intent" not in forwarded_types
    assert "pre_disclose" not in forwarded_types
    assert "prompt" not in forwarded_types
    assert "reasoning" in forwarded_types
    assert "tool_call" in forwarded_types
    assert "tool_result" in forwarded_types
    assert "content" in forwarded_types
    assert forwarded_types[-1] == "done"


# ---------------------------------------------------------------------------
# 3.2 三类动作各落一条记录
# ---------------------------------------------------------------------------


@pytest.mark.parametrize(
    "action,intent",
    [("REFUSE", "BLOCKED_CODING"), ("DIRECT", "GREETING"), ("DIRECT", "CLARIFY")],
)
def test_each_action_produces_a_record(action: str, intent: str):
    """WHEN 意图分类结果为 REFUSE 或 DIRECT（含 CLARIFY）
    THEN 同样各落一条记录，含意图判定与回复内容。
    """
    store = FakeStore()
    chunks = [
        StreamChunk(type="intent", intent=intent, confidence=0.9, action=action),
        StreamChunk(type="content", content="抱歉，无法回答。"),
        StreamChunk(type="done"),
    ]
    list(run_traced_stream(FakeAgent(chunks), "帮我写个快排", "c9", store))

    assert len(store.saved) == 1
    rec = store.saved[0]
    assert rec.answer == "抱歉，无法回答。"
    assert rec.events[0]["intent"] == intent
    assert rec.events[0]["action"] == action
    assert rec.prompt is None, "未执行状态图时不应有 prompt 快照"


def test_refused_trace_contains_no_retrieval_tool_calls():
    """WHEN 提问被拒答
    THEN 记录的事件序列中不含任何检索类工具调用。
    """
    store = FakeStore()
    chunks = [
        StreamChunk(type="intent", intent="BLOCKED_IRRELEVANT", confidence=0.93, action="REFUSE"),
        StreamChunk(type="content", content="抱歉。"),
        StreamChunk(type="done"),
    ]
    list(run_traced_stream(FakeAgent(chunks), "奖学金怎么评", "c9", store))

    rec = store.saved[0]
    tool_events = [e for e in rec.events if e["type"] in ("tool_call", "tool_result")]
    assert tool_events == []


# ---------------------------------------------------------------------------
# 3.3 断流落库并标记 degraded
# ---------------------------------------------------------------------------


def test_abandoned_stream_still_persists_partial_trace():
    """WHEN 消费者在流未结束时停止迭代（客户端断开）
    THEN 已采集的事件仍被落库，且记录被标记为 degraded。
    """

    class FakeAgentNoDone:
        def __init__(self):
            self.closed = False

        def chat_stream(self, user_input: str):
            try:
                yield StreamChunk(type="intent", intent="X", confidence=1.0, action="RETRIEVE")
                yield StreamChunk(type="content", content="部分答案")
                yield StreamChunk(type="content", content="……")
                # 没有 done
            finally:
                self.closed = True

    store = FakeStore()
    gen = run_traced_stream(FakeAgentNoDone(), "问题", "c1", store)
    next(gen)  # 只消费第一个事件
    gen.close()  # 模拟客户端断开

    assert len(store.saved) == 1
    rec = store.saved[0]
    assert rec.degraded is True
    assert rec.events[0]["type"] == "intent"


# ---------------------------------------------------------------------------
# 3.4 落库失败不影响对话
# ---------------------------------------------------------------------------


def test_store_failure_does_not_break_the_stream():
    """WHEN 落库抛出异常
    THEN 事件流仍完整转发，不向调用方抛出异常。
    """
    store = FakeStore(raise_on_save=True)
    forwarded = list(run_traced_stream(FakeAgent(_retrieve_turn()), "问题", "c1", store))

    assert forwarded[-1].type == "done"
    assert "".join(c.content for c in forwarded if c.type == "content") == "视觉方向需要安装 CUDA 11.8。"


def test_agent_exception_still_persists_degraded_trace():
    """WHEN 事件流中途抛出异常
    THEN 已采集的事件被落库并标记 degraded，异常继续向上传播。
    """
    store = FakeStore()
    agent = FakeAgent(_retrieve_turn(), fail_at=4)
    with pytest.raises(RuntimeError):
        list(run_traced_stream(agent, "问题", "c1", store))

    assert len(store.saved) == 1
    assert store.saved[0].degraded is True


def test_store_is_optional():
    """WHEN 采集开关关闭（store 为 None）
    THEN 事件流照常转发，不抛异常。
    """
    forwarded = list(run_traced_stream(FakeAgent(_retrieve_turn()), "问题", "c1", None))
    assert forwarded[-1].type == "done"


# ---------------------------------------------------------------------------
# 3.5 prompt 快照与用户原话分开
# ---------------------------------------------------------------------------


def test_prompt_snapshot_is_stored_outside_events():
    """WHEN 图执行完成并外发 prompt 快照
    THEN 快照存入独立字段，且不混入事件流。
    """
    store = FakeStore()
    list(run_traced_stream(FakeAgent(_retrieve_turn()), "原始提问", "c1", store))

    rec = store.saved[0]
    assert rec.prompt == [{"role": "system", "content": "sys"}]
    assert all(e["type"] != "prompt" for e in rec.events)


def test_user_input_excludes_enriched_pre_disclosure_content():
    """WHEN 预披露把检索结果拼进用户消息
    THEN user_input 仍是调用方传入的原始提问文本。
    """
    store = FakeStore()
    list(run_traced_stream(FakeAgent(_retrieve_turn()), "视觉方向要装什么软件", "c1", store))

    rec = store.saved[0]
    assert rec.user_input == "视觉方向要装什么软件"
    assert "初始标签检索结果" not in rec.user_input


# ---------------------------------------------------------------------------
# 3.6 会话内序号
# ---------------------------------------------------------------------------


def test_seq_starts_at_one_and_increments_within_conversation(store_with_db):
    """WHEN 同一会话内依次记录多个提问
    THEN seq 从 1 开始连续递增。
    """
    store, conversation_id = store_with_db
    from trace.recorder import TurnRecorder

    for i in range(3):
        recorder = TurnRecorder(user_input=f"问题{i}", conversation_id=conversation_id)
        recorder.add(StreamChunk(type="intent", intent="X", confidence=1.0, action="RETRIEVE"))
        recorder.add(StreamChunk(type="content", content=f"答案{i}"))
        recorder.add(StreamChunk(type="done"))
        store.save(recorder.build(duration_ms=10))

    seqs = store.list_seqs(conversation_id)
    assert seqs == [1, 2, 3]


def test_conversation_timestamps_are_maintained(store_with_db):
    """WHEN 会话首次产生记录并后续新增提问
    THEN 会话被创建，last_active_at 被更新。
    """
    store, conversation_id = store_with_db
    from trace.recorder import TurnRecorder

    for i in range(2):
        recorder = TurnRecorder(user_input=f"q{i}", conversation_id=conversation_id)
        recorder.add(StreamChunk(type="done"))
        store.save(recorder.build(duration_ms=5))

    row = store.get_conversation(conversation_id)
    assert row is not None
    assert row["created_at"] is not None
    assert row["last_active_at"] >= row["created_at"]
