"""标签同义词归一化模块。

解决同一概念在不同语境下产生多个等价标签的问题，例如：
- "电控方向" ↔ "嵌入式编程" ↔ "电控"
- "结构方向" ↔ "机械设计" ↔ "机器人结构设计"

归一化策略：
1. 入库前归一化 —— 新标签映射到标准标签（canonical tag）
2. 查询时扩展 —— 搜索标准标签时同时匹配其同义词
"""

from __future__ import annotations

# ============================================
# 同义词映射表：标准标签 → 同义词列表
# ============================================
# 规则：
#   - 键（key）是标准标签，即入库后实际存储的标签名
#   - 值（value）是该标准标签的同义词列表
#   - 同义词匹配不区分大小写，但替换时统一使用标准标签
#
# 维护建议：
#   - 标准标签应尽量使用团队内部统一用语
#   - 同义词列表包含常见变体、简称、近义词
# ============================================

_TAG_SYNONYMS: dict[str, list[str]] = {
    # 方向类
    "电控方向": [
        "电控",
        "嵌入式",
        "嵌入式编程",
        "嵌入式开发",
        "单片机",
        "STM32",
        "电路设计",
        "电机控制",
    ],
    "结构方向": [
        "结构",
        "机械设计",
        "机器人结构设计",
        "三维建模",
        "SolidWorks",
        "机械结构",
        "装配体",
    ],
    "计算机视觉": [
        "视觉",
        "视觉方向",
        "图像处理",
        "目标检测",
        "OpenCV",
        "机器视觉",
    ],
    "AI算法": [
        "人工智能",
        "深度学习",
        "机器学习",
        "神经网络",
        "模型训练",
    ],
    "路径规划": [
        "导航",
        "运动规划",
        "轨迹规划",
        "避障",
    ],
    # 团队/组织类
    "蓝网科技": [
        "蓝网",
        "蓝网团队",
        "蓝网科技创新团队",
        "BlueNet",
    ],
    "团队招新": [
        "招新",
        "纳新",
        " recruit",
        "加入我们",
        "报名",
    ],
    # 活动类
    "大学生竞赛": [
        "竞赛",
        "比赛",
        "赛事",
        "参赛",
    ],
    "参赛方向": [
        "赛项",
        "比赛项目",
        "竞赛方向",
    ],
    # 资源类
    "学习资源": [
        "教程",
        "资料",
        "学习资料",
        "参考",
    ],
    "学长指导": [
        "学长",
        "学姐",
        "前辈",
        " mentor",
        "指导",
    ],
    # 设备类
    "电脑配置": [
        "电脑",
        "笔记本",
        "游戏本",
        "硬件",
        "配置",
    ],
    "硬件要求": [
        "设备",
        "硬件需求",
        "设备要求",
    ],
    # 管理类
    "团队管理": [
        "管理",
        "制度",
        "规定",
        "退队",
    ],
    "时间安排": [
        "时间",
        "日程",
        "作息",
    ],
    "时间协调": [
        "协调",
        "调度",
        "冲突处理",
    ],
}

# 反向映射：同义词 → 标准标签（用于快速查找）
_SYNONYM_TO_CANONICAL: dict[str, str] = {}


def _build_reverse_map() -> None:
    """构建反向映射表，同义词（小写）→ 标准标签。"""
    global _SYNONYM_TO_CANONICAL
    _SYNONYM_TO_CANONICAL = {}
    for canonical, synonyms in _TAG_SYNONYMS.items():
        # 标准标签自身也映射到自己
        _SYNONYM_TO_CANONICAL[canonical.lower()] = canonical
        for synonym in synonyms:
            _SYNONYM_TO_CANONICAL[synonym.lower()] = canonical


def normalize_tag(tag: str) -> str:
    """将标签归一化为标准标签。

    如果标签匹配某个同义词组，返回对应的标准标签；
    否则原样返回。

    Args:
        tag: 原始标签名称。

    Returns:
        归一化后的标准标签。

    Examples:
        >>> normalize_tag("嵌入式")
        '电控方向'
        >>> normalize_tag("机械设计")
        '结构方向'
        >>> normalize_tag("蓝网")
        '蓝网科技'
        >>> normalize_tag("不存在的标签")
        '不存在的标签'
    """
    if not _SYNONYM_TO_CANONICAL:
        _build_reverse_map()

    return _SYNONYM_TO_CANONICAL.get(tag.strip().lower(), tag.strip())


def normalize_tags(tags: list[str]) -> list[str]:
    """批量归一化标签列表，自动去重。

    Args:
        tags: 原始标签列表。

    Returns:
        归一化后的唯一标签列表，保持原始顺序。
    """
    seen: set[str] = set()
    result: list[str] = []
    for tag in tags:
        canonical = normalize_tag(tag)
        if canonical not in seen:
            seen.add(canonical)
            result.append(canonical)
    return result


def expand_tag(tag: str) -> list[str]:
    """将标准标签扩展为其同义词列表（含自身）。

    用于搜索阶段：查询标准标签时，同时匹配其所有同义词。

    Args:
        tag: 标准标签或原始标签。

    Returns:
        该标签对应的所有等价标签列表（含自身）。

    Examples:
        >>> expand_tag("电控方向")
        ['电控方向', '电控', '嵌入式', '嵌入式编程', ...]
        >>> expand_tag("嵌入式")
        ['电控方向', '电控', '嵌入式', '嵌入式编程', ...]
    """
    if not _SYNONYM_TO_CANONICAL:
        _build_reverse_map()

    canonical = normalize_tag(tag)
    synonyms = _TAG_SYNONYMS.get(canonical, [])
    return [canonical] + synonyms


def expand_tags(tags: list[str]) -> list[str]:
    """批量扩展标签列表，返回所有等价标签（去重）。

    Args:
        tags: 标签列表。

    Returns:
        扩展后的唯一标签列表。
    """
    seen: set[str] = set()
    result: list[str] = []
    for tag in tags:
        for expanded in expand_tag(tag):
            if expanded not in seen:
                seen.add(expanded)
                result.append(expanded)
    return result


def get_canonical_tags() -> list[str]:
    """获取所有标准标签列表。"""
    return list(_TAG_SYNONYMS.keys())


# 模块加载时自动构建反向映射
_build_reverse_map()
