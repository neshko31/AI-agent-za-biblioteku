"""
Service interface for the reviews collection.
"""

from abc import ABC, abstractmethod

from model.review import Review, ReviewCreate, ReviewSearchResult, ReviewUpdate


class IReviewsService(ABC):
    """Abstract service interface for reviews collection management."""

    @abstractmethod
    def create_review(self, review: ReviewCreate) -> dict:
        """Creates a single review with dense embedding."""
        ...

    @abstractmethod
    def batch_create_reviews(self, reviews: list[ReviewCreate]) -> dict:
        """Creates multiple reviews in one request."""
        ...

    @abstractmethod
    def get_review(self, review_id: int) -> Review:
        """Fetches one review by Milvus ID."""
        ...

    @abstractmethod
    def list_reviews(
        self,
        rating: int | None,
        language: str | None,
        book_id: int | None,
        limit: int,
        offset: int,
    ) -> list[Review]:
        """Lists reviews with scalar filters and pagination."""
        ...

    @abstractmethod
    def update_review(self, review_id: int, update: ReviewUpdate) -> dict:
        """Updates a review and re-encodes embedding when review_text changes."""
        ...

    @abstractmethod
    def delete_review(self, review_id: int) -> dict:
        """Deletes one review by Milvus ID."""
        ...

    @abstractmethod
    def batch_delete_reviews(self, entity_ids: list[int]) -> dict:
        """Deletes multiple reviews by Milvus IDs."""
        ...

    @abstractmethod
    def semantic_search(self, query: str, top_k: int) -> list[ReviewSearchResult]:
        """Single-vector semantic ANN search over review_embedding."""
        ...

    @abstractmethod
    def hybrid_search(self, query: str, top_k: int) -> list[ReviewSearchResult]:
        """Hybrid dense+sparse search over review_embedding and sparse_bm25."""
        ...

    @abstractmethod
    def count_by_rating_votes(self, min_rating: float, min_votes: int) -> dict:
        """Counts reviews matching rating and votes thresholds."""
        ...

    @abstractmethod
    def get_stats(self) -> dict:
        """Returns collection statistics (row_count, etc.)."""
        ...

    @abstractmethod
    def reset_collection(self) -> None:
        """Drops and recreates the collection."""
        ...
