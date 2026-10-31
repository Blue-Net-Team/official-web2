"""日志配置测试：健康检查访问日志应被过滤。"""

from __future__ import annotations

import logging

from logging_config import HealthCheckFilter


def _make_access_record(path: str) -> logging.LogRecord:
    """构造与 uvicorn.access 一致的日志记录。"""
    record = logging.LogRecord(
        name="uvicorn.access",
        level=logging.INFO,
        pathname=__file__,
        lineno=1,
        msg='%s - "%s %s HTTP/%s" %d',
        args=("127.0.0.1:53360", "GET", path, "1.1", 200),
        exc_info=None,
    )
    return record


def test_health_check_access_log_is_filtered() -> None:
    access_filter = HealthCheckFilter()

    assert access_filter.filter(_make_access_record("/ai/v1/health")) is False
    assert access_filter.filter(_make_access_record("/ai/v1/health?probe=1")) is False


def test_business_access_log_is_kept() -> None:
    access_filter = HealthCheckFilter()

    assert access_filter.filter(_make_access_record("/ai/v1/chat")) is True
    # 名字相近但不是健康检查的接口不应被误伤
    assert access_filter.filter(_make_access_record("/ai/v1/health-report")) is True


def test_other_loggers_are_not_filtered() -> None:
    access_filter = HealthCheckFilter()
    record = logging.LogRecord(
        name="uvicorn.error",
        level=logging.INFO,
        pathname=__file__,
        lineno=1,
        msg="Application startup complete.",
        args=(),
        exc_info=None,
    )

    assert access_filter.filter(record) is True
