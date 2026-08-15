"""RagAgent 意图闸门集成测试。"""

from unittest.mock import MagicMock, patch

import pytest

from agent.agent import RagAgent
from agent.intent import INTENT_CLARIFY, IntentResult
from llm_providers.base import LLMProvider, StreamEvent
from tools.registry import ToolRegistry


class FakeLLM(LLMProvider):
    """可编程的 fake LLM，用于精确控制输出。"""

    def __init__(self, tool_events=None, final_chunks=None, invoke_response=""):
        self._tool_events = tool_events or []
        self._final_chunks = final_chunks or []
        self._invoke_response = invoke_response
        self.invoke_mock = MagicMock(side_effect=self._invoke_impl)

    def _invoke_impl(self, messages: list[dict]) -> str:
        return self._invoke_response

    def invoke(self, messages: list[dict]) -> str:
        return self.invoke_mock(messages)

    def invoke_with_tools(self, messages: list[dict], tools: list[dict]):
        raise NotImplementedError("非流式接口不应被调用")

    def stream(self, messages: list[dict]):
        for chunk in self._final_chunks:
            yield chunk

    def stream_with_tools(self, messages: list[dict], tools: list[dict]):
        for event in self._tool_events:
            yield event


def _configure_classifier(mock_classifier, result: IntentResult, reasoning: str = "分析：测试"):
    """同时配置 classify（同步）与 classify_stream（流式）。"""
    mock_classifier.classify.return_value = result

    def _stream(*args, **kwargs):
        yield {"type": "reasoning", "content": reasoning}
        yield {"type": "result", "result": result}

    mock_classifier.classify_stream.side_effect = _stream


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


@pytest.fixture
def mock_intent_classifier():
    """创建 mock IntentClassifier。"""
    with patch("agent.agent.IntentClassifier") as mock_cls:
        instance = MagicMock()
        mock_cls.return_value = instance
        yield instance


