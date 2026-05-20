"""Agent 可调用的工具集。

所有工具首次导入时自动注册到 ToolRegistry，
Agent 通过 ToolRegistry 统一调用。
"""

from .base import ToolDefinition
from .chunk_search import chunk_search
from .registry import ToolRegistry
from .tag_search import tag_generate, tag_search

# --- 注册 chunk_search ---
ToolRegistry.register(ToolDefinition(
    name="chunk_search",
    description="搜索知识库中的文本分片，返回与问题最相关的内容片段",
    parameters={
        "query": {
            "type": "string",
            "description": "搜索查询，应完整表达用户问题",
        },
        "top_k": {
            "type": "integer",
            "description": "返回的结果数量，默认 5",
            "default": 5,
        },
    },
    handler=chunk_search,
))

# --- 注册 tag_search ---
ToolRegistry.register(ToolDefinition(
    name="tag_search",
    description="搜索知识标签，找到与查询相关的知识分类标签",
    parameters={
        "query": {
            "type": "string",
            "description": "搜索查询文本",
        },
        "top_k": {
            "type": "integer",
            "description": "返回的结果数量，默认 5",
            "default": 5,
        },
    },
    handler=tag_search,
))

# --- 注册 tag_generate ---
ToolRegistry.register(ToolDefinition(
    name="tag_generate",
    description="根据用户问题生成相关的知识标签，用于后续检索",
    parameters={
        "query": {
            "type": "string",
            "description": "用户查询文本",
        },
    },
    required_params=["query"],
    handler=tag_generate,
))

__all__ = [
    "ToolDefinition",
    "ToolRegistry",
    "chunk_search",
    "tag_search",
    "tag_generate",
]
