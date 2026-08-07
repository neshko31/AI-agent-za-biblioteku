"""
Concrete implementation of IReviewsService for the reviews collection.
"""

import logging

from model.review import Review, ReviewCreate, ReviewSearchResult, ReviewUpdate
from repository.reviews_repository import reviews_repository
from service.i_reviews_service import IReviewsService
from services.minilm_embedding_service import minilm_service

logger = logging.getLogger(__name__)


def _sanitize_string(value: str) -> str:
    if not isinstance(value, str):
        return str(value)
    return value.replace("\\", "\\\\").replace('"', '\\"')


def _normalize_rating(value: float) -> int:
    rounded = int(round(float(value)))
    if rounded < 1 or rounded > 5:
        raise ValueError("Rating must be between 1 and 5")
    return rounded


def _build_filter(
    rating: int | None,
    language: str | None,
    book_id: int | None,
) -> str:
    clauses = []
    if rating is not None:
        clauses.append(f"rating == {_normalize_rating(rating)}")
    if language and language.strip():
        clauses.append(f'language == "{_sanitize_string(language.strip())}"')
    if book_id is not None:
        clauses.append(f'book_id == {book_id}')
    return " && ".join(clauses)


def _to_review_record(review: ReviewCreate, review_embedding: list[float]) -> dict:
    return {
        "review_id": review.review_id,
        "book_id": str(review.book_id),
        "isbn": review.isbn,
        "language": review.language,
        "rating": _normalize_rating(review.rating),
        "n_votes": int(review.n_votes),
        "date_added": int(review.date_added),
        "review_text": review.review_text,
        "review_embedding": review_embedding,
    }


def _to_review_search_result(row: dict) -> ReviewSearchResult:
    book_raw = row.get("book_id")
    try:
        book_id = int(book_raw)
    except (TypeError, ValueError):
        book_id = 1

    return ReviewSearchResult(
        id=row["id"],
        review_id=row.get("review_id", ""),
        book_id=book_id,
        isbn=row.get("isbn", ""),
        language=row.get("language", "en"),
        rating=float(row.get("rating", 1.0)),
        n_votes=int(row.get("n_votes", 0)),
        date_added=int(row.get("date_added", 0)),
        review_text=row.get("review_text", ""),
        score=row.get("score", 0.0),
        fused_score=row.get("fused_score"),
    )


