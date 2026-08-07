"""
Multimodal ingestion of books into Milvus.

Modalities:
Dataset: https://zenodo.org/records/4265096 (smaller version avilable as parquet file)
Embedding model: CLIP ViT-B/32 (clip-ViT-B-32 via sentence-transformers)

- TEXT:  description       ->  CLIP text encoder  ->  description_embedding
- IMAGE: image_bytes (PIL) ->  CLIP image encoder ->  cover_embedding

Both vectors reside in the same 512-dimensional CLIP space, 
enabling cross-modal retrieval.
"""

import argparse
import base64
import logging
from pathlib import Path

import pandas as pd

from config import BATCH_SIZE, BOOKS_COLLECTION, BOOKS_PARQUET_PATH
from schema.books_schema import books_index_params, books_schema
from services.embedding_service import embedding_service
from services.milvus_service import milvus_service

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)-8s %(message)s")
logger = logging.getLogger(__name__)

_REQUIRED_FIELDS = {"goodreads_id", "description_embedding", "cover_embedding", "has_image"}


def _safe_int(value, default: int = 0) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return default


def _safe_str(value, max_len: int, default: str = "") -> str:
    if value is None:
        return default
    try:
        if pd.isna(value):
            return default
    except Exception:
        pass
    return str(value).strip()[:max_len]


def _normalize_bytes_to_b64(value) -> str:
    if value is None:
        return ""
    if isinstance(value, (bytes, bytearray)):
        return base64.b64encode(bytes(value)).decode("utf-8")
    if isinstance(value, str):
        return value.strip()
    return ""


def _has_expected_schema() -> bool:
    try:
        info = milvus_service.client.describe_collection(BOOKS_COLLECTION)
        field_names = {field.get("name") for field in info.get("fields", [])}
        return _REQUIRED_FIELDS.issubset(field_names)
    except Exception:
        return False


def _prepare_collection(reset: bool) -> None:
    client = milvus_service.client

    if client.has_collection(BOOKS_COLLECTION):
        if reset:
            client.drop_collection(BOOKS_COLLECTION)
            logger.info("Dropped collection '%s' (--reset).", BOOKS_COLLECTION)
        elif not _has_expected_schema():
            client.drop_collection(BOOKS_COLLECTION)
            logger.info(
                "Dropped '%s' - outdated schema detected, recreating.",
                BOOKS_COLLECTION,
            )

    if not client.has_collection(BOOKS_COLLECTION):
        client.create_collection(
            collection_name=BOOKS_COLLECTION,
            schema=books_schema(client),
            index_params=books_index_params(client),
            consistency_level="Strong",
        )
        logger.info("Created collection '%s'.", BOOKS_COLLECTION)

    milvus_service.load_collection(BOOKS_COLLECTION)


def _already_ingested() -> bool:
    stats = milvus_service.collection_stats(BOOKS_COLLECTION)
    count = int(stats.get("row_count", 0))
    if count > 0:
        logger.info(
            "Collection '%s' already has %d rows - skipping. Use --reset to re-ingest.",
            BOOKS_COLLECTION,
            count,
        )
        return True
    return False


def _load_cover_image(row: dict):
    image_bytes = row.get("image_bytes")
    if image_bytes:
        try:
            raw = base64.b64decode(image_bytes)
            return embedding_service.image_from_bytes(raw)
        except Exception:
            pass

    cover_url = row.get("coverImg") or ""
    if cover_url:
        try:
            return embedding_service.image_from_url(cover_url)
        except Exception:
            pass

    return None


def _flush(batch: list[dict], total: int) -> int:
    descriptions = [row["description"] for row in batch]
    description_embeddings = embedding_service.encode_text(descriptions)

    valid_img_indices: list[int] = []
    valid_images: list = []
    for idx, row in enumerate(batch):
        image = _load_cover_image(row)
        if image is not None:
            valid_img_indices.append(idx)
            valid_images.append(image)

    if valid_images:
        try:
            valid_cover_embeddings = embedding_service.encode_images(valid_images)
        except Exception as exc:
            logger.warning("Cover image encoding failed (%s) - using zero vectors.", exc)
            valid_cover_embeddings = [embedding_service.zero_vector()] * len(valid_images)
    else:
        valid_cover_embeddings = []

    zero = embedding_service.zero_vector()
    cover_embeddings = [zero] * len(batch)
    has_image_list = [False] * len(batch)
    for pos, row_idx in enumerate(valid_img_indices):
        cover_embeddings[row_idx] = valid_cover_embeddings[pos]
        has_image_list[row_idx] = True

    records = []
    for row, desc_vec, cover_vec, has_image in zip(
        batch,
        description_embeddings,
        cover_embeddings,
        has_image_list,
    ):
        records.append(
            {
                "goodreads_id": row["goodreads_id"],
                "isbn": row["isbn"],
                "title": row["title"],
                "author": row["author"],
                "description": row["description"],
                "language": row["language"],
                "coverImg": row["coverImg"],
                "publisher": row["publisher"],
                "pages": row["pages"],
                "has_image": has_image,
                "description_embedding": desc_vec,
                "cover_embedding": cover_vec,
            }
        )

    result = milvus_service.insert(BOOKS_COLLECTION, records)
    inserted = result.get("insert_count", len(records))
    total += inserted
    logger.info("Inserted books: +%d (total=%d)", inserted, total)
    return total


def ingest(data_path: str = BOOKS_PARQUET_PATH, reset: bool = False, limit: int | None = None) -> None:
    _prepare_collection(reset)
    if _already_ingested():
        return

    parquet_path = Path(data_path)
    if not parquet_path.exists():
        raise FileNotFoundError(f"Books parquet not found: {parquet_path}")

    logger.info("Loading books from %s", parquet_path)
    rows = pd.read_parquet(parquet_path).to_dict(orient="records")
    if limit is not None and limit > 0:
        rows = rows[:limit]

    batch: list[dict] = []
    total = 0
    skipped = 0

    for row in rows:
        record = {
            "goodreads_id": _safe_str(
                row.get("goodreads_id") or row.get("book_id") or row.get("bookId"),
                64,
            ),
            "isbn": _safe_str(row.get("isbn"), 32),
            "title": _safe_str(row.get("title"), 512),
            "author": _safe_str(row.get("author"), 256),
            "description": _safe_str(row.get("description"), 6000),
            "language": _safe_str(row.get("language"), 32, default="en"),
            "coverImg": _safe_str(row.get("coverImg"), 2048),
            "publisher": _safe_str(row.get("publisher"), 256),
            "pages": _safe_int(row.get("pages"), 0),
            "image_bytes": _normalize_bytes_to_b64(row.get("image_bytes")),
        }

        if (
            not record["goodreads_id"]
            or not record["isbn"]
            or not record["title"]
            or not record["description"]
        ):
            skipped += 1
            continue

        batch.append(record)
        if len(batch) >= BATCH_SIZE:
            total = _flush(batch, total)
            batch = []

    if batch:
        total = _flush(batch, total)

    logger.info("Books ingest done. Total inserted=%d, skipped=%d", total, skipped)

def main():
    parser = argparse.ArgumentParser(description="Ingest books (text + images) into Milvus")
    parser.add_argument("--data", default=BOOKS_PARQUET_PATH, help="Path to books parquet")
    parser.add_argument("--reset", action="store_true", help="Drop and recreate collection")
    parser.add_argument("--limit", type=int, default=None, help="Optional max number of rows to ingest")
    args = parser.parse_args()
    ingest(data_path=args.data, reset=args.reset, limit=args.limit)


if __name__ == "__main__":
    main()