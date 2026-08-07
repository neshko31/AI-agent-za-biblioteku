from pydantic import AliasChoices, BaseModel, ConfigDict, Field, field_validator


class ReviewBase(BaseModel):
    """Validated scalar payload shared by all review DTOs."""

    model_config = ConfigDict(extra="forbid")

    review_id: str = Field(
        ..., min_length=1, max_length=64, validation_alias=AliasChoices("review_id", "reviewId")
    )
    book_id: int = Field(..., ge=1, validation_alias=AliasChoices("book_id", "bookId"))
    isbn: str = Field(..., min_length=1, max_length=32)
    language: str = Field(default="en", min_length=2, max_length=32)
    rating: float = Field(..., ge=1.0, le=5.0)
    n_votes: int = Field(default=0, ge=0)
    date_added: int = Field(..., ge=0, description="Unix timestamp (seconds)")
    review_text: str = Field(..., min_length=1, max_length=6000)

    @field_validator("review_id", "isbn", "language", "review_text")
    @classmethod
    def _strip_required(cls, value: str) -> str:
        cleaned = value.strip()
        if not cleaned:
            raise ValueError("Field cannot be empty")
        return cleaned


class ReviewCreate(ReviewBase):
    """Request model for inserting a new review."""


class ReviewUpdate(BaseModel):
    """Request model for partial review updates."""

    model_config = ConfigDict(extra="forbid")

    review_id: str | None = Field(
        default=None,
        min_length=1,
        max_length=64,
        validation_alias=AliasChoices("review_id", "reviewId"),
    )
    book_id: int | None = Field(default=None, ge=1, validation_alias=AliasChoices("book_id", "bookId"))
    isbn: str | None = Field(default=None, min_length=1, max_length=32)
    language: str | None = Field(default=None, min_length=2, max_length=32)
    rating: float | None = Field(default=None, ge=1.0, le=5.0)
    n_votes: int | None = Field(default=None, ge=0)
    date_added: int | None = Field(default=None, ge=0, description="Unix timestamp (seconds)")
    review_text: str | None = Field(default=None, min_length=1, max_length=6000)

    @field_validator("review_id", "isbn", "language", "review_text")
    @classmethod
    def _strip_optional_required(cls, value: str | None) -> str | None:
        if value is None:
            return None
        cleaned = value.strip()
        if not cleaned:
            raise ValueError("Field cannot be empty")
        return cleaned


class Review(ReviewBase):
    """Review entity as stored in Milvus."""

    id: int


class ReviewSearchResult(Review):
    """Review search payload enriched with similarity scores."""

    score: float
    fused_score: float | None = None