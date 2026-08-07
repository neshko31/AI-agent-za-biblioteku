from typing import Generator

from pymilvus import AnnSearchRequest, RRFRanker

from config import REVIEWS_COLLECTION, REVIEWS_NPROBE
from schema.reviews_schema import REVIEWS_OUTPUT_FIELDS, reviews_index_params, reviews_schema
from services.milvus_service import milvus_service


class ReviewsRepository:
    def __init__(self):
        self._client = milvus_service.client
        self._collection = REVIEWS_COLLECTION
        self._search_params = {"metric_type": "COSINE", "params": {"nprobe": REVIEWS_NPROBE}}

    # CRUD
    def insert(self, records: list[dict]) -> dict:
        return self._client.insert(collection_name=self._collection, data=records)

    def batch_insert(self, records: list[dict], batch_size: int = 100) -> list[dict]:
        results = []
        for i in range(0, len(records), batch_size):
            results.append(
                self._client.insert(
                    collection_name=self._collection,
                    data=records[i : i + batch_size],
                )
            )
        return results

    def upsert(self, records: dict | list[dict]) -> dict:
        data = [records] if isinstance(records, dict) else records
        return self._client.upsert(collection_name=self._collection, data=data)

    def delete_by_id(self, entity_id: int) -> dict:
        return self._client.delete(collection_name=self._collection, ids=[entity_id])

    def batch_delete(self, entity_ids: list[int]) -> dict:
        return self._client.delete(collection_name=self._collection, ids=entity_ids)

    def find_existing_ids(self, entity_ids: list[int]) -> set[int]:
        if not entity_ids:
            return set()

        rows = self._client.get(
            collection_name=self._collection,
            ids=entity_ids,
            output_fields=["id"],
        )
        return {int(row["id"]) for row in rows}

    def find_by_id(self, entity_id: int) -> dict | None:
        rows = self._client.get(
            collection_name=self._collection,
            ids=[entity_id],
            output_fields=["id"] + REVIEWS_OUTPUT_FIELDS,
        )
        return rows[0] if rows else None

    def find_by_id_with_vectors(self, entity_id: int) -> dict | None:
        fields = ["id"] + REVIEWS_OUTPUT_FIELDS + ["review_embedding"]
        rows = self._client.get(
            collection_name=self._collection,
            ids=[entity_id],
            output_fields=list(dict.fromkeys(fields)),
        )
        return rows[0] if rows else None

    def find_by_review_id(self, review_id: str) -> dict | None:
        safe_review_id = review_id.replace("\\", "\\\\").replace('"', '\\"')
        rows = self._client.query(
            collection_name=self._collection,
            filter=f'review_id == "{safe_review_id}"',
            output_fields=["id"] + REVIEWS_OUTPUT_FIELDS,
            limit=1,
        )
        return rows[0] if rows else None

    def find_all(self, filter_expr: str = "", limit: int = 20, offset: int = 0) -> list[dict]:
        return self._client.query(
            collection_name=self._collection,
            filter=filter_expr or "",
            output_fields=["id"] + REVIEWS_OUTPUT_FIELDS,
            limit=limit,
            offset=offset,
        )

    def count(self, filter_expr: str = "") -> int:
        result = self._client.query(
            collection_name=self._collection,
            filter=filter_expr or "",
            output_fields=["count(*)"],
        )
        return int(result[0]["count(*)"]) if result else 0

    # Simple scalar count query: rating >= X and n_votes >= Y
    def count_by_rating_votes(self, min_rating: float, min_votes: int) -> int:
        filter_expr = f"rating >= {float(min_rating)} && n_votes >= {int(min_votes)}"
        return self.count(filter_expr)

    def iterate_all(self, batch_size: int = 100, filter_expr: str = "") -> Generator[list[dict], None, None]:
        iterator = self._client.query_iterator(
            collection_name=self._collection,
            filter=filter_expr or "",
            output_fields=["id"] + REVIEWS_OUTPUT_FIELDS,
            batch_size=batch_size,
        )
        while True:
            batch = iterator.next()
            if not batch:
                iterator.close()
                break
            yield batch

    # Single-vector dense search
    def search_dense(
        self,
        query_vectors: list[list[float]],
        top_k: int = 10,
        filter_expr: str = "",
        offset: int = 0,
    ) -> list[list[dict]]:
        kwargs = dict(
            collection_name=self._collection,
            data=query_vectors,
            anns_field="review_embedding",
            search_params=self._search_params,
            limit=top_k,
            offset=offset,
            output_fields=["id"] + REVIEWS_OUTPUT_FIELDS,
        )
        if filter_expr:
            kwargs["filter"] = filter_expr
        return self._parse_hits(self._client.search(**kwargs))

    # Vector + filter with two conditions
    def search_dense_with_filters(
        self,
        query_vectors: list[list[float]],
        language: str,
        min_votes: int,
        top_k: int = 10,
    ) -> list[list[dict]]:
        safe_language = language.replace("\\", "\\\\").replace('"', '\\"')
        filter_expr = f'language == "{safe_language}" && n_votes >= {int(min_votes)}'
        return self.search_dense(query_vectors, top_k=top_k, filter_expr=filter_expr)

    # Vector + filter + iterator/pagination through result windows
    def search_dense_iterative(
        self,
        query_vectors: list[list[float]],
        filter_expr: str = "",
        batch_size: int = 20,
        max_items: int = 200,
    ) -> list[dict]:
        results: list[dict] = []
        offset = 0

        while len(results) < max_items:
            need = min(batch_size, max_items - len(results))
            batch = self.search_dense(
                query_vectors=query_vectors,
                top_k=need,
                filter_expr=filter_expr,
                offset=offset,
            )[0]
            if not batch:
                break
            results.extend(batch)
            offset += len(batch)
            if len(batch) < need:
                break

        return results

    # Hybrid dense+sparse retrieval via RRFRanker
    def hybrid_search(
        self,
        query_text: str,
        query_vector: list[float],
        top_k: int = 10,
        filter_expr: str = "",
    ) -> list[dict]:
        dense_req = AnnSearchRequest(
            data=[query_vector],
            anns_field="review_embedding",
            param={"metric_type": "COSINE", "params": {"nprobe": REVIEWS_NPROBE}},
            limit=top_k,
            expr=filter_expr or None,
        )
        sparse_req = AnnSearchRequest(
            data=[query_text],
            anns_field="sparse_bm25",
            param={"metric_type": "BM25"},
            limit=top_k,
            expr=filter_expr or None,
        )
        raw = self._client.hybrid_search(
            collection_name=self._collection,
            reqs=[dense_req, sparse_req],
            ranker=RRFRanker(),
            limit=top_k,
            output_fields=["id"] + REVIEWS_OUTPUT_FIELDS,
        )
        parsed = self._parse_hits(raw)
        return parsed[0] if parsed else []

    # Collection management
    def get_stats(self) -> dict:
        return self._client.get_collection_stats(collection_name=self._collection)

    def reset(self) -> None:
        schema = reviews_schema(self._client)
        index_params = reviews_index_params(self._client)
        if self._client.has_collection(self._collection):
            self._client.drop_collection(self._collection)
        self._client.create_collection(
            collection_name=self._collection,
            schema=schema,
            index_params=index_params,
            consistency_level="Strong",
        )
        self._client.load_collection(self._collection)

    @staticmethod
    def _parse_hits(raw) -> list[list[dict]]:
        def _parse_hit(hit) -> dict:
            row = dict(hit.get("entity", {}))
            row["id"] = hit.get("id")
            row["score"] = round(hit.get("distance", 0.0), 6)
            return row

        results = []
        if not raw:
            return results

        if hasattr(raw[0], "get"):
            return [[_parse_hit(hit) for hit in raw]]

        for hits in raw:
            batch = []
            if hasattr(hits, "get"):
                batch.append(_parse_hit(hits))
            else:
                for hit in hits:
                    batch.append(_parse_hit(hit))
            results.append(batch)
        return results


reviews_repository = ReviewsRepository()
