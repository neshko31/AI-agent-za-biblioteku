"""
Health controller - HTTP routes for service availability checks.
"""

import logging

from fastapi import APIRouter

from services.milvus_service import milvus_service

logger = logging.getLogger(__name__)
router = APIRouter(tags=["Health"])


@router.get("/health", summary="Service availability check")
def health():
    return {"status": "ok"}


@router.get("/health/milvus", summary="Milvus connectivity check")
def milvus_health():
    try:
        collections = milvus_service.client.list_collections()
        return {"status": "ok", "collections": collections}
    except Exception as exc:
        logger.exception("Milvus health check failed")
        return {"status": "error", "detail": str(exc)}


@router.get("/collections/{collection_name}/stats", summary="Stats for any collection")
def collection_stats(collection_name: str):
    try:
        return milvus_service.collection_stats(collection_name)
    except Exception as exc:
        return {"error": str(exc)}
