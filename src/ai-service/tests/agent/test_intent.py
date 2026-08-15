"""意图分类器单元测试。"""

from unittest.mock import MagicMock, patch

import pytest

from agent.intent import (
    ACTION_DIRECT,
    ACTION_REFUSE,
    ACTION_RETRIEVE,
    ALLOWED_INTENTS,
    BLOCKED_INTENTS,
    DIRECT_INTENTS,
    INTENT_BLOCKED_ASSESSMENT_CONTENT,
    INTENT_BLOCKED_CODING,
    INTENT_BLOCKED_DEPLOYMENT,
    INTENT_BLOCKED_IRRELEVANT,
    INTENT_BLOCKED_SECURITY,
    INTENT_BLOCKED_TECH_SUPPORT,
    INTENT_CLARIFY,
    INTENT_GREETING,
    INTENT_REGISTRATION,
    IntentClassifier,
    IntentResult,
    intent_to_action,
)


# ---------------------------------------------------------------------------
# intent_to_action 映射测试
# ---------------------------------------------------------------------------


@pytest.mark.parametrize(
    "intent,expected_action",
    [
        (INTENT_REGISTRATION, ACTION_RETRIEVE),
        ("ASSESSMENT_PROCESS", ACTION_RETRIEVE),
        ("LAB_INTRODUCTION", ACTION_RETRIEVE),
        ("SOFTWARE_DOWNLOAD", ACTION_RETRIEVE),
        (INTENT_BLOCKED_ASSESSMENT_CONTENT, ACTION_REFUSE),
        (INTENT_BLOCKED_CODING, ACTION_REFUSE),
        (INTENT_BLOCKED_TECH_SUPPORT, ACTION_REFUSE),
        (INTENT_BLOCKED_DEPLOYMENT, ACTION_REFUSE),
        (INTENT_BLOCKED_SECURITY, ACTION_REFUSE),
        (INTENT_BLOCKED_IRRELEVANT, ACTION_REFUSE),
        (INTENT_GREETING, ACTION_DIRECT),
        (INTENT_CLARIFY, ACTION_DIRECT),
        ("UNKNOWN_INTENT", ACTION_REFUSE),  # 未知意图默认拒绝（安全优先）
    ],
)
def test_intent_to_action(intent, expected_action):
    assert intent_to_action(intent) == expected_action


# ---------------------------------------------------------------------------
# IntentClassifier 初始化测试
# ---------------------------------------------------------------------------


class TestIntentClassifierInit:
    """测试 IntentClassifier 初始化。"""

    @patch("agent.intent.LLMFactory")
    def test_init_with_default_settings(self, mock_factory):
        """WHEN 未配置独立 intent LLM THEN 复用主 LLM 配置。"""
        mock_llm = MagicMock()
        mock_factory.create.return_value = mock_llm

        with patch("agent.intent.settings") as mock_settings:
            mock_settings.INTENT_LLM_PROVIDER = ""
            mock_settings.INTENT_LLM_MODEL = ""
            mock_settings.INTENT_LLM_TEMPERATURE = 0.0
            mock_settings.INTENT_LLM_TIMEOUT = 30
            mock_settings.LLM_PROVIDER = "siliconflow"
            mock_settings.LLM_MODEL = "deepseek-ai/DeepSeek-V3"

            classifier = IntentClassifier()

            mock_factory.create.assert_called_once_with(
                provider="siliconflow",
                model="deepseek-ai/DeepSeek-V3",
                temperature=0.0,
                timeout=30,
            )
            assert classifier._llm == mock_llm

    @patch("agent.intent.LLMFactory")
    def test_init_with_dedicated_settings(self, mock_factory):
        """WHEN 配置了独立 intent LLM THEN 使用专用配置。"""
        mock_llm = MagicMock()
        mock_factory.create.return_value = mock_llm

        with patch("agent.intent.settings") as mock_settings:
            mock_settings.INTENT_LLM_PROVIDER = "deepseek"
            mock_settings.INTENT_LLM_MODEL = "deepseek-chat"
            mock_settings.INTENT_LLM_TEMPERATURE = 0.1
            mock_settings.INTENT_LLM_TIMEOUT = 15
            mock_settings.LLM_PROVIDER = "siliconflow"
            mock_settings.LLM_MODEL = ""

            IntentClassifier()

            mock_factory.create.assert_called_once_with(
                provider="deepseek",
                model="deepseek-chat",
                temperature=0.1,
                timeout=15,
            )


# ---------------------------------------------------------------------------
# IntentClassifier 分类测试（mock LLM）
# ---------------------------------------------------------------------------


