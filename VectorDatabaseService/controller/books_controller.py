"""
Controller for operations on the books collection.
Route prefix: /api/v1/books

Phase 0 - Collection management
  POST /schema/reset              Drop and recreate the collection (destructive demo reset)
  GET  /stats                     Row count and collection info

Phase 1 - CRUD
    POST   /                        Insert a single book
    POST   /batch                   Insert multiple books
    GET    /{book_id}               Fetch one book by Milvus ID
    GET    /                        Scalar-filtered listing with pagination
    PUT    /{book_id}               Update one book (upsert)
    DELETE /{book_id}               Delete one book by Milvus ID
    DELETE /batch                   Delete multiple books by Milvus IDs

Phase 2 - Query operations
    GET /search/semantic            Single-vector search over description_embedding
    GET /search/cover               Single-vector search over cover_embedding
    GET /search/similar/{book_id}   Multi-vector search with RRF fusion

Phase 3 - Complex queries
    GET  /search/filtered           Vector search + scalar filters (language + page range)
    GET  /search/iterator           Vector similarity over Milvus iterator batches
    POST /search/hybrid             Multimodal dense+dense hybrid search with RRF
"""

import logging

from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel, Field

from config import BOOKS_COLLECTION, BOOKS_TOP_K
from model.book import BookCreate, BookUpdate
from service.impl.books_service import books_service

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/books", tags=["Books"])


class BatchDeleteRequest(BaseModel):
    entity_ids: list[int] = Field(..., min_length=1, max_length=1000)


class BooksHybridSearchRequest(BaseModel):
    query: str = Field(..., min_length=1, description="Text query")
    image_url: str | None = Field(default=None, description="Optional image URL")
    image_base64: str | None = Field(default=None, description="Optional base64 image payload")
    text_weight: float = Field(0.5, ge=0.0, le=1.0, description="0=image only, 1=text only")
    top_k: int = Field(BOOKS_TOP_K, ge=1, le=100)


# ─────────────────────────────────────────────────────────────────────────────
# Phase 0 - Collection management
# ─────────────────────────────────────────────────────────────────────────────

@router.post(
    "/schema/reset",
    summary="[Phase 0] Drop and recreate the collection (destructive demo reset)",
    tags=["Phase 0 - Collection Management"],
)
def reset_schema():
    """Drops and recreates the books collection with all indexes."""
    books_service.reset_collection()
    return {"message": f"Collection '{BOOKS_COLLECTION}' has been reset."}


@router.get(
    "/stats",
    summary="[Phase 0] Collection statistics",
    tags=["Phase 0 - Collection Management"],
)
def collection_stats():
    return books_service.get_stats()

# ─────────────────────────────────────────────────────────────────────────────
# Phase 1 - CRUD
# ─────────────────────────────────────────────────────────────────────────────

@router.post(
    "/",
    summary="[Phase 1] Insert a single book",
    tags=["Phase 1 - CRUD"],
)
def create_book(book: BookCreate):
    try:
        return books_service.create_book(book)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))


@router.post(
    "/batch",
    summary="[Phase 1] Insert multiple books in one request",
    tags=["Phase 1 - CRUD"],
)
def batch_create_books(books: list[BookCreate]):
    try:
        return books_service.batch_create_books(books)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))


@router.get(
    "/{book_id}",
    summary="[Phase 1] Fetch one book by Milvus ID",
    tags=["Phase 1 - CRUD"],
)
def get_book(book_id: int):
    try:
        return books_service.get_book(book_id).model_dump(by_alias=True)
    except KeyError:
        raise HTTPException(status_code=404, detail="Book not found")
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))


@router.get(
    "/",
    summary="[Phase 1] List books with scalar filters and pagination",
    tags=["Phase 1 - CRUD"],
)
def list_books(
    language: str | None = Query(None, description="Filter by language"),
    author: str | None = Query(None, description="Filter by author"),
    publisher: str | None = Query(None, description="Filter by publisher"),
    has_image: bool | None = Query(None, description="Filter by image availability"),
    limit: int = Query(20, ge=1, le=200),
    offset: int = Query(0, ge=0),
):
    try:
        books = books_service.list_books(language, author, publisher, has_image, limit, offset)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    return {
        "count": len(books),
        "results": [book.model_dump(by_alias=True) for book in books],
    }


@router.put(
    "/{book_id}",
    summary="[Phase 1] Update one book (upsert; re-encodes changed fields)",
    tags=["Phase 1 - CRUD"],
)
def update_book(book_id: int, update: BookUpdate):
    try:
        return books_service.update_book(book_id, update)
    except KeyError:
        raise HTTPException(status_code=404, detail="Book not found")
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))


@router.delete(
    "/batch",
    summary="[Phase 1] Delete multiple books by Milvus IDs",
    tags=["Phase 1 - CRUD"],
)
def batch_delete_books(request: BatchDeleteRequest):
    try:
        return books_service.batch_delete_books(request.entity_ids)
    except KeyError:
        raise HTTPException(status_code=404, detail="Book not found")
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))


