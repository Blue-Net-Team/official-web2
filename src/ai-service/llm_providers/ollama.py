"""Ollama 本地模型提供者 (Embedding + Reranker + LLM)。"""

import json
import re
import time
from typing import Iterator

from langchain_ollama import ChatOllama, OllamaEmbeddings
from loguru import logger

from .base import EmbeddingProvider, LLMProvider, LLMResponse, RerankResult, RerankerProvider

MAX_RETRIES = 3
BACKOFF_BASE = 2.0


def _retry(func):
    """简单重试装饰器：指数退避，只重试网络/超时错误。"""

    def wrapper(*args, **kwargs):
        last_exc = None
        for attempt in range(1, MAX_RETRIES + 1):
            try:
                return func(*args, **kwargs)
            except Exception as exc:
                last_exc = exc
                if attempt < MAX_RETRIES:
                    sleep = BACKOFF_BASE ** attempt
                    logger.warning(f"[{func.__qualname__}] 第 {attempt} 次失败: {exc}，{sleep:.1f}s 后重试...")
                    time.sleep(sleep)
                else:
                    logger.error(f"[{func.__qualname__}] 重试 {MAX_RETRIES} 次后仍失败: {exc}")
                    raise last_exc
        raise last_exc

    return wrapper


class OllamaEmbedding(EmbeddingProvider):
    """Ollama Embedding。"""

    def __init__(self, base_url: str = "http://localhost:11434", model: str = "bge-m3"):
        self._embeddings = OllamaEmbeddings(base_url=base_url, model=model)

    @_retry
    def embed_texts(self, texts: list[str]) -> list[list[float]]:
        logger.info(f"请求嵌入 {len(texts)} 条文本, model={self._embeddings.model}")
        result = self._embeddings.embed_documents(texts)
        logger.info(f"嵌入完成, 返回 {len(result)} 个向量")
        return result


class OllamaReranker(RerankerProvider):
    """基于 Ollama LLM 的 Reranker。

    Ollama 没有原生的 Cross-Encoder 接口，通过 LLM 对每个文档打分来实现 rerank。
    """

    def __init__(self, base_url: str = "http://localhost:11434", model: str = "qwen3:8b"):
        self._llm = ChatOllama(base_url=base_url, model=model, temperature=0)

    def rerank(self, query: str, documents: list[str], top_k: int = 10) -> list[RerankResult]:
        prompt = (
            "你是一个相关性评分专家。请根据 Query 对给定的 Document 进行相关性打分，"
            "分数范围 0.0 ~ 1.0，1.0 表示完全相关。\n\n"
            f"Query: {query}\n\n"
            "请严格按以下 JSON 格式返回结果，不要输出其他内容：\n"
            "[{\"index\": 0, \"score\": 0.95}, {\"index\": 1, \"score\": 0.3}]\n\n"
        )

        # 分批打分，每批最多 10 个文档，避免超出上下文长度
        batch_size = 10
        all_scored: list[tuple[int, float]] = []

        for batch_start in range(0, len(documents), batch_size):
            batch = documents[batch_start : batch_start + batch_size]
            doc_text = "\n\n".join(f"[{i}] {doc}" for i, doc in enumerate(batch))
            full_prompt = prompt + f"Documents:\n{doc_text}"

            logger.info(f"发送 rerank 请求, batch={batch_start}-{batch_start + len(batch) - 1}, model={self._llm.model}")
            response = self._llm.invoke(full_prompt).content
            logger.info(f"收到 rerank 响应, 长度={len(response)}")
            # 从 LLM 输出中提取 JSON
            match = re.search(r"\[.*\]", response, re.DOTALL)
            if match:
                try:
                    scores = json.loads(match.group())
                    for item in scores:
                        all_scored.append((batch_start + item["index"], float(item.get("score", 0.0))))
                except (json.JSONDecodeError, KeyError):
                    # 解析失败，该批次全部给 0 分
                    for i in range(len(batch)):
                        all_scored.append((batch_start + i, 0.0))
            else:
                # LLM 输出中无 JSON，该批次全部给 0 分
                for i in range(len(batch)):
                    all_scored.append((batch_start + i, 0.0))

        all_scored.sort(key=lambda x: x[1], reverse=True)
        return [
            RerankResult(index=idx, relevance_score=score, text=documents[idx])
            for idx, score in all_scored[:top_k]
        ]


class OllamaLLM(LLMProvider):
    """Ollama LLM。"""

    def __init__(self, base_url: str = "http://localhost:11434", model: str = "qwen3:8b", temperature: float = 0.7):
        self._llm = ChatOllama(base_url=base_url, model=model, temperature=temperature)

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
        logger.info(f"发送 LLM 请求, messages={len(messages)}, model={self._llm.model}")
        lc_messages = self._build_messages(messages)
        result = self._llm.invoke(lc_messages).content
        logger.info(f"收到 LLM 响应, 长度={len(result)}")
        return result

    @_retry
    def invoke_with_tools(self, messages: list[dict], tools: list[dict]) -> LLMResponse:
        logger.info(f"发送 LLM 请求(含工具), messages={len(messages)}, tools={len(tools)}, model={self._llm.model}")
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
        logger.info(f"流式 LLM 请求, messages={len(messages)}, model={self._llm.model}")
        lc_messages = self._build_messages(messages)
        for chunk in self._llm.stream(lc_messages):
            text = chunk.content if hasattr(chunk, "content") else str(chunk)
            if text:
                yield text
        logger.info("流式 LLM 响应结束")

    def stream_with_tools(self, messages: list[dict], tools: list[dict]) -> Iterator[LLMResponse]:
        logger.info(f"流式 LLM 请求(含工具), messages={len(messages)}, tools={len(tools)}, model={self._llm.model}")
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
