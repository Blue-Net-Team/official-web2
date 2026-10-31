from fastapi import APIRouter
from .chat import router as chat_router
from .judge_config_draft import router as judge_config_draft_router

router = APIRouter(prefix="/ai/v1")
router.include_router(chat_router)
router.include_router(judge_config_draft_router)