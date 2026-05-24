"""硅基流动 API 提供者 (Embedding + Reranker + LLM)。"""

import time
from typing import Iterator

import httpx
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from loguru import logger

from .base import EmbeddingProvider, LLMProvider, LLMResponse, RerankResult, RerankerProvider

API_BASE_URL = "https://api.siliconflow.cn/v1"

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
                # 不重试明确的 4xx 客户端错误（除 429 限流外）
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


class SiliconFlowEmbedding(EmbeddingProvider):
    """硅基流动 Embedding (OpenAI 兼容接口)。"""

    def __init__(self, api_key: str, model: str = "BAAI/bge-m3"):
        self._embeddings = OpenAIEmbeddings(api_key=api_key, base_url=API_BASE_URL, model=model)

    @_retry
    def embed_texts(self, texts: list[str]) -> list[list[float]]:
        logger.info(f"请求嵌入 {len(texts)} 条文本, model={self._embeddings.model}")
        result = self._embeddings.embed_documents(texts)
        logger.info(f"嵌入完成, 返回 {len(result)} 个向量")
        return result


class SiliconFlowReranker(RerankerProvider):
    """硅基流动 Reranker。

    硅基流动的 /rerank 接口不是 OpenAI 标准接口，用 httpx 直接调用。
    """

    def __init__(self, api_key: str, model: str = "BAAI/bge-reranker-v2-m3"):
        self._model = model
        self._client = httpx.Client(base_url=API_BASE_URL, headers={"Authorization": f"Bearer {api_key}"}, timeout=30)

    @_retry
    def rerank(self, query: str, documents: list[str], top_k: int = 10) -> list[RerankResult]:
        logger.info(f"发送 rerank 请求, query_len={len(query)}, docs={len(documents)}, model={self._model}")
        resp = self._client.post(
            "/rerank",
            json={"model": self._model, "query": query, "documents": documents, "top_n": top_k},
        )
        resp.raise_for_status()
        data = resp.json()
        logger.info(f"收到 rerank 响应, results={len(data.get('results', []))}")
        return [
            RerankResult(index=item["index"], relevance_score=item["relevance_score"], text=documents[item["index"]])
            for item in data["results"]
        ]


class SiliconFlowLLM(LLMProvider):
    """硅基流动 LLM (OpenAI 兼容接口)。"""

    def __init__(self, api_key: str, model: str = "deepseek-ai/DeepSeek-V3", temperature: float = 0.7, timeout: float = 60):
        self._llm = ChatOpenAI(api_key=api_key, base_url=API_BASE_URL, model=model, temperature=temperature, request_timeout=timeout)

    def _build_messages(self, messages: list[dict]):
        from langchain_core.messages import AIMessage, HumanMessage, SystemMessage, ToolMessage

        def _to_lc_message(m: dict):
            role = m["role"]
            if role == "system":
                return SystemMessage(content=m["content"])
            if role in ("human", "user"):
                return HumanMessage(content=m["content"])
            if role in ("ai", "assistant"):
                tool_calls = m.get("tool_calls")
                if tool_calls:
                    return AIMessage(content=m["content"], tool_calls=tool_calls)
                return AIMessage(content=m["content"])
            if role == "tool":
                return ToolMessage(content=m["content"], tool_call_id=m.get("tool_call_id", ""))
            return HumanMessage(content=m["content"])

        return [_to_lc_message(m) for m in messages]

    @_retry
    def invoke(self, messages: list[dict]) -> str:
        logger.info(f"发送 LLM 请求, messages={len(messages)}, model={self._llm.model_name}")
        lc_messages = self._build_messages(messages)
        result = self._llm.invoke(lc_messages).content
        logger.info(f"收到 LLM 响应, 长度={len(result)}")
        return result

    @_retry
    def invoke_with_tools(self, messages: list[dict], tools: list[dict]) -> LLMResponse:
        logger.info(f"发送 LLM 请求(含工具), messages={len(messages)}, tools={len(tools)}, model={self._llm.model_name}")
        lc_messages = self._build_messages(messages)
        llm_with_tools = self._llm.bind_tools(tools)
        result = llm_with_tools.invoke(lc_messages)

        tool_calls = []
        if hasattr(result, "tool_calls") and result.tool_calls:
            tool_calls = result.tool_calls

        content = result.content or ""
        logger.info(f"收到 LLM 响应, content_len={len(content)}, tool_calls={len(tool_calls)}")
        return LLMResponse(content=content, tool_calls=tool_calls)

    def stream(self, messages: list[dict]) -> Iterator[str]:
        logger.info(f"流式 LLM 请求, messages={len(messages)}, model={self._llm.model_name}")
        lc_messages = self._build_messages(messages)
        for chunk in self._llm.stream(lc_messages):
            text = chunk.content if hasattr(chunk, "content") else str(chunk)
            if text:
                yield text
        logger.info("流式 LLM 响应结束")

    def stream_with_tools(self, messages: list[dict], tools: list[dict]) -> Iterator[LLMResponse]:
        logger.info(f"流式 LLM 请求(含工具), messages={len(messages)}, tools={len(tools)}, model={self._llm.model_name}")
        lc_messages = self._build_messages(messages)
        llm_with_tools = self._llm.bind_tools(tools)

        full_content = ""
        tool_calls_buffer = []

        for chunk in llm_with_tools.stream(lc_messages):
            delta = chunk.content if hasattr(chunk, "content") else ""
            if delta:
                full_content += delta

            if hasattr(chunk, "tool_calls") and chunk.tool_calls:
                for tc in chunk.tool_calls:
                    tool_calls_buffer.append(tc)

        yield LLMResponse(content=full_content, tool_calls=tool_calls_buffer)
        logger.info(f"流式 LLM 响应结束, content_len={len(full_content)}, tool_calls={len(tool_calls_buffer)}")
