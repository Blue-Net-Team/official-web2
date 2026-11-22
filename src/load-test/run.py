"""Run one local k6 scenario with synchronized monitoring and a partial-result-safe report."""

from __future__ import annotations

import click
from datetime import datetime, timezone
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import time

ROOT = Path(__file__).resolve().parents[2]
LOAD_TESTS = ROOT / "src" / "load-test"
SCENARIOS = {
    "enrollment": "scenarios/enrollment.js",
    "file": "scenarios/assessment-file-upload.js",
    "algorithm": "scenarios/algorithm-python.js",
}


def load_local_credentials() -> dict[str, str]:
    """只从 docker/.env 读取监控所需白名单变量，避免 source 整个含 cron 的 env 文件。"""
    allowed = {
        "SYSTEM_USER_STUDENT_ID",
        "SYSTEM_USER_PASSWORD",
        "RABBITMQ_USERNAME",
        "RABBITMQ_PASSWORD",
    }
    values = {}
    for line in (ROOT / "docker" / ".env").read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        if key in allowed and key not in os.environ:
            values[key] = value.strip().strip('"').strip("'")
    return values


def command_version(command: list[str]) -> str:
    """以有界超时收集实验工具版本；工具缺失时记录原因而不是中断结果保存。"""
    try:
        result = subprocess.run(command, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=15)
        return result.stdout.strip() or result.stderr.strip()
    except (OSError, subprocess.SubprocessError) as error:
        return f"unavailable: {error}"


def set_phase(path: Path, phase: str) -> None:
    """更新监控器共享的当前阶段，使不同文件中的样本具有相同业务时间线。"""
    path.write_text(phase, encoding="utf-8")


@click.command(help=__doc__)
@click.argument("scenario", type=click.Choice(list(SCENARIOS)))
@click.option("--run-id", default=f"load-{int(time.time())}", show_default=True)
@click.option("--reset", is_flag=True, help="清空并重建已授权的本地 bluenet/dev 环境。")
@click.option("--preflight", is_flag=True, help="仅运行单用户或小样本预跑。")
@click.option("--recovery-seconds", type=click.IntRange(min=0), default=30, show_default=True)
@click.option("--unique-variable", default="none", show_default=True)
def main(scenario: str, run_id: str, reset: bool, preflight: bool, recovery_seconds: int, unique_variable: str) -> None:
    """编排重置、监控、k6、回收观察和报告，并在异常时仍保存部分实验数据。"""
    if scenario.startswith("algorithm") and reset:
        raise click.ClickException("先重置环境并完成 prepare-algorithm/measure limits，再不带 --reset 运行算法场景")
    if shutil.which("k6") is None and not Path("C:/Program Files/k6/k6.exe").is_file():
        raise click.ClickException("k6 is not installed")
    k6 = shutil.which("k6") or "C:/Program Files/k6/k6.exe"
    env = {
        **load_local_credentials(),
        **os.environ,
        "API_BASE_URL": os.environ.get("API_BASE_URL", "http://localhost:8080"),
        "JUDGE_BASE_URL": os.environ.get("JUDGE_BASE_URL", "http://localhost:8090"),
        "STORAGE_PUBLIC_URL": os.environ.get("STORAGE_PUBLIC_URL", "http://host.docker.internal:7000"),
        "CANDIDATE_PASSWORD": os.environ.get("CANDIDATE_PASSWORD", "LoadTestOnly-Local"),
        "MONITOR_USER_STUDENT_ID": os.environ.get("MONITOR_USER_STUDENT_ID", "999999000001"),
        "MONITOR_USER_PASSWORD": os.environ.get("MONITOR_USER_PASSWORD", "LoadTestMonitor-Local"),
    }
    if scenario.startswith("algorithm") and not env.get("ALGORITHM_CONTEXT_FILE"):
        raise click.ClickException("算法场景需要 ALGORITHM_CONTEXT_FILE，先执行准备和测速确认步骤")
    if preflight:
        env["PREFLIGHT"] = "true"

    if reset:
        subprocess.run(
            [
                sys.executable,
                str(LOAD_TESTS / "setup" / "reset.py"),
                "--project",
                "bluenet",
                "--env-name",
                "dev",
                "--confirm-project",
                "bluenet",
                "--execute",
            ],
            cwd=ROOT,
            env=env,
            check=True,
        )

    if reset:
        subprocess.run(
            [k6, "run", str(LOAD_TESTS / "setup" / "monitor-user.js"), "--quiet"],
            cwd=ROOT,
            env=env,
            check=True,
        )

    run_dir = LOAD_TESTS / "results" / run_id
    run_dir.mkdir(parents=True, exist_ok=True)
    phase_file = run_dir / "phase"
    set_phase(phase_file, "idle")
    monitor = subprocess.Popen(
        [
            sys.executable,
            str(LOAD_TESTS / "monitoring" / "monitor.py"),
            "--run-id",
            run_id,
            "--phase-file",
            str(phase_file),
            "--duration-seconds",
            "1800",
        ],
        cwd=ROOT,
        env=env,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    started_at = datetime.now(timezone.utc).isoformat()
    k6_code = None
    error = None
    try:
        time.sleep(3)
        set_phase(phase_file, "preflight" if preflight else "warmup")
        process = subprocess.Popen(
            [
                k6,
                "run",
                str(LOAD_TESTS / SCENARIOS[scenario]),
                "--summary-export",
                str(run_dir / "k6-summary.json"),
                "--quiet",
            ],
            cwd=ROOT,
            env=env,
        )
        started = time.monotonic()
        while process.poll() is None:
            if not preflight and time.monotonic() - started >= 10:
                set_phase(phase_file, "steady")
            time.sleep(0.2)
        k6_code = process.returncode
        set_phase(phase_file, "drain")
        time.sleep(5)
        set_phase(phase_file, "recovery")
        time.sleep(recovery_seconds)
    except (OSError, subprocess.SubprocessError) as caught:
        error = str(caught)
        k6_code = 1
    finally:
        set_phase(phase_file, "complete")
        monitor.terminate()
        try:
            monitor.wait(timeout=10)
        except subprocess.TimeoutExpired:
            monitor.kill()
            monitor.wait(timeout=5)

    metadata = {
        "runId": run_id,
        "scenario": scenario,
        "preflight": preflight,
        "startedAt": started_at,
        "finishedAt": datetime.now(timezone.utc).isoformat(),
        "complete": k6_code == 0,
        "k6ExitCode": k6_code,
        "error": error,
        "uniqueVariable": unique_variable,
        "k6Version": command_version([k6, "version"]),
        "dockerVersion": command_version(["docker", "version", "--format", "{{.Server.Version}}"]),
        "gitRevision": command_version(["git", "rev-parse", "HEAD"]),
        "notes": "Local Docker only; not a production capacity claim. Warmup/steady transition is orchestrator-paced.",
    }
    (run_dir / "metadata.json").write_text(json.dumps(metadata, ensure_ascii=False, indent=2), encoding="utf-8")
    from monitoring.report import sanitize_k6_summary, write_report

    sanitize_k6_summary(run_dir / "k6-summary.json")
    write_report(run_dir)
    click.echo(f"result: {run_dir / 'report.md'}")
    raise click.exceptions.Exit(k6_code)


if __name__ == "__main__":
    main()
