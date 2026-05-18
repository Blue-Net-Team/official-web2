"""基于 LLM 的语义主题分段器。"""

from loguru import logger

from llm_providers.base import LLMProvider

_DEFAULT_PROMPT = """\
请分析以下文档内容，按语义主题将其分为若干段落。
每个段落应围绕一个独立的主题或观点，保持语义完整性。
不要改变原文内容，只进行分段。

只输出分段后的文本，用 "---SEGMENT---" 分隔各段。
不要输出任何其他解释、标题或标记。

文档内容：
{text}"""

# 简单估算：1 token ≈ 1.5 个中文字符 或 4 个英文字符
# 这里用保守估计，按 1 token ≈ 2 字符计算
_CHARS_PER_TOKEN = 2


class SemanticChunker:
    """基于 LLM 的语义主题分段器。

    将长文本按固定窗口提交给 LLM，由模型识别语义边界并输出分段结果。
    相邻窗口保留重叠内容，避免边界信息丢失。
    """

    def __init__(
        self,
        llm: LLMProvider,
        max_tokens_per_chunk: int = 4000,
        overlap_tokens: int = 200,
        prompt_template: str = _DEFAULT_PROMPT,
    ):
        self.llm = llm
        self.max_tokens = max_tokens_per_chunk
        self.overlap_tokens = overlap_tokens
        self.prompt_template = prompt_template
        self._max_chars = max_tokens_per_chunk * _CHARS_PER_TOKEN
        self._overlap_chars = overlap_tokens * _CHARS_PER_TOKEN

    def _split_into_windows(self, text: str) -> list[str]:
        """将长文本切分为重叠窗口。"""
        if len(text) <= self._max_chars:
            return [text]

        windows: list[str] = []
        start = 0
        while start < len(text):
            end = start + self._max_chars
            window = text[start:end]
            windows.append(window)
            # 下一个窗口起始点，保留重叠
            start = end - self._overlap_chars
            if start >= len(text):
                break
        return windows

    def _call_llm_for_segments(self, text: str) -> list[str]:
        """调用 LLM 对单个窗口进行语义分段。"""
        prompt = self.prompt_template.format(text=text)
        messages = [{"role": "user", "content": prompt}]
        try:
            raw = self.llm.invoke(messages)
        except Exception as exc:
            logger.warning(f"LLM 语义分段失败: {exc}，返回原始文本")
            return [text]

        segments = [s.strip() for s in raw.split("---SEGMENT---") if s.strip()]
        if not segments:
            return [text]
        return segments

    def split(self, text: str) -> list[str]:
        """将长文本按语义主题分段。

        Args:
            text: 原始长文本

        Returns:
            list[str]: 分段后的文本列表
        """
        if not text or not text.strip():
            return []

        windows = self._split_into_windows(text)
        all_segments: list[str] = []

        for i, window in enumerate(windows):
            logger.info(f"语义分段窗口 {i + 1}/{len(windows)}, 长度={len(window)}")
            segments = self._call_llm_for_segments(window)
            all_segments.extend(segments)

        # 简单去重：相邻且内容相同的段合并
        deduped: list[str] = []
        for seg in all_segments:
            if deduped and seg == deduped[-1]:
                continue
            deduped.append(seg)

        logger.info(f"语义分段完成: 原始 {len(all_segments)} 段, 去重后 {len(deduped)} 段")
        return deduped
