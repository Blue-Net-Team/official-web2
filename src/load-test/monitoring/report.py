"""Generate a sanitized experiment summary from k6 and monitoring JSONL files."""

from __future__ import annotations

import click
import json
from pathlib import Path

try:
    from monitoring.processing import sanitize_text, summarize_containers
except ModuleNotFoundError:
    from processing import sanitize_text, summarize_containers


def read_jsonl(path: Path) -> list[dict]:
    """读取 JSONL；文件尚不存在时返回空列表，使不完整实验也能生成部分报告。"""
    if not path.exists():
        return []
    return [json.loads(line) for line in path.read_text(encoding="utf-8").splitlines() if line.strip()]


def peak(rows: list[dict], service: str, field: str) -> float | None:
    """提取某服务数值字段的峰值，忽略缺失或非数值样本。"""
    values = [row.get(field) for row in rows if row.get("service") == service and isinstance(row.get(field), (int, float))]
    return max(values) if values else None


def queue_summary(rows: list[dict]) -> dict:
    """分别汇总 run/formal 的积压峰值、最终深度和采样缺失数。"""
    result = {}
    for operation in ["run", "formal"]:
        selected = [row for row in rows if row.get("operation") == operation]
        result[operation] = {
            "peak_ready": max((row.get("ready", 0) for row in selected if row.get("ready") is not None), default=None),
            "peak_unacked": max((row.get("unacked", 0) for row in selected if row.get("unacked") is not None), default=None),
            "missing_samples": sum(1 for row in selected if row.get("error")),
            "final_ready": next((row.get("ready") for row in reversed(selected) if row.get("ready") is not None), None),
            "final_unacked": next((row.get("unacked") for row in reversed(selected) if row.get("unacked") is not None), None),
        }
    return result


SENSITIVE_KEYS = {"password", "cookie", "cookies", "csrftoken", "authtoken", "authorization", "uploadurl", "callbacktoken"}


def sanitize_json(value):
    """递归清理字典中的敏感键；列表和标量保留原有业务指标结构。"""
    if isinstance(value, dict):
        return {
            key: "<redacted>" if key.lower() in SENSITIVE_KEYS else sanitize_json(item)
            for key, item in value.items()
        }
    if isinstance(value, list):
        return [sanitize_json(item) for item in value]
    return value


def sanitize_k6_summary(path: Path) -> None:
    """删除包含用户池/会话的 setup_data，并在报告前递归脱敏 k6 摘要。"""
    if not path.exists():
        return
    payload = json.loads(path.read_text(encoding="utf-8"))
    payload.pop("setup_data", None)
    path.write_text(json.dumps(sanitize_json(payload), ensure_ascii=False, indent=2), encoding="utf-8")


def k6_summary(path: Path) -> dict:
    """兼容新旧 k6 summary-export 结构，保留数量、比率、延迟和通过/失败统计。"""
    if not path.exists():
        return {"complete": False}
    payload = json.loads(path.read_text(encoding="utf-8"))
    metrics = {}
    for name, value in payload.get("metrics", {}).items():
        values = value.get("values", value)
        metrics[name] = {
            key: values[key]
            for key in ["count", "rate", "avg", "med", "p(95)", "p(99)", "passes", "fails"]
            if key in values
        }
    return {"complete": True, "metrics": metrics}


def write_report(run_dir: Path) -> dict:
    """汇总元数据、k6、容器、JVM 和队列结果，生成 JSON 与 Markdown 两种报告。"""
    containers = read_jsonl(run_dir / "containers.jsonl")
    jvm = read_jsonl(run_dir / "jvm.jsonl")
    queues = read_jsonl(run_dir / "queues.jsonl")
    metadata_path = run_dir / "metadata.json"
    metadata = json.loads(metadata_path.read_text(encoding="utf-8")) if metadata_path.exists() else {}
    report = {
        "runId": metadata.get("runId", run_dir.name),
        "scenario": metadata.get("scenario"),
        "complete": metadata.get("complete", False),
        "metadata": metadata,
        "containers": summarize_containers(containers),
        "jvm": {
            service: {
                "peak_heap_used_mib": peak(jvm, service, "heap_used_mib"),
                "peak_heap_committed_mib": peak(jvm, service, "heap_committed_mib"),
                "peak_nonheap_used_mib": peak(jvm, service, "nonheap_used_mib"),
                "max_threads_live": peak(jvm, service, "threads_live"),
                "max_gc_pause_seconds": peak(jvm, service, "gc_pause"),
                "missing_samples": sum(1 for row in jvm if row.get("service") == service and row.get("error")),
            }
            for service in ["api", "judge"]
        },
        "queues": queue_summary(queues),
        "k6": k6_summary(run_dir / "k6-summary.json"),
    }
    (run_dir / "report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    lines = [
        f"# 压测实验 {report['runId']}",
        "",
        f"- 场景：{report['scenario']}",
        f"- 完整完成：{report['complete']}",
        "",
        "## 容器内存（MiB）",
        "",
        "| 服务 | 空闲 | 峰值 | 回收期 | OOM | 最大重启数 | 缺失样本 |",
        "|---|---:|---:|---:|---|---:|---:|",
    ]
    for service, summary in report["containers"].items():
        lines.append(
            f"| {service} | {summary['idle_mib']} | {summary['peak_mib']} | {summary['recovery_mib']} | "
            f"{summary['oom']} | {summary['max_restarts']} | {summary['missing_samples']} |"
        )
    lines.extend(["", "## JVM 峰值", "", "| 服务 | Heap used MiB | Heap committed MiB | Non-heap MiB | 线程 | GC 累计秒 | 缺失样本 |", "|---|---:|---:|---:|---:|---:|---:|"])
    for service, summary in report["jvm"].items():
        lines.append(
            f"| {service} | {summary['peak_heap_used_mib']} | {summary['peak_heap_committed_mib']} | "
            f"{summary['peak_nonheap_used_mib']} | {summary['max_threads_live']} | {summary['max_gc_pause_seconds']} | {summary['missing_samples']} |"
        )
    lines.extend(["", "原始时序与 k6 指标见同目录 JSONL、JSON 文件。结果仅用于同环境同负载对照，不代表生产容量。", ""])
    (run_dir / "report.md").write_text(sanitize_text("\n".join(lines)), encoding="utf-8")
    return report


@click.command(help=__doc__)
@click.argument("run_dir", type=click.Path(path_type=Path, exists=True, file_okay=False))
def main(run_dir: Path) -> None:
    """为指定实验目录生成报告，并输出不含敏感信息的完成状态。"""
    report = write_report(run_dir)
    click.echo(json.dumps({"runId": report["runId"], "complete": report["complete"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
