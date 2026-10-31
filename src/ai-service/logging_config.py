"""统一日志配置：将标准 logging 拦截到 loguru。"""

from __future__ import annotations

import logging
import sys
from typing import Iterable

from loguru import logger

# 健康检查路径：这些请求的访问日志属于噪声，默认不输出
DEFAULT_ACCESS_LOG_IGNORED_PATHS: frozenset[str] = frozenset({
    "/ai/v1/health"
})


class HealthCheckFilter(logging.Filter):
    """过滤 uvicorn 访问日志中的健康检查请求。

    uvicorn 的 access 日志通过 ``record.args`` 传递请求信息
    （client_addr, method, path, http_version, status_code），
    这里优先按完整的 path 判断，避免误伤名字相似的业务接口。
    """

    def __init__(self, ignored_paths: Iterable[str] = DEFAULT_ACCESS_LOG_IGNORED_PATHS) -> None:
        super().__init__()
        self._ignored_paths = frozenset(ignored_paths)

    def filter(self, record: logging.LogRecord) -> bool:
        if record.name != "uvicorn.access":
            return True

        path: str | None = None
        args = record.args
        if isinstance(args, tuple) and len(args) >= 3 and isinstance(args[2], str):
            path = args[2]
        elif record.msg:
            # 兜底：从原始日志模板中提取路径
            message = record.getMessage()
            for part in message.split():
                if part.startswith("/"):
                    path = part
                    break

        if not path:
            return True

        return path.split("?", 1)[0] not in self._ignored_paths


class InterceptHandler(logging.Handler):
    """将标准库日志记录拦截到 loguru。"""

    def emit(self, record: logging.LogRecord) -> None:
        # 获取对应的 loguru 级别
        try:
            level = logger.level(record.levelname).name
        except ValueError:
            level = record.levelno

        # 使用 record 中的模块信息，而不是通过栈帧查找
        # 这样即使经过 logging.callHandlers，也能正确显示来源
        logger.bind(
            name=record.name,
            function=record.funcName,
            line=record.lineno,
        ).opt(depth=0, exception=record.exc_info).log(
            level, record.getMessage()
        )


def setup_logging() -> None:
    """配置日志：移除默认 loguru handler，添加自定义格式，并拦截标准 logging。"""
    # 移除 loguru 默认的 stderr handler
    logger.remove()

    # 添加自定义格式的 stderr handler，使用 loguru 的彩色输出
    logger.add(
        sys.stderr,
        format="<green>{time:YYYY-MM-DD HH:mm:ss.SSS}</green> | <level>{level: <8}</level> | <cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - <level>{message}</level>",
        level="INFO",
        colorize=True,
    )

    # 拦截 Python 标准 logging
    logging.basicConfig(handlers=[InterceptHandler()], level=0, force=True)

    # 将已有 logger 的 handler 替换掉
    for name in logging.root.manager.loggerDict:
        logging_logger = logging.getLogger(name)
        logging_logger.handlers = [InterceptHandler()]
        logging_logger.propagate = False

    # 过滤健康检查访问日志（无法预知 uvicorn 何时创建该 logger，重复设置无副作用）
    access_logger = logging.getLogger("uvicorn.access")
    access_filter = HealthCheckFilter()
    access_logger.addFilter(access_filter)
    for handler in access_logger.handlers:
        handler.addFilter(access_filter)
