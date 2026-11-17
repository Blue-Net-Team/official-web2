"""Manage credentials for the Docker Desktop external observability stack."""

from __future__ import annotations

import argparse
import base64
import json
import os
from pathlib import Path
import subprocess
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parent
PRIVATE = ROOT / ".private"
EXPECTED_CONTEXT = "docker-desktop"
NAMESPACE = "bluenet"
SERVICE_ACCOUNT = "local-observability"
IDENTITY = f"system:serviceaccount:{NAMESPACE}:{SERVICE_ACCOUNT}"


def kubectl(*args: str, capture: bool = False, check: bool = True) -> str:
    result = subprocess.run(
        ["kubectl", *args],
        check=check,
        capture_output=capture,
        text=capture,
        encoding="utf-8" if capture else None,
    )
    return result.stdout.strip() if capture else ""


def guard(context: str, confirmation: str) -> None:
    current = kubectl("config", "current-context", capture=True)
    if context != EXPECTED_CONTEXT or confirmation != EXPECTED_CONTEXT or current != EXPECTED_CONTEXT:
        raise SystemExit(
            f"Refusing target: current={current!r}, requested={context!r}. "
            f"Both arguments and current context must be {EXPECTED_CONTEXT!r}."
        )


def initialize(context: str, confirmation: str) -> None:
    guard(context, confirmation)
    kubectl("apply", "-f", str(ROOT / "rbac.yaml"))
    token = kubectl(
        "-n", NAMESPACE, "create", "token", SERVICE_ACCOUNT, "--duration=8760h", capture=True
    )
    raw = json.loads(kubectl("config", "view", "--minify", "--raw", "-o", "json", capture=True))
    cluster = raw["clusters"][0]["cluster"]
    server = cluster["server"]
    host = urlparse(server).hostname
    port = urlparse(server).port or 6443

    PRIVATE.mkdir(mode=0o700, exist_ok=True)
    (PRIVATE / "token").write_text(token + "\n", encoding="utf-8")
    (PRIVATE / "ca.crt").write_bytes(base64.b64decode(cluster["certificate-authority-data"]))
    (PRIVATE / "kubernetes.env").write_text(
        f"KUBERNETES_API_SERVER=host.docker.internal:{port}\n"
        f"KUBERNETES_TLS_SERVER_NAME={host}\n",
        encoding="utf-8",
    )
    (PRIVATE / "kubeconfig").write_text(
        "apiVersion: v1\nkind: Config\nclusters:\n"
        "- name: docker-desktop\n  cluster:\n"
        f"    server: https://host.docker.internal:{port}\n"
        "    certificate-authority: /etc/bluenet/kubernetes-ca.crt\n"
        f"    tls-server-name: {host}\n"
        "users:\n- name: local-observability\n  user:\n"
        f"    token: {token}\n"
        "contexts:\n- name: docker-desktop\n  context:\n"
        "    cluster: docker-desktop\n    user: local-observability\n"
        "current-context: docker-desktop\n",
        encoding="utf-8",
    )
    for path in PRIVATE.iterdir():
        if path.is_file():
            os.chmod(path, 0o600)
    print(f"Initialized local observability credentials in {PRIVATE}; secrets were not printed.")


def verify() -> None:
    allowed = [
        ("get", "pods"),
        ("list", "nodes"),
        ("watch", "services"),
        ("get", "endpoints"),
        ("get", "pods/proxy"),
        ("get", "nodes/proxy"),
    ]
    denied = [
        ("get", "secrets"),
        ("create", "deployments"),
        ("update", "deployments"),
        ("delete", "pods"),
    ]
    failures = []
    for verb, resource in allowed:
        answer = kubectl("auth", "can-i", verb, resource, f"--as={IDENTITY}", capture=True)
        if answer != "yes":
            failures.append(f"expected allow: {verb} {resource} ({answer})")
    for verb, resource in denied:
        answer = kubectl("auth", "can-i", verb, resource, f"--as={IDENTITY}", capture=True, check=False)
        if answer != "no":
            failures.append(f"expected deny: {verb} {resource} ({answer})")
    if failures:
        raise SystemExit("RBAC verification failed:\n" + "\n".join(failures))
    print("RBAC verification passed: proxy/read access allowed; secret and workload writes denied.")


def cleanup(context: str, confirmation: str) -> None:
    guard(context, confirmation)
    for resource in ("clusterrolebinding", "clusterrole"):
        kubectl("delete", resource, SERVICE_ACCOUNT, "--ignore-not-found=true")
    kubectl("-n", NAMESPACE, "delete", "serviceaccount", SERVICE_ACCOUNT, "--ignore-not-found=true")
    print("Removed local observability RBAC; business workloads and data were not changed.")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    subparsers = parser.add_subparsers(dest="command", required=True)
    for name in ("init", "cleanup"):
        subparser = subparsers.add_parser(name)
        subparser.add_argument("--context", required=True)
        subparser.add_argument("--confirm", required=True)
    subparsers.add_parser("verify")
    args = parser.parse_args()

    if args.command == "init":
        initialize(args.context, args.confirm)
        verify()
    elif args.command == "cleanup":
        cleanup(args.context, args.confirm)
    else:
        verify()


if __name__ == "__main__":
    main()
