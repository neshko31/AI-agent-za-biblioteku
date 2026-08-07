from typing import Generator

from config import BOOKS_COLLECTION, BOOKS_NPROBE
from schema.books_schema import BOOKS_OUTPUT_FIELDS, books_index_params, books_schema
from services.milvus_service import milvus_service


class BooksRepository:
    def __init__(self):
        self._client = milvus_service.client
        self._collection = BOOKS_COLLECTION
        self._search_params = {"metric_type": "COSINE", "params": {"nprobe": BOOKS_NPROBE}}

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
            output_fields=["id"] + BOOKS_OUTPUT_FIELDS,
        )
        return rows[0] if rows else None

    def find_by_id_with_vectors(self, entity_id: int) -> dict | None:
        fields = ["id"] + BOOKS_OUTPUT_FIELDS + ["description_embedding", "cover_embedding"]
        rows = self._client.get(
            collection_name=self._collection,
            ids=[entity_id],
            output_fields=list(dict.fromkeys(fields)),
        )
        return rows[0] if rows else None

    def find_all(self, filter_expr: str = "", limit: int = 20, offset: int = 0) -> list[dict]:
        return self._client.query(
            collection_name=self._collection,
            filter=filter_expr or "",
            output_fields=["id"] + BOOKS_OUTPUT_FIELDS,
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

    def iterate_all(self, batch_size: int = 100, filter_expr: str = "") -> Generator[list[dict], None, None]:
        iterator = self._client.query_iterator(
            collection_name=self._collection,
            filter=filter_expr or "",
            output_fields=["id"] + BOOKS_OUTPUT_FIELDS,
            batch_size=batch_size,
        )
        while True:
            batch = iterator.next()
            if not batch:
                iterator.close()
                break
            yield batch

    # Simple vector search on description_embedding
    def search_description(
        self,
        query_vectors: list[list[float]],
        top_k: int = 10,
        filter_expr: str = "",
        offset: int = 0,
    ) -> list[list[dict]]:
        kwargs = dict(
            collection_name=self._collection,
            data=query_vectors,
            anns_field="description_embedding",
            search_params=self._search_params,
            limit=top_k,
            offset=offset,
            output_fields=["id"] + BOOKS_OUTPUT_FIELDS,
        )
        if filter_expr:
            kwargs["filter"] = filter_expr
        return self._parse_hits(self._client.search(**kwargs))

    # Simple vector search on cover_embedding
    def search_cover(
        self,
        query_vectors: list[list[float]],
        top_k: int = 10,
        filter_expr: str = "",
        offset: int = 0,
    ) -> list[list[dict]]:
        kwargs = dict(
            collection_name=self._collection,
            data=query_vectors,
            anns_field="cover_embedding",
            search_params=self._search_params,
            limit=top_k,
            offset=offset,
            output_fields=["id"] + BOOKS_OUTPUT_FIELDS,
        )
        if filter_expr:
            kwargs["filter"] = filter_expr
        return self._parse_hits(self._client.search(**kwargs))

    # Vector + filter with two conditions (language + pages range)
    def search_description_with_filters(
        self,
        query_vectors: list[list[float]],
        language: str,
        min_pages: int,
        max_pages: int,
        top_k: int = 10,
    ) -> list[list[dict]]:
        safe_language = language.replace("\\", "\\\\").replace('"', '\\"')
        filter_expr = (
            f'language == "{safe_language}" && pages >= {int(min_pages)} && pages <= {int(max_pages)}'
        )
        return self.search_description(query_vectors, top_k=top_k, filter_expr=filter_expr)

    # Iterator over filtered rows including vectors for in-service scoring/reranking
    def iterate_filtered_with_vectors(
        self,
        filter_expr: str,
        batch_size: int = 50,
    ) -> Generator[list[dict], None, None]:
        fields = list(dict.fromkeys(["id"] + BOOKS_OUTPUT_FIELDS + ["description_embedding"]))
        iterator = self._client.query_iterator(
            collection_name=self._collection,
            filter=filter_expr or "",
            output_fields=fields,
            batch_size=batch_size,
        )
        while True:
            batch = iterator.next()
            if not batch:
                iterator.close()
                break
            yield batch

    # Vector + filter + pagination/iteration through result windows
    def search_description_iterative(
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
            batch = self.search_description(
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

    # Collection management
    def get_stats(self) -> dict:
        return self._client.get_collection_stats(collection_name=self._collection)

    def reset(self) -> None:
        schema = books_schema(self._client)
        index_params = books_index_params(self._client)
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


books_repository = BooksRepository()
