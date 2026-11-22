#!/usr/bin/env python3
"""Confirm local load-test Python limits from real Judge benchmark maxima.

Input is produced by scenarios/prepare-algorithm.js. This script never submits
candidate run/formal jobs and never starts k6.
"""

import argparse
import hashlib
import http.cookiejar
import http.cookies
import json
import math
import os
from pathlib import Path
from urllib.error import HTTPError
from urllib.parse import urlparse
from urllib.request import HTTPCookieProcessor, Request, build_opener


def measured_limit_ms(max_time_ms, extra_ms=100):
    """Use the observed maximum, not p95/suggestedTimeLimitMs or fixed limits."""
    try:
        value = float(max_time_ms)
    except (TypeError, ValueError) as exc:
        raise ValueError(f"invalid benchmark maxTimeMs: {max_time_ms!r}") from exc
    if not math.isfinite(value) or value < 0:
        raise ValueError(f"invalid benchmark maxTimeMs: {max_time_ms!r}")
    if not isinstance(extra_ms, int) or extra_ms < 0:
        raise ValueError("extra_ms must be a non-negative integer")
    return math.ceil(value) + extra_ms


def validated_measurements(context, extra_ms=100):
    algorithms = context.get("algorithms")
    if not isinstance(algorithms, list) or not algorithms:
        raise ValueError("prepared context contains no algorithms")
    measurements = []
    for question in algorithms:
        question_id = question.get("questionId")
        benchmark = question.get("benchmark") or {}
        if benchmark.get("status") != "DONE":
            raise ValueError(f"question {question_id}: Python benchmark must be DONE")
        max_time = benchmark.get("maxTimeMs")
        limit = measured_limit_ms(max_time, extra_ms)
        measurements.append((question_id, max_time, limit))
    return measurements


class AdminApi:
    def __init__(self, base_url, student_id, password):
        hostname = urlparse(base_url).hostname
        if hostname not in {"localhost", "127.0.0.1", "::1", "host.docker.internal"}:
            raise ValueError("measurement confirmation is restricted to local API targets")
        self.base_url = base_url.rstrip("/")
        self.auth_token = None
        self.opener = build_opener(HTTPCookieProcessor(http.cookiejar.CookieJar()))
        result = self.request("POST", "/api/v1/auth/login/student-id", {
            "studentId": student_id,
            "password": hashlib.sha256(password.encode("utf-8")).hexdigest(),
        })
        self.csrf_token = (result.get("data") or {}).get("csrfToken")
        if not self.csrf_token:
            raise RuntimeError("administrator login returned no CSRF token")

    def request(self, method, path, body=None, csrf_token=None):
        headers = {"Content-Type": "application/json"}
        if csrf_token:
            headers["X-CSRF-Token"] = csrf_token
        if self.auth_token:
            headers["Authorization"] = f"Bearer {self.auth_token}"
        request = Request(
            self.base_url + path,
            data=None if body is None else json.dumps(body).encode("utf-8"),
            headers=headers,
            method=method,
        )
        try:
            with self.opener.open(request, timeout=30) as response:
                payload = json.load(response)
                if path == "/api/v1/auth/login/student-id":
                    for set_cookie in response.headers.get_all("Set-Cookie", []):
                        cookie = http.cookies.SimpleCookie()
                        cookie.load(set_cookie)
                        if "auth_token" in cookie:
                            self.auth_token = cookie["auth_token"].value
        except HTTPError as exc:
            detail = exc.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"{method} {path}: HTTP {exc.code}: {detail}") from exc
        if payload.get("code") != 200:
            raise RuntimeError(f"{method} {path}: business failure: {payload}")
        return payload

    def confirm_python_limit(self, question_id, time_limit_ms):
        return self.request(
            "PUT",
            f"/api/v1/admin/judge/questions/{question_id}/config/language-limits/python",
            {"timeLimitMs": time_limit_ms, "memoryLimitKb": 262144, "outputLimitKb": 1024},
            self.csrf_token,
        )


def confirm_context(context, api, extra_ms=100):
    measurements = validated_measurements(context, extra_ms)
    by_id = {question["questionId"]: question for question in context["algorithms"]}
    for question_id, max_time, limit in measurements:
        api.confirm_python_limit(question_id, limit)
        by_id[question_id]["timeLimitMs"] = limit
        by_id[question_id]["limitMeasurement"] = {
            "source": "judge_benchmark_max",
            "maxTimeMs": max_time,
            "extraMs": extra_ms,
            "formula": "ceil(maxTimeMs) + extraMs",
        }
        print(f"question {question_id}: max={max_time}ms, confirmed={limit}ms")
    # Defensive removal: neither prepared nor confirmed output may contain sessions.
    for user in context.get("users", []):
        user.pop("session", None)
        user.pop("password", None)
    return context


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", type=Path, help="JSON written by prepare-algorithm.js")
    parser.add_argument("output", type=Path, help="confirmed JSON consumed by algorithm-python.js")
    parser.add_argument("--extra-ms", type=int, default=100)
    args = parser.parse_args()
    context = json.loads(args.input.read_text(encoding="utf-8"))
    student_id = os.environ.get("SYSTEM_USER_STUDENT_ID")
    password = os.environ.get("SYSTEM_USER_PASSWORD")
    if not student_id or not password:
        raise SystemExit("SYSTEM_USER_STUDENT_ID and SYSTEM_USER_PASSWORD are required")
    api = AdminApi(context["baseUrl"], student_id, password)
    confirmed = confirm_context(context, api, args.extra_ms)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(confirmed, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"confirmed context: {args.output}")


if __name__ == "__main__":
    main()
