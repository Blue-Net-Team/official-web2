from __future__ import annotations

import re


def parse_mib(value: object) -> float | None:
    """将 Docker 的 B/KiB/MiB/GiB 等内存文本统一转换为 MiB；非法输入返回 None。"""
    if not isinstance(value, str):
        return None
    match = re.fullmatch(r"\s*([\d.]+)\s*([kmgt]?i?b)\s*", value, re.IGNORECASE)
    if not match:
        return None
    amount = float(match.group(1))
    unit = match.group(2).lower()
    factors = {
        "b": 1 / (1024 * 1024),
        "kb": 1 / 1024,
        "kib": 1 / 1024,
        "mb": 1,
        "mib": 1,
        "gb": 1024,
        "gib": 1024,
        "tb": 1024 * 1024,
        "tib": 1024 * 1024,
    }
    return amount * factors[unit]


def sanitize_text(value: str) -> str:
    """清理键值/JSON 中的凭据和预签名 URL，作为报告落盘前的最后一道脱敏。"""
    result = re.sub(
        r'(?i)"?(password|cookie|csrf(?:token|_token)?|auth(?:_token|orization)?|uploadurl)"?\s*:\s*"[^"]*"',
        lambda match: f'"{match.group(1)}":"<redacted>"',
        value,
    )
    result = re.sub(
        r"(?i)(password|cookie|csrf(?:token|_token)?|auth(?:_token|orization)?|uploadurl)\s*([=:]|\s*=\>)\s*(\")?[^,;\s\"]+(\")?",
        lambda match: f"{match.group(1)}=<redacted>",
        result,
    )
    return re.sub(r"https?://\S*presign\S*", "<redacted-url>", result, flags=re.IGNORECASE)


def summarize_containers(rows: list[dict]) -> dict:
    """按服务汇总样本数、空闲/峰值/回收内存、OOM、重启和数据完整性。"""
    summaries = {}
    for service in sorted({row.get("service", "unknown") for row in rows}):
        service_rows = [row for row in rows if row.get("service") == service]
        values = [row["memory_mib"] for row in service_rows if isinstance(row.get("memory_mib"), (int, float))]
        idle_values = [row["memory_mib"] for row in service_rows if row.get("phase") == "idle" and row.get("memory_mib") is not None]
        recovery_values = [row["memory_mib"] for row in service_rows if row.get("phase") == "recovery" and row.get("memory_mib") is not None]
        missing = sum(1 for row in service_rows if row.get("memory_mib") is None)
        summaries[service] = {
            "samples": len(service_rows),
            "missing_samples": missing,
            "complete": missing == 0,
            "idle_mib": idle_values[0] if idle_values else None,
            "peak_mib": max(values) if values else None,
            "recovery_mib": recovery_values[-1] if recovery_values else None,
            "oom": any(bool(row.get("oom")) for row in service_rows),
            "max_restarts": max((int(row.get("restarts", 0)) for row in service_rows), default=0),
        }
    return summaries