class TestIntentClassifierClassify:
    """测试 IntentClassifier.classify 方法。"""

    @pytest.fixture
    def mock_llm(self):
        """创建 mock LLM，支持 with_structured_output。"""
        llm = MagicMock()
        structured = MagicMock()
        llm._llm.with_structured_output.return_value = structured
        return llm, structured

    @pytest.fixture
    def classifier(self, mock_llm):
        """创建测试用 classifier，跳过真实 LLM 初始化。"""
        llm, _ = mock_llm
        with patch("agent.intent.LLMFactory") as mock_factory:
            mock_factory.create.return_value = llm
            with patch("agent.intent.settings") as mock_settings:
                mock_settings.INTENT_LLM_PROVIDER = ""
                mock_settings.INTENT_LLM_MODEL = ""
                mock_settings.INTENT_LLM_TEMPERATURE = 0.0
                mock_settings.INTENT_LLM_TIMEOUT = 30
                mock_settings.LLM_PROVIDER = "siliconflow"
                mock_settings.LLM_MODEL = ""
                mock_settings.INTENT_GUARD_ENABLED = True

                cls = IntentClassifier()
                return cls

    def test_classify_allowed_intent(self, classifier, mock_llm):
        """WHEN 分类器返回 RETRIEVE 意图 THEN 正常返回结构化结果。"""
        _, structured = mock_llm
        structured.invoke.return_value = IntentResult(
            intent=INTENT_REGISTRATION,
            confidence=0.95,
            reason="用户询问报名方式",
            action=ACTION_RETRIEVE,
        )

        result = classifier.classify("怎么报名蓝网团队？")

        assert result.intent == INTENT_REGISTRATION
        assert result.action == ACTION_RETRIEVE
        assert result.confidence == 0.95

    def test_classify_blocked_intent(self, classifier, mock_llm):
        """WHEN 分类器返回 REFUSE 意图 THEN 正常返回结构化结果。"""
        _, structured = mock_llm
        structured.invoke.return_value = IntentResult(
            intent=INTENT_BLOCKED_TECH_SUPPORT,
            confidence=0.93,
            reason="用户询问具体技术实现",
            action=ACTION_REFUSE,
        )

        result = classifier.classify("这个零件怎么画？")

        assert result.intent == INTENT_BLOCKED_TECH_SUPPORT
        assert result.action == ACTION_REFUSE

    def test_classify_direct_intent(self, classifier, mock_llm):
        """WHEN 分类器返回 DIRECT 意图 THEN 正常返回结构化结果。"""
        _, structured = mock_llm
        structured.invoke.return_value = IntentResult(
            intent=INTENT_GREETING,
            confidence=0.99,
            reason="用户打招呼",
            action=ACTION_DIRECT,
        )

        result = classifier.classify("你好")

        assert result.intent == INTENT_GREETING
        assert result.action == ACTION_DIRECT

    def test_classify_with_context(self, classifier, mock_llm):
        """WHEN 传入上下文 THEN 上下文消息被加入分类 prompt。"""
        _, structured = mock_llm
        structured.invoke.return_value = IntentResult(
            intent="ASSESSMENT_PROCESS",
            confidence=0.88,
            reason="用户询问考核难度",
            action=ACTION_RETRIEVE,
        )

        context = [
            {"role": "user", "content": "怎么报名？"},
            {"role": "assistant", "content": "请关注招新群..."},
        ]
        result = classifier.classify("考核难吗？", context_messages=context)

        assert result.action == ACTION_RETRIEVE
        # 验证 structured.invoke 被调用，且消息中包含上下文
        call_args = structured.invoke.call_args
        messages = call_args[0][0]
        assert any("怎么报名" in m["content"] for m in messages)
        assert any("考核难吗" in m["content"] for m in messages)

    def test_classify_guard_disabled(self, classifier, mock_llm):
        """WHEN 意图围栏禁用 THEN 直接返回 RETRIEVE。"""
        with patch("agent.intent.settings") as mock_settings:
            mock_settings.INTENT_GUARD_ENABLED = False

            result = classifier.classify("任意问题")

            assert result.action == ACTION_RETRIEVE
            assert result.confidence == 1.0
            assert "禁用" in result.reason

    def test_classify_fallback_on_structured_failure(self, classifier, mock_llm):
        """WHEN 结构化输出失败 THEN 回退到文本解析模式。"""
        llm, structured = mock_llm
        # 让 with_structured_output 抛出异常
        structured.invoke.side_effect = Exception("structured output error")
        # 让文本模式返回有效 JSON
        llm.invoke.return_value = '{"intent": "GREETING", "confidence": 0.9, "reason": "test", "action": "DIRECT"}'

        result = classifier.classify("你好")

        assert result.intent == INTENT_GREETING
        assert result.action == ACTION_DIRECT

    def test_classify_fallback_on_invalid_json(self, classifier, mock_llm):
        """WHEN 文本解析也失败 THEN 返回 CLARIFY（DIRECT），不放行检索。"""
        llm, structured = mock_llm
        structured.invoke.side_effect = Exception("structured output error")
        llm.invoke.return_value = "这不是 JSON"

        result = classifier.classify("任意问题")

        assert result.action == ACTION_DIRECT
        assert result.intent == INTENT_CLARIFY
        assert result.confidence == 0.0
        assert "解析失败" in result.reason

    def test_classify_fallback_on_llm_exception(self, classifier, mock_llm):
        """WHEN LLM 调用异常 THEN 返回 CLARIFY（DIRECT），不放行检索。"""
        llm, structured = mock_llm
        structured.invoke.side_effect = Exception("structured output error")
        llm.invoke.side_effect = Exception("network error")

        result = classifier.classify("任意问题")

        assert result.action == ACTION_DIRECT
        assert result.intent == INTENT_CLARIFY
        assert "异常" in result.reason

    def test_classify_no_structured_output_support(self, classifier, mock_llm):
        """WHEN LLM 不支持 with_structured_output THEN 回退到文本解析模式。"""
        llm, _ = mock_llm
        # 模拟没有 _llm 属性的 provider（如 DeepSeekLLM）
        llm._llm = None
        llm.invoke.return_value = '{"intent": "REGISTRATION", "confidence": 0.9, "reason": "test", "action": "RETRIEVE"}'

        result = classifier.classify("怎么报名？")

        assert result.intent == INTENT_REGISTRATION
        assert result.action == ACTION_RETRIEVE

    def test_action_corrected_when_inconsistent_with_intent(self, classifier, mock_llm):
        """WHEN 模型输出的 action 与 intent 不一致 THEN 以 intent 为准修正 action。"""
        llm, structured = mock_llm
        structured.invoke.side_effect = Exception("structured output error")
        # 模型"伪造"了不一致的 action（例如 BLOCKED 意图却给 RETRIEVE）
        llm.invoke.return_value = (
            '{"intent": "BLOCKED_SECURITY", "confidence": 0.9, "reason": "test", "action": "RETRIEVE"}'
        )

        result = classifier.classify("数据库密码是多少？")

        assert result.intent == INTENT_BLOCKED_SECURITY
        assert result.action == ACTION_REFUSE  # 被修正


