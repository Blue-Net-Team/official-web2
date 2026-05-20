from __future__ import annotations

from typing import Any

from loguru import logger

from .base import ToolDefinition

_log = logger.bind(module="ToolRegistry")


class ToolRegistry:
    _tools: dict[str, ToolDefinition] = {}

    @classmethod
    def register(cls, tool: ToolDefinition) -> None:
        cls._tools[tool.name] = tool
        _log.debug(f"注册工具: {tool.name}")

    @classmethod
    def get(cls, name: str) -> ToolDefinition | None:
        return cls._tools.get(name)

    @classmethod
    def list_tools(cls) -> list[ToolDefinition]:
        return list(cls._tools.values())

    @classmethod
    def execute(cls, name: str, **kwargs) -> str:
        tool = cls._tools.get(name)
        if tool is None:
            raise ValueError(f"未知工具: {name}，可用工具: {list(cls._tools)}")
        _log.info(f"执行工具: {name}, 参数: {kwargs}")
        result = tool.handler(**kwargs)
        return cls._format_result(name, result)

    @classmethod
    def get_function_calling_specs(cls) -> list[dict]:
        specs = []
        for tool in cls._tools.values():
            spec = {
                "type": "function",
                "function": {
                    "name": tool.name,
                    "description": tool.description,
                    "parameters": {
                        "type": "object",
                        "properties": tool.parameters,
                        "required": tool.required_params,
                    },
                },
            }
            specs.append(spec)
        return specs

    @classmethod
    def _format_result(cls, name: str, result: Any) -> str:
        from .chunk_search import RerankResult

        if isinstance(result, list) and len(result) > 0:
            if isinstance(result[0], RerankResult):
                return cls._format_rerank_results(result)
            if isinstance(result[0], str):
                formatted = "\n".join(f"- {item}" for item in result)
                return f"共 {len(result)} 项:\n{formatted}"

        return str(result)

    @classmethod
    def _format_rerank_results(cls, results: list) -> str:
        lines = [f"共 {len(results)} 条结果:\n"]
        for i, r in enumerate(results, 1):
            text_preview = r.text[:200] + "..." if len(r.text) > 200 else r.text
            lines.append(f"[{i}] 相关度: {r.relevance_score:.4f}")
            lines.append(f"    内容: {text_preview}")
        return "\n".join(lines)

    @classmethod
    def reset(cls) -> None:
        cls._tools.clear()
        _log.debug("工具注册表已重置")
