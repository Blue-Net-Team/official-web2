"""RagAgent 流式对话事件序列测试。"""

from unittest.mock import MagicMock

import pytest

from agent.agent import RagAgent
from agent.types import StreamChunk
from llm_providers.base import LLMProvider, StreamEvent
from tools.registry import ToolRegistry


class FakeLLM(LLMProvider):
    """可编程的 fake LLM，用于精确控制 stream_with_tools 和 stream 的输出。"""

    def __init__(self, tool_events, final_chunks):
        self._tool_events = tool_events
        self._final_chunks = final_chunks

    def invoke(self, messages: list[dict]) -> str:
        return "".join(self._final_chunks)

    def invoke_with_tools(self, messages: list[dict], tools: list[dict]):
        raise NotImplementedError("非流式接口不应被调用")

    def stream(self, messages: list[dict]):
        for chunk in self._final_chunks:
            yield chunk

    def stream_with_tools(self, messages: list[dict], tools: list[dict]):
        for event in self._tool_events:
            yield event


@pytest.fixture(autouse=True)
def stub_tool_registry_execute(monkeypatch):
    """避免单元测试实际连接数据库或外部服务。"""
    monkeypatch.setattr(
        ToolRegistry,
        "execute",
        staticmethod(lambda name, **kwargs: f"mock result for {name}"),
    )


def test_chat_stream_reasoning_tool_call_result_content_done():
    """WHEN Phase 1 输出 reasoning + tool_call，Phase 2 输出 content
    THEN 事件序列为 reasoning → tool_call → tool_result → content → done。
    """
    fake_llm = FakeLLM(
        tool_events=[
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
        final_chunks=["蓝网团队", "是", "广东海洋大学", "的", "科技创新团队", "。"],
    )

    agent = RagAgent(llm=fake_llm)
    chunks = list(agent.chat_stream("介绍一下蓝网团队"))

    assert chunks[0] == StreamChunk(type="reasoning", content="让我搜索")
    assert chunks[1] == StreamChunk(type="reasoning", content="相关资料")
    assert chunks[2] == StreamChunk(
        type="tool_call",
        tool_name="tag_search_detailed",
        tool_args={"query": "蓝网团队", "top_k": 10},
    )
    assert chunks[3].type == "tool_result"
    assert chunks[3].content == "mock result for tag_search_detailed"
    assert chunks[3].tool_name == "tag_search_detailed"

    # 最终 content 片段
    content_chunks = [c for c in chunks if c.type == "content"]
    assert "".join(c.content for c in content_chunks) == "蓝网团队是广东海洋大学的科技创新团队。"

    assert chunks[-1] == StreamChunk(type="done")


def test_chat_stream_no_tool_call_goes_directly_to_content():
    """WHEN Phase 1 没有 tool_call 直接结束
    THEN 事件序列直接进入 content → done。
    """
    fake_llm = FakeLLM(
        tool_events=[
            StreamEvent(type="reasoning", delta="直接回答"),
            StreamEvent(type="done"),
        ],
        final_chunks=["你好", "！"],
    )

    agent = RagAgent(llm=fake_llm)
    chunks = list(agent.chat_stream("hello"))

    assert chunks[0] == StreamChunk(type="reasoning", content="直接回答")
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
