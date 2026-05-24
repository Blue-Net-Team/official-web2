"""FastAPI 应用入口。"""

from __future__ import annotations

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger

from api import router as api_router
from logging_config import setup_logging

# 初始化统一日志（拦截标准库 logging 到 loguru）
setup_logging()

_log = logger.bind(module="Main")


# ---------------------------------------------------------------------------
# 生命周期管理
# ---------------------------------------------------------------------------

@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用启动时触发工具自动注册，关闭时清理。"""
    _log.info("应用启动，工具将在首次导入时自动注册...")
    yield
    _log.info("应用关闭，清理资源...")


# ---------------------------------------------------------------------------
# FastAPI 应用实例
# ---------------------------------------------------------------------------

app = FastAPI(
    title="BlueNet AI Service",
    description="蓝网团队 AI 智能检索客服 API",
    version="0.1.0",
    lifespan=lifespan,
    redirect_slashes=False,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


app.include_router(api_router)


@app.get("/ai/v1/health")
async def health_check():
    """健康检查接口。"""
    return {"status": "ok"}
