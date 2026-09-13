"""软件资源查询工具。

向 Agent 暴露两个职责分离的工具：

- :func:`software_resource_list`：按方向返回资源**索引**（名称 / 分类 / 方向），
  工具内部自动翻页取全量，供 Agent 判断"某个方向有哪些软件"。
- :func:`software_resource_lookup`：按软件名列表返回**完整资源**（含下载地址），
  工具内部完成名称容错匹配并上报未命中项。

两者均调用后端公开接口 ``/api/v1/software-resources``。后端在传入 ``direction``
时会自动并入 ``GENERAL`` 方向，因此按方向浏览不会漏掉跨方向的通用软件。
"""

from __future__ import annotations

import difflib
import re
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

# 后端 ``size`` 上限为 100，工具按此分页。
_PAGE_SIZE = 100
# 工具内部总量上限，超过则截断并显式告知 Agent。
_MAX_LIST_ITEMS = 500
# 单次按名查询可接受的名称数量上限。
_MAX_NAMES = 10
# 相似度匹配阈值。
_SIMILARITY_THRESHOLD = 0.6

_ERR_UNAVAILABLE = "软件资源服务暂不可用，请稍后重试。"
_ERR_UNKNOWN = "软件资源查询出现异常，请稍后重试。"
_ERR_NAMES = "错误：names 参数必须是非空的软件名称列表。"


# ---------------------------------------------------------------------------
# 基础辅助
# ---------------------------------------------------------------------------


def _resolve_direction(direction: str | None) -> str | None:
    """将中文方向标签或枚举值统一解析为后端枚举值。"""
    if direction is None or direction.strip() == "":
        return None
    normalized = direction.strip()
    return _DIRECTION_MAPPING.get(normalized)


def _normalize(text: str) -> str:
    """归一化名称：小写并去除空白与常见分隔符，便于容错匹配。"""
    return re.sub(r"[\s\-_./()（）\[\]+]+", "", (text or "").strip().lower())


def _match_score(query: str, resource_name: str) -> float:
    """计算查询名与资源名的匹配得分，0 表示不匹配。

    匹配顺序：归一化精确 → 双向子串 → 相似度。工具不内置任何别名数据，
    同一软件的不同写法应由调用方在同一次查询中作为多个候选传入。
    """
    query_key = _normalize(query)
    resource_key = _normalize(resource_name)
    if not query_key or not resource_key:
        return 0.0

    if query_key == resource_key:
        return 1.0

    if len(query_key) >= 2 and (query_key in resource_key or resource_key in query_key):
        return 0.9

    ratio = difflib.SequenceMatcher(None, query_key, resource_key).ratio()
    if ratio >= _SIMILARITY_THRESHOLD:
        return 0.5 + ratio * 0.3
    return 0.0


def _direction_label(direction: str | None) -> str:
    """将后端方向枚举值转为中文标签。"""
    if not direction:
        return ""
    return _DIRECTION_LABELS.get(direction, direction)


def _format_index_item(resource: dict) -> str:
    """将单个资源格式化为索引行（不含描述与下载地址）。"""
    name = resource.get("name", "未知资源")
    label = _direction_label(resource.get("direction", ""))
    category = resource.get("category", "")
    suffix = f"（分类：{category}）" if category else ""
    prefix = f"[{label}] " if label else ""
    return f"- {prefix}{name}{suffix}"


def _format_resource(resource: dict) -> str:
    """将单个资源格式化为完整详情（含下载地址）。"""
    name = resource.get("name", "未知资源")
    label = _direction_label(resource.get("direction", ""))
    category = resource.get("category", "")
    description = resource.get("description", "")
    external_url = resource.get("externalUrl", "")

    lines = [f"- [{label}] {name}"] if label else [f"- {name}"]
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


# ---------------------------------------------------------------------------
# 后端拉取
# ---------------------------------------------------------------------------


