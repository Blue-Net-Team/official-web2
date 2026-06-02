"""FastAPI 应用入口。"""

from __future__ import annotations

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger

from api import router as api_router
from logging_config import setup_logging
from messaging.parse_consumer import start_parse_consumer
from pipeline.document_parser import update_doc_status
from retrieval import PgVectorStore

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

    # 启动前清理僵尸任务：上次运行遗留的 pending / parsing / canceling 任务标记为 canceled
    try:
        with PgVectorStore() as store:
            rows = store._execute(
                "SELECT id FROM tb_rag_docs WHERE status IN ('pending', 'parsing', 'canceling')",
                fetch=True,
            )
            if rows:
                zombie_ids = [row["id"] for row in rows]
                _log.warning(f"发现 {len(zombie_ids)} 个僵尸任务，标记为已取消: {zombie_ids}")
                for doc_id in zombie_ids:
                    update_doc_status(doc_id, "canceled")
            else:
                _log.info("未发现僵尸任务")
    except Exception as exc:
        _log.warning(f"清理僵尸任务失败（不影响服务启动）: {exc}")

    consumer_task = None
    try:
        consumer_task = await start_parse_consumer()
    except Exception as exc:
        _log.warning(f"RabbitMQ 消费者启动失败（可能 RabbitMQ 未运行）: {exc}")
    yield
    if consumer_task is not None:
        consumer_task.cancel()
        try:
            await consumer_task
        except asyncio.CancelledError:
            pass
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