class ReviewsService(IReviewsService):
    """Implementation of reviews collection management operations."""

    def create_review(self, review: ReviewCreate) -> dict:
        review_embedding = minilm_service.encode_one(review.review_text)
        record = _to_review_record(review, review_embedding)
        result = reviews_repository.insert([record])
        return {
            "inserted_ids": list(result.get("ids", [])),        # ← dodaj list()
            "insert_count": int(result.get("insert_count", 0)), # ← dodaj int()
        }

    def batch_create_reviews(self, reviews: list[ReviewCreate]) -> dict:
        if not reviews:
            raise ValueError("Reviews list cannot be empty")
        if len(reviews) > 1000:
            raise ValueError("Maximum 1000 reviews per batch request")

        texts = [review.review_text for review in reviews]
        embeddings = minilm_service.encode(texts)
        records = [_to_review_record(review, emb) for review, emb in zip(reviews, embeddings)]

        results = reviews_repository.batch_insert(records, batch_size=100)
        inserted_ids = []
        insert_count = 0
        for result in results:
            insert_count += int(result.get("insert_count", 0))
            inserted_ids.extend(result.get("ids", []))

        return {
            "insert_count": insert_count,
            "inserted_ids": inserted_ids,
        }

    def get_review(self, review_id: int) -> Review:
        if review_id <= 0:
            raise ValueError("Review ID must be positive")

        row = reviews_repository.find_by_id(review_id)
        if row is None:
            raise KeyError(f"Review {review_id} not found")
        return Review(**row)

    def list_reviews(
        self,
        rating: int | None,
        language: str | None,
        book_id: int | None,
        limit: int,
        offset: int,
    ) -> list[Review]:
        if limit <= 0:
            raise ValueError("Limit must be positive")
        if offset < 0:
            raise ValueError("Offset cannot be negative")

        filter_expr = _build_filter(rating, language, book_id)
        rows = reviews_repository.find_all(filter_expr, limit=limit, offset=offset)
        return [Review(**row) for row in rows]

    def update_review(self, review_id: int, update: ReviewUpdate) -> dict:
        if review_id <= 0:
            raise ValueError("Review ID must be positive")

        record = reviews_repository.find_by_id_with_vectors(review_id)
        if record is None:
            raise KeyError(f"Review {review_id} not found")
        if "id" not in record:
            raise ValueError("Primary key 'id' missing from record")

        update_data = update.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            if value is None:
                raise ValueError(f"Field '{key}' cannot be null")

        if not update_data:
            return {"upserted_count": 0}

        if "book_id" in update_data:
            update_data["book_id"] = str(update_data["book_id"])
        if "rating" in update_data:
            update_data["rating"] = _normalize_rating(update_data["rating"])

        text_changed = "review_text" in update_data
        record.update(update_data)

        if text_changed:
            record["review_embedding"] = minilm_service.encode_one(record["review_text"])

        result = reviews_repository.upsert(record)
        return {"upserted_count": result.get("upsert_count", 0)}

    def delete_review(self, review_id: int) -> dict:
        if review_id <= 0:
            raise ValueError("Review ID must be positive")

        if reviews_repository.find_by_id(review_id) is None:
            raise KeyError(f"Review {review_id} not found")

        result = reviews_repository.delete_by_id(review_id)
        return {"delete_count": result.get("delete_count", 0)}

    def batch_delete_reviews(self, entity_ids: list[int]) -> dict:
        if not entity_ids:
            raise ValueError("Entity IDs list cannot be empty")
        if len(entity_ids) > 1000:
            raise ValueError("Maximum 1000 IDs per batch delete request")
        if any(entity_id <= 0 for entity_id in entity_ids):
            raise ValueError("All entity IDs must be positive")

        existing_ids = reviews_repository.find_existing_ids(entity_ids)
        missing_ids = [entity_id for entity_id in entity_ids if entity_id not in existing_ids]
        if missing_ids:
            raise KeyError(f"Reviews not found: {missing_ids}")

        result = reviews_repository.batch_delete(entity_ids)
        return {"delete_count": result.get("delete_count", 0)}

    def semantic_search(self, query: str, top_k: int) -> list[ReviewSearchResult]:
        if not query or not query.strip():
            raise ValueError("Query cannot be empty")
        if top_k <= 0:
            raise ValueError("top_k must be positive")

        query_vector = minilm_service.encode_one(query)
        hits = reviews_repository.search_dense([query_vector], top_k=top_k)[0]
        return [_to_review_search_result(hit) for hit in hits]

    def hybrid_search(self, query: str, top_k: int) -> list[ReviewSearchResult]:
        if not query or not query.strip():
            raise ValueError("Query cannot be empty")
        if top_k <= 0:
            raise ValueError("top_k must be positive")

        query_vector = minilm_service.encode_one(query)
        hits = reviews_repository.hybrid_search(query_text=query, query_vector=query_vector, top_k=top_k)
        return [_to_review_search_result(hit) for hit in hits]

    def count_by_rating_votes(self, min_rating: int, min_votes: int) -> dict:
        if min_rating < 0:
            raise ValueError("min_rating cannot be negative")
        if min_votes < 0:
            raise ValueError("min_votes cannot be negative")

        filter_expr = f"rating >= {int(min_rating)} && n_votes >= {int(min_votes)}"
        count = sum(len(batch) for batch in reviews_repository.iterate_all(batch_size=1000, filter_expr=filter_expr))
        return {
            "min_rating": int(min_rating),
            "min_votes": int(min_votes),
            "count": count,
        }

    def get_stats(self) -> dict:
        return reviews_repository.get_stats()

    def reset_collection(self) -> None:
        reviews_repository.reset()


reviews_service = ReviewsService()
