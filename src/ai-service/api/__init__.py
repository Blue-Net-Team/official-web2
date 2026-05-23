from fastapi import APIRouter
from .chat import router as chat_router

router = APIRouter(prefix="/ai/v1")
router.include_router(chat_router)