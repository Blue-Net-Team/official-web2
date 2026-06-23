"""DeepSeek 官方 API 提供者 (LLM only)。

DeepSeek API 兼容 OpenAI 格式，base_url 为 https://api.deepseek.com/v1。
推理模型 deepseek-reasoner 会在流式输出中通过 reasoning_content 字段返回思考过程。
"""

import json
import time
from typing import Iterator

from openai import OpenAI
from loguru import logger

from .base import LLMProvider, LLMResponse, StreamEvent

API_BASE_URL = "https://api.deepseek.com/v1"

MAX_RETRIES = 3
BACKOFF_BASE = 2.0


def _retry(func):
    """简单重试装饰器：指数退避，只重试网络/超时/5xx 错误。"""

    def wrapper(*args, **kwargs):
        last_exc = None
        for attempt in range(1, MAX_RETRIES + 1):
            try:
                return func(*args, **kwargs)
            except Exception as exc:
                last_exc = exc
                if hasattr(exc, "status_code") and exc.status_code in (400, 401, 403, 404):
                    raise
                if attempt < MAX_RETRIES:
                    sleep = BACKOFF_BASE ** attempt
                    logger.warning(f"[{func.__qualname__}] 第 {attempt} 次失败: {exc}，{sleep:.1f}s 后重试...")
                    time.sleep(sleep)
                else:
                    logger.error(f"[{func.__qualname__}] 重试 {MAX_RETRIES} 次后仍失败: {exc}")
                    raise last_exc
        raise last_exc

    return wrapper