@router.delete(
    "/{book_id}",
    summary="[Phase 1] Delete one book by Milvus ID",
    tags=["Phase 1 - CRUD"],
)
def delete_book(book_id: int):
    try:
        return books_service.delete_book(book_id)
    except KeyError:
        raise HTTPException(status_code=404, detail="Book not found")
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))

# ─────────────────────────────────────────────────────────────────────────────
# Phase 2 - Query operations
# ─────────────────────────────────────────────────────────────────────────────

@router.get(
    "/search/semantic",
    summary="[Phase 2] Semantic search over book descriptions",
    tags=["Phase 2 - Query Operations"],
)
def semantic_search(
    query: str = Query(..., min_length=1, description="Natural language query"),
    top_k: int = Query(BOOKS_TOP_K, ge=1, le=100),
):
    try:
        results = books_service.semantic_search(query, top_k)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    return {
        "query": query,
        "results": [result.model_dump(by_alias=True) for result in results],
    }


@router.get(
    "/search/cover",
    summary="[Phase 2] Single-vector search over cover embeddings",
    tags=["Phase 2 - Query Operations"],
)
def cover_search(
    query: str = Query(..., min_length=1, description="Text query for visual cover similarity"),
    top_k: int = Query(BOOKS_TOP_K, ge=1, le=100),
):
    try:
        results = books_service.cover_search(query, top_k)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    return {
        "query": query,
        "results": [result.model_dump(by_alias=True) for result in results],
    }

@router.get(
    "/search/similar/{book_id}",
    summary="[Phase 2] Multi-vector similar books (description + cover, RRF fusion)",
    tags=["Phase 2 - Query Operations"],
)
def similar_books_search(
    book_id: int,
    top_k: int = Query(BOOKS_TOP_K, ge=1, le=100),
    text_weight: float = Query(0.5, ge=0.0, le=1.0),
):
    try:
        return books_service.find_similar(book_id, text_weight, top_k)
    except KeyError:
        raise HTTPException(status_code=404, detail="Book not found")
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    
# ─────────────────────────────────────────────────────────────────────────────
# Phase 3 - Complex queries
# ─────────────────────────────────────────────────────────────────────────────

@router.get(
    "/search/filtered",
    summary="[Phase 3] Filtered semantic search (language + pages range)",
    tags=["Phase 3 - Complex Queries"],
)
def filtered_semantic_search(
    query: str = Query(..., min_length=1, description="Natural language query"),
    language: str = Query(..., min_length=2, description="Language filter"),
    min_pages: int = Query(..., ge=0, description="Minimum pages"),
    max_pages: int = Query(..., ge=0, description="Maximum pages"),
    top_k: int = Query(BOOKS_TOP_K, ge=1, le=100),
):
    try:
        results = books_service.filtered_semantic_search(query, language, min_pages, max_pages, top_k)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    return {
        "query": query,
        "filters": {
            "language": language,
            "min_pages": min_pages,
            "max_pages": max_pages,
        },
        "results": [result.model_dump(by_alias=True) for result in results],
    }


@router.get(
    "/search/iterator",
    summary="[Phase 3] Iterator-based semantic search (author + pages range)",
    tags=["Phase 3 - Complex Queries"],
)
def iterator_semantic_search(
    query: str = Query(..., min_length=1, description="Natural language query"),
    author: str = Query(..., min_length=1, description="Author filter"),
    min_pages: int = Query(..., ge=0, description="Minimum pages"),
    max_pages: int = Query(..., ge=0, description="Maximum pages"),
    top_k: int = Query(BOOKS_TOP_K, ge=1, le=100),
    batch_size: int = Query(50, ge=1, le=500, description="Iterator batch size"),
):
    try:
        results = books_service.iterator_semantic_search(
            query,
            author,
            min_pages,
            max_pages,
            top_k,
            batch_size,
        )
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    return {
        "query": query,
        "filters": {
            "author": author,
            "min_pages": min_pages,
            "max_pages": max_pages,
        },
        "batch_size": batch_size,
        "results": [result.model_dump(by_alias=True) for result in results],
    }


@router.post(
    "/search/hybrid",
    summary="[Phase 3] Multimodal hybrid search (text + image)",
    tags=["Phase 3 - Complex Queries"],
)
def multimodal_hybrid_search(request: BooksHybridSearchRequest):
    has_url = bool((request.image_url or "").strip())
    has_base64 = bool((request.image_base64 or "").strip())
    if has_url == has_base64:
        raise HTTPException(status_code=422, detail="Provide exactly one of image_url or image_base64")

    try:
        return books_service.hybrid_multimodal_search(
            query=request.query,
            image_url=request.image_url,
            image_base64=request.image_base64,
            text_weight=request.text_weight,
            top_k=request.top_k,
        )
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))