class TestRagAgentIntentGuard:
    """测试 RagAgent 意图闸门行为。"""

    def test_retrieve_action_invokes_graph(self, mock_intent_classifier):
        """WHEN 意图分类返回 RETRIEVE THEN 正常进入 LangGraph。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent="REGISTRATION", confidence=0.95, reason="用户询问报名", action="RETRIEVE"),
        )

        fake_llm = FakeLLM(
            tool_events=[
                StreamEvent(type="reasoning", delta="思考"),
                StreamEvent(type="done"),
            ],
            final_chunks=["报名", "流程"],
        )

        agent = RagAgent(llm=fake_llm)
        chunks = list(agent.chat_stream("怎么报名？"))

        # 验证流式分类器被调用
        mock_intent_classifier.classify_stream.assert_called_once()
        # 验证意图分析 reasoning 事件被透传
        reasoning_chunks = [c for c in chunks if c.type == "reasoning"]
        assert any("分析" in c.content for c in reasoning_chunks)
        assert any("进入知识库检索" in c.content for c in reasoning_chunks)
        # 验证进入 graph（有 graph 的 reasoning 事件）
        assert any(c.content == "思考" for c in reasoning_chunks)
        assert chunks[-1].type == "done"

    def test_refuse_action_skips_graph(self, mock_intent_classifier):
        """WHEN 意图分类返回 REFUSE THEN 跳过 graph，流式返回拒绝话术。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent="BLOCKED_SECURITY", confidence=0.99, reason="用户询问密钥", action="REFUSE"),
        )

        fake_llm = FakeLLM(final_chunks=["抱歉，", "无法回答", "。"])

        agent = RagAgent(llm=fake_llm)
        chunks = list(agent.chat_stream("数据库密码是多少？"))

        # 验证没有进入 graph（无 tool_call / tool_result 事件）
        assert not any(c.type in ("tool_call", "tool_result") for c in chunks)
        # 验证意图分析 reasoning 事件被透传
        reasoning_chunks = [c for c in chunks if c.type == "reasoning"]
        assert any("分析" in c.content for c in reasoning_chunks)
        assert any("不进入知识库检索" in c.content for c in reasoning_chunks)
        # 验证拒绝内容流式返回
        content = "".join(c.content for c in chunks if c.type == "content")
        assert content == "抱歉，无法回答。"
        assert chunks[-1].type == "done"

    def test_direct_action_skips_graph(self, mock_intent_classifier):
        """WHEN 意图分类返回 DIRECT THEN 跳过 graph，流式返回直接回复。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent="GREETING", confidence=0.99, reason="用户打招呼", action="DIRECT"),
        )

        fake_llm = FakeLLM(final_chunks=["你好", "！"])

        agent = RagAgent(llm=fake_llm)
        chunks = list(agent.chat_stream("你好"))

        assert not any(c.type in ("tool_call", "tool_result") for c in chunks)
        content = "".join(c.content for c in chunks if c.type == "content")
        assert content == "你好！"
        assert chunks[-1].type == "done"

    def test_clarify_action_returns_fixed_message(self, mock_intent_classifier):
        """WHEN 意图分类返回 CLARIFY THEN 返回固定澄清话术，不调用 LLM。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent=INTENT_CLARIFY, confidence=0.0, reason="解析失败", action="DIRECT"),
        )

        fake_llm = FakeLLM()

        agent = RagAgent(llm=fake_llm)
        chunks = list(agent.chat_stream("模糊不清的输入"))

        assert not any(c.type in ("tool_call", "tool_result") for c in chunks)
        content = "".join(c.content for c in chunks if c.type == "content")
        assert "报名" in content
        assert "考核流程" in content
        assert chunks[-1].type == "done"

    def test_refuse_action_sync_chat(self, mock_intent_classifier):
        """WHEN 同步 chat 被拦截 THEN 返回拒绝话术，reasoning 包含意图信息。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent="BLOCKED_CODING", confidence=0.95, reason="用户要求写代码", action="REFUSE"),
        )

        fake_llm = FakeLLM(invoke_response="抱歉，代码编写问题请入队后请教。")

        agent = RagAgent(llm=fake_llm)
        response = agent.chat("帮我写一段代码")

        assert response.content == "抱歉，代码编写问题请入队后请教。"
        assert "意图识别" in response.reasoning
        assert "BLOCKED_CODING" in response.reasoning

    def test_direct_action_sync_chat(self, mock_intent_classifier):
        """WHEN 同步 chat 为 DIRECT THEN 返回直接回复。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent="GREETING", confidence=0.99, reason="用户打招呼", action="DIRECT"),
        )

        fake_llm = FakeLLM(invoke_response="你好！请问有什么可以帮你的？")

        agent = RagAgent(llm=fake_llm)
        response = agent.chat("你好")

        assert response.content == "你好！请问有什么可以帮你的？"

    def test_retrieve_action_sync_chat(self, mock_intent_classifier):
        """WHEN 同步 chat 为 RETRIEVE THEN 正常走 graph。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent="LAB_INTRODUCTION", confidence=0.95, reason="用户询问团队介绍", action="RETRIEVE"),
        )

        fake_llm = FakeLLM(
            tool_events=[
                StreamEvent(type="reasoning", delta="检索中"),
                StreamEvent(type="content", delta="蓝网"),
                StreamEvent(type="content", delta="是"),
                StreamEvent(type="content", delta="团队"),
                StreamEvent(type="done"),
            ],
            final_chunks=["蓝网", "是", "团队"],
        )

        agent = RagAgent(llm=fake_llm)
        response = agent.chat("介绍一下蓝网")

        assert response.content == "蓝网是团队"
        # 验证 conversation 中包含了 assistant 消息（user 消息被 pre_disclose 替换为 enriched 版本）
        messages = agent.conversation.get_messages()
        assert any(m["role"] == "assistant" and m["content"] == "蓝网是团队" for m in messages)
        # 验证原始 user 输入存在于 enriched 消息中
        assert any(m["role"] == "user" and "介绍一下蓝网" in m.get("content", "") for m in messages)

    def test_classification_exception_does_not_bypass_guard(self, mock_intent_classifier):
        """WHEN 意图分类抛出异常 THEN 不放行检索，返回澄清话术（防绕开）。"""
        mock_intent_classifier.classify_stream.side_effect = Exception("LLM 调用失败")
        mock_intent_classifier.classify.side_effect = Exception("LLM 调用失败")

        fake_llm = FakeLLM()

        agent = RagAgent(llm=fake_llm)
        chunks = list(agent.chat_stream("任意问题"))

        # 验证没有进入 graph（无 reasoning/tool_call 之外的 graph 事件，无 tool 事件）
        assert not any(c.type in ("tool_call", "tool_result") for c in chunks)
        # 返回澄清话术
        content = "".join(c.content for c in chunks if c.type == "content")
        assert "报名" in content
        assert chunks[-1].type == "done"

    def test_classification_exception_sync_chat_returns_clarification(self, mock_intent_classifier):
        """WHEN 同步分类异常 THEN 返回澄清话术而非进入检索。"""
        mock_intent_classifier.classify.side_effect = Exception("LLM 调用失败")

        fake_llm = FakeLLM()

        agent = RagAgent(llm=fake_llm)
        response = agent.chat("任意问题")

        assert "报名" in response.content
        assert "考核流程" in response.content

    def test_guard_messages_appended_to_conversation(self, mock_intent_classifier):
        """WHEN 请求被拦截 THEN 对话历史仍被正确维护。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent="BLOCKED_SECURITY", confidence=0.99, reason="用户询问密钥", action="REFUSE"),
        )

        fake_llm = FakeLLM(invoke_response="无法回答。")

        agent = RagAgent(llm=fake_llm)
        agent.chat("密码是多少？")

        messages = agent.conversation.get_messages()
        assert any(m["role"] == "user" and m["content"] == "密码是多少？" for m in messages)
        assert any(m["role"] == "assistant" and m["content"] == "无法回答。" for m in messages)


