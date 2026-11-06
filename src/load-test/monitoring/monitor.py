"""Low-frequency Docker, Actuator and RabbitMQ sampler for local load tests."""

from __future__ import annotations

import click
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import time
from urllib.parse import quote

try:
    from monitoring.processing import parse_mib
except ModuleNotFoundError:
    from processing import parse_mib

CONTAINERS = {"api": "bluenet-api-service", "judge": "bluenet-judge-service"}
QUEUES = {"run": "algorithm.judge.run", "formal": "algorithm.judge.formal"}
HEAP_POOLS = ["Eden Space", "Survivor Space", "Tenured Gen"]
HEAP_METRICS = {
    "heap_used_mib": "jvm.memory.used",
    "heap_committed_mib": "jvm.memory.committed",
    "heap_max_mib": "jvm.memory.max",
}
OTHER_METRICS = {
    "direct_buffer_used_mib": ("jvm.buffer.memory.used", "id:direct"),
    "threads_live": ("jvm.threads.live", None),
    "gc_pause": ("jvm.gc.pause", None),
}
CURL = shutil.which("curl.exe") or shutil.which("curl") or "curl"


def timestamp() -> str:
    """生成带时区的 UTC ISO 时间戳，供所有监控文件对齐同一采样时刻。"""
    return datetime.now(timezone.utc).isoformat()


def run_json_lines(command: list[str]) -> list[dict]:
    """执行有界 Docker 命令并把每行 JSON 解析为对象。"""
    result = subprocess.run(command, check=True, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=20)
    return [json.loads(line) for line in result.stdout.splitlines() if line.strip()]


def curl_json(url: str, *, cookie_jar: Path | None = None, auth: tuple[str, str] | None = None) -> dict:
    """使用带超时的 curl 获取 JSON，避免本机 urllib 间歇性阻塞影响监控退出。"""
    command = [CURL, "--silent", "--show-error", "--fail", "--max-time", "8"]
    if cookie_jar:
        command.extend(["--cookie", str(cookie_jar)])
    if auth:
        command.extend(["--user", f"{auth[0]}:{auth[1]}"])
    command.append(url)
    result = subprocess.run(command, check=True, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=12)
    return json.loads(result.stdout)


def docker_sample() -> list[dict]:
    """采集容器总 RSS、limit、CPU、OOM 和重启数；单服务失败不丢失另一服务样本。"""
    rows = []
    stats = {
        row.get("Name"): row
        for row in run_json_lines(
            ["docker", "stats", "--no-stream", "--format", "{{json .}}", *CONTAINERS.values()]
        )
    }
    for service, container in CONTAINERS.items():
        try:
            stat = stats[container]
            state = run_json_lines(["docker", "inspect", container, "--format", "{{json .State}}"])[0]
            memory, limit = stat.get("MemUsage", "").split("/", 1)
            rows.append({
                "service": service,
                "memory_mib": parse_mib(memory),
                "limit_mib": parse_mib(limit),
                "cpu_percent": float(stat.get("CPUPerc", "0").rstrip("%")),
                "oom": bool(state.get("OOMKilled")),
                "restarts": int(state.get("RestartCount", 0)),
                "status": state.get("Status"),
            })
        except (KeyError, ValueError, OSError, subprocess.SubprocessError) as error:
            rows.append({"service": service, "memory_mib": None, "error": str(error)})
    return rows


class ApiMetricsClient:
    """维护独立监控账号的 Actuator 会话，并屏蔽 API 的响应包装差异。"""

    def __init__(self, base_url: str, student_id: str, password: str):
        self.base_url = base_url.rstrip("/")
        self.student_id = student_id
        self.password = password
        self.tempdir = tempfile.TemporaryDirectory(prefix="bluenet-monitor-")
        self.cookie_jar = Path(self.tempdir.name) / "cookies"
        self._login()

    def _login(self) -> None:
        """通过正常登录接口创建监控 Cookie；凭据不进入采样结果。"""
        payload = json.dumps({
            "studentId": self.student_id,
            "password": hashlib.sha256(self.password.encode()).hexdigest(),
        })
        command = [
            CURL,
            "--silent",
            "--show-error",
            "--fail",
            "--max-time",
            "10",
            "--cookie-jar",
            str(self.cookie_jar),
            "--header",
            "Content-Type: application/json",
            "--data",
            payload,
            f"{self.base_url}/api/v1/auth/login/student-id",
        ]
        result = subprocess.run(command, check=True, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=15)
        if json.loads(result.stdout).get("code") != 200:
            raise RuntimeError("monitor login returned a business failure")

    def metric(self, name: str, tag: str | None) -> dict:
        """读取 API 指标；会话被其他登录撤销时允许重新登录并重试一次。"""
        path = f"{self.base_url}/actuator/metrics/{quote(name)}"
        if tag:
            path += f"?tag={quote(tag)}"
        try:
            return curl_json(path, cookie_jar=self.cookie_jar)
        except subprocess.CalledProcessError:
            # k6 setup may log in the same system account and revoke this session once.
            self._login()
            return curl_json(path, cookie_jar=self.cookie_jar)


def plain_metric(base_url: str, name: str, tag: str | None) -> dict:
    """读取 Judge 未包装的 Actuator 指标响应。"""
    path = f"{base_url.rstrip('/')}/actuator/metrics/{quote(name)}"
    if tag:
        path += f"?tag={quote(tag)}"
    return curl_json(path)