# ---------------------------------------------------------------------------
# IntentClassifier 流式分类测试
# ---------------------------------------------------------------------------


class TestIntentClassifierStream:
    """测试 IntentClassifier.classify_stream 方法。"""

    @pytest.fixture
    def classifier(self):
        """创建测试用 classifier，底层 LLM 为 MagicMock。"""
        llm = MagicMock()
        with patch("agent.intent.LLMFactory") as mock_factory:
            mock_factory.create.return_value = llm
            with patch("agent.intent.settings") as mock_settings:
                mock_settings.INTENT_LLM_PROVIDER = ""
                mock_settings.INTENT_LLM_MODEL = ""
                mock_settings.INTENT_LLM_TEMPERATURE = 0.0
                mock_settings.INTENT_LLM_TIMEOUT = 30
                mock_settings.LLM_PROVIDER = "deepseek"
                mock_settings.LLM_MODEL = "deepseek-v4-flash"
                mock_settings.INTENT_GUARD_ENABLED = True

                cls = IntentClassifier()
                return cls, llm

    def test_stream_emits_reasoning_then_result(self, classifier):
        """WHEN 流式分类 THEN 先输出分析 reasoning 事件，最后输出 result 事件。"""
        cls, llm = classifier
        llm.stream.return_value = iter([
            "分析：用户询问报名方式",
            "，属于报名咨询\n",
            '{"intent": "REGISTRATION", "confidence": 0.95, "reason": "报名", "action": "RETRIEVE"}',
        ])

        events = list(cls.classify_stream("怎么报名？"))

        reasoning_events = [e for e in events if e["type"] == "reasoning"]
        result_events = [e for e in events if e["type"] == "result"]
        assert len(reasoning_events) == 2
        assert reasoning_events[0]["content"] == "分析：用户询问报名方式"
        assert len(result_events) == 1
        assert result_events[0]["result"].intent == INTENT_REGISTRATION
        assert result_events[0]["result"].action == ACTION_RETRIEVE

    def test_stream_json_brace_spanning_delta(self, classifier):
        """WHEN '{' 出现在 delta 中间 THEN '{' 前的文本仍作为 reasoning 外发。"""
        cls, llm = classifier
        llm.stream.return_value = iter([
            "分析：问候",
            '\n{"intent": "GREETING", "confidence": 0.99, "reason": "g", "action": "DIRECT"}',
        ])

        events = list(cls.classify_stream("你好"))

        reasoning_text = "".join(e["content"] for e in events if e["type"] == "reasoning")
        assert reasoning_text == "分析：问候\n"
        result = [e for e in events if e["type"] == "result"][0]["result"]
        assert result.intent == INTENT_GREETING

    def test_stream_json_not_leaked_to_reasoning(self, classifier):
        """WHEN 流式输出 THEN JSON 内容不会出现在 reasoning 事件中。"""
        cls, llm = classifier
        llm.stream.return_value = iter([
            "分析：敏感",
            '{"intent": "BLOCKED_SECURITY"',
            ', "confidence": 0.99, "reason": "key", "action": "REFUSE"}',
        ])

        events = list(cls.classify_stream("密码是多少"))

        reasoning_text = "".join(e["content"] for e in events if e["type"] == "reasoning")
        assert "{" not in reasoning_text
        assert "intent" not in reasoning_text
        result = [e for e in events if e["type"] == "result"][0]["result"]
        assert result.action == ACTION_REFUSE

    def test_stream_llm_exception_returns_clarify(self, classifier):
        """WHEN 流式调用异常 THEN 返回 CLARIFY 结果事件，不放行检索。"""
        cls, llm = classifier
        llm.stream.side_effect = Exception("network error")

        events = list(cls.classify_stream("任意问题"))

        result_events = [e for e in events if e["type"] == "result"]
        assert len(result_events) == 1
        assert result_events[0]["result"].intent == INTENT_CLARIFY
        assert result_events[0]["result"].action == ACTION_DIRECT

    def test_stream_invalid_json_returns_clarify(self, classifier):
        """WHEN 流式输出无法解析 THEN 返回 CLARIFY 结果事件。"""
        cls, llm = classifier
        llm.stream.return_value = iter(["分析：嗯", "没有 JSON 输出"])

        events = list(cls.classify_stream("任意问题"))

        result = [e for e in events if e["type"] == "result"][0]["result"]
        assert result.intent == INTENT_CLARIFY
        assert result.action == ACTION_DIRECT

    def test_stream_guard_disabled(self, classifier):
        """WHEN 围栏禁用 THEN 流式分类直接返回 RETRIEVE 结果。"""
        cls, llm = classifier
        with patch("agent.intent.settings") as mock_settings:
            mock_settings.INTENT_GUARD_ENABLED = False

            events = list(cls.classify_stream("任意问题"))

        assert len(events) == 1
        assert events[0]["type"] == "result"
        assert events[0]["result"].action == ACTION_RETRIEVE


