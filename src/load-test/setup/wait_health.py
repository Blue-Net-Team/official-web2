"""Poll local API/Judge health endpoints with bounded timeouts."""

from __future__ import annotations

import click
import json
import time
from urllib.error import URLError
from urllib.request import urlopen


def check_health(url: str, timeout: float = 3.0) -> tuple[bool, str]:
    """检查单次 HTTP 200 和 UP 状态，将网络/JSON 异常转换为可记录的诊断信息。"""
    try:
        with urlopen(url, timeout=timeout) as response:
            body = response.read().decode("utf-8")
            if response.status != 200:
                return False, f"HTTP {response.status}"
            payload = json.loads(body)
            status = payload.get("status") or payload.get("data", {}).get("status")
            return status == "UP", f"HTTP 200 status={status}"
    except (OSError, URLError, ValueError) as error:
        return False, str(error)


def wait_for_services(
    api_url: str,
    judge_url: str,
    timeout_seconds: int = 180,
    interval_seconds: float = 2.0,
    sleep=time.sleep,
) -> None:
    """并行轮询 API/Judge 健康状态，直到全部成功或超过统一截止时间。"""
    targets = {
        "api": f"{api_url.rstrip('/')}/api/v1/health",
        "judge": f"{judge_url.rstrip('/')}/api/v1/judge/health",
    }
    remaining = dict.fromkeys(targets, True)
    deadline = time.monotonic() + timeout_seconds
    last_errors = {}
    while remaining and time.monotonic() < deadline:
        for name in list(remaining):
            healthy, detail = check_health(targets[name])
            if healthy:
                print(f"{name} healthy: {targets[name]}")
                del remaining[name]
            else:
                last_errors[name] = detail
        if remaining:
            sleep(interval_seconds)
    if remaining:
        details = ", ".join(f"{name}: {last_errors.get(name, 'no response')}" for name in remaining)
        raise TimeoutError(f"services did not become healthy within {timeout_seconds}s ({details})")


@click.command(help=__doc__)
@click.option("--api-url", default="http://localhost:8080", show_default=True)
@click.option("--judge-url", default="http://localhost:8090", show_default=True)
@click.option("--timeout", type=click.IntRange(min=1), default=180, show_default=True)
def main(api_url: str, judge_url: str, timeout: int) -> None:
    """命令行入口；超时以 Click 异常返回非零状态供重置/CI 编排停止后续压测。"""
    try:
        wait_for_services(api_url, judge_url, timeout)
    except TimeoutError as error:
        raise click.ClickException(str(error)) from error


if __name__ == "__main__":
    main()
