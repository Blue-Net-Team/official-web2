"""知识库分片重新嵌入 RabbitMQ 消费者。

接收来自 API Service 的 re-embed 任务消息，重新计算分片向量并置为已同步。
"""

from __future__ import annotations

import asyncio
import json

import aio_pika
from aio_pika.abc import AbstractIncomingMessage
from loguru import logger

from pipeline.document_parser import reembed_chunk
from setting import settings

_log = logger.bind(module="reembed_consumer")

KNOWLEDGE_EXCHANGE = "knowledge"
KNOWLEDGE_REEMBED_QUEUE = "knowledge.reembed"
KNOWLEDGE_REEMBED_ROUTING_KEY = "re-embed"


async def _on_message(message: AbstractIncomingMessage) -> None:
    """处理单条 re-embed 消息。"""
    async with message.process():
        try:
            body = json.loads(message.body.decode("utf-8"))
            chunk_id = body.get("chunkId")

            if not chunk_id:
                _log.error(f"消息字段缺失: {body}")
                return

            _log.info(f"收到 re-embed 任务: chunk_id={chunk_id}")

            loop = asyncio.get_running_loop()
            processed = await loop.run_in_executor(None, reembed_chunk, int(chunk_id))
            if not processed:
                _log.warning(f"re-embed 目标分片不存在，消息丢弃: chunk_id={chunk_id}")
        except Exception as exc:
            _log.error(f"处理 re-embed 消息失败: {exc}")


async def start_reembed_consumer() -> asyncio.Task:
    """启动分片重新嵌入消费者。

    Returns:
        消费者任务，可用于取消。
    """
    connection = await aio_pika.connect_robust(
        host=settings.RABBITMQ_HOST,
        port=settings.RABBITMQ_PORT,
        login=settings.RABBITMQ_USERNAME,
        password=settings.RABBITMQ_PASSWORD,
    )

    channel = await connection.channel()
    await channel.set_qos(prefetch_count=1)

    exchange = await channel.declare_exchange(
        KNOWLEDGE_EXCHANGE, aio_pika.ExchangeType.DIRECT, durable=True
    )
    queue = await channel.declare_queue(KNOWLEDGE_REEMBED_QUEUE, durable=True)
    await queue.bind(exchange, routing_key=KNOWLEDGE_REEMBED_ROUTING_KEY)

    await queue.consume(_on_message)
    _log.info(f"re-embed 消费者已启动，监听队列: {KNOWLEDGE_REEMBED_QUEUE}")

    async def keepalive():
        try:
            while True:
                await asyncio.sleep(60)
        except asyncio.CancelledError:
            _log.info("re-embed 消费者正在关闭...")
            await connection.close()
            raise

    return asyncio.create_task(keepalive())