# ---------------------------------------------------------------------------
# IntentResult 模型验证测试
# ---------------------------------------------------------------------------


class TestIntentResultValidation:
    """测试 IntentResult Pydantic 模型验证。"""

    def test_valid_intent_result(self):
        result = IntentResult(
            intent="REGISTRATION",
            confidence=0.95,
            reason="测试",
            action="RETRIEVE",
        )
        assert result.intent == "REGISTRATION"
        assert result.confidence == 0.95

    def test_confidence_bounds(self):
        with pytest.raises(Exception):  # Pydantic ValidationError
            IntentResult(intent="X", confidence=1.5, reason="x", action="RETRIEVE")

        with pytest.raises(Exception):
            IntentResult(intent="X", confidence=-0.1, reason="x", action="RETRIEVE")

    def test_required_fields(self):
        with pytest.raises(Exception):
            IntentResult()  # 缺少必填字段


# ---------------------------------------------------------------------------
# 意图常量集合测试
# ---------------------------------------------------------------------------


def test_intent_constants_disjoint():
    """确保三类意图集合互不相交。"""
    assert ALLOWED_INTENTS.isdisjoint(BLOCKED_INTENTS)
    assert ALLOWED_INTENTS.isdisjoint(DIRECT_INTENTS)
    assert BLOCKED_INTENTS.isdisjoint(DIRECT_INTENTS)


def test_intent_constants_complete():
    """确保所有定义的意图常量都在对应集合中。"""
    all_intents = {
        INTENT_REGISTRATION,
        "ASSESSMENT_PROCESS",
        "LAB_INTRODUCTION",
        "SOFTWARE_DOWNLOAD",
        INTENT_BLOCKED_ASSESSMENT_CONTENT,
        INTENT_BLOCKED_CODING,
        INTENT_BLOCKED_TECH_SUPPORT,
        INTENT_BLOCKED_DEPLOYMENT,
        INTENT_BLOCKED_SECURITY,
        INTENT_BLOCKED_IRRELEVANT,
        INTENT_GREETING,
        INTENT_CLARIFY,
    }
    assert all_intents == ALLOWED_INTENTS | BLOCKED_INTENTS | DIRECT_INTENTS
