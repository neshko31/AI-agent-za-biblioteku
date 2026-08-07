import logging
import time
from pymilvus import MilvusClient
from config import MILVUS_URI, NPROBE

logger = logging.getLogger(__name__)


def _connect_with_retry(uri: str, retries: int = 12, delay: float = 5.0) -> MilvusClient:
    """Attempts to connect to Milvus multiple times before giving up."""
    for attempt in range(1, retries + 1):
        try:
            client = MilvusClient(uri=uri)
            client.list_collections()
            logger.info("Connected to Milvus at %s", uri)
            return client
        except Exception as exc:
            logger.warning("Milvus not ready (attempt %d/%d): %s", attempt, retries, exc)
            if attempt == retries:
                raise
            time.sleep(delay)


class MilvusService:
    """Wrapper service for Milvus operations including CRUD and Vector Search."""

    def __init__(self, uri: str = MILVUS_URI):
        self._client = _connect_with_retry(uri)

    @property
    def client(self) -> MilvusClient:
        """Exposes the underlying Milvus client if direct access is needed."""
        return self._client

    def ensure_collection(self, name: str, schema, index_params) -> None:
        """Creates a collection only if it doesn't already exist."""
        if self._client.has_collection(name):
            return
        self._client.create_collection(
            collection_name=name,
            schema=schema,
            index_params=index_params,
            consistency_level="Strong", # Ensures data is searchable immediately after write
        )
        logger.info("Created collection '%s'.", name)

    def drop_and_recreate(self, name: str, schema, index_params) -> None:
        """Wipes an existing collection and starts fresh (useful for re-ingestion)."""
        if self._client.has_collection(name):
            self._client.drop_collection(name)
        self._client.create_collection(
            collection_name=name,
            schema=schema,
            index_params=index_params,
            consistency_level="Strong",
        )
        logger.info("Recreated collection '%s'.", name)

    def load_collection(self, name: str) -> None:
        """Moves collection data from disk to RAM to enable search operations."""
        self._client.load_collection(name)

    def collection_stats(self, name: str) -> dict:
        """Returns metadata like row count and memory usage."""
        return self._client.get_collection_stats(collection_name=name)

    def insert(self, collection_name: str, data: list[dict]) -> dict:
        """Adds new records to the database."""
        return self._client.insert(collection_name=collection_name, data=data)

    def get(self, collection_name: str, ids: list, output_fields: list[str] | None = None) -> list[dict]:
        """Retrieves specific rows by their primary keys."""
        kwargs = {"collection_name": collection_name, "ids": ids}
        if output_fields:
            kwargs["output_fields"] = output_fields
        return self._client.get(**kwargs)

    def upsert(self, collection_name: str, data: list[dict]) -> dict:
        """Updates records if they exist, or inserts them if they do not."""
        return self._client.upsert(collection_name=collection_name, data=data)

    def delete(self, collection_name: str, ids: list) -> dict:
        """Removes records by primary key."""
        return self._client.delete(collection_name=collection_name, ids=ids)

    def query(self, collection_name: str, filter_expr: str, output_fields: list[str],
              limit: int = 20, offset: int = 0) -> list[dict]:
        """Standard attribute filtering (non-vector search) using boolean expressions."""
        return self._client.query(
            collection_name=collection_name,
            filter=filter_expr,
            output_fields=output_fields,
            limit=limit,
            offset=offset,
        )

    def search(self, collection_name: str, query_vectors: list[list[float]], anns_field: str,
               output_fields: list[str], top_k: int = 10, filter_expr: str = "",
               metric_type: str = "COSINE", include_vectors: bool = False) -> list[list[dict]]:
        """Performs Approximate Nearest Neighbor (ANN) search to find similar vectors."""
        fields = list(output_fields)
        if include_vectors:
            # Optionally return raw vector data in the results
            for vf in ("text_embedding", "image_embedding"):
                if vf not in fields:
                    fields.append(vf)

        raw = self._client.search(
            collection_name=collection_name,
            data=query_vectors,
            anns_field=anns_field,
            search_params={"metric_type": metric_type, "params": {"nprobe": NPROBE}},
            limit=top_k,
            filter=filter_expr,
            output_fields=fields,
        )

        results = []
        for hits in raw:
            batch = []
            for hit in hits:
                row = {"id": hit["id"], "distance": hit["distance"]}
                row.update(hit.get("entity", {}))
                batch.append(row)
            results.append(batch)
        return results


milvus_service = MilvusService()
