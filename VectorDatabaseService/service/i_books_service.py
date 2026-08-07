"""
Service interface for the books collection.
"""

from abc import ABC, abstractmethod

from extraction.books_filter_extraction import BookSearchFilters
from model.book import Book, BookCreate, BookSearchResult, BookUpdate


class IBooksService(ABC):
    """Abstract service interface for books collection management."""

    @abstractmethod
    def create_book(self, book: BookCreate) -> dict:
        """Creates a single book with embeddings."""
        ...

    @abstractmethod
    def batch_create_books(self, books: list[BookCreate]) -> dict:
        """Creates multiple books in a single request."""
        ...

    @abstractmethod
    def get_book(self, book_id: int) -> Book:
        """Fetches a single book by Milvus ID."""
        ...

    @abstractmethod
    def list_books(
        self,
        language: str | None,
        author: str | None,
        publisher: str | None,
        has_image: bool | None,
        limit: int,
        offset: int,
    ) -> list[Book]:
        """Lists books with optional scalar filters and pagination."""
        ...

    @abstractmethod
    def update_book(self, book_id: int, update: BookUpdate) -> dict:
        """Updates a book and re-encodes embeddings when needed."""
        ...

    @abstractmethod
    def delete_book(self, book_id: int) -> dict:
        """Deletes one book by Milvus ID."""
        ...

    @abstractmethod
    def batch_delete_books(self, entity_ids: list[int]) -> dict:
        """Deletes multiple books by Milvus IDs."""
        ...

    @abstractmethod
    def semantic_search(self, query: str, top_k: int) -> list[BookSearchResult]:
        """Single-vector semantic ANN search over description_embedding."""
        ...

    @abstractmethod
    def cover_search(self, query: str, top_k: int) -> list[BookSearchResult]:
        """Single-vector ANN search over cover_embedding."""
        ...

    @abstractmethod
    def filtered_semantic_search(
        self,
        query: str,
        language: str,
        min_pages: int,
        max_pages: int,
        top_k: int,
    ) -> list[BookSearchResult]:
        """Vector search over description_embedding with scalar filters."""
        ...

    @abstractmethod
    def multi_filtered_semantic_search(
        self,
        query: str,
        filters: BookSearchFilters,
        top_k: int,
    ) -> list[BookSearchResult]:
        """Vector search over description_embedding with any combination of
        language/author/publisher/page-range filters (all optional)."""
        ...

    @abstractmethod
    def iterator_semantic_search(
        self,
        query: str,
        author: str,
        min_pages: int,
        max_pages: int,
        top_k: int,
        batch_size: int,
    ) -> list[BookSearchResult]:
        """Vector similarity search over iterator batches from Milvus."""
        ...

    @abstractmethod
    def hybrid_multimodal_search(
        self,
        query: str,
        image_url: str | None,
        image_base64: str | None,
        text_weight: float,
        top_k: int,
        filters: BookSearchFilters | None = None,
    ) -> dict:
        """Hybrid multimodal search over description + cover vectors with RRF.

        `filters`, when provided and non-empty, restricts both recall legs
        via Milvus filter_expr (language/author/publisher/page range). The
        cover leg always additionally requires has_image == true.
        """
        ...

    @abstractmethod
    def find_similar(self, book_id: int, text_weight: float, top_k: int) -> dict:
        """Multi-vector search (description + cover) with RRF fusion."""
        ...

    @abstractmethod
    def get_stats(self) -> dict:
        """Returns collection statistics (row_count, etc.)."""
        ...

    @abstractmethod
    def reset_collection(self) -> None:
        """Drops and recreates the collection."""
        ...