"""Agent 可调用的工具集。

所有工具首次导入时自动注册到 ToolRegistry，
Agent 通过 ToolRegistry 统一调用。
"""

from .base import TagSearchResult, ToolDefinition
from .chunk_search import chunk_search
from .chunk_search_by_tags import chunk_search_by_tags
from .registry import ToolRegistry
from .software_resource_search import software_resource_list, software_resource_lookup
from .software_resource_search import (
    software_resource_list as _software_resource_list_handler,
)
from .software_resource_search import (
    software_resource_lookup as _software_resource_lookup_handler,
)
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

# --- 注册 software_resource_list ---
ToolRegistry.register(ToolDefinition(
    name="software_resource_list",
    description=(
        "按方向列出蓝网软件资源索引（仅名称、分类、方向，不含下载地址）。"
        "当用户询问某个方向需要什么软件、有哪些软件可用时调用。"
        "省略 direction 可一次获取全部方向的资源；如需对比多个方向，"
        "不要分多次调用，直接省略 direction。"
    ),
    parameters={
        "direction": {
            "type": "string",
            "description": (
                "可选方向：计算机视觉/视觉方向/COMPUTER_VISION、"
                "结构设计/结构方向/STRUCTURAL_DESIGN、"
                "嵌入式开发/电控方向/EMBEDDED、通用/GENERAL；省略则返回全部方向"
            ),
        },
    },
    required_params=[],
    handler=_software_resource_list_handler,
))

# --- 注册 software_resource_lookup ---
ToolRegistry.register(ToolDefinition(
    name="software_resource_lookup",
    description=(
        "按软件名称列表查询蓝网软件资源库，返回资源详情与下载地址。"
        "当用户点名了具体软件，或已从知识库获取到推荐软件名时调用。"
        "支持一次传入多个名称，请勿为每个软件分别调用。"
    ),
    parameters={
        "names": {
            "type": "array",
            "items": {"type": "string"},
            "description": "软件名称列表，如 ['Keil uVision5', '立创EDA']，最多 10 个",
        },
        "direction": {
            "type": "string",
            "description": (
                "可选方向，用于缩小候选集：计算机视觉/视觉方向/COMPUTER_VISION、"
                "结构设计/结构方向/STRUCTURAL_DESIGN、"
                "嵌入式开发/电控方向/EMBEDDED、通用/GENERAL"
            ),
        },
    },
    required_params=["names"],
    handler=_software_resource_lookup_handler,
))

__all__ = [
    "TagSearchResult",
    "ToolDefinition",
    "ToolRegistry",
    "chunk_search",
    "chunk_search_by_tags",
    "software_resource_list",
    "software_resource_lookup",
    "tag_search",
    "tag_search_detailed",
    "tag_generate",
]
