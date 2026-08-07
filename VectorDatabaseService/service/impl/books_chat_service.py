import logging

from config import BOOKS_TOP_K
from extraction.books_filter_extraction import BookSearchFilters
from service.impl.books_service import books_service
from services.llm_service import llm_service
from services.embedding_service import embedding_service
from services.redis_cache_service import redis_cache

logger = logging.getLogger(__name__)

_SYSTEM_PROMPT = """You are a helpful library assistant.
You answer user questions using only the provided book context.

Rules:
- Prefer recommending books that are clearly relevant to the question.
- Mention exact titles and authors from the context when possible.
- If context is insufficient, say you do not have enough information from the provided books.
- Keep the answer concise (under 220 words).
"""

_FILTER_EXTRACTION_PROMPT = """You extract structured search filters from a library chat message.

Only fill in a field if the user EXPLICITLY mentions it. If a field is not
clearly mentioned, leave it null — never guess or infer a value.

Examples:
- "knjige na engleskom" -> language = "english"
- "nesto od Tolkina" -> author = "Tolkien"
- "kraca knjiga, do 200 strana" -> max_pages = 200
- "preporuci mi dobru knjigu" -> all fields null
"""


class BooksChatService:
    def chat(
        self,
        message: str,
        top_k: int = 6,
        image_url: str | None = None,
        image_base64: str | None = None,
    ) -> dict:
        effective_top_k = max(1, min(top_k, BOOKS_TOP_K))
        has_image = bool((image_url or "").strip()) or bool((image_base64 or "").strip())

        # Cache key must reflect whether an image was attached, otherwise a
        # text-only cache entry could wrongly satisfy an image+text request
        # (or vice versa).
        cache_namespace = "BOOKS_IMG" if has_image else "BOOKS"

        # 1. Tačna cache provera (samo za text-only tok — image upiti se ne
        #    keširaju jer je svaka slika praktično jedinstvena)
        if not has_image:
            cached = redis_cache.get_exact(message, cache_namespace)
            if cached:
                cached["cache_hit"] = "exact"
                return cached

        # 2. Generiši embedding (za semantic keš, samo text-only tok)
        query_embedding = None
        if not has_image:
            try:
                query_embedding = embedding_service.encode_text_one(message)
            except Exception as exc:
                logger.warning("Embedding generacija neuspešna: %s — preskačem semantic keš", exc)
                query_embedding = None

            if query_embedding:
                cached = redis_cache.get_semantic(query_embedding, cache_namespace)
                if cached:
                    cached["cache_hit"] = "semantic"
                    return cached

        # 3. Ekstrakcija filtera iz poruke (poseban, brz LLM poziv).
        #    Ako extraction padne iz bilo kog razloga, nastavljamo bez filtera
        #    — pogrešna/nepostojeća ekstrakcija ne treba da blokira ceo chat.
        filters = self._extract_filters(message)

        # 4. Milvus pretraga — grananje po tome da li imamo sliku i/ili filtere
        if has_image:
            search_result = books_service.hybrid_multimodal_search(
                query=message,
                image_url=image_url,
                image_base64=image_base64,
                text_weight=0.5,
                top_k=effective_top_k,
                filters=filters,
            )
            hits = search_result["results"]
            get = lambda hit, key: hit.get(key)  # noqa: E731 - dict rows here, not BookSearchResult
        elif not filters.is_empty():
            # Covers any combination of language/author/publisher/page range
            # extracted from the message — not just language + pages.
            hits = books_service.multi_filtered_semantic_search(
                query=message,
                filters=filters,
                top_k=effective_top_k,
            )
            get = lambda hit, key: getattr(hit, key)  # noqa: E731 - BookSearchResult objects here
        else:
            hits = books_service.semantic_search(message, effective_top_k)
            get = lambda hit, key: getattr(hit, key)  # noqa: E731

        if not hits:
            return {
                "response": "Nisam pronašao dovoljno relevantnih knjiga za ovo pitanje.",
                "context_books": [],
                "applied_filters": filters.model_dump(),
                "cache_hit": "miss",
            }

        context_lines = []
        for idx, hit in enumerate(hits, start=1):
            description = (get(hit, "description") or "").replace("\n", " ").strip()
            if len(description) > 700:
                description = f"{description[:700]}..."
            context_lines.append(
                f"[Book {idx}] Title: {get(hit, 'title')} | Author: {get(hit, 'author')} | Description: {description}"
            )

        user_prompt = (
            f"Retrieved books:\n{chr(10).join(context_lines)}\n\n"
            f"User question: {message}\n\n"
            "Please answer the question using only the retrieved books above."
        )

        # 5. Ollama generisanje (drugi, odvojeni LLM poziv)
        response = llm_service.chat(_SYSTEM_PROMPT, user_prompt)
        response_data = {
            "response": response,
            "context_books": [
                {
                    "id": get(hit, "id"),
                    "title": get(hit, "title"),
                    "author": get(hit, "author"),
                    "score": get(hit, "score"),
                }
                for hit in hits
            ],
            "applied_filters": filters.model_dump(),
            "cache_hit": "miss",
        }

        # 6. Upis u keš (samo text-only tok, isto kao kod čitanja keša)
        if not has_image:
            cache_payload = {k: v for k, v in response_data.items() if k != "cache_hit"}
            redis_cache.set_exact(message, cache_payload, cache_namespace)
            if query_embedding:
                redis_cache.set_semantic(message, query_embedding, cache_payload, cache_namespace)

        return response_data

    def _extract_filters(self, message: str) -> BookSearchFilters:
        try:
            raw = llm_service.chat_structured(_FILTER_EXTRACTION_PROMPT, message, BookSearchFilters)
            return BookSearchFilters(**raw)
        except Exception as exc:
            logger.warning("Filter ekstrakcija neuspešna: %s — nastavljam bez filtera", exc)
            return BookSearchFilters()


books_chat_service = BooksChatService()