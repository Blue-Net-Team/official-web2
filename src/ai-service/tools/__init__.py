"""Agent 可调用的工具集。

所有工具首次导入时自动注册到 ToolRegistry，
Agent 通过 ToolRegistry 统一调用。
"""

from .base import TagSearchResult, ToolDefinition
from .chunk_search import chunk_search
from .chunk_search_by_tags import chunk_search_by_tags
from .registry import ToolRegistry
from .software_resource_search import software_resource_search as _software_resource_search_handler
from .tag_search import tag_generate, tag_search
from .tag_search_detailed import tag_search_detailed

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

# --- 注册 tag_search_detailed ---
ToolRegistry.register(ToolDefinition(
    name="tag_search_detailed",
    description="搜索标签并返回详细结果，包含相关度分数和关联文档数，用于 Agent 判断标签质量",
    parameters={
        "query": {
            "type": "string",
            "description": "搜索查询文本，应完整表达用户问题",
        },
        "top_k": {
            "type": "integer",
            "description": "返回的结果数量，默认 10",
            "default": 10,
        },
    },
    handler=tag_search_detailed,
))

# --- 注册 chunk_search_by_tags ---
ToolRegistry.register(ToolDefinition(
    name="chunk_search_by_tags",
    description="按标签过滤搜索文本分片，返回重排序后的结果及 score 分布（最高/最低/平均分），用于判断搜索结果质量",
    parameters={
        "query": {
            "type": "string",
            "description": "搜索查询文本",
        },
        "tags": {
            "type": "string",
            "description": "逗号分隔的标签列表，如 'LSTM, 梯度消失'",
        },
        "top_k": {
            "type": "integer",
            "description": "返回的结果数量，默认 10",
            "default": 10,
        },
    },
    required_params=["query", "tags"],
    handler=chunk_search_by_tags,
))

# --- 注册 software_resource_search ---
ToolRegistry.register(ToolDefinition(
    name="software_resource_search",
    description="查询蓝网软件资源库，获取方向相关的软件下载地址。当用户询问软件下载、安装工具、某个方向需要什么软件时调用",
    parameters={
        "query": {
            "type": "string",
            "description": "搜索关键词，如软件名称、分类或描述中的关键词",
        },
        "direction": {
            "type": "string",
            "description": "可选方向：计算机视觉/视觉方向/COMPUTER_VISION、结构设计/结构方向/STRUCTURAL_DESIGN、嵌入式开发/电控方向/EMBEDDED、通用/GENERAL",
        },
    },
    required_params=["query"],
    handler=_software_resource_search_handler,
))

__all__ = [
    "TagSearchResult",
    "ToolDefinition",
    "ToolRegistry",
    "chunk_search",
    "chunk_search_by_tags",
    "tag_search",
    "tag_search_detailed",
    "tag_generate",
]