def measurement_value(payload: dict, statistic: str = "VALUE") -> float | None:
    """兼容 API 的 ResponseMessage.data 包装和 Judge 原始响应，提取指定统计值。"""
    if isinstance(payload.get("data"), dict):
        payload = payload["data"]
    measurements = payload.get("measurements") or []
    measurement = next((item for item in measurements if item.get("statistic") == statistic), None)
    value = measurement.get("value") if measurement else None
    return float(value) if value is not None else None


def jvm_sample(api_client: ApiMetricsClient, judge_url: str) -> list[dict]:
    """按 Eden/Survivor/Tenured 求和 Heap，并采集 Non-heap、直接缓冲区、GC 和线程。"""
    rows = []
    for service in ["api", "judge"]:
        row = {"service": service}
        try:
            get_metric = api_client.metric if service == "api" else lambda name, tag: plain_metric(judge_url, name, tag)
            heap_used_bytes = 0.0
            for field, name in HEAP_METRICS.items():
                values = []
                for pool in HEAP_POOLS:
                    try:
                        values.append(measurement_value(get_metric(name, f"id:{pool}")))
                    except subprocess.CalledProcessError:
                        # A pool with no active measurement contributes zero, not a missing JVM row.
                        values.append(0.0)
                total = sum(value for value in values if value is not None)
                row[field] = total / (1024 * 1024)
                if field == "heap_used_mib":
                    heap_used_bytes = total
            total_used = measurement_value(get_metric("jvm.memory.used", None))
            row["nonheap_used_mib"] = (
                (total_used - heap_used_bytes) / (1024 * 1024) if total_used is not None else None
            )
            for field, (name, tag) in OTHER_METRICS.items():
                value = measurement_value(get_metric(name, tag), "TOTAL_TIME" if field == "gc_pause" else "VALUE")
                if field == "direct_buffer_used_mib":
                    value = value / (1024 * 1024) if value is not None else None
                row[field] = value
            row["gc_count"] = measurement_value(get_metric("jvm.gc.pause", None), "COUNT")
        except (OSError, ValueError, RuntimeError, subprocess.SubprocessError) as error:
            row["error"] = str(error)
        rows.append(row)
    return rows


def queue_sample(management_url: str, username: str, password: str) -> list[dict]:
    """分别读取 run/formal 队列 ready/unacked，用于识别真实判题积压。"""
    rows = []
    for operation, queue in QUEUES.items():
        try:
            url = f"{management_url.rstrip('/')}/api/queues/%2F/{quote(queue, safe='')}"
            payload = curl_json(url, auth=(username, password))
            rows.append({
                "operation": operation,
                "queue": queue,
                "ready": payload.get("messages_ready"),
                "unacked": payload.get("messages_unacknowledged"),
            })
        except (OSError, ValueError, subprocess.SubprocessError) as error:
            rows.append({"operation": operation, "queue": queue, "error": str(error)})
    return rows


def append_jsonl(path: Path, run_id: str, phase: str, rows: list[dict]) -> None:
    """逐行追加带 runId/时间/阶段的样本，实验中止时已写入数据仍可恢复。"""
    with path.open("a", encoding="utf-8") as output:
        for row in rows:
            output.write(json.dumps({"runId": run_id, "timestamp": timestamp(), "phase": phase, **row}) + "\n")


@click.command(help=__doc__)
@click.option("--run-id", required=True)
@click.option(
    "--output-root",
    type=click.Path(path_type=Path, file_okay=False),
    default=Path(__file__).resolve().parents[1] / "results",
    show_default=True,
)
@click.option("--interval", type=click.FloatRange(min=0.1), default=5.0, show_default=True)
@click.option("--duration-seconds", type=click.FloatRange(min=0), default=300, show_default=True)
@click.option("--phase-file", type=click.Path(path_type=Path, dir_okay=False))
def main(
    run_id: str,
    output_root: Path,
    interval: float,
    duration_seconds: float,
    phase_file: Path | None,
) -> None:
    """启动有界采样循环；阶段由共享 phase 文件驱动，结果按 runId 分目录保存。"""
    output = output_root / run_id
    output.mkdir(parents=True, exist_ok=True)
    phase_file = phase_file or output / "phase"
    if not phase_file.exists():
        phase_file.write_text("idle", encoding="utf-8")

    api_client = ApiMetricsClient(
        os.environ.get("API_BASE_URL", "http://localhost:8080"),
        os.environ.get("MONITOR_USER_STUDENT_ID", os.environ["SYSTEM_USER_STUDENT_ID"]),
        os.environ.get("MONITOR_USER_PASSWORD", os.environ["SYSTEM_USER_PASSWORD"]),
    )
    judge_url = os.environ.get("JUDGE_BASE_URL", "http://localhost:8090")
    rabbit_url = os.environ.get("RABBITMQ_MANAGEMENT_URL", "http://localhost:15672")
    rabbit_user = os.environ.get("RABBITMQ_USERNAME", "")
    rabbit_password = os.environ.get("RABBITMQ_PASSWORD", "")

    deadline = time.monotonic() + duration_seconds
    while time.monotonic() < deadline:
        phase = phase_file.read_text(encoding="utf-8").strip() or "unknown"
        append_jsonl(output / "containers.jsonl", run_id, phase, docker_sample())
        append_jsonl(output / "jvm.jsonl", run_id, phase, jvm_sample(api_client, judge_url))
        append_jsonl(output / "queues.jsonl", run_id, phase, queue_sample(rabbit_url, rabbit_user, rabbit_password))
        time.sleep(interval)


if __name__ == "__main__":
    main()
