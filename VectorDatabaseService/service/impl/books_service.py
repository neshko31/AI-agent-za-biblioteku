"""
Concrete implementation of IBooksService for the books collection.
"""

import heapq
import logging
from concurrent.futures import ThreadPoolExecutor

from extraction.books_filter_extraction import BookSearchFilters
from model.book import Book, BookCreate, BookSearchResult, BookUpdate
from repository.books_repository import books_repository
from service.i_books_service import IBooksService
from services.embedding_service import embedding_service

logger = logging.getLogger(__name__)


def _sanitize_string(value: str) -> str:
    if not isinstance(value, str):
        return str(value)
    return value.replace("\\", "\\\\").replace('"', '\\"')


def _build_filter(
    language: str | None,
    author: str | None,
    publisher: str | None,
    has_image: bool | None,
    min_pages: int | None = None,
    max_pages: int | None = None,
) -> str:
    clauses = []
    if language and language.strip():
        clauses.append(f'language == "{_sanitize_string(language.strip())}"')
    if author and author.strip():
        clauses.append(f'author == "{_sanitize_string(author.strip())}"')
    if publisher and publisher.strip():
        clauses.append(f'publisher == "{_sanitize_string(publisher.strip())}"')
    if has_image is True:
        clauses.append("has_image == true")
    elif has_image is False:
        clauses.append("has_image == false")
    if min_pages is not None:
        clauses.append(f"pages >= {int(min_pages)}")
    if max_pages is not None:
        clauses.append(f"pages <= {int(max_pages)}")
    return " && ".join(clauses)


def _to_book_record(
    book: BookCreate,
    description_embedding: list[float],
    cover_embedding: list[float],
    has_image: bool,
) -> dict:
    return {
        "goodreads_id": str(book.goodreads_id),
        "isbn": book.isbn,
        "title": book.title,
        "author": book.author,
        "description": book.description,
        "language": book.language,
        "coverImg": (book.cover_img or ""),
        "publisher": book.publisher,
        "pages": book.pages,
        "has_image": has_image,
        "description_embedding": description_embedding,
        "cover_embedding": cover_embedding,
    }


def _build_cover_embedding(cover_url: str) -> tuple[list[float], bool]:
    cleaned_url = (cover_url or "").strip()
    if not cleaned_url:
        return embedding_service.zero_vector(), False

    try:
        return embedding_service.encode_from_url(cleaned_url), True
    except Exception as exc:
        logger.warning("Failed to encode cover image from URL: %s", exc)
        return embedding_service.zero_vector(), False


def _fuse_scores(
    hits_a: list[dict],
    hits_b: list[dict],
    weight_a: float = 0.5,
    top_k: int = 10,
    k: int = 60,
) -> list[dict]:
    scores: dict[int, dict] = {}
    weight_b = 1.0 - weight_a

    for rank, hit in enumerate(hits_a, start=1):
        doc_id = hit["id"]
        rrf = weight_a / (rank + k)
        if doc_id not in scores:
            scores[doc_id] = {"fused_score": 0.0, **hit}
        scores[doc_id]["fused_score"] += rrf

    for rank, hit in enumerate(hits_b, start=1):
        doc_id = hit["id"]
        rrf = weight_b / (rank + k)
        if doc_id not in scores:
            scores[doc_id] = {"fused_score": 0.0, **hit}
        else:
            for key, value in hit.items():
                if key not in scores[doc_id] or key == "id":
                    scores[doc_id][key] = value
        scores[doc_id]["fused_score"] += rrf

    ranked = sorted(scores.values(), key=lambda item: item["fused_score"], reverse=True)
    return ranked[:top_k]


def _to_book_search_result(row: dict) -> BookSearchResult:
    goodreads_raw = row.get("goodreads_id")
    try:
        goodreads_id = int(goodreads_raw)
    except (TypeError, ValueError):
        goodreads_id = 1

    return BookSearchResult(
        id=row["id"],
        goodreads_id=goodreads_id,
        isbn=row.get("isbn", ""),
        title=row.get("title", ""),
        author=row.get("author", ""),
        description=row.get("description", ""),
        language=row.get("language", "en"),
        coverImg=row.get("coverImg"),
        publisher=row.get("publisher", ""),
        pages=row.get("pages", 0),
        has_image=row.get("has_image", False),
        score=row.get("score", 0.0),
        fused_score=row.get("fused_score"),
    )


def _build_author_pages_filter(author: str, min_pages: int, max_pages: int) -> str:
    safe_author = _sanitize_string(author.strip())
    return f'author == "{safe_author}" && pages >= {int(min_pages)} && pages <= {int(max_pages)}'


