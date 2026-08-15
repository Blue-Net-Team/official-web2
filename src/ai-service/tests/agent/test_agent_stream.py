"""RagAgent 流式对话事件序列测试。"""


import pytest
from unittest.mock import MagicMock, patch

from agent.agent import RagAgent
from agent.types import StreamChunk
from llm_providers.base import LLMProvider, StreamEvent
from tools.registry import ToolRegistry


class FakeLLM(LLMProvider):
    """可编程的 fake LLM，按调用轮次精确控制 stream_with_tools 的输出。

    ``tool_event_rounds`` 中每个元素对应一轮 ``stream_with_tools`` 调用的事件列表，
    轮次用尽后重复最后一轮事件。``final_chunks`` 供 ``stream`` 使用（当前 graph 不调用）。
    """

    def __init__(self, tool_events, final_chunks):
        # 兼容旧的单轮列表写法：tool_events=[event, ...]
        if tool_events and isinstance(tool_events[0], StreamEvent):
            tool_events = [tool_events]
        self._rounds = tool_events
        self._final_chunks = final_chunks
        self._call_count = 0

    def invoke(self, messages: list[dict]) -> str:
        return "".join(self._final_chunks)

    def invoke_with_tools(self, messages: list[dict], tools: list[dict]):
        raise NotImplementedError("非流式接口不应被调用")

    def stream(self, messages: list[dict]):
        for chunk in self._final_chunks:
            yield chunk

    def stream_with_tools(self, messages: list[dict], tools: list[dict]):
        idx = min(self._call_count, len(self._rounds) - 1)
        self._call_count += 1
        for event in self._rounds[idx]:
            yield event


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
    """Mock tag_generate / tag_search_detailed，避免 pre_disclose 连接真实数据库。"""
    monkeypatch.setattr(
        "agent.graph.tag_generate",
        lambda query: ["mock_tag"],
    )
    monkeypatch.setattr(
        "agent.graph.tag_search_detailed",
        lambda query, top_k=10: [],
    )


@pytest.fixture(autouse=True)
def mock_intent_classifier():
    """Mock IntentClassifier，避免初始化真实 LLM。"""
    with patch("agent.agent.IntentClassifier") as mock_cls:
        instance = MagicMock()
        from agent.intent import IntentResult
        result = IntentResult(
            intent="REGISTRATION",
            confidence=1.0,
            reason="测试放行",
            action="RETRIEVE",
        )
        instance.classify.return_value = result

        def _stream(*args, **kwargs):
            yield {"type": "result", "result": result}

        instance.classify_stream.side_effect = _stream
        mock_cls.return_value = instance
        yield instance


def test_chat_stream_reasoning_tool_call_result_content_done():
    """WHEN 第 1 轮输出 reasoning + tool_call，第 2 轮输出 content
    THEN 事件序列为 reasoning → tool_call → tool_result → content → done。
    """
    fake_llm = FakeLLM(
        tool_events=[
            # 第 1 轮：推理 + 工具调用
            [
                StreamEvent(type="reasoning", delta="让我搜索"),
                StreamEvent(type="reasoning", delta="相关资料"),
                StreamEvent(
                    type="tool_call",
                    tool_call_id="call_1",
                    tool_name="tag_search_detailed",
                    tool_args={"query": "蓝网团队", "top_k": 10},
                ),
                StreamEvent(type="done"),
            ],
            # 第 2 轮：最终答案
            [
                StreamEvent(type="content", delta="蓝网团队"),
                StreamEvent(type="content", delta="是"),
                StreamEvent(type="content", delta="广东海洋大学"),
                StreamEvent(type="content", delta="的"),
                StreamEvent(type="content", delta="科技创新团队"),
                StreamEvent(type="content", delta="。"),
                StreamEvent(type="done"),
            ],
        ],
        final_chunks=["蓝网团队", "是", "广东海洋大学", "的", "科技创新团队", "。"],
    )

    agent = RagAgent(llm=fake_llm)
    chunks = list(agent.chat_stream("介绍一下蓝网团队"))

    # 过滤掉意图识别的 reasoning 提示后，验证 graph 事件序列
    reasoning_chunks = [c for c in chunks if c.type == "reasoning" and "让我搜索" in c.content or c.type == "reasoning" and "相关资料" in c.content]
    assert [c.content for c in reasoning_chunks] == ["让我搜索", "相关资料"]
    tool_call_chunks = [c for c in chunks if c.type == "tool_call"]
    assert tool_call_chunks[0] == StreamChunk(
        type="tool_call",
        tool_name="tag_search_detailed",
        tool_args={"query": "蓝网团队", "top_k": 10},
    )
    tool_result_chunks = [c for c in chunks if c.type == "tool_result"]
    assert tool_result_chunks[0].content == "mock result for tag_search_detailed"
    assert tool_result_chunks[0].tool_name == "tag_search_detailed"

    # 最终 content 片段
    content_chunks = [c for c in chunks if c.type == "content"]
    assert "".join(c.content for c in content_chunks) == "蓝网团队是广东海洋大学的科技创新团队。"

    assert chunks[-1] == StreamChunk(type="done")


def test_chat_stream_no_tool_call_goes_directly_to_content():
    """WHEN 第 1 轮没有 tool_call 直接结束
    THEN 事件序列直接进入 content → done。
    """
    fake_llm = FakeLLM(
        tool_events=[
            StreamEvent(type="reasoning", delta="直接回答"),
            StreamEvent(type="content", delta="你好"),
            StreamEvent(type="content", delta="！"),
            StreamEvent(type="done"),
        ],
        final_chunks=["你好", "！"],
    )

    agent = RagAgent(llm=fake_llm)
    chunks = list(agent.chat_stream("hello"))

    # 首个 reasoning 是意图识别提示，随后才是 graph 的 reasoning
    reasoning_chunks = [c for c in chunks if c.type == "reasoning"]
    assert any(c.content == "直接回答" for c in reasoning_chunks)
    content_chunks = [c for c in chunks if c.type == "content"]
    assert "".join(c.content for c in content_chunks) == "你好！"
    assert chunks[-1] == StreamChunk(type="done")
    # 不应有 tool_call / tool_result
    assert not any(c.type in ("tool_call", "tool_result") for c in chunks)


def test_chat_stream_accumulates_reasoning_into_messages():
    """WHEN Phase 1 有 reasoning 和 tool_call
    THEN assistant message 中应包含完整的 reasoning_content 和 tool_calls。
    """
    fake_llm = FakeLLM(
        tool_events=[
            StreamEvent(type="reasoning", delta="思考片段1"),
            StreamEvent(type="reasoning", delta="思考片段2"),
            StreamEvent(
                type="tool_call",
                tool_call_id="call_1",
                tool_name="tag_search_detailed",
                tool_args={"query": "x"},
            ),
            StreamEvent(type="done"),
        ],
        final_chunks=["答案"],
    )

    agent = RagAgent(llm=fake_llm)
    list(agent.chat_stream("test"))

    # 验证 conversation messages 中 assistant message 包含 reasoning_content
    messages = agent.conversation.get_messages()
    assistant_msgs = [m for m in messages if m["role"] == "assistant"]
    # 第一个 assistant msg 在 Phase 1（带 tool_calls），第二个在 Phase 2（最终答案）
    assert any(
        m.get("reasoning_content") == "思考片段1思考片段2" and m.get("tool_calls")
        for m in assistant_msgs
    )
