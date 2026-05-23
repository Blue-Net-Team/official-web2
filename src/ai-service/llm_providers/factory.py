"""模型提供商工厂 — Embedding / LLM / Reranker 三类工厂。

每个工厂负责根据提供商名称创建对应的模型实例，优先从 settings（环境变量）读取默认值。
调用时可显式传参覆盖 settings 中的配置。
"""

from setting import settings

from .base import EmbeddingProvider, LLMProvider, RerankerProvider

# ---------------------------------------------------------------------------
# 默认模型映射（settings 中未配置时的后备默认值）
# ---------------------------------------------------------------------------

_DEFAULT_EMBEDDING_MODEL: dict[str, str] = {
    "siliconflow": "BAAI/bge-m3",
    "ollama": "bge-m3",
}

_DEFAULT_LLM_MODEL: dict[str, str] = {
    "siliconflow": "deepseek-ai/DeepSeek-V3",
    "ollama": "qwen3:8b",
    "deepseek": "deepseek-reasoner",
}

_DEFAULT_RERANKER_MODEL: dict[str, str] = {
    "siliconflow": "BAAI/bge-reranker-v2-m3",
    "ollama": "qwen3:8b",
}


def _model_or_default(provider: str, model: str, default_map: dict[str, str]) -> str:
    """优先用显式传入的 model，其次用 settings 中的模型名（如已配置），最后用硬编码默认值。"""
    if model:
        return model
    return default_map.get(provider, "")


def _api_key_for(provider: str) -> str:
    """根据提供商返回对应的 API Key。"""
    if provider == "siliconflow":
        return settings.SILICONFLOW_API_KEY
    if provider == "deepseek":
        return settings.DEEPSEEK_API_KEY
    return ""


def _base_url_for(provider: str, base_url: str = "") -> str:
    """根据提供商返回对应的 base_url。"""
    if base_url:
        return base_url
    if provider == "ollama":
        return settings.OLLAMA_BASE_URL
    return ""


# ---------------------------------------------------------------------------
# EmbeddingFactory
# ---------------------------------------------------------------------------


class EmbeddingFactory:
    """嵌入模型工厂。

    支持的提供商：
    - siliconflow — SiliconFlow API，需要 api_key
    - ollama — Ollama 本地服务，需要 base_url

    settings 环境变量（前缀 TBD_RAG_）：
      EMBEDDING_PROVIDER, SILICONFLOW_API_KEY, OLLAMA_BASE_URL, EMBEDDING_MODEL
    """

    @staticmethod
    def create(
        provider: str = "",
        api_key: str = "",
        model: str = "",
        base_url: str = "",
    ) -> EmbeddingProvider:
        provider = provider or settings.EMBEDDING_PROVIDER
        api_key = api_key or _api_key_for(provider)
        base_url = _base_url_for(provider, base_url)
        model = _model_or_default(provider, model or settings.EMBEDDING_MODEL, _DEFAULT_EMBEDDING_MODEL)

        if provider == "siliconflow":
            from .siliconflow import SiliconFlowEmbedding

            return SiliconFlowEmbedding(api_key=api_key, model=model)

        if provider == "ollama":
            from .ollama import OllamaEmbedding

            return OllamaEmbedding(base_url=base_url, model=model)

        raise ValueError(f"不支持的嵌入提供商: {provider}，支持: {list(_DEFAULT_EMBEDDING_MODEL)}")


# ---------------------------------------------------------------------------
# LLMFactory
# ---------------------------------------------------------------------------


class LLMFactory:
    """LLM 工厂。

    支持的提供商：
    - siliconflow — SiliconFlow API，需要 api_key
    - deepseek — DeepSeek 官方 API，需要 api_key
    - ollama — Ollama 本地服务，需要 base_url

    settings 环境变量（前缀 TBD_RAG_）：
      LLM_PROVIDER, SILICONFLOW_API_KEY, DEEPSEEK_API_KEY, OLLAMA_BASE_URL,
      LLM_MODEL, LLM_TEMPERATURE, LLM_TIMEOUT
    """

    @staticmethod
    def create(
        provider: str = "",
        api_key: str = "",
        model: str = "",
        base_url: str = "",
        temperature: float | None = None,
        timeout: int | None = None,
    ) -> LLMProvider:
        provider = provider or settings.LLM_PROVIDER
        api_key = api_key or _api_key_for(provider)
        base_url = _base_url_for(provider, base_url)
        temperature = temperature if temperature is not None else settings.LLM_TEMPERATURE
        timeout = timeout if timeout is not None else settings.LLM_TIMEOUT
        model = _model_or_default(provider, model or settings.LLM_MODEL, _DEFAULT_LLM_MODEL)

        if provider in ("siliconflow", "deepseek"):
            if not api_key:
                raise ValueError(
                    f"{provider} LLM 需要 api_key，"
                    f"请在环境变量 TBD_RAG_SILICONFLOW_API_KEY 或 TBD_RAG_DEEPSEEK_API_KEY 中配置"
                )
            if provider == "siliconflow":
                from .siliconflow import SiliconFlowLLM

                llm = SiliconFlowLLM(api_key=api_key, model=model, temperature=temperature, timeout=timeout)
            else:
                from .deepseek import DeepSeekLLM

                llm = DeepSeekLLM(api_key=api_key, model=model, temperature=temperature, timeout=timeout)
            if base_url:
                llm._llm.base_url = base_url
            return llm

        if provider == "ollama":
            from .ollama import OllamaLLM

            return OllamaLLM(base_url=base_url, model=model, temperature=temperature)

        raise ValueError(f"不支持的 LLM 提供商: {provider}，支持: {list(_DEFAULT_LLM_MODEL)}")


# ---------------------------------------------------------------------------
# RerankerFactory
# ---------------------------------------------------------------------------


class RerankerFactory:
    """Reranker 工厂。

    支持的提供商：
    - siliconflow — SiliconFlow API，需要 api_key
    - ollama — Ollama 本地服务，需要 base_url

    settings 环境变量（前缀 TBD_RAG_）：
      RERANKER_PROVIDER, SILICONFLOW_API_KEY, OLLAMA_BASE_URL, RERANKER_MODEL
    """

    @staticmethod
    def create(
        provider: str = "",
        api_key: str = "",
        model: str = "",
        base_url: str = "",
    ) -> RerankerProvider:
        provider = provider or settings.RERANKER_PROVIDER
        api_key = api_key or _api_key_for(provider)
        base_url = _base_url_for(provider, base_url)
        model = _model_or_default(provider, model or settings.RERANKER_MODEL, _DEFAULT_RERANKER_MODEL)

        if provider == "siliconflow":
            from .siliconflow import SiliconFlowReranker

            return SiliconFlowReranker(api_key=api_key, model=model)

        if provider == "ollama":
            from .ollama import OllamaReranker

            return OllamaReranker(base_url=base_url, model=model)

        raise ValueError(f"不支持的 Reranker 提供商: {provider}，支持: {list(_DEFAULT_RERANKER_MODEL)}")