class BooksService(IBooksService):
    """Implementation of books collection management operations."""

    def create_book(self, book: BookCreate) -> dict:
        description_embedding = embedding_service.encode_text_one(book.description)
        cover_embedding, has_image = _build_cover_embedding(book.cover_img or "")

        record = _to_book_record(book, description_embedding, cover_embedding, has_image)
        result = books_repository.insert([record])
        return {
            "inserted_ids": list(result.get("ids", [])),       
            "insert_count": int(result.get("insert_count", 0)), 
        }

    def batch_create_books(self, books: list[BookCreate]) -> dict:
        if not books:
            raise ValueError("Books list cannot be empty")
        if len(books) > 1000:
            raise ValueError("Maximum 1000 books per batch request")

        descriptions = [book.description for book in books]
        description_embeddings = embedding_service.encode_text(descriptions)

        records = []
        for book, description_embedding in zip(books, description_embeddings):
            cover_embedding, has_image = _build_cover_embedding(book.cover_img or "")
            records.append(_to_book_record(book, description_embedding, cover_embedding, has_image))

        results = books_repository.batch_insert(records, batch_size=100)
        inserted_ids = []
        insert_count = 0
        for result in results:
            insert_count += int(result.get("insert_count", 0))
            inserted_ids.extend(result.get("ids", []))

        return {
            "insert_count": insert_count,
            "inserted_ids": inserted_ids,
        }

    def get_book(self, book_id: int) -> Book:
        if book_id <= 0:
            raise ValueError("Book ID must be positive")

        row = books_repository.find_by_id(book_id)
        if row is None:
            raise KeyError(f"Book {book_id} not found")
        return Book(**row)

    def list_books(
        self,
        language: str | None,
        author: str | None,
        publisher: str | None,
        has_image: bool | None,
        limit: int,
        offset: int,
    ) -> list[Book]:
        if limit <= 0:
            raise ValueError("Limit must be positive")
        if offset < 0:
            raise ValueError("Offset cannot be negative")

        filter_expr = _build_filter(language, author, publisher, has_image)
        rows = books_repository.find_all(filter_expr, limit=limit, offset=offset)
        return [Book(**row) for row in rows]

    def update_book(self, book_id: int, update: BookUpdate) -> dict:
        if book_id <= 0:
            raise ValueError("Book ID must be positive")

        record = books_repository.find_by_id_with_vectors(book_id)
        if record is None:
            raise KeyError(f"Book {book_id} not found")
        if "id" not in record:
            raise ValueError("Primary key 'id' missing from record")

        update_data = update.model_dump(exclude_unset=True, by_alias=True)
        # has_image is derived from cover availability/encoding and cannot be set directly
        update_data.pop("has_image", None)
        for key, value in update_data.items():
            if key != "coverImg" and value is None:
                raise ValueError(f"Field '{key}' cannot be null")

        if "coverImg" in update_data and update_data["coverImg"] is None:
            update_data["coverImg"] = ""

        if "goodreads_id" in update_data and update_data["goodreads_id"] is not None:
            update_data["goodreads_id"] = str(update_data["goodreads_id"])

        if not update_data:
            return {"upserted_count": 0}

        description_changed = "description" in update_data
        cover_changed = "coverImg" in update_data

        record.update(update_data)

        if description_changed:
            record["description_embedding"] = embedding_service.encode_text_one(record["description"])

        if cover_changed:
            record["cover_embedding"], record["has_image"] = _build_cover_embedding(record.get("coverImg") or "")

        result = books_repository.upsert(record)
        return {"upserted_count": result.get("upsert_count", 0)}

    def delete_book(self, book_id: int) -> dict:
        if book_id <= 0:
            raise ValueError("Book ID must be positive")

        if books_repository.find_by_id(book_id) is None:
            raise KeyError(f"Book {book_id} not found")

        result = books_repository.delete_by_id(book_id)
        return {"delete_count": result.get("delete_count", 0)}

    def batch_delete_books(self, entity_ids: list[int]) -> dict:
        if not entity_ids:
            raise ValueError("Entity IDs list cannot be empty")
        if len(entity_ids) > 1000:
            raise ValueError("Maximum 1000 IDs per batch delete request")
        if any(entity_id <= 0 for entity_id in entity_ids):
            raise ValueError("All entity IDs must be positive")

        existing_ids = books_repository.find_existing_ids(entity_ids)
        missing_ids = [entity_id for entity_id in entity_ids if entity_id not in existing_ids]
        if missing_ids:
            raise KeyError(f"Books not found: {missing_ids}")

        result = books_repository.batch_delete(entity_ids)
        return {"delete_count": result.get("delete_count", 0)}

    def semantic_search(self, query: str, top_k: int) -> list[BookSearchResult]:
        if not query or not query.strip():
            raise ValueError("Query cannot be empty")
        if top_k <= 0:
            raise ValueError("top_k must be positive")

        query_vector = embedding_service.encode_text_one(query)
        hits = books_repository.search_description([query_vector], top_k=top_k)[0]
        return [_to_book_search_result(hit) for hit in hits]

    def cover_search(self, query: str, top_k: int) -> list[BookSearchResult]:
        if not query or not query.strip():
            raise ValueError("Query cannot be empty")
        if top_k <= 0:
            raise ValueError("top_k must be positive")

        query_vector = embedding_service.encode_text_one(query)
        hits = books_repository.search_cover(
            [query_vector], top_k=top_k, filter_expr="has_image == true",
        )[0]
        return [_to_book_search_result(hit) for hit in hits]

    def filtered_semantic_search(
        self,
        query: str,
        language: str,
        min_pages: int,
        max_pages: int,
        top_k: int,
    ) -> list[BookSearchResult]:
        if not query or not query.strip():
            raise ValueError("Query cannot be empty")
        if not language or not language.strip():
            raise ValueError("language cannot be empty")
        if min_pages < 0 or max_pages < 0:
            raise ValueError("Page range must be non-negative")
        if min_pages > max_pages:
            raise ValueError("min_pages cannot be greater than max_pages")
        if top_k <= 0:
            raise ValueError("top_k must be positive")

        query_vector = embedding_service.encode_text_one(query.strip())
        hits = books_repository.search_description_with_filters(
            [query_vector],
            language=language.strip(),
            min_pages=min_pages,
            max_pages=max_pages,
            top_k=top_k,
        )[0]
        return [_to_book_search_result(hit) for hit in hits]

    def multi_filtered_semantic_search(
        self,
        query: str,
        filters: BookSearchFilters,
        top_k: int,
    ) -> list[BookSearchResult]:
        """Vector search over description_embedding with any combination of
        scalar filters (language/author/publisher/page range).

        Unlike `filtered_semantic_search` (which is fixed to language + a
        required page range, mirroring an older fixed-schema endpoint), this
        accepts whatever subset of filters was actually extracted from a
        chat message — including author/publisher alone, with no language
        or page range at all.
        """
        if not query or not query.strip():
            raise ValueError("Query cannot be empty")
        if top_k <= 0:
            raise ValueError("top_k must be positive")
        if filters.is_empty():
            raise ValueError("At least one filter must be set; use semantic_search for unfiltered queries")

        filter_expr = _build_filter(
            language=filters.language,
            author=filters.author,
            publisher=filters.publisher,
            has_image=None,
            min_pages=filters.min_pages,
            max_pages=filters.max_pages,
        )

        query_vector = embedding_service.encode_text_one(query.strip())
        hits = books_repository.search_description([query_vector], top_k, filter_expr)[0]
        return [_to_book_search_result(hit) for hit in hits]

    def iterator_semantic_search(
        self,
        query: str,
        author: str,
        min_pages: int,
        max_pages: int,
        top_k: int,
        batch_size: int,
    ) -> list[BookSearchResult]:
        if not query or not query.strip():
            raise ValueError("Query cannot be empty")
        if not author or not author.strip():
            raise ValueError("author cannot be empty")
        if min_pages < 0 or max_pages < 0:
            raise ValueError("Page range must be non-negative")
        if min_pages > max_pages:
            raise ValueError("min_pages cannot be greater than max_pages")
        if top_k <= 0:
            raise ValueError("top_k must be positive")
        if batch_size <= 0:
            raise ValueError("batch_size must be positive")

        query_vector = embedding_service.encode_text_one(query.strip())
        filter_expr = _build_author_pages_filter(author, min_pages, max_pages)

        # Maintain only top_k rows in memory while scanning iterator batches.
        heap: list[tuple[float, int, dict]] = []
        for batch in books_repository.iterate_filtered_with_vectors(filter_expr, batch_size=batch_size):
            for row in batch:
                emb = row.get("description_embedding")
                if not emb:
                    continue

                score = round(embedding_service.cosine_similarity(query_vector, emb), 6)
                scored_row = dict(row)
                scored_row["score"] = score
                row_id = int(scored_row.get("id", 0))

                if len(heap) < top_k:
                    heapq.heappush(heap, (score, row_id, scored_row))
                    continue

                if score > heap[0][0]:
                    heapq.heapreplace(heap, (score, row_id, scored_row))

        ranked_rows = [item[2] for item in sorted(heap, key=lambda entry: entry[0], reverse=True)]
        return [_to_book_search_result(row) for row in ranked_rows]

    def hybrid_multimodal_search(
        self,
        query: str,
        image_url: str | None,
        image_base64: str | None,
        text_weight: float,
        top_k: int,
        filters: BookSearchFilters | None = None,
    ) -> dict:
        if not query or not query.strip():
            raise ValueError("Query cannot be empty")
        if not 0.0 <= text_weight <= 1.0:
            raise ValueError("text_weight must be between 0.0 and 1.0")
        if top_k <= 0:
            raise ValueError("top_k must be positive")

        has_url = bool((image_url or "").strip())
        has_base64 = bool((image_base64 or "").strip())
        if has_url == has_base64:
            raise ValueError("Provide exactly one of image_url or image_base64")

        text_vector = embedding_service.encode_text_one(query.strip())

        if has_url:
            image_vector = embedding_service.encode_from_url(image_url.strip())
            image_source = "url"
        else:
            image_vector = embedding_service.encode_from_base64(image_base64.strip())
            image_source = "base64"

        # Scalar filters extracted from the chat message (if any) apply to both
        # recall legs. The cover leg additionally always requires has_image,
        # since there is nothing useful to compare a query image against
        # otherwise — _build_filter combines both conditions into one expr.
        user_filter_expr = ""
        if filters is not None and not filters.is_empty():
            user_filter_expr = _build_filter(
                language=filters.language,
                author=filters.author,
                publisher=filters.publisher,
                has_image=None,
                min_pages=filters.min_pages,
                max_pages=filters.max_pages,
            )

        text_filter_expr = user_filter_expr
        cover_filter_expr = (
            f"({user_filter_expr}) && has_image == true" if user_filter_expr else "has_image == true"
        )

        recall_k = max(top_k * 3, 50)
        with ThreadPoolExecutor(max_workers=2) as executor:
            text_future = executor.submit(
                books_repository.search_description, [text_vector], recall_k, text_filter_expr,
            )
            cover_future = executor.submit(
                books_repository.search_cover,
                [image_vector],
                recall_k,
                cover_filter_expr,
            )
            text_hits = text_future.result()[0]
            cover_hits = cover_future.result()[0]

        fused = _fuse_scores(text_hits, cover_hits, weight_a=text_weight, top_k=top_k)
        return {
            "query": query,
            "text_encoder": "CLIP",
            "image_source": image_source,
            "text_weight": text_weight,
            "image_weight": 1 - text_weight,
            "applied_filters": filters.model_dump() if filters is not None else None,
            "results": [_to_book_search_result(hit).model_dump(by_alias=True) for hit in fused],
        }

    def find_similar(self, book_id: int, text_weight: float, top_k: int) -> dict:
        if book_id <= 0:
            raise ValueError("Book ID must be positive")
        if not 0.0 <= text_weight <= 1.0:
            raise ValueError("text_weight must be between 0.0 and 1.0")
        if top_k <= 0:
            raise ValueError("top_k must be positive")

        record = books_repository.find_by_id_with_vectors(book_id)
        if record is None:
            raise KeyError(f"Book {book_id} not found")

        text_emb = record.get("description_embedding")
        cover_emb = record.get("cover_embedding")
        if text_emb is None or cover_emb is None:
            raise ValueError("Book is missing one or both embedding vectors")

        recall_k = max(top_k * 3, 50)
        text_hits = books_repository.search_description([text_emb], top_k=recall_k)[0]
        cover_hits = books_repository.search_cover(
            [cover_emb], top_k=recall_k, filter_expr="has_image == true",
        )[0]

        text_hits = [hit for hit in text_hits if hit["id"] != book_id]
        cover_hits = [hit for hit in cover_hits if hit["id"] != book_id]

        fused = _fuse_scores(text_hits, cover_hits, weight_a=text_weight, top_k=top_k)
        return {
            "book_id": book_id,
            "text_weight": text_weight,
            "cover_weight": 1 - text_weight,
            "results": [_to_book_search_result(hit).model_dump(by_alias=True) for hit in fused],
        }

    def get_stats(self) -> dict:
        return books_repository.get_stats()

    def reset_collection(self) -> None:
        books_repository.reset()


books_service = BooksService()