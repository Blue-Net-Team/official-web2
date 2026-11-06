"""Guarded reset for the dedicated local load-test Compose environment."""

from __future__ import annotations

import click
from dataclasses import dataclass
import os
from pathlib import Path
import subprocess

DEFAULT_PROJECT = "bluenet"
DEFAULT_ENV = "dev"
ALLOWED_LOCAL_TARGETS = {("bluenet", "dev"), ("bluenet-loadtest", "loadtest")}


@dataclass(frozen=True)
class ResetRequest:
    project: str
    env_name: str


def validate_reset_request(
    project: str,
    env_name: str,
    confirm_project: str,
    confirm_text: str,
    docker_host: str,
) -> ResetRequest:
    """在任何 Docker 命令之前验证本地目标白名单和两组独立确认信息。"""
    if (project, env_name) not in ALLOWED_LOCAL_TARGETS:
        raise ValueError("reset requires an explicitly allowed dedicated local environment")
    if confirm_project != project or confirm_text != "DELETE":
        raise ValueError("both matching project confirmation and DELETE confirmation are required")
    if docker_host and not (
        docker_host.startswith("npipe://") or docker_host.startswith("unix://")
    ):
        raise ValueError("reset is only supported against a local Docker endpoint")
    return ResetRequest(project=project, env_name=env_name)


def compose_base(root: Path, request: ResetRequest) -> list[str]:
    """构造固定使用仓库配置、docker/.env 和压测 override 的 Compose 命令前缀。"""
    return [
        "docker",
        "compose",
        "--project-name",
        request.project,
        "--env-file",
        str(root / "docker" / ".env"),
        "--file",
        str(root / "docker" / "docker-compose.yml"),
        "--file",
        str(root / "src" / "load-test" / "setup" / "docker-compose.loadtest.yml"),
    ]


def build_reset_commands(root: Path, request: ResetRequest) -> list[list[str]]:
    """生成带 full profile 的清理/重建命令，确保 profile 内的数据卷也被删除。"""
    base = compose_base(root, request)
    return [
        [*base, "--profile", "full", "down", "--volumes", "--remove-orphans"],
        [
            *base,
            "--profile",
            "full",
            "up",
            "--detach",
            "database",
            "redis",
            "rabbitmq",
            "oss",
            "api-service",
            "judge-service",
        ],
    ]


@click.command(help=__doc__)
@click.option("--project", default=DEFAULT_PROJECT, show_default=True)
@click.option("--env-name", default=DEFAULT_ENV, show_default=True)
@click.option("--confirm-project", default="", help="必须与 project 完全一致。")
@click.option("--execute", is_flag=True, help="实际删除并重建；缺省时仅 dry-run。")
def main(project: str, env_name: str, confirm_project: str, execute: bool) -> None:
    """默认仅打印计划；显式 --execute 后才执行重置并等待 API/Judge 健康。"""
    try:
        request = validate_reset_request(
            project,
            env_name,
            confirm_project,
            os.environ.get("LOADTEST_RESET_CONFIRM", ""),
            os.environ.get("DOCKER_HOST", ""),
        )
    except ValueError as error:
        raise click.UsageError(f"Refused reset: {error}") from error

    root = Path(__file__).resolve().parents[3]
    commands = build_reset_commands(root, request)
    for command in commands:
        click.echo(" ".join(command))

    if not execute:
        click.echo("Dry run only; add --execute after verifying the dedicated target.")
        return

    env_file = root / "docker" / ".env"
    if not env_file.is_file():
        raise click.UsageError("docker/.env is required for --execute")

    env = {**os.environ, "ENV": request.env_name}
    try:
        for command in commands:
            subprocess.run(command, cwd=root, env=env, check=True)
        from wait_health import wait_for_services

        wait_for_services(
            os.environ.get("API_BASE_URL", "http://localhost:8080"),
            os.environ.get("JUDGE_BASE_URL", "http://localhost:8090"),
        )
    except (OSError, subprocess.CalledProcessError, TimeoutError) as error:
        raise click.ClickException(f"Reset failed: {error}") from error


if __name__ == "__main__":
    main()