def _fetch_all(
    direction: str | None = None,
    keyword: str | None = None,
) -> tuple[list[dict], int, str | None]:
    """拉取资源列表，内部自动翻页。

    Args:
        direction: 可选方向，支持中文标签或后端枚举值。
        keyword: 可选关键字。

    Returns:
        ``(items, total_elements, error)``。``error`` 非 None 表示中途失败；
        若 ``items`` 非空说明已拿到部分结果。
    """
    base_url = settings.BACKEND_API_URL.rstrip("/")
    resolved_direction = _resolve_direction(direction)

    items: list[dict] = []
    total = 0
    page = 0

    _log.info(
        f"查询软件资源: direction={direction}, resolved={resolved_direction}, keyword={keyword}"
    )

    try:
        with _create_client() as client:
            while True:
                params: dict[str, str | int] = {"page": page, "size": _PAGE_SIZE}
                if resolved_direction:
                    params["direction"] = resolved_direction
                if keyword and keyword.strip():
                    params["keyword"] = keyword.strip()

                url = f"{base_url}{_SOFTWARE_RESOURCE_ENDPOINT}?{urlencode(params)}"
                response = client.get(url)
                response.raise_for_status()
                payload = response.json()

                data = payload.get("data", {}) if isinstance(payload, dict) else {}
                if not isinstance(data, dict):
                    break

                content = data.get("content", [])
                total = data.get("totalElements", 0) or 0
                if not isinstance(content, list) or not content:
                    break

                items.extend([item for item in content if isinstance(item, dict)])
                page += 1

                if len(items) >= total or len(items) >= _MAX_LIST_ITEMS:
                    break
    except httpx.TimeoutException:
        _log.warning("查询软件资源超时")
        return items, total, _ERR_UNAVAILABLE
    except httpx.HTTPError as exc:
        _log.warning(f"查询软件资源失败: {exc}")
        return items, total, _ERR_UNAVAILABLE
    except Exception as exc:
        _log.error(f"查询软件资源时发生未知错误: {exc}")
        return items, total, _ERR_UNKNOWN

    if len(items) > _MAX_LIST_ITEMS:
        items = items[:_MAX_LIST_ITEMS]

    return items, total, None


# ---------------------------------------------------------------------------
# 工具：按方向浏览索引
# ---------------------------------------------------------------------------


def software_resource_list(direction: str | None = None) -> str:
    """按方向列出软件资源索引（名称 / 分类 / 方向），不含描述与下载地址。

    省略 ``direction`` 时可一次获取全部方向的资源。

    Args:
        direction: 可选方向，支持中文标签（如"电控方向"）或后端枚举值。

    Returns:
        格式化的资源索引文本；无结果或出错时返回对应提示。
    """
    items, total, error = _fetch_all(direction)

    if error and not items:
        return error
    if not items:
        return "未找到匹配的软件资源。"

    lines: list[str] = []
    if error:
        lines.append(f"（注意：{error}）以下为已获取的部分结果，结果不完整：")
    else:
        lines.append(f"共 {total} 个软件资源：")

    for resource in items:
        lines.append(_format_index_item(resource))

    if total > len(items):
        lines.append(
            f"【注意】结果已截断，仅展示 {len(items)} / {total} 条"
            f"（单次上限 {_MAX_LIST_ITEMS} 条），建议改用具体软件名查询。"
        )

    return "\n".join(lines)


# ---------------------------------------------------------------------------
# 工具：按软件名查询完整资源
# ---------------------------------------------------------------------------


def software_resource_lookup(names: list[str], direction: str | None = None) -> str:
    """按软件名列表查询完整资源（含下载地址），支持名称容错匹配。

    Args:
        names: 软件名称列表，最多 ``_MAX_NAMES`` 个。
        direction: 可选方向，用于缩小候选集；省略时在全库范围内匹配。

    Returns:
        命中的资源详情与未命中的名称清单；出错时返回对应提示。
    """
    if not isinstance(names, (list, tuple)) or not names:
        return _ERR_NAMES

    cleaned = [name.strip() for name in names if isinstance(name, str) and name.strip()]
    if not cleaned:
        return _ERR_NAMES

    ignored: list[str] = []
    if len(cleaned) > _MAX_NAMES:
        ignored = cleaned[_MAX_NAMES:]
        cleaned = cleaned[:_MAX_NAMES]

    items, _total, error = _fetch_all(direction)
    if error and not items:
        return error

    matched: list[tuple[str, dict]] = []
    unmatched: list[str] = []
    used: set[int] = set()

    for name in cleaned:
        best: dict | None = None
        best_score = 0.0
        for resource in items:
            if id(resource) in used:
                continue
            score = _match_score(name, resource.get("name", ""))
            if score > best_score:
                best_score = score
                best = resource
        if best is None:
            unmatched.append(name)
        else:
            used.add(id(best))
            matched.append((name, best))

    _log.info(
        f"软件名查询完成: 命中 {len(matched)}, 未命中 {len(unmatched)}, 忽略 {len(ignored)}"
    )

    sections: list[str] = []
    if matched:
        lines = [f"找到 {len(matched)} 个软件资源："]
        for _name, resource in matched:
            lines.append(_format_resource(resource))
        sections.append("\n".join(lines))

    if unmatched:
        if matched:
            sections.append("以下软件在资源库中未找到：" + "、".join(unmatched))
        else:
            sections.append("资源库中不存在以下软件：" + "、".join(unmatched))

    if ignored:
        sections.append(
            f"（注意：名称数量超过单次上限 {_MAX_NAMES} 个，已忽略：{'、'.join(ignored)}）"
        )

    if error:
        sections.append(f"（注意：{error}）结果可能不完整。")

    if not sections:
        return "未找到匹配的软件资源。"

    return "\n\n".join(sections)
