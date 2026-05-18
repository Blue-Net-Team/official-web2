"""DeepSeek 官方 API 提供者 (LLM only)。

DeepSeek API 兼容 OpenAI 格式，base_url 为 https://api.deepseek.com/v1。
推理模型 deepseek-reasoner 会在流式输出中通过 reasoning_content 字段返回思考过程。
"""

import time
from typing import Iterator

from langchain_openai import ChatOpenAI
from loguru import logger

from .base import LLMProvider

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
    """

    def __init__(self, api_key: str, model: str = "deepseek-reasoner", temperature: float = 0.7, timeout: float = 120):
        self._llm = ChatOpenAI(
            api_key=api_key,
            base_url=API_BASE_URL,
            model=model,
            temperature=temperature,
            request_timeout=timeout,
        )

    def _build_messages(self, messages: list[dict]):
        from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
        msg_map = {"system": SystemMessage, "human": HumanMessage, "user": HumanMessage, "ai": AIMessage}
        return [msg_map[m["role"]](content=m["content"]) for m in messages]

    @_retry
    def invoke(self, messages: list[dict]) -> str:
        logger.info(f"发送 LLM 请求, messages={len(messages)}, model={self._llm.model_name}")
        lc_messages = self._build_messages(messages)
        result = self._llm.invoke(lc_messages).content
        logger.info(f"收到 LLM 响应, 长度={len(result)}")
        return result

    def stream(self, messages: list[dict]) -> Iterator[str]:
        """流式调用，同时 yield 思考过程和内容。

        对于 deepseek-reasoner 模型，chunk 中可能包含 reasoning_content 和 content。
        本方法会将两者都 yield 出来，调用方可根据需求过滤。
        """
        logger.info(f"流式 LLM 请求, messages={len(messages)}, model={self._llm.model_name}")
        lc_messages = self._build_messages(messages)
        for chunk in self._llm.stream(lc_messages):
            delta = chunk.content if hasattr(chunk, "content") else str(chunk)
            reasoning = getattr(chunk, "reasoning_content", None)
            if reasoning:
                yield reasoning
            if delta:
                yield delta
        logger.info("流式 LLM 响应结束")
