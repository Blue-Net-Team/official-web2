"""知识库文档解析 RabbitMQ 消费者。

接收来自 API Service 的解析任务消息，执行文档下载、分段、向量化入库。
"""

from __future__ import annotations

import asyncio
import json

import aio_pika
from aio_pika.abc import AbstractIncomingMessage
from loguru import logger

from pipeline.document_parser import parse_single_document
from setting import settings

_log = logger.bind(module="parse_consumer")


# 交换机/队列名称（与 API Service 的 KnowledgeQueueConfig 保持一致）
KNOWLEDGE_EXCHANGE = "knowledge"
KNOWLEDGE_PARSE_QUEUE = "knowledge.parse"
KNOWLEDGE_PARSE_ROUTING_KEY = "parse"


async def _on_message(message: AbstractIncomingMessage) -> None:
    """处理单条解析消息。"""
    async with message.process():
        try:
            body = json.loads(message.body.decode("utf-8"))
            doc_id = body.get("docId")
            file_id = body.get("fileId")
            download_url = body.get("downloadUrl")
            reparse = body.get("reparse", False)

            if not all([doc_id, file_id, download_url]):
                _log.error(f"消息字段缺失: {body}")
                return

            _log.info(f"收到解析任务: doc_id={doc_id}, reparse={reparse}")

            # 在线程池中执行 CPU/IO 密集型解析，避免阻塞事件循环
            loop = asyncio.get_running_loop()
            await loop.run_in_executor(
                None,
                parse_single_document,
                int(doc_id),
                int(file_id),
                download_url,
                bool(reparse),
            )
            _log.info(f"解析任务完成: doc_id={doc_id}")

        except Exception as exc:
            _log.error(f"处理解析消息失败: {exc}")


async def start_parse_consumer() -> asyncio.Task:
    """启动知识库文档解析消费者。

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
    queue = await channel.declare_queue(KNOWLEDGE_PARSE_QUEUE, durable=True)
    await queue.bind(exchange, routing_key=KNOWLEDGE_PARSE_ROUTING_KEY)

    await queue.consume(_on_message)
    _log.info(f"知识库解析消费者已启动，监听队列: {KNOWLEDGE_PARSE_QUEUE}")

    # 返回一个守护任务，保持连接活跃
    async def keepalive():
        try:
            while True:
                await asyncio.sleep(60)
        except asyncio.CancelledError:
            _log.info("知识库解析消费者正在关闭...")
            await connection.close()
            raise

    return asyncio.create_task(keepalive())
