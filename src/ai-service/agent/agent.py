from __future__ import annotations

from typing import Iterator

from langchain_openai import ChatOpenAI
from loguru import logger
from langchain_core.messages import AIMessage

from config import settings
from tools import ToolRegistry
from tools.tag_search_detailed import tag_search_detailed
from tools.tag_search import tag_generate

from .conversation import Conversation
from .prompts import TAG_RETRIEVAL_SYSTEM_PROMPT

_log = logger.bind(module="RagAgent")

_DEFAULT_BASE_URLS: dict[str, str] = {
    "siliconflow": "https://api.siliconflow.cn/v1",
    "deepseek": "https://api.deepseek.com/v1",
    "ollama": "http://localhost:11434/v1",
}

_DEFAULT_LLM_MODELS: dict[str, str] = {
    "siliconflow": "deepseek-ai/DeepSeek-V3",
    "deepseek": "deepseek-chat",
    "ollama": "qwen3:8b",
}

_MAX_TAG_ROUNDS = 3
_MAX_CHUNK_ROUNDS = 2


class RagAgent:
    def __init__(
        self,
        system_prompt: str = "",
        provider: str = "",
        api_key: str = "",
        model: str = "",
        base_url: str = "",
        temperature: float | None = None,
        timeout: int | None = None,
    ):
        provider = provider or settings.LLM_PROVIDER
        api_key = api_key or settings.LLM_API_KEY
        model = model or settings.LLM_MODEL or _DEFAULT_LLM_MODELS.get(provider, "")
        base_url = base_url or settings.LLM_BASE_URL or _DEFAULT_BASE_URLS.get(provider, "")
        temperature = temperature if temperature is not None else settings.LLM_TEMPERATURE
        timeout = timeout or settings.LLM_TIMEOUT

        if not api_key:
            raise ValueError(f"LLM API Key 未配置，请在环境变量 TBD_RAG_LLM_API_KEY 中设置")

        self._llm = ChatOpenAI(
            api_key=api_key,
            base_url=base_url,
            model=model,
            temperature=temperature,
            request_timeout=timeout,
        )

        self._tool_specs = ToolRegistry.get_function_calling_specs()
        self._llm_with_tools = self._llm.bind_tools(self._tool_specs) if self._tool_specs else self._llm

        system_prompt = system_prompt or TAG_RETRIEVAL_SYSTEM_PROMPT
        self.conversation = Conversation(system_prompt=system_prompt)

        self._tag_rounds = 0
        self._chunk_rounds = 0

        tool_names = [t["function"]["name"] for t in self._tool_specs]
        _log.info(f"Agent 初始化完成, provider={provider}, model={model}, tools={tool_names}")

    # ------------------------------------------------------------------
    # 同步对话
    # ------------------------------------------------------------------

    def chat(self, user_input: str) -> str:
        enriched_input = self._pre_disclosure(user_input)
        self.conversation.add_user_message(enriched_input)
        messages = self.conversation.get_messages()

        self._tag_rounds = 0
        self._chunk_rounds = 0
        response = self._run_two_stage_loop(messages)
        final_text = response.content or ""

        self.conversation.add_assistant_message(final_text)
        _log.info(f"Agent 响应完成, 长度={len(final_text)}, tag_rounds={self._tag_rounds}, chunk_rounds={self._chunk_rounds}")
        return final_text

    # ------------------------------------------------------------------
    # 流式对话
    # ------------------------------------------------------------------

    def chat_stream(self, user_input: str) -> Iterator[str]:
        enriched_input = self._pre_disclosure(user_input)
        self.conversation.add_user_message(enriched_input)
        messages = self.conversation.get_messages()

        self._tag_rounds = 0
        self._chunk_rounds = 0
        response = self._run_two_stage_loop(messages)

        if response.tool_calls:
            yield "工具调用次数过多，请简化查询"
            return

        full_content = ""
        for chunk in self._llm_with_tools.stream(messages):
            if chunk.content:
                full_content += chunk.content
                yield chunk.content

        self.conversation.add_assistant_message(full_content)
        _log.info(f"Agent 流式响应完成, 长度={len(full_content)}")

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
        lines.append("  3. 调用 chunk_search_by_tags 检索分片（最多2轮）")
        lines.append("  4. 基于结果生成答案")
        return "\n".join(lines)

    def _run_two_stage_loop(self, messages: list[dict]) -> AIMessage:
        max_total = _MAX_TAG_ROUNDS + _MAX_CHUNK_ROUNDS + 1
        for turn in range(max_total):
            response: AIMessage = self._llm_with_tools.invoke(messages)

            if not response.tool_calls:
                return response

            _log.info(
                f"工具调用第 {turn + 1} 轮: "
                f"{[tc['name'] for tc in response.tool_calls]}"
                f"(tag={self._tag_rounds}, chunk={self._chunk_rounds})"
            )

            for tc in response.tool_calls:
                result = self._handle_tool_call_with_limit(tc, messages)
                if result is not None:
                    tool_call_id = tc.get("id", "")
                    messages.append({"role": "tool", "content": result, "tool_call_id": tool_call_id})

        _log.warning(f"工具调用已达上限 {max_total} 轮")
        return response

    def _handle_tool_call_with_limit(self, tc: dict, messages: list[dict]) -> str | None:
        tool_name = tc["name"]
        tool_args = tc.get("args", {})

        if tool_name == "tag_search_detailed":
            self._tag_rounds += 1
            if self._tag_rounds > _MAX_TAG_ROUNDS:
                msg = f"标签搜索已达上限 {_MAX_TAG_ROUNDS} 轮，请直接基于已有标签进入选择阶段"
                _log.warning(msg)
                return msg

        if tool_name == "chunk_search_by_tags":
            self._chunk_rounds += 1
            if self._chunk_rounds > _MAX_CHUNK_ROUNDS:
                msg = f"分片检索已达上限 {_MAX_CHUNK_ROUNDS} 轮，请基于已有检索结果生成答案"
                _log.warning(msg)
                return msg

        return ToolRegistry.execute(tool_name, **tool_args)

    def reset_conversation(self) -> None:
        self.conversation.clear()
        self._tag_rounds = 0
        self._chunk_rounds = 0
        _log.info("对话历史已重置")
