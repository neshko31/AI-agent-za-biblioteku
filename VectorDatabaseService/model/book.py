from pydantic import AliasChoices, BaseModel, ConfigDict, Field, field_validator


class BookBase(BaseModel):
    """Validated scalar payload shared by all book DTOs."""

    model_config = ConfigDict(populate_by_name=True, extra="forbid")

    goodreads_id: int = Field(..., ge=1, validation_alias=AliasChoices("goodreads_id", "goodreadsId"))
    isbn: str = Field(..., min_length=1, max_length=32)
    title: str = Field(..., min_length=1, max_length=512)
    author: str = Field(..., min_length=1, max_length=256)
    description: str = Field(..., min_length=1, max_length=6000)
    language: str = Field(default="en", min_length=2, max_length=32)
    publisher: str = Field(default="", max_length=256)
    pages: int = Field(default=0, ge=0, le=100_000)
    cover_img: str | None = Field(default=None, max_length=2048, alias="coverImg")
    has_image: bool = Field(default=False)

    @field_validator("isbn", "title", "author", "description", "language")
    @classmethod
    def _strip_required(cls, value: str) -> str:
        cleaned = value.strip()
        if not cleaned:
            raise ValueError("Field cannot be empty")
        return cleaned

    @field_validator("publisher")
    @classmethod
    def _strip_publisher(cls, value: str) -> str:
        return value.strip()

    @field_validator("cover_img")
    @classmethod
    def _normalize_cover(cls, value: str | None) -> str | None:
        if value is None:
            return None
        cleaned = value.strip()
        return cleaned or None

class BookCreate(BookBase):
    """Request model for inserting a new book."""


class BookUpdate(BaseModel):
    """Request model for partial book updates."""

    model_config = ConfigDict(populate_by_name=True, extra="forbid")

    goodreads_id: int | None = Field(
        default=None,
        ge=1,
        validation_alias=AliasChoices("goodreads_id", "goodreadsId"),
    )
    isbn: str | None = Field(default=None, min_length=1, max_length=32)
    title: str | None = Field(default=None, min_length=1, max_length=512)
    author: str | None = Field(default=None, min_length=1, max_length=256)
    description: str | None = Field(default=None, min_length=1, max_length=6000)
    language: str | None = Field(default=None, min_length=2, max_length=32)
    publisher: str | None = Field(default=None, max_length=256)
    pages: int | None = Field(default=None, ge=0, le=100_000)
    cover_img: str | None = Field(default=None, max_length=2048, alias="coverImg")
    has_image: bool | None = Field(default=None)

    @field_validator("isbn", "title", "author", "description", "language")
    @classmethod
    def _strip_optional_required(cls, value: str | None) -> str | None:
        if value is None:
            return None
        cleaned = value.strip()
        if not cleaned:
            raise ValueError("Field cannot be empty")
        return cleaned

    @field_validator("publisher")
    @classmethod
    def _strip_optional_publisher(cls, value: str | None) -> str | None:
        if value is None:
            return None
        return value.strip()

    @field_validator("cover_img")
    @classmethod
    def _normalize_optional_cover(cls, value: str | None) -> str | None:
        if value is None:
            return None
        cleaned = value.strip()
        return cleaned or None

class Book(BookBase):
    """Book entity as stored in Milvus."""

    id: int


class BookSearchResult(Book):
    """Book search payload enriched with similarity scores."""

    score: float
    fused_score: float | None = None