"""软件资源查询工具。

调用后端公开接口获取已启用的软件资源列表，供 Agent 在对话中回答软件下载相关问题。
"""

from __future__ import annotations

from urllib.parse import urlencode

import httpx
from loguru import logger

from setting import settings

_log = logger.bind(module="software_resource_search")

_SOFTWARE_RESOURCE_ENDPOINT = "/api/v1/software-resources"

_DIRECTION_MAPPING: dict[str, str] = {
    "COMPUTER_VISION": "COMPUTER_VISION",
    "计算机视觉": "COMPUTER_VISION",
    "视觉": "COMPUTER_VISION",
    "视觉方向": "COMPUTER_VISION",
    "STRUCTURAL_DESIGN": "STRUCTURAL_DESIGN",
    "结构设计": "STRUCTURAL_DESIGN",
    "结构": "STRUCTURAL_DESIGN",
    "结构方向": "STRUCTURAL_DESIGN",
    "EMBEDDED": "EMBEDDED",
    "嵌入式开发": "EMBEDDED",
    "嵌入式": "EMBEDDED",
    "电控": "EMBEDDED",
    "电控方向": "EMBEDDED",
    "GENERAL": "GENERAL",
    "通用": "GENERAL",
    "通用方向": "GENERAL",
}

_DIRECTION_LABELS: dict[str, str] = {
    "COMPUTER_VISION": "计算机视觉",
    "STRUCTURAL_DESIGN": "结构设计",
    "EMBEDDED": "嵌入式开发",
    "GENERAL": "通用",
}

_DEFAULT_TIMEOUT = 10.0
_DEFAULT_PAGE_SIZE = 20


def _resolve_direction(direction: str | None) -> str | None:
    """将中文方向标签或枚举值统一解析为后端枚举值。"""
    if direction is None or direction.strip() == "":
        return None
    normalized = direction.strip()
    return _DIRECTION_MAPPING.get(normalized)


def _format_resource(resource: dict) -> str:
    """将单个资源格式化为易读文本。"""
    name = resource.get("name", "未知资源")
    direction = resource.get("direction", "")
    direction_label = _DIRECTION_LABELS.get(direction, direction)
    category = resource.get("category", "")
    description = resource.get("description", "")
    external_url = resource.get("externalUrl", "")

    lines = [f"- [{direction_label}] {name}"]
    if category:
        lines.append(f"  分类：{category}")
    if description:
        lines.append(f"  描述：{description}")
    if external_url:
        lines.append(f"  下载地址：{external_url}")
    return "\n".join(lines)


def _create_client():
    """创建 HTTP 客户端，便于测试替换。"""
    return httpx.Client(timeout=_DEFAULT_TIMEOUT)


def software_resource_search(query: str, direction: str | None = None) -> str:
    """搜索后端软件资源库并返回格式化结果。

    Args:
        query: 搜索关键词，匹配资源名称、分类、描述。
        direction: 可选方向，支持中文标签（如"视觉方向"）或后端枚举值。

    Returns:
        格式化的资源列表文本；无结果或出错时返回对应提示。
    """
    base_url = settings.BACKEND_API_URL.rstrip("/")
    params: dict[str, str | int] = {"page": 0, "size": _DEFAULT_PAGE_SIZE}
    if query and query.strip():
        params["keyword"] = query.strip()

    resolved_direction = _resolve_direction(direction)
    if resolved_direction:
        params["direction"] = resolved_direction

    url = f"{base_url}{_SOFTWARE_RESOURCE_ENDPOINT}?{urlencode(params)}"
    _log.info(f"查询软件资源: query={query}, direction={direction}, resolved={resolved_direction}")

    try:
        with _create_client() as client:
            response = client.get(url)
            response.raise_for_status()
            payload = response.json()
    except httpx.TimeoutException:
        _log.warning("查询软件资源超时")
        return "软件资源服务暂不可用，请稍后重试。"
    except httpx.HTTPError as exc:
        _log.warning(f"查询软件资源失败: {exc}")
        return "软件资源服务暂不可用，请稍后重试。"
    except Exception as exc:
        _log.error(f"查询软件资源时发生未知错误: {exc}")
        return "软件资源查询出现异常，请稍后重试。"

    data = payload.get("data", {}) if isinstance(payload.get("data"), dict) else {}
    content = data.get("content", []) if isinstance(data, dict) else []
    total_elements = data.get("totalElements", 0) if isinstance(data, dict) else 0

    if not isinstance(content, list) or not content:
        return "未找到匹配的软件资源。"

    lines = [f"找到 {total_elements} 个软件资源："]
    for resource in content:
        if isinstance(resource, dict):
            lines.append(_format_resource(resource))

    return "\n\n".join(lines)
