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
from .prompts import TAG_RETRIEVAL_SYSTEM_PROMPT
from .types import AgentResponse, StreamChunk

_log = logger.bind(module="RagAgent")


class RagAgent:
    """RAG Agent：使用 LangGraph StateGraph 实现多轮检索与答案生成。

    同步对话（``chat``）与流式对话（``chat_stream``）复用同一个状态图，
    仅在输出层区分完整响应与 SSE 事件流。
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

        tool_names = [t["function"]["name"] for t in self._tool_specs]
        _log.info(f"Agent 初始化完成, tools={tool_names}, thread_id={self._thread_id}")

    # ------------------------------------------------------------------
    # 同步对话
    # ------------------------------------------------------------------

    def chat(self, user_input: str) -> AgentResponse:
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
        return AgentResponse(content=final_text, reasoning=reasoning)

    # ------------------------------------------------------------------
    # 流式对话
    # ------------------------------------------------------------------

    def chat_stream(self, user_input: str) -> Iterator[StreamChunk]:
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
