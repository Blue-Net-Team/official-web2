"""统一日志配置：将标准 logging 拦截到 loguru。"""

from __future__ import annotations

import logging
import sys

from loguru import logger


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
