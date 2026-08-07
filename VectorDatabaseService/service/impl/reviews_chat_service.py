import logging

from config import REVIEWS_TOP_K
from service.impl.reviews_service import reviews_service
from services.llm_service import llm_service
from services.minilm_embedding_service import MiniLMEmbeddingService
from services.redis_cache_service import redis_cache

logger = logging.getLogger(__name__)

_SYSTEM_PROMPT = """You are a helpful assistant for book review analysis.
You answer using only provided review excerpts retrieved from the review database.

Rules:
- Base the answer only on the given review context.
- Highlight patterns in sentiment, quality, strengths, and weaknesses when relevant.
- If context is insufficient, say you do not have enough information from the provided reviews.
- Keep the answer concise (under 220 words).
"""


class ReviewsChatService:
    def chat(self, message: str, top_k: int = 6) -> dict:
        effective_top_k = max(1, min(top_k, REVIEWS_TOP_K))
        
        # 1. Tačna cache provera
        cached = redis_cache.get_exact(message, "REVIEWS")
        if cached:
            cached["cache_hit"] = "exact"
            return cached
        
        # 2. Generiši embedding
        try:
            query_embedding = MiniLMEmbeddingService.encode_one(message)
        except Exception as exc:
            logger.warning("Embedding generacija neuspešna: %s - preskačem semantic keš", exc)
            query_embedding = None

        # 3. Semantic cache provera
        if query_embedding:
            cached = redis_cache.get_semantic(query_embedding, "REVIEWS")
            if cached:
                cached["cache_hit"] = "semantic"
                return cached

        # 4. Milvus pretraga
        hits = reviews_service.hybrid_search(message, effective_top_k)

        if not hits:
            return {
                "response": "Nisam pronašao dovoljno relevantnih recenzija za ovo pitanje.",
                "context_reviews": [],
                "cache_hit": "miss",
            }

        context_lines = []
        for idx, hit in enumerate(hits, start=1):
            review_text = (hit.review_text or "").replace("\n", " ").strip()
            if len(review_text) > 700:
                review_text = f"{review_text[:700]}..."
            context_lines.append(
                (
                    f"[Review {idx}] ReviewID: {hit.review_id} | ISBN: {hit.isbn} | "
                    f"Rating: {hit.rating} | Votes: {hit.n_votes} | Text: {review_text}"
                )
            )

        user_prompt = (
            f"Retrieved reviews:\n{chr(10).join(context_lines)}\n\n"
            f"User question: {message}\n\n"
            "Please answer using only the retrieved review context above."
        )

        # 5. Ollama generisanje 
        response = llm_service.chat(_SYSTEM_PROMPT, user_prompt)
        response_data = {
            "response": response,
            "context_reviews": [
                {
                    "id": hit.id,
                    "review_id": hit.review_id,
                    "isbn": hit.isbn,
                    "rating": hit.rating,
                    "score": hit.score,
                }
                for hit in hits
            ],
            "cache_hit": "miss",
        }
    
        # 6. Upis u keš
        cache_payload = {k: v for k, v in response_data.items() if k != "cache_hit"}
        redis_cache.set_exact(message, cache_payload, "REVIEWS")
        if query_embedding:
            redis_cache.set_semantic(message, query_embedding, cache_payload, "REVIEWS")

        return response_data


reviews_chat_service = ReviewsChatService()
