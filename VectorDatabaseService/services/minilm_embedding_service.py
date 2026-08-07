import logging

import numpy as np
from sentence_transformers import SentenceTransformer

from config import REVIEWS_EMBEDDING_DIM, REVIEWS_EMBEDDING_MODEL

logger = logging.getLogger(__name__)


class MiniLMEmbeddingService:
    """Service for generating text embeddings using the MiniLM model (optimized for RAG)."""
    def __init__(self, model_name: str = REVIEWS_EMBEDDING_MODEL):
        # Loads the small but efficient sentence-transformer model
        logger.info("Loading MiniLM model '%s' ...", model_name)
        self._model = SentenceTransformer(model_name)
        logger.info("MiniLM model loaded (dim=%d).", REVIEWS_EMBEDDING_DIM)

    def encode(self, texts: list[str]) -> list[list[float]]:
        """Converts a batch of text into normalized vector representations."""
        return self._model.encode(
            texts,
            batch_size=64, # Larger batch size than CLIP because MiniLM is much lighter
            show_progress_bar=False,
            convert_to_numpy=True,
            normalize_embeddings=True, # Critical for accurate similarity calculations
        ).tolist()

    def encode_one(self, text: str) -> list[float]:
        """Encodes a single string into its vector form."""
        return self.encode([text])[0]

    @staticmethod
    def cosine_similarity(a: list[float], b: list[float]) -> float:
        """Calculates the similarity between two text vectors."""
        va = np.array(a, dtype=np.float32)
        vb = np.array(b, dtype=np.float32)
        return float(np.dot(va, vb))

    @staticmethod
    def zero_vector() -> list[float]:
        """Returns a vector of zeros matching the model's output dimension."""
        return [0.0] * REVIEWS_EMBEDDING_DIM


minilm_service = MiniLMEmbeddingService()
