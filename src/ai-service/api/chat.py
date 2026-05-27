"""FastAPI Chat API —— 暴露 RagAgent 对话能力。"""

from __future__ import annotations

import json

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from loguru import logger
import uuid

from agent.agent import RagAgent
from agent.types import StreamChunk

_log = logger.bind(module="ChatAPI")

router = APIRouter(prefix="/chat", tags=["Chat"])


# ---------------------------------------------------------------------------
# 请求 / 响应模型
# ---------------------------------------------------------------------------

class ChatRequest(BaseModel):
    """单次对话请求。"""

    message: str = Field(..., min_length=1, description="用户输入内容")
    conversation_id: str | None = Field(None, description="会话标识（可选，用于多轮对话）")


class ChatResponse(BaseModel):
    """非流式对话响应。"""

    reply: str = Field(..., description="助手回复内容")
    reasoning: str | None = Field(None, description="助手思考过程")
    conversation_id: str | None = Field(None, description="会话标识")


class ResetResponse(BaseModel):
    """重置会话响应。"""

    success: bool = Field(..., description="是否重置成功")
    conversation_id: str | None = Field(None, description="被重置的会话标识")


# ---------------------------------------------------------------------------
# 会话管理（内存级，生产环境建议换 Redis）
# ---------------------------------------------------------------------------

_conversations: dict[str, RagAgent] = {}


def _get_or_create_agent(conversation_id: str | None) -> tuple[str, RagAgent]:
    """根据 conversation_id 获取或新建 RagAgent。

    如果不传 conversation_id，自动生成一个新的唯一会话 ID，
    确保每个未指定会话的请求都有独立的上下文。
    """
    cid = conversation_id or str(uuid.uuid4())
    if cid not in _conversations:
        _conversations[cid] = RagAgent()
        _log.info(f"新建会话: {cid}")
    return cid, _conversations[cid]


# ---------------------------------------------------------------------------
# 路由
# ---------------------------------------------------------------------------

@router.post("/", response_model=ChatResponse)
async def chat(req: ChatRequest) -> ChatResponse:
    """非流式对话接口。"""
    cid, agent = _get_or_create_agent(req.conversation_id)

    try:
        response = agent.chat(req.message)
    except Exception as exc:
        _log.error(f"对话异常: {exc}")
        raise HTTPException(status_code=500, detail=f"对话处理失败: {exc}") from exc

    return ChatResponse(reply=response.content, reasoning=response.reasoning or None, conversation_id=cid)


@router.post("/stream")
async def chat_stream(req: ChatRequest):
    """流式对话接口（SSE）。"""
    from fastapi.responses import StreamingResponse

    cid, agent = _get_or_create_agent(req.conversation_id)

    def _event_generator():
        try:
            for chunk in agent.chat_stream(req.message):
                if isinstance(chunk, StreamChunk):
                    data = {
                        "type": chunk.type,
                        "content": chunk.content,
                    }
                    if chunk.tool_name:
                        data["tool_name"] = chunk.tool_name
                    if chunk.tool_args:
                        data["tool_args"] = chunk.tool_args
                    yield f"data: {json.dumps(data, ensure_ascii=False)}\n\n"
                else:
                    # 兼容旧格式（字符串）
                    yield f"data: {json.dumps({'type': 'content', 'content': str(chunk)}, ensure_ascii=False)}\n\n"
        except Exception as exc:
            _log.error(f"流式对话异常: {exc}")
            yield f"data: {json.dumps({'type': 'error', 'content': str(exc)}, ensure_ascii=False)}\n\n"

    return StreamingResponse(
        _event_generator(),
        media_type="text/event-stream",
    )


@router.post("/reset", response_model=ResetResponse)
async def reset_conversation(conversation_id: str | None = None) -> ResetResponse:
    """重置指定会话的历史记录。"""
    cid = conversation_id or "default"
    agent = _conversations.get(cid)
    if agent is None:
        return ResetResponse(success=False, conversation_id=cid)

    agent.reset_conversation()
    return ResetResponse(success=True, conversation_id=cid)
