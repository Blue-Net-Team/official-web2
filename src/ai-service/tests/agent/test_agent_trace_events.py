"""结构化轨迹事件的流式外发测试。

覆盖 ai-trace-capture 规格中"结构化事件"相关的需求：
- 意图判定以结构化字段外发
- 预披露阶段的标签检索结果外发
- 工具事件携带提问内的轮次序号
- 轮次超限被拦截的结果可被识别

这些测试在实现 `trace/` 模块之前应当全部失败。
"""

from unittest.mock import MagicMock, patch

import pytest

from agent.agent import RagAgent
from agent.intent import IntentResult
from llm_providers.base import LLMProvider, StreamEvent
from tools.base import TagSearchResult
from tools.registry import ToolRegistry


class FakeLLM(LLMProvider):
    """可编程 fake LLM：每个元素对应一轮 stream_with_tools 的事件列表。"""

    def __init__(self, rounds, final_chunks=None):
        self._rounds = rounds
        self._final_chunks = final_chunks or ["答案"]
        self._call_count = 0

    def invoke(self, messages: list[dict]) -> str:
        return "".join(self._final_chunks)

    def invoke_with_tools(self, messages: list[dict], tools: list[dict]):
        raise NotImplementedError

    def stream(self, messages: list[dict]):
        for chunk in self._final_chunks:
            yield chunk

    def stream_with_tools(self, messages: list[dict], tools: list[dict]):
        idx = min(self._call_count, len(self._rounds) - 1)
        self._call_count += 1
        for event in self._rounds[idx]:
            yield event


def _tool_round(tool_name: str, args: dict, reasoning: str = "思考", call_id: str | None = None):
    """构造一轮包含 reasoning + tool_call + done 的事件列表。

    ``call_id`` 需在同一提问内唯一：``should_continue`` 按 tool_call_id 集合
    判断是否已补齐 tool 消息，重复 id 会导致循环提前结束。
    """
    return [
        StreamEvent(type="reasoning", delta=reasoning),
        StreamEvent(
            type="tool_call",
            tool_call_id=call_id or f"call_{tool_name}",
            tool_name=tool_name,
            tool_args=args,
        ),
        StreamEvent(type="done"),
    ]


def _make_agent(llm: LLMProvider, intent_result: IntentResult | None = None) -> RagAgent:
    """构造 RagAgent，并把意图分类器替换为返回指定结果的 mock。"""
    result = intent_result or IntentResult(
        intent="REGISTRATION",
        confidence=0.93,
        reason="用户询问报名",
        action="RETRIEVE",
    )
    with patch("agent.agent.IntentClassifier") as mock_cls:
        instance = MagicMock()
        instance.classify.return_value = result

        def _stream(*args, **kwargs):
            yield {"type": "result", "result": result}

        instance.classify_stream.side_effect = _stream
        mock_cls.return_value = instance
        agent = RagAgent(llm=llm)
    agent._intent_classifier = instance
    return agent


@pytest.fixture(autouse=True)
def stub_tool_registry_execute(monkeypatch):
    """避免单元测试实际连接数据库或外部服务。"""
    monkeypatch.setattr(
        ToolRegistry,
        "execute",
        staticmethod(lambda name, **kwargs: f"mock result for {name}"),
    )


@pytest.fixture(autouse=True)
def stub_tag_tools(monkeypatch):
    """Mock pre_disclose 使用的标签工具，避免连接真实数据库。"""
    monkeypatch.setattr("agent.graph.tag_generate", lambda query: ["视觉", "软件"])
    monkeypatch.setattr(
        "agent.graph.tag_search_detailed",
        lambda query, top_k=10: [
            TagSearchResult(
                tag_name="视觉方向详解",
                relevance_score=0.81,
                chunks_count=3,
                tag_description="视觉方向介绍",
            ),
        ],
    )


# ---------------------------------------------------------------------------
# 2.1 意图判定结构化外发
# ---------------------------------------------------------------------------


def test_intent_event_is_emitted_with_structured_fields():
    """WHEN 意图分类产生结果
    THEN 外发一个结构化意图事件，含 intent / confidence / action
    AND 该事件先于其它事件出现。
    """
    llm = FakeLLM([[StreamEvent(type="content", delta="你好"), StreamEvent(type="done")]])
    agent = _make_agent(llm)

    chunks = list(agent.chat_stream("怎么报名"))

    intent_chunks = [c for c in chunks if c.type == "intent"]
    assert len(intent_chunks) == 1, f"应恰好外发一个意图事件，实际 {len(intent_chunks)}"
    assert intent_chunks[0].intent == "REGISTRATION"
    assert intent_chunks[0].confidence == 0.93
    assert intent_chunks[0].action == "RETRIEVE"

    # 意图事件必须先于状态图产生的任何事件
    graph_event_types = {"pre_disclose", "tool_call", "tool_result", "content"}
    intent_idx = next(i for i, c in enumerate(chunks) if c.type == "intent")
    first_graph_idx = next(i for i, c in enumerate(chunks) if c.type in graph_event_types)
    assert intent_idx < first_graph_idx


