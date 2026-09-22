"""知识库标签向量 upsert RabbitMQ 消费者。

接收来自 API Service 的 tag-upsert 任务消息（标签新建/重命名/删除），
重新计算标签向量；删除场景附带 recount 标志以重算引用计数。
"""

from __future__ import annotations

import asyncio
import json

import aio_pika
from aio_pika.abc import AbstractIncomingMessage
from loguru import logger

from pipeline.document_parser import upsert_tag_vector
from setting import settings

_log = logger.bind(module="tag_upsert_consumer")

KNOWLEDGE_EXCHANGE = "knowledge"
KNOWLEDGE_TAG_UPSERT_QUEUE = "knowledge.tag_upsert"
KNOWLEDGE_TAG_UPSERT_ROUTING_KEY = "tag-upsert"


async def _on_message(message: AbstractIncomingMessage) -> None:
    """处理单条 tag-upsert 消息。"""
    async with message.process():
        try:
            body = json.loads(message.body.decode("utf-8"))
            tag_id = body.get("tagId")
            recount = bool(body.get("recount", False))

            if not tag_id:
                _log.error(f"消息字段缺失: {body}")
                return

            _log.info(f"收到 tag-upsert 任务: tag_id={tag_id}, recount={recount}")

            loop = asyncio.get_running_loop()
            processed = await loop.run_in_executor(None, upsert_tag_vector, int(tag_id), recount)
            if not processed:
                _log.warning(f"tag-upsert 目标标签不存在，消息丢弃: tag_id={tag_id}")
        except Exception as exc:
            _log.error(f"处理 tag-upsert 消息失败: {exc}")


async def start_tag_upsert_consumer() -> asyncio.Task:
    """启动标签向量 upsert 消费者。

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
    queue = await channel.declare_queue(KNOWLEDGE_TAG_UPSERT_QUEUE, durable=True)
    await queue.bind(exchange, routing_key=KNOWLEDGE_TAG_UPSERT_ROUTING_KEY)

    await queue.consume(_on_message)
    _log.info(f"tag-upsert 消费者已启动，监听队列: {KNOWLEDGE_TAG_UPSERT_QUEUE}")

    async def keepalive():
        try:
            while True:
                await asyncio.sleep(60)
        except asyncio.CancelledError:
            _log.info("tag-upsert 消费者正在关闭...")
            await connection.close()
            raise

    return asyncio.create_task(keepalive())
