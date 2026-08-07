import base64
import io
import logging
from typing import Union

import numpy as np
import requests
from PIL import Image
from sentence_transformers import SentenceTransformer

from config import EMBEDDING_MODEL_NAME, IMAGE_DOWNLOAD_TIMEOUT, MAX_IMAGE_SIZE_PX, EMBEDDING_DIM

logger = logging.getLogger(__name__)


class CLIPEmbeddingService:
    """Service for generating multimodal embeddings using the OpenAI CLIP model."""
    def __init__(self, model_name: str = EMBEDDING_MODEL_NAME):
        # Initializes the model using SentenceTransformer library
        logger.info("Loading CLIP model '%s' ...", model_name)
        self._model = SentenceTransformer(model_name)
        logger.info("CLIP model loaded (dim=%d).", EMBEDDING_DIM)

    def encode_text(self, texts: list[str]) -> list[list[float]]:
        # Converts a list of text strings into normalized vector embeddings
        CLIP_MAX_CHARS = 77
        truncated = [t[:CLIP_MAX_CHARS] for t in texts]
        return self._model.encode(
            truncated,
            batch_size=32,
            show_progress_bar=False,
            convert_to_numpy=True, # Returns NumPy array for better performance before conversion
            normalize_embeddings=True, # Ensures cosine similarity can be calculated via dot product
        ).tolist()

    def encode_text_one(self, text: str) -> list[float]:
        # Helper for encoding a single string
        return self.encode_text([text])[0]

    def encode_images(self, images: list[Image.Image]) -> list[list[float]]:
        # Converts a list of PIL Image objects into normalized vector embeddings
        return self._model.encode(
            images,
            batch_size=16,
            show_progress_bar=False,
            convert_to_numpy=True,
            normalize_embeddings=True,
        ).tolist()

    def encode_image_one(self, image: Image.Image) -> list[float]:
        # Helper for encoding a single image object
        return self.encode_images([image])[0]

    def image_from_url(self, url: str) -> Image.Image:
        # Downloads an image from a URL and converts it to a standardized RGB format
        try:
            response = requests.get(url, timeout=IMAGE_DOWNLOAD_TIMEOUT, stream=True)
            response.raise_for_status()
            img = Image.open(io.BytesIO(response.content)).convert("RGB")
            return self._resize_if_needed(img)
        except Exception as exc:
            raise ValueError(f"Failed to load image from URL '{url}': {exc}") from exc

    def image_from_bytes(self, data: bytes) -> Image.Image:
        # Decodes raw binary image data into a PIL Image object
        try:
            img = Image.open(io.BytesIO(data)).convert("RGB")
            return self._resize_if_needed(img)
        except Exception as exc:
            raise ValueError(f"Failed to decode image bytes: {exc}") from exc

    def image_from_base64(self, b64_string: str) -> Image.Image:
         # Decodes Base64 encoded strings, handling optional data-URL prefixes
        if "," in b64_string:
            b64_string = b64_string.split(",", 1)[1]
        try:
            return self.image_from_bytes(base64.b64decode(b64_string))
        except Exception as exc:
            raise ValueError(f"Failed to decode base64 image: {exc}") from exc

    def encode_from_url(self, url: str) -> list[float]:
        # Pipeline: Download -> Preprocess -> Encode
        return self.encode_image_one(self.image_from_url(url))

    def encode_from_bytes(self, data: bytes) -> list[float]:
        # Pipeline: Decode Bytes -> Preprocess -> Encode
        return self.encode_image_one(self.image_from_bytes(data))

    def encode_from_base64(self, b64_string: str) -> list[float]:
        # Pipeline: Decode Base64 -> Preprocess -> Encode
        return self.encode_image_one(self.image_from_base64(b64_string))

    @staticmethod
    def cosine_similarity(a: list[float], b: list[float]) -> float:
        # Calculates similarity score (0.0 to 1.0) between two vectors using dot product
        va = np.array(a, dtype=np.float32)
        vb = np.array(b, dtype=np.float32)
        return float(np.dot(va, vb))

    @staticmethod
    def zero_vector() -> list[float]:
        # Generates a dummy vector of zeros for initialization or error handling
        return [0.0] * EMBEDDING_DIM

    @staticmethod
    def _resize_if_needed(img: Image.Image) -> Image.Image:
        # Resizes massive images down to limit defined in config to save memory/processing time
        if max(img.size) > MAX_IMAGE_SIZE_PX:
            # Maintains aspect ratio using high-quality LANCZOS downsampling
            img.thumbnail((MAX_IMAGE_SIZE_PX, MAX_IMAGE_SIZE_PX), Image.LANCZOS)
        return img

# Singleton instance for global app usage
embedding_service = CLIPEmbeddingService()
