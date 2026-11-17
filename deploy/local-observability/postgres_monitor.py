"""Create or remove the least-privilege local PostgreSQL monitoring role."""

from __future__ import annotations

import argparse
import os
from pathlib import Path
import subprocess

EXPECTED_CONTEXT = "docker-desktop"
ROLE = "bluenet_monitor"


def kubectl(*args: str, **kwargs) -> subprocess.CompletedProcess:
    return subprocess.run(["kubectl", *args], check=True, **kwargs)


def guard(confirmation: str) -> None:
    current = kubectl("config", "current-context", capture_output=True, text=True).stdout.strip()
    if current != EXPECTED_CONTEXT or confirmation != EXPECTED_CONTEXT:
        raise SystemExit(f"Refusing PostgreSQL target {current!r}; expected and confirm {EXPECTED_CONTEXT!r}.")


def admin_environment() -> dict[str, str]:
    result = kubectl(
        "-n", "bluenet", "get", "secret", "bluenet-postgres",
        "-o", "jsonpath={.data.postgres-password}", capture_output=True, text=True,
    )
    import base64
    password = base64.b64decode(result.stdout).decode("utf-8")
    environment = os.environ.copy()
    environment["PGPASSWORD"] = password
    return environment


def execute(sql: str) -> None:
    kubectl(
        "-n", "bluenet", "exec", "-i", "bluenet-postgres-0", "--",
        "psql", "-U", "postgres", "-d", "db_blue_net", "-v", "ON_ERROR_STOP=1",
        input=sql, text=True, env=admin_environment(), capture_output=True,
    )


def literal(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def initialize(confirmation: str) -> None:
    guard(confirmation)
    password = os.environ.get("POSTGRES_MONITOR_PASSWORD", "")
    if not password:
        raise SystemExit("POSTGRES_MONITOR_PASSWORD must be set in the process environment.")
    execute(
        f"DO $do$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '{ROLE}') THEN "
        f"CREATE ROLE {ROLE} LOGIN PASSWORD {literal(password)}; END IF; END $do$;\n"
        f"ALTER ROLE {ROLE} LOGIN PASSWORD {literal(password)};\n"
        f"GRANT pg_monitor TO {ROLE};\n"
    )
    print("PostgreSQL monitor role is ready with pg_monitor only; password was not printed.")


def cleanup(confirmation: str) -> None:
    guard(confirmation)
    execute(
        f"SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE usename = '{ROLE}' AND pid <> pg_backend_pid();\n"
        f"REVOKE pg_monitor FROM {ROLE};\n"
        f"DROP ROLE IF EXISTS {ROLE};\n"
    )
    print("PostgreSQL monitor role removed; business roles and data were not changed.")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("init", "cleanup"))
    parser.add_argument("--confirm", required=True)
    args = parser.parse_args()
    if args.command == "init":
        initialize(args.confirm)
    else:
        cleanup(args.confirm)


if __name__ == "__main__":
    main()
