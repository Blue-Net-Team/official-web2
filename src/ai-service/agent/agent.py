"""基于 LangGraph StateGraph 的 RAG Agent。"""

from __future__ import annotations

import uuid
from typing import Iterator

from langgraph.checkpoint.memory import MemorySaver
from loguru import logger

from llm_providers.base import LLMProvider
from llm_providers.factory import LLMFactory
from tools import ToolRegistry

from .conversation import Conversation
from .graph import build_rag_graph
from .intent import (
    ACTION_DIRECT,
    ACTION_REFUSE,
    INTENT_CLARIFY,
    IntentClassifier,
    IntentResult,
    clarify_result,
)
from .prompts import (
    DIRECT_REPLY_SYSTEM_PROMPT,
    REFUSAL_SYSTEM_PROMPT,
    TAG_RETRIEVAL_SYSTEM_PROMPT,
)
from .types import AgentResponse, StreamChunk

_log = logger.bind(module="RagAgent")

# 分类服务不可用时的固定澄清话术（不进入检索，防绕开）
_CLARIFICATION_FALLBACK_REPLY = (
    "抱歉，我暂时没能理解你的问题。我主要解答报名加入、考核流程、团队介绍和软件下载相关问题，"
    "你可以换个方式描述一下吗？"
)


class RagAgent:
    """RAG Agent：使用 LangGraph StateGraph 实现多轮检索与答案生成。

    同步对话（``chat``）与流式对话（``chat_stream``）复用同一个状态图，
    仅在输出层区分完整响应与 SSE 事件流。

    所有请求在进入状态图之前先经过意图识别闸门：
    - ``RETRIEVE``：进入 LangGraph RAG 检索流程；
    - ``REFUSE``：不检索，由 LLM 生成拒绝话术；
    - ``DIRECT``：不检索，由 LLM 生成简短直接回复。
    """

    def __init__(
        self,
        system_prompt: str = "",
        llm: LLMProvider | None = None,
        thread_id: str | None = None,
    ):
        self._llm = llm or LLMFactory.create()
        self._tool_specs = ToolRegistry.get_function_calling_specs()

        system_prompt = system_prompt or TAG_RETRIEVAL_SYSTEM_PROMPT
        self.conversation = Conversation(system_prompt=system_prompt)

        self._thread_id = thread_id or str(uuid.uuid4())
        self._checkpointer = MemorySaver()
        builder = build_rag_graph(self._llm, self._tool_specs)
        self._graph = builder.compile(checkpointer=self._checkpointer)

        # 意图识别 / 安全围栏
        self._intent_classifier = IntentClassifier()

        tool_names = [t["function"]["name"] for t in self._tool_specs]
        _log.info(f"Agent 初始化完成, tools={tool_names}, thread_id={self._thread_id}")

    # ------------------------------------------------------------------
    # 同步对话
    # ------------------------------------------------------------------

    def chat(self, user_input: str) -> AgentResponse:
        # 意图识别闸门（任何异常都不放行检索，防绕开）
        intent_result = self._classify_intent(user_input)
        intent_note = (
            f"[意图识别] {intent_result.intent} "
            f"(置信度 {intent_result.confidence:.2f}): {intent_result.reason}"
        )

        if intent_result.action == ACTION_REFUSE:
            content = self._generate_refusal(user_input, intent_result)
            self._append_guard_messages(user_input, content)
            _log.info(f"Agent 拦截请求并返回拒绝话术, intent={intent_result.intent}")
            return AgentResponse(content=content, reasoning=intent_note)

        if intent_result.action == ACTION_DIRECT:
            content = self._generate_direct_reply(user_input, intent_result)
            self._append_guard_messages(user_input, content)
            _log.info(f"Agent 直接回复请求, intent={intent_result.intent}")
            return AgentResponse(content=content, reasoning=intent_note)

        # RETRIEVE：正常走 RAG 检索流程
        self.conversation.add_user_message(user_input)
        messages = self.conversation.get_messages()

        result = self._run_graph(messages)
        final_text = result["final_content"] or ""
        reasoning = result["final_reasoning"] or ""

        # 把图执行期间产生的中间消息（含 tool_calls / reasoning_content）同步回 conversation
        final_messages = list(result["messages"])
        final_messages.append({"role": "assistant", "content": final_text})
        self.conversation.messages = final_messages

        _log.info(f"Agent 响应完成, 长度={len(final_text)}, reasoning_len={len(reasoning)}")
        return AgentResponse(content=final_text, reasoning=intent_note + "\n" + reasoning)

    # ------------------------------------------------------------------
    # 流式对话
    # ------------------------------------------------------------------

    def chat_stream(self, user_input: str) -> Iterator[StreamChunk]:
        # 意图识别闸门：流式输出分类分析过程（reasoning 事件），任何异常都不放行检索
        intent_result: IntentResult | None = None
        try:
            context = self.conversation.get_messages()[-5:] if self.conversation.messages else None
            for event in self._intent_classifier.classify_stream(user_input, context_messages=context):
                if event["type"] == "reasoning":
                    yield StreamChunk(type="reasoning", content=event["content"])
                elif event["type"] == "result":
                    intent_result = event["result"]
        except Exception as exc:
            _log.warning(f"意图分类异常: {exc}，返回澄清话术（不放行检索）")
            intent_result = clarify_result(f"分类异常: {exc}")

        if intent_result is None:
            intent_result = clarify_result("分类无结果")

        if intent_result.action == ACTION_REFUSE:
            yield StreamChunk(
                type="reasoning",
                content=f"\n[意图识别] 判定为 {intent_result.intent}，不进入知识库检索，生成拒绝回复。\n",
            )
            content = ""
            for chunk in self._stream_guard_reply(self._refusal_messages(user_input, intent_result)):
                content += chunk.content
                yield chunk
            self._append_guard_messages(user_input, content)
            yield StreamChunk(type="done")
            _log.info(f"Agent 流式拦截请求并返回拒绝话术, intent={intent_result.intent}")
            return

        if intent_result.action == ACTION_DIRECT:
            if intent_result.intent == INTENT_CLARIFY:
                yield StreamChunk(
                    type="reasoning",
                    content="\n[意图识别] 未能明确问题意图，请求用户澄清。\n",
                )
                content = _CLARIFICATION_FALLBACK_REPLY
                yield StreamChunk(type="content", content=content)
            else:
                yield StreamChunk(
                    type="reasoning",
                    content=f"\n[意图识别] 判定为 {intent_result.intent}，直接回复，无需检索。\n",
                )
                content = ""
                for chunk in self._stream_guard_reply(self._direct_reply_messages(user_input)):
                    content += chunk.content
                    yield chunk
            self._append_guard_messages(user_input, content)
            yield StreamChunk(type="done")
            _log.info(f"Agent 流式直接回复请求, intent={intent_result.intent}")
            return

        # RETRIEVE：正常走 RAG 检索流程
        yield StreamChunk(
            type="reasoning",
            content=f"\n[意图识别] 判定为 {intent_result.intent}，进入知识库检索。\n",
        )
        self.conversation.add_user_message(user_input)
        messages = self.conversation.get_messages()

        for chunk in self._run_graph_stream(messages):
            yield chunk

        # 从 checkpointer 获取最终状态，同步中间消息与最终答案
        config = self._build_config()
        final_state = self._graph.get_state(config)
        final_messages = list(final_state.values["messages"])
        final_content = final_state.values["final_content"] or ""
        final_messages.append({"role": "assistant", "content": final_content})
        self.conversation.messages = final_messages

        _log.info(f"Agent 流式响应完成, 长度={len(final_content)}")
        yield StreamChunk(type="done")

    # ------------------------------------------------------------------
    # 意图识别与回复生成
    # ------------------------------------------------------------------

    def _classify_intent(self, user_input: str) -> IntentResult:
        """对用户输入进行意图分类（非流式）。

        分类服务异常时返回 CLARIFY（DIRECT），不会放行检索，防止绕过围栏。
        """
        try:
            # 传入最近几轮对话作为上下文，消除歧义
            context = self.conversation.get_messages()[-5:] if self.conversation.messages else None
            return self._intent_classifier.classify(user_input, context_messages=context)
        except Exception as exc:
            _log.warning(f"意图分类异常: {exc}，返回澄清话术（不放行检索）")
            return clarify_result(f"分类异常: {exc}")

    def _refusal_messages(self, user_input: str, intent_result: IntentResult) -> list[dict]:
        """构建拒绝话术生成的消息列表。"""
        prompt = (
            f"用户问题：{user_input}\n"
            f"分类结果：{intent_result.intent}\n"
            f"分类理由：{intent_result.reason}\n\n"
            "请生成拒绝回复。"
        )
        return [
            {"role": "system", "content": REFUSAL_SYSTEM_PROMPT},
            {"role": "user", "content": prompt},
        ]

    def _direct_reply_messages(self, user_input: str) -> list[dict]:
        """构建直接回复生成的消息列表。"""
        return [
            {"role": "system", "content": DIRECT_REPLY_SYSTEM_PROMPT},
            {"role": "user", "content": user_input},
        ]

    def _generate_refusal(self, user_input: str, intent_result: IntentResult) -> str:
        """根据拦截意图生成拒绝话术（非流式）。"""
        try:
            return self._llm.invoke(self._refusal_messages(user_input, intent_result))
        except Exception as exc:
            _log.error(f"拒绝话术生成失败: {exc}")
            return (
                "抱歉，我目前主要解答报名加入、考核流程、团队介绍和软件下载相关问题。"
                "你的问题我可能无法直接回答，建议关注招新群通知或入队后向对应方向的同学请教。"
            )

    def _generate_direct_reply(self, user_input: str, intent_result: IntentResult) -> str:
        """生成直接回复（非流式）。CLARIFY 意图返回固定澄清话术。"""
        if intent_result.intent == INTENT_CLARIFY:
            return _CLARIFICATION_FALLBACK_REPLY
        try:
            return self._llm.invoke(self._direct_reply_messages(user_input))
        except Exception as exc:
            _log.error(f"直接回复生成失败: {exc}")
            return "你好！我是蓝网团队的 AI 助手，可以帮你解答报名、考核流程、团队介绍和软件下载相关问题，请问有什么可以帮你的？"

    def _stream_guard_reply(self, messages: list[dict]) -> Iterator[StreamChunk]:
        """流式生成拒绝/直接回复，逐段 yield content 事件。

        调用方自行拼接 ``chunk.content`` 获得完整文本。
        流式失败时回退到非流式 ``invoke``，再失败返回固定兜底话术。
        """
        try:
            for delta in self._llm.stream(messages):
                if delta:
                    yield StreamChunk(type="content", content=delta)
            return
        except Exception as exc:
            _log.error(f"流式回复生成失败: {exc}，尝试非流式兜底")
        try:
            fallback = self._llm.invoke(messages)
        except Exception as exc2:
            _log.error(f"非流式兜底也失败: {exc2}")
            fallback = (
                "抱歉，我目前主要解答报名加入、考核流程、团队介绍和软件下载相关问题。"
                "你的问题我可能无法直接回答。"
            )
        yield StreamChunk(type="content", content=fallback)

    def _append_guard_messages(self, user_input: str, assistant_content: str) -> None:
        """把被拦截/直接回复的对话同步到 conversation，保持多轮上下文。"""
        self.conversation.add_user_message(user_input)
        self.conversation.add_assistant_message(assistant_content)

    # ------------------------------------------------------------------
    # 内部方法
    # ------------------------------------------------------------------

    def _build_config(self) -> dict:
        """构建 LangGraph 调用配置。"""
        return {
            "configurable": {
                "thread_id": self._thread_id,
                "llm": self._llm,
                "tool_specs": self._tool_specs,
            }
        }

    def _build_initial_state(self, messages: list[dict]) -> dict:
        """构建状态图初始状态。"""
        return {
            "messages": messages,
            "tag_rounds": 0,
            "chunk_rounds": 0,
            "fallback_rounds": 0,
            "final_content": "",
            "final_reasoning": "",
        }

    def _run_graph(self, messages: list[dict]) -> dict:
        """同步调用状态图。"""
        config = self._build_config()
        initial_state = self._build_initial_state(messages)
        return self._graph.invoke(initial_state, config)

    def _run_graph_stream(self, messages: list[dict]) -> Iterator[StreamChunk]:
        """流式调用状态图，将 writer 事件转译为 ``StreamChunk``。"""
        config = self._build_config()
        initial_state = self._build_initial_state(messages)

        for event in self._graph.stream(initial_state, config, stream_mode="custom"):
            if isinstance(event, dict) and "type" in event:
                yield StreamChunk(
                    type=event["type"],
                    content=event.get("content", ""),
                    tool_name=event.get("tool_name"),
                    tool_args=event.get("tool_args") if "tool_args" in event else {},
                )
            else:
                _log.warning(f"未知自定义事件: {event}")

    def reset_conversation(self) -> None:
        self.conversation.clear()
        _log.info("对话历史已重置")
