"""SiliconFlow 与 Ollama provider 的 stream_with_tools 接口兼容测试。"""

from llm_providers.base import LLMResponse, StreamEvent
from llm_providers.ollama import OllamaLLM
from llm_providers.siliconflow import SiliconFlowLLM


def test_siliconflow_stream_with_tools_wraps_invoke_result():
    """WHEN SiliconFlow 的 invoke_with_tools 返回完整结果
    THEN stream_with_tools 将其包装为统一 StreamEvent 序列。
    """
    llm = SiliconFlowLLM(api_key="test-key", model="test-model")
    # 直接复写内部非流式结果
    llm.invoke_with_tools = lambda _messages, _tools: LLMResponse(
        content="思考结果",
        reasoning_content="reasoning text",
        tool_calls=[
            {"id": "call_1", "name": "tag_search_detailed", "args": {"query": "蓝网"}, "type": "function"},
        ],
    )

    events = list(llm.stream_with_tools([{"role": "user", "content": "hi"}], []))

    assert events[0] == StreamEvent(type="reasoning", delta="reasoning text")
    assert events[1] == StreamEvent(type="content", delta="思考结果")
    assert events[2] == StreamEvent(
        type="tool_call",
        tool_call_id="call_1",
        tool_name="tag_search_detailed",
        tool_args={"query": "蓝网"},
    )
    assert events[-1] == StreamEvent(type="done")


def test_siliconflow_stream_with_tools_no_tool_call():
    """WHEN 没有工具调用时
    THEN 事件序列只包含可选 reasoning/content 和 done。
    """
    llm = SiliconFlowLLM(api_key="test-key", model="test-model")
    llm.invoke_with_tools = lambda _messages, _tools: LLMResponse(
        content="直接回答",
        tool_calls=[],
    )

    events = list(llm.stream_with_tools([{"role": "user", "content": "hi"}], []))

    assert events[0] == StreamEvent(type="content", delta="直接回答")
    assert events[-1] == StreamEvent(type="done")


def test_ollama_stream_with_tools_wraps_invoke_result():
    """WHEN Ollama 的 invoke_with_tools 返回完整结果
    THEN stream_with_tools 将其包装为统一 StreamEvent 序列。
    """
    llm = OllamaLLM(base_url="http://localhost:11434", model="test-model")
    llm.invoke_with_tools = lambda _messages, _tools: LLMResponse(
        content="ollama result",
        tool_calls=[
            {"id": "call_o1", "name": "chunk_search_by_tags", "args": {"tags": "团队简介"}, "type": "function"},
        ],
    )

    events = list(llm.stream_with_tools([{"role": "user", "content": "hi"}], []))

    assert events[0] == StreamEvent(type="content", delta="ollama result")
    assert events[1] == StreamEvent(
        type="tool_call",
        tool_call_id="call_o1",
        tool_name="chunk_search_by_tags",
        tool_args={"tags": "团队简介"},
    )
    assert events[-1] == StreamEvent(type="done")
