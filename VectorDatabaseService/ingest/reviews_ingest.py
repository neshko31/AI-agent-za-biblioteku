"""
Multimodal ingestion of book reviews into Milvus.

Modalities:
Dataset: https://cseweb.ucsd.edu/~jmcauley/datasets/goodreads.html (smaller version avilable as parquet file)
Embedding model: MiniLM-L6-v2
"""

import argparse
import logging
import math
from datetime import datetime
from pathlib import Path

import pandas as pd

from config import BATCH_SIZE, REVIEWS_COLLECTION, REVIEWS_PARQUET_PATH
from schema.reviews_schema import reviews_index_params, reviews_schema
from services.milvus_service import milvus_service
from services.minilm_embedding_service import minilm_service

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)-8s %(message)s")
logger = logging.getLogger(__name__)

_REQUIRED_FIELDS = {"review_embedding", "sparse_bm25", "review_text", "book_id", "rating"}


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


def _parse_rating(value) -> int:
    if value is None:
        return 0
    try:
        numeric = float(value)
    except (TypeError, ValueError):
        return 0
    if math.isnan(numeric) or numeric <= 0:
        return 0
    return max(1, min(5, int(round(numeric))))


def _parse_timestamp(value) -> int:
    if value is None:
        return 0

    if hasattr(value, "timestamp"):
        try:
            return max(int(value.timestamp()), 0)
        except Exception:
            pass

    if isinstance(value, (int, float)):
        if isinstance(value, float) and math.isnan(value):
            return 0
        ts = int(value)
        if ts > 10_000_000_000:
            return ts // 1000
        return max(ts, 0)

    text = str(value).strip()
    if not text:
        return 0

    if text.isdigit():
        ts = int(text)
        if ts > 10_000_000_000:
            return ts // 1000
        return max(ts, 0)

    for fmt in ("%a %b %d %H:%M:%S %z %Y", "%Y-%m-%d %H:%M:%S", "%Y-%m-%d"):
        try:
            return int(datetime.strptime(text, fmt).timestamp())
        except ValueError:
            pass
    return 0


def _has_expected_schema() -> bool:
    try:
        info = milvus_service.client.describe_collection(REVIEWS_COLLECTION)
        field_names = {field.get("name") for field in info.get("fields", [])}
        return _REQUIRED_FIELDS.issubset(field_names)
    except Exception:
        return False


def _prepare_collection(reset: bool) -> None:
    client = milvus_service.client

    if client.has_collection(REVIEWS_COLLECTION):
        if reset:
            client.drop_collection(REVIEWS_COLLECTION)
            logger.info("Dropped collection '%s' (--reset).", REVIEWS_COLLECTION)
        elif not _has_expected_schema():
            client.drop_collection(REVIEWS_COLLECTION)
            logger.info(
                "Dropped '%s' - outdated schema detected, recreating.",
                REVIEWS_COLLECTION,
            )

    if not client.has_collection(REVIEWS_COLLECTION):
        client.create_collection(
            collection_name=REVIEWS_COLLECTION,
            schema=reviews_schema(client),
            index_params=reviews_index_params(client),
            consistency_level="Strong",
        )
        logger.info("Created collection '%s'.", REVIEWS_COLLECTION)

    milvus_service.load_collection(REVIEWS_COLLECTION)


def _already_ingested() -> bool:
    stats = milvus_service.collection_stats(REVIEWS_COLLECTION)
    count = int(stats.get("row_count", 0))
    if count > 0:
        logger.info(
            "Collection '%s' already has %d rows - skipping. Use --reset to re-ingest.",
            REVIEWS_COLLECTION,
            count,
        )
        return True
    return False


def _flush(batch: list[dict], total: int) -> int:
    texts = [record["review_text"] for record in batch]
    embeddings = minilm_service.encode(texts)

    records = [{**row, "review_embedding": emb} for row, emb in zip(batch, embeddings)]
    result = milvus_service.insert(REVIEWS_COLLECTION, records)
    inserted = result.get("insert_count", len(records))
    total += inserted
    logger.info("Inserted reviews: +%d (total=%d)", inserted, total)
    return total


def ingest(data_path: str = REVIEWS_PARQUET_PATH, reset: bool = False, limit: int | None = None) -> None:
    _prepare_collection(reset)
    if _already_ingested():
        return

    parquet_path = Path(data_path)
    if not parquet_path.exists():
        legacy_path = parquet_path.parent / "book_reviews.parquet"
        if legacy_path.exists():
            parquet_path = legacy_path
        else:
            raise FileNotFoundError(
                f"Reviews parquet not found: {parquet_path} (and fallback {legacy_path})"
            )

    logger.info("Loading reviews from %s", parquet_path)
    rows = pd.read_parquet(parquet_path).to_dict(orient="records")
    if limit is not None and limit > 0:
        rows = rows[:limit]

    batch: list[dict] = []
    total = 0
    skipped = 0

    for row in rows:
        record = {
            "review_id": _safe_str(row.get("review_id"), 64),
            "isbn": _safe_str(row.get("isbn"), 32),
            "book_id": _safe_str(row.get("book_id") or row.get("goodreads_id"), 64),
            "language": _safe_str(row.get("language"), 32, default="en"),
            "rating": _parse_rating(row.get("rating")),
            "date_added": _parse_timestamp(row.get("date_added")),
            "n_votes": _safe_int(row.get("n_votes"), 0),
            "review_text": _safe_str(row.get("review_text"), 6000),
        }

        if not record["review_id"] or not record["isbn"] or not record["book_id"] or not record["review_text"]:
            skipped += 1
            continue
        if record["rating"] <= 0:
            skipped += 1
            continue

        batch.append(record)
        if len(batch) >= BATCH_SIZE:
            total = _flush(batch, total)
            batch = []

    if batch:
        total = _flush(batch, total)

    logger.info("Reviews ingest done. Total inserted=%d, skipped=%d", total, skipped)

def main():
    parser = argparse.ArgumentParser(description="Ingest reviews into Milvus")
    parser.add_argument("--data", default=REVIEWS_PARQUET_PATH, help="Path to reviews parquet")
    parser.add_argument("--reset", action="store_true", help="Drop and recreate collection")
    parser.add_argument("--limit", type=int, default=None, help="Optional max number of rows to ingest")
    args = parser.parse_args()
    ingest(data_path=args.data, reset=args.reset, limit=args.limit)


if __name__ == "__main__":
    main()