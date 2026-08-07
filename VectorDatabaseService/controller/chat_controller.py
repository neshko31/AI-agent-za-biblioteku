import logging

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field, model_validator

from service.impl.books_chat_service import books_chat_service
from service.impl.reviews_chat_service import reviews_chat_service
from services.llm_service import llm_service
from config import OLLAMA_MODEL

logger = logging.getLogger(__name__)
router = APIRouter(tags=["AI Chat"])


class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=500)
    image_url: str | None = Field(default=None, description="Books-only. Mutually exclusive with image_base64.")
    image_base64: str | None = Field(default=None, description="Books-only. Mutually exclusive with image_url.")

    @model_validator(mode="after")
    def _at_most_one_image_source(self):
        if self.image_url and self.image_base64:
            raise ValueError("Provide at most one of image_url or image_base64, not both.")
        return self


@router.get("/api/v1/chat/health", summary="Check if Ollama LLM is reachable")
def chat_health():
    available = llm_service.is_available()
    return {"ollama_available": available, "model": OLLAMA_MODEL}


@router.post(
    "/api/v1/books/chat",
    summary="Books assistant — retrieves top-k books and asks Ollama to answer with book context",
    description=(
        "1. Extracts optional search filters (language/author/publisher/page range) from the "
        "message text via a constrained LLM call — the user never has to fill in separate fields.\n"
        "2. If an image is attached, runs multimodal hybrid search (CLIP text + image embeddings, "
        "RRF-fused) over description_embedding and cover_embedding, narrowed by any extracted filters.\n"
        "   Otherwise runs filtered or plain semantic search on description_embedding.\n"
        "3. Builds a prompt with title, author, and description from top-k books.\n"
        "4. Sends to Ollama and returns the answer/recommendation."
    ),
)
def books_chat(request: ChatRequest):
    try:
        return books_chat_service.chat(
            request.message,
            image_url=request.image_url,
            image_base64=request.image_base64,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc))
    except Exception as exc:
        logger.error("Books chat error: %s", exc)
        raise HTTPException(status_code=500, detail=str(exc))


@router.post(
    "/api/v1/reviews/chat",
    summary="Reviews assistant — hybrid retrieval (dense + BM25) then Ollama answer",
    description=(
        "1. Runs hybrid retrieval on reviews (review_embedding + sparse_bm25).\n"
        "2. Builds a prompt with top-k review excerpts as context.\n"
        "3. Sends to Ollama and returns the answer."
    ),
)
def reviews_chat(request: ChatRequest):
    try:
        return reviews_chat_service.chat(request.message)
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc))
    except Exception as exc:
        logger.error("Reviews chat error: %s", exc)
        raise HTTPException(status_code=500, detail=str(exc))