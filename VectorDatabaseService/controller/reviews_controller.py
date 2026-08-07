"""
Controller for operations on the reviews collection.
Route prefix: /api/v1/reviews

Phase 0 - Collection management
  POST /schema/reset              Drop and recreate the collection (destructive demo reset)
  GET  /stats                     Row count and collection info

Phase 1 - CRUD
    POST   /                        Insert a single review
    POST   /batch                   Insert multiple reviews
    GET    /{review_id}             Fetch one review by Milvus ID
    GET    /                        Scalar-filtered listing with pagination
    PUT    /{review_id}             Update one review (upsert)
    DELETE /{review_id}             Delete one review by Milvus ID
    DELETE /batch                   Delete multiple reviews by Milvus IDs

Phase 2 - Query operations
    GET /count                      Count by rating and votes thresholds
    GET /search/semantic            Single-vector search over review_embedding

Phase 3 - Complex queries
    GET /search/hybrid              Hybrid dense+sparse search with RRF
"""

import logging

from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel, Field

from config import REVIEWS_COLLECTION, REVIEWS_TOP_K
from model.review import ReviewCreate, ReviewUpdate
from service.impl.reviews_service import reviews_service

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/reviews", tags=["Reviews"])


class BatchDeleteRequest(BaseModel):
    entity_ids: list[int] = Field(..., min_length=1, max_length=1000)

# ─────────────────────────────────────────────────────────────────────────────
# Phase 0 - Collection management
# ─────────────────────────────────────────────────────────────────────────────

@router.post(
    "/schema/reset",
    summary="[Phase 0] Drop and recreate the collection (destructive demo reset)",
    tags=["Phase 0 - Collection Management"],
)
def reset_schema():
    """Drops and recreates the reviews collection with all indexes."""
    reviews_service.reset_collection()
    return {"message": f"Collection '{REVIEWS_COLLECTION}' has been reset."}


@router.get(
    "/stats",
    summary="[Phase 0] Collection statistics",
    tags=["Phase 0 - Collection Management"],
)
def collection_stats():
    return reviews_service.get_stats()

# ─────────────────────────────────────────────────────────────────────────────
# Phase 2 - Query operations
# ─────────────────────────────────────────────────────────────────────────────

@router.get(
    "/count",
    summary="[Phase 2] Count reviews with rating and votes thresholds",
    tags=["Phase 2 - Query Operations"],
)
def count_reviews(
    min_rating: int = Query(..., ge=0.0, le=5.0, description="Count reviews where rating >= min_rating"),
    min_votes: int = Query(0, ge=0, description="Count reviews where n_votes >= min_votes"),
):
    try:
        return reviews_service.count_by_rating_votes(min_rating, min_votes)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))


@router.get(
    "/search/semantic",
    summary="[Phase 2] Semantic search over review embeddings",
    tags=["Phase 2 - Query Operations"],
)
def semantic_search(
    query: str = Query(..., min_length=1, description="Natural language query"),
    top_k: int = Query(REVIEWS_TOP_K, ge=1, le=100),
):
    try:
        results = reviews_service.semantic_search(query, top_k)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    return {
        "query": query,
        "results": [result.model_dump() for result in results],
    }

# ─────────────────────────────────────────────────────────────────────────────
# Phase 1 - CRUD
# ─────────────────────────────────────────────────────────────────────────────

@router.post(
    "/",
    summary="[Phase 1] Insert a single review",
    tags=["Phase 1 - CRUD"],
)
def create_review(review: ReviewCreate):
    try:
        return reviews_service.create_review(review)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))


@router.post(
    "/batch",
    summary="[Phase 1] Insert multiple reviews in one request",
    tags=["Phase 1 - CRUD"],
)
def batch_create_reviews(reviews: list[ReviewCreate]):
    try:
        return reviews_service.batch_create_reviews(reviews)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    
@router.get(
    "/{review_id}",
    summary="[Phase 1] Fetch one review by Milvus ID",
    tags=["Phase 1 - CRUD"],
)
def get_review(review_id: int):
    try:
        return reviews_service.get_review(review_id).model_dump()
    except KeyError:
        raise HTTPException(status_code=404, detail="Review not found")
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))


@router.get(
    "/",
    summary="[Phase 1] List reviews with scalar filters and pagination",
    tags=["Phase 1 - CRUD"],
)
def list_reviews(
    rating: int | None = Query(None, ge=1, le=5, description="Filter by exact rating"),
    language: str | None = Query(None, description="Filter by language"),
    book_id: int | None = Query(None, ge=1, description="Filter by book ID"),
    limit: int = Query(20, ge=1, le=200),
    offset: int = Query(0, ge=0),
):
    try:
        reviews = reviews_service.list_reviews(rating, language, book_id, limit, offset)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    return {
        "count": len(reviews),
        "results": [review.model_dump() for review in reviews],
    }


@router.put(
    "/{review_id}",
    summary="[Phase 1] Update one review (upsert; re-encodes review_text embedding)",
    tags=["Phase 1 - CRUD"],
)
def update_review(review_id: int, update: ReviewUpdate):
    try:
        return reviews_service.update_review(review_id, update)
    except KeyError:
        raise HTTPException(status_code=404, detail="Review not found")
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))


@router.delete(
    "/batch",
    summary="[Phase 1] Delete multiple reviews by Milvus IDs",
    tags=["Phase 1 - CRUD"],
)
def batch_delete_reviews(request: BatchDeleteRequest):
    try:
        return reviews_service.batch_delete_reviews(request.entity_ids)
    except KeyError:
        raise HTTPException(status_code=404, detail="Review not found")
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))


@router.delete(
    "/{review_id}",
    summary="[Phase 1] Delete one review by Milvus ID",
    tags=["Phase 1 - CRUD"],
)
def delete_review(review_id: int):
    try:
        return reviews_service.delete_review(review_id)
    except KeyError:
        raise HTTPException(status_code=404, detail="Review not found")
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))

# ─────────────────────────────────────────────────────────────────────────────
# Phase 3 - Complex queries
# ─────────────────────────────────────────────────────────────────────────────

@router.get(
    "/search/hybrid",
    summary="[Phase 3] Hybrid reviews search (dense + sparse BM25)",
    tags=["Phase 3 - Complex Queries"],
)
def hybrid_search(
    query: str = Query(..., min_length=1, description="Natural language query"),
    top_k: int = Query(REVIEWS_TOP_K, ge=1, le=100),
):
    try:
        results = reviews_service.hybrid_search(query, top_k)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    return {
        "query": query,
        "results": [result.model_dump() for result in results],
    }
