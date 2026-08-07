"""
LLM-extracted search filters for the books chat assistant.

This is NOT a Milvus collection schema (see schema/books_schema.py) and NOT
a domain entity (see model/book.py). It is the intermediate structure the
LLM is asked to fill in from a free-form user message, before that message
ever reaches embedding/search. The chat service feeds this model's JSON
schema to Ollama (see services/llm_service.py -> chat_structured) so the
model's reply is constrained to exactly these fields.

All fields are optional on purpose: the whole point is letting the LLM say
"the user didn't mention this" instead of inventing a value to satisfy a
required field.
"""

from pydantic import BaseModel, ConfigDict, Field, model_validator


class BookSearchFilters(BaseModel):
    """Optional scalar filters the LLM may detect inside a chat message.

    Field names intentionally mirror the parameters already accepted by
    BooksService.filtered_semantic_search / _build_filter, so extracted
    values can be passed straight through without any renaming step.
    """

    model_config = ConfigDict(extra="ignore")

    language: str | None = Field(
        default=None,
        description="Language the user explicitly asked for, e.g. 'english', 'serbian'.",
    )
    author: str | None = Field(
        default=None,
        description="Author name explicitly mentioned by the user.",
    )
    publisher: str | None = Field(
        default=None,
        description="Publisher explicitly mentioned by the user.",
    )
    min_pages: int | None = Field(
        default=None,
        ge=0,
        description="Minimum page count explicitly requested by the user.",
    )
    max_pages: int | None = Field(
        default=None,
        ge=0,
        description="Maximum page count explicitly requested by the user.",
    )

    @model_validator(mode="before")
    @classmethod
    def _blank_strings_to_none(cls, data):
        """Ollama tends to emit "" instead of null for "not mentioned" fields."""
        if not isinstance(data, dict):
            return data
        cleaned = dict(data)
        for key in ("language", "author", "publisher"):
            value = cleaned.get(key)
            if isinstance(value, str):
                stripped = value.strip()
                cleaned[key] = stripped or None
        return cleaned

    @model_validator(mode="after")
    def _drop_inverted_page_range(self):
        """If the model mixes up min/max, discard both rather than risk an
        empty or misleading filter_expr downstream."""
        if (
            self.min_pages is not None
            and self.max_pages is not None
            and self.min_pages > self.max_pages
        ):
            self.min_pages = None
            self.max_pages = None
        return self

    def is_empty(self) -> bool:
        """True when nothing was extracted — caller should fall back to a
        plain (unfiltered) search instead of building an empty filter_expr."""
        return not any(
            (self.language, self.author, self.publisher, self.min_pages is not None, self.max_pages is not None)
        )