class DeepSeekLLM(LLMProvider):
    """DeepSeek 官方 LLM (OpenAI 兼容接口)。

    支持推理模型 deepseek-reasoner，流式输出时会通过 reasoning_content 返回思考过程。
    直接使用 OpenAI 客户端，确保 reasoning_content 正确传递。
    """

    def __init__(self, api_key: str, model: str = "deepseek-reasoner", temperature: float = 0.7, timeout: float = 120):
        self._client = OpenAI(api_key=api_key, base_url=API_BASE_URL, timeout=timeout)
        self._model = model
        self._temperature = temperature

    def _to_openai_messages(self, messages: list[dict]) -> list[dict]:
        """将内部消息格式转换为 OpenAI API 格式，保留 reasoning_content。"""
        openai_msgs = []
        for m in messages:
            role = m["role"]
            if role in ("ai", "assistant"):
                msg = {"role": "assistant", "content": m.get("content", "")}
                tool_calls = m.get("tool_calls")
                if tool_calls:
                    openai_tool_calls = []
                    for tc in tool_calls:
                        if "function" in tc:
                            openai_tool_calls.append(tc)
                        else:
                            openai_tool_calls.append({
                                "id": tc.get("id", ""),
                                "type": tc.get("type", "function"),
                                "function": {
                                    "name": tc.get("name", ""),
                                    "arguments": json.dumps(tc.get("args", {}))
                                }
                            })
                    msg["tool_calls"] = openai_tool_calls
                reasoning = m.get("reasoning_content")
                if reasoning:
                    msg["reasoning_content"] = reasoning
                openai_msgs.append(msg)
            elif role == "tool":
                openai_msgs.append({
                    "role": "tool",
                    "content": m.get("content", ""),
                    "tool_call_id": m.get("tool_call_id", ""),
                })
            elif role == "system":
                openai_msgs.append({"role": "system", "content": m.get("content", "")})
            else:
                # user / human
                openai_msgs.append({"role": "user", "content": m.get("content", "")})
        return openai_msgs

    @_retry
    def invoke(self, messages: list[dict]) -> str:
        logger.info(f"发送 LLM 请求, messages={len(messages)}, model={self._model}")
        openai_msgs = self._to_openai_messages(messages)
        resp = self._client.chat.completions.create(
            model=self._model,
            messages=openai_msgs,
            temperature=self._temperature,
        )
        result = resp.choices[0].message.content or ""
        logger.info(f"收到 LLM 响应, 长度={len(result)}")
        return result

    @_retry
    def invoke_with_tools(self, messages: list[dict], tools: list[dict]) -> LLMResponse:
        logger.info(f"发送 LLM 请求(含工具), messages={len(messages)}, tools={len(tools)}, model={self._model}")
        openai_msgs = self._to_openai_messages(messages)
        resp = self._client.chat.completions.create(
            model=self._model,
            messages=openai_msgs,
            temperature=self._temperature,
            tools=tools,
            parallel_tool_calls=False,
        )
        msg = resp.choices[0].message

        tool_calls = []
        if hasattr(msg, "tool_calls") and msg.tool_calls:
            # 转换为 LangChain 兼容的格式
            for tc in msg.tool_calls:
                args_str = tc.function.arguments
                try:
                    args = json.loads(args_str) if isinstance(args_str, str) else args_str
                except json.JSONDecodeError:
                    args = {}
                tool_calls.append({
                    "id": tc.id,
                    "name": tc.function.name,
                    "args": args,
                    "type": "function",
                })

        content = msg.content or ""
        reasoning = getattr(msg, "reasoning_content", None) or ""
        logger.info(f"收到 LLM 响应, content_len={len(content)}, reasoning_len={len(reasoning)}, tool_calls={len(tool_calls)}")
        return LLMResponse(content=content, tool_calls=tool_calls, reasoning_content=reasoning)

    def stream(self, messages: list[dict]) -> Iterator[str]:
        """流式调用，仅 yield 最终答案内容（不含 reasoning_content）。

        思考过程已在 ``stream_with_tools`` 阶段通过 ``reasoning_content`` 单独输出，
        最终生成阶段只需返回正式回复内容，避免推理片段混入答案。
        """
        logger.info(f"流式 LLM 请求, messages={len(messages)}, model={self._model}")
        openai_msgs = self._to_openai_messages(messages)
        for chunk in self._client.chat.completions.create(
            model=self._model,
            messages=openai_msgs,
            temperature=self._temperature,
            stream=True,
        ):
            delta = chunk.choices[0].delta
            # 最终生成阶段不输出 reasoning_content，只输出正式内容
            if delta.content:
                yield delta.content
        logger.info("流式 LLM 响应结束")

    def stream_with_tools(self, messages: list[dict], tools: list[dict]) -> Iterator[StreamEvent]:
        """支持 function calling 的流式调用，逐片段 yield 思考过程、内容和工具调用。"""
        logger.info(f"流式 LLM 请求(含工具), messages={len(messages)}, tools={len(tools)}, model={self._model}")
        openai_msgs = self._to_openai_messages(messages)

        tool_calls_agg: dict[int, dict] = {}
        has_emitted_tool_calls = False

        for chunk in self._client.chat.completions.create(
            model=self._model,
            messages=openai_msgs,
            temperature=self._temperature,
            tools=tools,
            stream=True,
            parallel_tool_calls=False,
        ):
            delta = chunk.choices[0].delta
            finish_reason = chunk.choices[0].finish_reason

            if hasattr(delta, "reasoning_content") and delta.reasoning_content:
                yield StreamEvent(type="reasoning", delta=delta.reasoning_content)

            if delta.content:
                yield StreamEvent(type="content", delta=delta.content)

            if delta.tool_calls:
                for tc in delta.tool_calls:
                    idx = tc.index
                    if idx not in tool_calls_agg:
                        tool_calls_agg[idx] = {"id": "", "name": "", "args": ""}
                    if tc.id:
                        tool_calls_agg[idx]["id"] = tc.id
                    if tc.function and tc.function.name:
                        tool_calls_agg[idx]["name"] = tc.function.name
                    if tc.function and tc.function.arguments:
                        tool_calls_agg[idx]["args"] += tc.function.arguments

            if finish_reason == "tool_calls" and not has_emitted_tool_calls:
                for idx in sorted(tool_calls_agg.keys()):
                    tc = tool_calls_agg[idx]
                    args_str = tc["args"]
                    try:
                        args = json.loads(args_str) if isinstance(args_str, str) and args_str else {}
                    except json.JSONDecodeError:
                        args = {}
                    yield StreamEvent(
                        type="tool_call",
                        tool_call_id=tc["id"],
                        tool_name=tc["name"],
                        tool_args=args,
                    )
                has_emitted_tool_calls = True
                tool_calls_agg.clear()

            if finish_reason in ("tool_calls", "stop"):
                yield StreamEvent(type="done")

        # 兜底：流正常结束但 finish_reason 未明确标记 tool_calls 时，如果还有聚合中的 tool_call 则发出
        if tool_calls_agg and not has_emitted_tool_calls:
            for idx in sorted(tool_calls_agg.keys()):
                tc = tool_calls_agg[idx]
                args_str = tc["args"]
                try:
                    args = json.loads(args_str) if isinstance(args_str, str) and args_str else {}
                except json.JSONDecodeError:
                    args = {}
                yield StreamEvent(
                    type="tool_call",
                    tool_call_id=tc["id"],
                    tool_name=tc["name"],
                    tool_args=args,
                )
            yield StreamEvent(type="done")
            logger.info(f"流式 LLM 响应结束, tool_calls={len(tool_calls_agg)} (兜底发出)")
        elif not has_emitted_tool_calls:
            # 如果正常结束且未发出过 done，兜底发出一次
            yield StreamEvent(type="done")
            logger.info("流式 LLM 响应结束")
        else:
            logger.info("流式 LLM 响应结束")