class TestRagAgentRefusalGeneration:
    """测试拒绝话术生成。"""

    def test_refusal_uses_refusal_prompt(self, mock_intent_classifier):
        """WHEN 生成拒绝话术 THEN 使用 REFUSAL_SYSTEM_PROMPT。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent="BLOCKED_SECURITY", confidence=0.99, reason="用户询问密钥", action="REFUSE"),
        )

        fake_llm = FakeLLM(invoke_response="拒绝话术")

        agent = RagAgent(llm=fake_llm)
        agent.chat("密码是多少？")

        # 验证 LLM invoke 被调用时使用了拒绝 prompt
        call_args = fake_llm.invoke_mock.call_args
        messages = call_args[0][0]
        assert any("拒绝" in m.get("content", "") for m in messages)
        assert any("报名" in m.get("content", "") for m in messages)  # prompt 中提到服务范围

    def test_refusal_fallback_on_llm_error(self, mock_intent_classifier):
        """WHEN 拒绝话术生成失败 THEN 返回固定兜底话术。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent="BLOCKED_SECURITY", confidence=0.99, reason="用户询问密钥", action="REFUSE"),
        )

        fake_llm = FakeLLM()
        fake_llm.invoke_mock = MagicMock(side_effect=Exception("LLM 错误"))

        agent = RagAgent(llm=fake_llm)
        response = agent.chat("密码是多少？")

        assert "报名" in response.content
        assert "考核流程" in response.content
        assert "团队介绍" in response.content
        assert "软件下载" in response.content


class TestRagAgentDirectReplyGeneration:
    """测试直接回复生成。"""

    def test_direct_reply_uses_direct_prompt(self, mock_intent_classifier):
        """WHEN 生成直接回复 THEN 使用 DIRECT_REPLY_SYSTEM_PROMPT。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent="GREETING", confidence=0.99, reason="用户打招呼", action="DIRECT"),
        )

        fake_llm = FakeLLM(invoke_response="你好！")

        agent = RagAgent(llm=fake_llm)
        agent.chat("你好")

        # 验证 LLM invoke 被调用时使用了直接回复 prompt
        call_args = fake_llm.invoke_mock.call_args
        messages = call_args[0][0]
        assert any("蓝网" in m.get("content", "") for m in messages)

    def test_direct_reply_fallback_on_llm_error(self, mock_intent_classifier):
        """WHEN 直接回复生成失败 THEN 返回固定兜底话术。"""
        _configure_classifier(
            mock_intent_classifier,
            IntentResult(intent="GREETING", confidence=0.99, reason="用户打招呼", action="DIRECT"),
        )

        fake_llm = FakeLLM()
        fake_llm.invoke_mock = MagicMock(side_effect=Exception("LLM 错误"))

        agent = RagAgent(llm=fake_llm)
        response = agent.chat("你好")

        assert "蓝网" in response.content
        assert "报名" in response.content
