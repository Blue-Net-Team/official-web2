from __future__ import annotations

from typing import Iterator

from loguru import logger

from llm_providers.base import LLMProvider, LLMResponse
from llm_providers.factory import LLMFactory
from tools import ToolRegistry
from tools.tag_search_detailed import tag_search_detailed
from tools.tag_search import tag_generate

from .conversation import Conversation
from .prompts import TAG_RETRIEVAL_SYSTEM_PROMPT
from .types import AgentResponse, StreamChunk

_log = logger.bind(module="RagAgent")

_MAX_TAG_ROUNDS = 4
_MAX_CHUNK_ROUNDS = 3
_MAX_FALLBACK_ROUNDS = 1  # chunk_search 兜底最多 1 轮


class RagAgent:
    def __init__(
        self,
        system_prompt: str = "",
        llm: LLMProvider | None = None,
    ):
        self._llm = llm or LLMFactory.create()

        self._tool_specs = ToolRegistry.get_function_calling_specs()

        system_prompt = system_prompt or TAG_RETRIEVAL_SYSTEM_PROMPT
        self.conversation = Conversation(system_prompt=system_prompt)

        self._tag_rounds = 0
        self._chunk_rounds = 0
        self._fallback_rounds = 0

        tool_names = [t["function"]["name"] for t in self._tool_specs]
        _log.info(f"Agent 初始化完成, tools={tool_names}")

    # ------------------------------------------------------------------
    # 同步对话
    # ------------------------------------------------------------------

    def chat(self, user_input: str) -> AgentResponse:
        enriched_input = self._pre_disclosure(user_input)
        self.conversation.add_user_message(enriched_input)
        messages = self.conversation.get_messages()

        self._tag_rounds = 0
        self._chunk_rounds = 0
        llm_response = self._run_two_stage_loop(messages)
        final_text = llm_response.content or ""
        reasoning = llm_response.reasoning_content or ""

        self.conversation.add_assistant_message(final_text)
        _log.info(f"Agent 响应完成, 长度={len(final_text)}, reasoning_len={len(reasoning)}, tag_rounds={self._tag_rounds}, chunk_rounds={self._chunk_rounds}")
        return AgentResponse(content=final_text, reasoning=reasoning)

    # ------------------------------------------------------------------
    # 流式对话
    # ------------------------------------------------------------------

    def chat_stream(self, user_input: str) -> Iterator[StreamChunk]:
        enriched_input = self._pre_disclosure(user_input)
        self.conversation.add_user_message(enriched_input)
        messages = self.conversation.get_messages()

        self._tag_rounds = 0
        self._chunk_rounds = 0

        # 阶段 1：工具调用循环（非流式，收集结果）
        max_total = _MAX_TAG_ROUNDS + _MAX_CHUNK_ROUNDS + 1
        for turn in range(max_total):
            # 最后一轮前提示模型
            if turn == max_total - 1:
                messages.append({
                    "role": "user",
                    "content": "【系统提示】工具调用即将达到上限，这是你最后的机会。请基于已获取的所有检索信息，直接生成最终答案，不要再调用任何工具。"
                })

            llm_response = self._llm.invoke_with_tools(messages, self._tool_specs)

            # 透出思考过程
            if llm_response.reasoning_content:
                _log.info(f"Agent 思考: {llm_response.reasoning_content}")
                yield StreamChunk(type="reasoning", content=llm_response.reasoning_content)

            if not llm_response.tool_calls:
                # 没有工具调用了，进入阶段 2 流式输出最终答案
                break

            _log.info(
                f"流式-工具调用第 {turn + 1} 轮: "
                f"{[tc['name'] for tc in llm_response.tool_calls]}"
                f"(tag={self._tag_rounds}, chunk={self._chunk_rounds})"
            )

            # 添加 assistant 消息（包含 tool_calls 和 reasoning_content）
            assistant_msg = {
                "role": "assistant",
                "content": llm_response.content,
                "tool_calls": llm_response.tool_calls,
            }
            if llm_response.reasoning_content:
                assistant_msg["reasoning_content"] = llm_response.reasoning_content
            messages.append(assistant_msg)

            # 执行工具
            for tc in llm_response.tool_calls:
                yield StreamChunk(
                    type="tool_call",
                    tool_name=tc.get("name"),
                    tool_args=tc.get("args", {}),
                )
                result = self._handle_tool_call_with_limit(tc, messages)
                if result is not None:
                    yield StreamChunk(
                        type="tool_result",
                        content=result,
                        tool_name=tc.get("name"),
                    )
                    tool_call_id = tc.get("id", "")
                    messages.append({
                        "role": "tool",
                        "content": result,
                        "tool_call_id": tool_call_id
                    })
        else:
            # 达到上限，强制让模型基于已有信息生成答案
            _log.warning(f"工具调用已达上限 {max_total} 轮，强制生成答案")
            messages.append({
                "role": "user",
                "content": "【系统提示】工具调用次数已达上限。请基于已获取的所有检索结果，直接给出完整准确的最终答案，不要调用任何工具。"
            })

        # 阶段 2：流式输出最终答案（纯文本，无 tools）
        full_content = ""
        for chunk in self._llm.stream(messages):
            if chunk:
                full_content += chunk
                yield StreamChunk(type="content", content=chunk)

        self.conversation.add_assistant_message(full_content)
        _log.info(f"Agent 流式响应完成, 长度={len(full_content)}")
        yield StreamChunk(type="done")

    # ------------------------------------------------------------------
    # 内部方法
    # ------------------------------------------------------------------

    def _pre_disclosure(self, user_input: str) -> str:
        _log.info("预披露阶段: 自动生成初始标签并检索")
        try:
            pre_tags = tag_generate(user_input)
            _log.info(f"初始标签: {pre_tags}")
        except Exception as e:
            _log.warning(f"标签生成失败: {e}")
            pre_tags = []

        pre_tag_results = []
        if pre_tags:
            try:
                pre_tag_results = tag_search_detailed(" ".join(pre_tags), top_k=10)
                _log.info(f"初始标签检索结果: {len(pre_tag_results)} 项")
            except Exception as e:
                _log.warning(f"标签检索失败: {e}")

        lines = [f"用户问题: {user_input}", ""]
        if pre_tag_results:
            lines.append("=== 初始标签检索结果 ===")
            if pre_tags:
                lines.append(f"自动生成的标签: {', '.join(pre_tags)}")
                lines.append("")
            for i, r in enumerate(pre_tag_results, 1):
                desc = f" - {r.tag_description[:60]}" if r.tag_description else ""
                lines.append(f"  [{i}] {r.tag_name:<12} score={r.relevance_score:.4f}  docs={r.chunks_count}{desc}")
        else:
            lines.append("未找到相关标签，请尝试搜索。")

        lines.append("")
        lines.append("请按两阶段工作流执行：")
        lines.append("  1. 如果标签不充分，调用 tag_search_detailed 扩展（最多3轮）")
        lines.append("  2. 标签充分后，选择标签子集")
        lines.append("  3. 调用 chunk_search_by_tags 检索分片（最多3轮）")
        lines.append("  4. 如果 chunk_search_by_tags 诊断显示'标签均不在库中'，调用 chunk_search(query) 进行兜底语义搜索（最多1轮）")
        lines.append("  5. 基于结果生成答案")
        return "\n".join(lines)

    def _run_two_stage_loop(self, messages: list[dict]) -> LLMResponse:
        max_total = _MAX_TAG_ROUNDS + _MAX_CHUNK_ROUNDS + 1
        for turn in range(max_total):
            # 最后一轮前提示模型
            if turn == max_total - 1:
                messages.append({
                    "role": "user",
                    "content": "【系统提示】工具调用即将达到上限，这是你最后的机会。请基于已获取的所有检索信息，直接生成最终答案，不要再调用任何工具。"
                })

            llm_response = self._llm.invoke_with_tools(messages, self._tool_specs)

            # 透出思考过程到日志
            if llm_response.reasoning_content:
                _log.info(f"Agent 思考: {llm_response.reasoning_content}")

            if not llm_response.tool_calls:
                return llm_response

            _log.info(
                f"工具调用第 {turn + 1} 轮: "
                f"{[tc['name'] for tc in llm_response.tool_calls]}"
                f"(tag={self._tag_rounds}, chunk={self._chunk_rounds})"
            )

            # 添加 assistant 消息（包含 tool_calls 和 reasoning_content）
            assistant_msg = {
                "role": "assistant",
                "content": llm_response.content,
                "tool_calls": llm_response.tool_calls,
            }
            if llm_response.reasoning_content:
                assistant_msg["reasoning_content"] = llm_response.reasoning_content
            messages.append(assistant_msg)

            for tc in llm_response.tool_calls:
                result = self._handle_tool_call_with_limit(tc, messages)
                if result is not None:
                    tool_call_id = tc.get("id", "")
                    messages.append({
                        "role": "tool",
                        "content": result,
                        "tool_call_id": tool_call_id
                    })

        # 达到上限，强制让模型基于已有信息生成答案
        _log.warning(f"工具调用已达上限 {max_total} 轮，强制生成答案")
        messages.append({
            "role": "user",
            "content": "【系统提示】工具调用次数已达上限。请基于已获取的所有检索结果，直接给出完整准确的最终答案，不要调用任何工具。"
        })
        return self._llm.invoke(messages)

    def _handle_tool_call_with_limit(self, tc: dict, messages: list[dict]) -> str | None:
        tool_name = tc["name"]
        tool_args = tc.get("args", {})

        if tool_name == "tag_search_detailed":
            self._tag_rounds += 1
            if self._tag_rounds > _MAX_TAG_ROUNDS:
                msg = f"标签搜索已达上限 {_MAX_TAG_ROUNDS} 轮，请直接基于已有标签进入选择阶段"
                _log.warning(msg)
                return msg
            if self._tag_rounds == _MAX_TAG_ROUNDS:
                _log.info("标签搜索剩余 1 次机会")

        if tool_name == "chunk_search_by_tags":
            self._chunk_rounds += 1
            if self._chunk_rounds > _MAX_CHUNK_ROUNDS:
                msg = f"分片检索已达上限 {_MAX_CHUNK_ROUNDS} 轮，请基于已有检索结果生成答案"
                _log.warning(msg)
                return msg
            if self._chunk_rounds == _MAX_CHUNK_ROUNDS:
                _log.info("分片检索剩余 1 次机会")

        if tool_name == "chunk_search":
            self._fallback_rounds += 1
            if self._fallback_rounds > _MAX_FALLBACK_ROUNDS:
                msg = f"兜底语义搜索已达上限 {_MAX_FALLBACK_ROUNDS} 轮，请基于已有检索结果生成答案"
                _log.warning(msg)
                return msg

        return ToolRegistry.execute(tool_name, **tool_args)

    def reset_conversation(self) -> None:
        self.conversation.clear()
        self._tag_rounds = 0
        self._chunk_rounds = 0
        self._fallback_rounds = 0
        _log.info("对话历史已重置")


def test() -> None:
    agent = RagAgent()
    response = agent.chat("3个方向应该怎么选")
    print(response.content)
    if response.reasoning:
        print(f"\n[思考过程]\n{response.reasoning}")


if __name__ == "__main__":
    test()
# end main