def test_intent_event_is_emitted_for_refused_question():
    """WHEN 意图分类判定为拒答
    THEN 仍然外发结构化意图事件，且 action 为 REFUSE。
    """
    llm = FakeLLM([[StreamEvent(type="content", delta="抱歉"), StreamEvent(type="done")]])
    agent = _make_agent(
        llm,
        intent_result=IntentResult(
            intent="BLOCKED_CODING",
            confidence=0.95,
            reason="要求写代码",
            action="REFUSE",
        ),
    )

    chunks = list(agent.chat_stream("帮我写个快排"))

    intent_chunks = [c for c in chunks if c.type == "intent"]
    assert len(intent_chunks) == 1
    assert intent_chunks[0].intent == "BLOCKED_CODING"
    assert intent_chunks[0].action == "REFUSE"


def test_intent_event_is_emitted_when_classification_fails():
    """WHEN 意图分类异常导致回退为澄清
    THEN 仍然外发意图事件，且 action 为 DIRECT。
    """
    llm = FakeLLM([[StreamEvent(type="content", delta="你好"), StreamEvent(type="done")]])
    with patch("agent.agent.IntentClassifier") as mock_cls:
        instance = MagicMock()
        instance.classify.side_effect = RuntimeError("分类服务不可用")
        instance.classify_stream.side_effect = RuntimeError("分类服务不可用")
        mock_cls.return_value = instance
        agent = RagAgent(llm=llm)

    chunks = list(agent.chat_stream("随便问问"))

    intent_chunks = [c for c in chunks if c.type == "intent"]
    assert len(intent_chunks) == 1
    assert intent_chunks[0].action == "DIRECT"


# ---------------------------------------------------------------------------
# 2.2 / 2.3 预披露事件
# ---------------------------------------------------------------------------


def test_pre_disclose_event_carries_tags_and_hits():
    """WHEN pre_disclose 完成标签生成与标签检索
    THEN 外发预披露事件，含自动标签列表与命中标签的名称/分数/分段数。
    """
    llm = FakeLLM([[StreamEvent(type="content", delta="答案"), StreamEvent(type="done")]])
    agent = _make_agent(llm)

    chunks = list(agent.chat_stream("视觉方向要装什么软件"))

    pre = [c for c in chunks if c.type == "pre_disclose"]
    assert len(pre) == 1, f"应恰好外发一个预披露事件，实际 {len(pre)}"
    assert pre[0].tags == ["视觉", "软件"]
    assert len(pre[0].hits) == 1
    hit = pre[0].hits[0]
    assert hit["tag_name"] == "视觉方向详解"
    assert hit["score"] == pytest.approx(0.81)
    assert hit["chunks_count"] == 3


def test_pre_disclose_event_is_empty_when_tag_search_fails(monkeypatch):
    """WHEN 标签生成或标签检索抛出异常
    THEN 仍外发预披露事件，但命中列表为空（可与"未执行检索"区分）。
    """

    def _boom(query, top_k=10):
        raise RuntimeError("向量库不可用")

    monkeypatch.setattr("agent.graph.tag_search_detailed", _boom)
    llm = FakeLLM([[StreamEvent(type="content", delta="答案"), StreamEvent(type="done")]])
    agent = _make_agent(llm)

    chunks = list(agent.chat_stream("视觉方向"))

    pre = [c for c in chunks if c.type == "pre_disclose"]
    assert len(pre) == 1
    assert pre[0].hits == []


# ---------------------------------------------------------------------------
# 2.4 轮次序号
# ---------------------------------------------------------------------------


def test_tool_events_carry_incrementing_round_numbers():
    """WHEN 同一提问内完成多次 agent 决策
    THEN 工具调用与工具结果事件携带从 1 开始递增的轮次序号。
    """
    llm = FakeLLM([
        _tool_round("tag_search_detailed", {"query": "视觉"}),
        _tool_round("chunk_search_by_tags", {"query": "CUDA", "tags": "视觉"}),
        [StreamEvent(type="content", delta="最终答案"), StreamEvent(type="done")],
    ])
    agent = _make_agent(llm)

    chunks = list(agent.chat_stream("视觉方向要装什么软件"))

    calls = [c for c in chunks if c.type == "tool_call"]
    results = [c for c in chunks if c.type == "tool_result"]
    assert [c.round for c in calls] == [1, 2]
    assert [c.round for c in results] == [1, 2]


# ---------------------------------------------------------------------------
# 2.5 轮次超限可识别
# ---------------------------------------------------------------------------


def test_round_limit_result_is_marked_as_blocked():
    """WHEN 分片检索达到 _MAX_CHUNK_ROUNDS 上限而被代码拦截
    THEN 该工具结果事件被标记为 blocked，与真实工具执行结果可区分。
    """
    from agent.graph import _MAX_CHUNK_ROUNDS

    rounds = [
        _tool_round("chunk_search_by_tags", {"query": f"q{i}", "tags": "视觉"}, call_id=f"call_{i}")
        for i in range(_MAX_CHUNK_ROUNDS + 1)
    ]
    rounds.append([StreamEvent(type="content", delta="最终答案"), StreamEvent(type="done")])
    llm = FakeLLM(rounds)
    agent = _make_agent(llm)

    chunks = list(agent.chat_stream("视觉"))

    results = [c for c in chunks if c.type == "tool_result"]
    assert len(results) == _MAX_CHUNK_ROUNDS + 1
    # 前 _MAX_CHUNK_ROUNDS 次是真实执行，之后被拦截
    assert all(not c.blocked for c in results[:_MAX_CHUNK_ROUNDS])
    assert results[_MAX_CHUNK_ROUNDS].blocked is True
