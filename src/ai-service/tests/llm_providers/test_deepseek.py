"""DeepSeek provider 流式工具调用测试。"""

from unittest.mock import MagicMock


from llm_providers.deepseek import DeepSeekLLM


class FakeFunctionDelta:
    def __init__(self, name=None, arguments=None):
        self.name = name
        self.arguments = arguments


class FakeToolCallDelta:
    def __init__(self, index, id=None, function=None):
        self.index = index
        self.id = id
        self.function = function


class FakeDelta:
    def __init__(self, reasoning_content=None, content=None, tool_calls=None):
        self.reasoning_content = reasoning_content
        self.content = content
        self.tool_calls = tool_calls


class FakeChoice:
    def __init__(self, delta, finish_reason=None):
        self.delta = delta
        self.finish_reason = finish_reason


class FakeChunk:
    def __init__(self, delta, finish_reason=None):
        self.choices = [FakeChoice(delta, finish_reason)]


def _make_llm(mock_stream):
    llm = DeepSeekLLM(api_key="test-key", model="deepseek-reasoner")
    llm._client = MagicMock()
    llm._client.chat.completions.create.return_value = iter(mock_stream)
    return llm


def test_stream_with_tools_emits_reasoning_chunks():
    """WHEN DeepSeek 返回多个 reasoning_content 片段
    THEN stream_with_tools 按片段 yield reasoning 事件，最后 yield done。
    """
    stream = [
        FakeChunk(FakeDelta(reasoning_content="第一步")),
        FakeChunk(FakeDelta(reasoning_content="思考")),
        FakeChunk(FakeDelta(reasoning_content="结束"), finish_reason="stop"),
    ]
    llm = _make_llm(stream)

    events = list(llm.stream_with_tools([{"role": "user", "content": "hi"}], []))

    reasoning_events = [e for e in events if e.type == "reasoning"]
    assert len(reasoning_events) == 3
    assert "".join(e.delta for e in reasoning_events) == "第一步思考结束"
    assert events[-1].type == "done"


def test_stream_with_tools_aggregates_tool_call():
    """WHEN function calling 参数被分片推送
    THEN provider 聚合后只发出一个完整 tool_call 事件。
    """
    stream = [
        FakeChunk(FakeDelta(reasoning_content="让我搜索一下")),
        FakeChunk(
            FakeDelta(
                tool_calls=[
                    FakeToolCallDelta(0, id="call_1", function=FakeFunctionDelta(name="tag_search_detailed")),
                ]
            )
        ),
        FakeChunk(
            FakeDelta(
                tool_calls=[
                    FakeToolCallDelta(0, function=FakeFunctionDelta(arguments='{"query": "蓝网')),
                ]
            )
        ),
        FakeChunk(
            FakeDelta(
                tool_calls=[
                    FakeToolCallDelta(0, function=FakeFunctionDelta(arguments=' 团队", "top_k": 10}')),
                ]
            ),
            finish_reason="tool_calls",
        ),
    ]
    llm = _make_llm(stream)

    events = list(llm.stream_with_tools([{"role": "user", "content": "hi"}], []))

    reasoning_events = [e for e in events if e.type == "reasoning"]
    assert reasoning_events and reasoning_events[0].delta == "让我搜索一下"

    tool_call_events = [e for e in events if e.type == "tool_call"]
    assert len(tool_call_events) == 1
    assert tool_call_events[0].tool_call_id == "call_1"
    assert tool_call_events[0].tool_name == "tag_search_detailed"
    assert tool_call_events[0].tool_args == {"query": "蓝网 团队", "top_k": 10}

    assert events[-1].type == "done"


def test_stream_with_tools_emits_content_during_tool_phase():
    """WHEN 模型在工具调用前输出解释性 content
    THEN content 也作为增量事件发出。
    """
    stream = [
        FakeChunk(FakeDelta(reasoning_content="思考中")),
        FakeChunk(FakeDelta(content="我需要")),
        FakeChunk(FakeDelta(content="查询资料")),
        FakeChunk(FakeDelta(content="完成")),
        FakeChunk(FakeDelta(), finish_reason="stop"),
    ]
    llm = _make_llm(stream)

    events = list(llm.stream_with_tools([{"role": "user", "content": "hi"}], []))

    content_events = [e for e in events if e.type == "content"]
    assert "".join(e.delta for e in content_events) == "我需要查询资料完成"
    assert events[-1].type == "done"


def test_stream_with_tools_multiple_tool_calls():
    """WHEN 一次响应包含多个 tool_calls
    THEN 每个 tool_call 都单独完整发出。
    """
    stream = [
        FakeChunk(
            FakeDelta(
                tool_calls=[
                    FakeToolCallDelta(0, id="call_1", function=FakeFunctionDelta(name="tag_search_detailed")),
                ]
            )
        ),
        FakeChunk(
            FakeDelta(
                tool_calls=[
                    FakeToolCallDelta(0, function=FakeFunctionDelta(arguments='{"query": "x"}')),
                    FakeToolCallDelta(1, id="call_2", function=FakeFunctionDelta(name="chunk_search_by_tags")),
                ]
            )
        ),
        FakeChunk(
            FakeDelta(
                tool_calls=[
                    FakeToolCallDelta(1, function=FakeFunctionDelta(arguments='{"tags": "团队简介"}')),
                ]
            ),
            finish_reason="tool_calls",
        ),
    ]
    llm = _make_llm(stream)

    events = list(llm.stream_with_tools([{"role": "user", "content": "hi"}], []))

    tool_call_events = [e for e in events if e.type == "tool_call"]
    assert len(tool_call_events) == 2
    assert tool_call_events[0].tool_call_id == "call_1"
    assert tool_call_events[0].tool_name == "tag_search_detailed"
    assert tool_call_events[0].tool_args == {"query": "x"}
    assert tool_call_events[1].tool_call_id == "call_2"
    assert tool_call_events[1].tool_name == "chunk_search_by_tags"
    assert tool_call_events[1].tool_args == {"tags": "团队简介"}
    assert events[-1].type == "done"
