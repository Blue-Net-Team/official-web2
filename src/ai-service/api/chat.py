"""FastAPI Chat API —— 暴露 RagAgent 对话能力。

流式接口在转发事件的同时采集执行轨迹（best-effort 落库），
并以下发首帧 ``conversation_id`` 的方式确立会话标识的服务端权威。
"""

from __future__ import annotations

import json
import uuid
from typing import Iterator

from fastapi import APIRouter, HTTPException
from loguru import logger
from pydantic import BaseModel, Field

from agent.agent import RagAgent
from agent.types import StreamChunk
from trace.capture import run_traced_stream
from trace.store import TraceStore, get_trace_store

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

    conversation_id 由服务端权威生成：请求未携带时生成新标识并回传给客户端，
    客户端此后应复用该标识。
    """
    cid = conversation_id or str(uuid.uuid4())
    if cid not in _conversations:
        _conversations[cid] = RagAgent(thread_id=cid)
        _log.info(f"新建会话: {cid}")
    return cid, _conversations[cid]


# ---------------------------------------------------------------------------
# SSE 帧构造
# ---------------------------------------------------------------------------

def _sse_frame(payload: dict) -> str:
    return f"data: {json.dumps(payload, ensure_ascii=False)}\n\n"


def _chunk_to_sse_data(chunk: StreamChunk) -> dict:
    """把 StreamChunk 转成 SSE 负载。

    只携带既有契约字段（type / content / tool_name / tool_args），
    轮次与拦截标记等采集元信息仅进入轨迹，不下发客户端。
    """
    data: dict = {"type": chunk.type, "content": chunk.content}
    if chunk.tool_name:
        data["tool_name"] = chunk.tool_name
    if chunk.tool_args:
        data["tool_args"] = chunk.tool_args
    return data


def build_sse_stream(
    agent: RagAgent,
    user_input: str,
    conversation_id: str,
    store: TraceStore | None,
) -> Iterator[str]:
    """构造 SSE 帧序列。

    首帧为 ``conversation_id``，其后转发客户端可见事件，同时采集完整轨迹。
    """
    yield _sse_frame({"type": "conversation_id", "conversation_id": conversation_id})
    for chunk in run_traced_stream(agent, user_input, conversation_id, store):
        yield _sse_frame(_chunk_to_sse_data(chunk))


# ---------------------------------------------------------------------------
# 路由
# ---------------------------------------------------------------------------

@router.post("/", response_model=ChatResponse)
async def chat(req: ChatRequest) -> ChatResponse:
    """非流式对话接口。

    注意：该路径不产生流式事件，因此不采集轨迹（前端使用的是 /chat/stream）。
    """
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
    store = get_trace_store()

    def _event_generator():
        stream = build_sse_stream(agent, req.message, cid, store)
        try:
            yield from stream
        except Exception as exc:
            _log.error(f"流式对话异常: {exc}")
            yield _sse_frame({"type": "error", "content": str(exc)})
        finally:
            close = getattr(stream, "close", None)
            if callable(close):
                close()

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
