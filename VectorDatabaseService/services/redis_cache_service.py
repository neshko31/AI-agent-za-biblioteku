"""
Redis keš servis za VectorDatabaseService.

Implementira dve strategije keširanja:
  1. Exact match  — SHA-256 hash teksta upita kao Redis ključ
  2. Semantic match — kosinusna sličnost između embeddinga novog upita i prethodno keširanih embeddinga

Tok pri svakom chat zahtevu:
  1. Normalizuj i hashiraj tekst → proveri exact keš
  2. Ako nema exact hita → generiši embedding → proveri semantic keš
  3. Ako nema ni semantic hita → pozovi Milvus + Ollama → upiši u keš
"""

import hashlib
import json
import logging
import os
from typing import Any

from config import REDIS_HOST, REDIS_PORT, EXACT_CACHE_TTL, SEMANTIC_CACHE_TTL, EMBED_INDEX_TTL, SEMANTIC_THRESHOLD

import numpy as np
import redis

logger = logging.getLogger(__name__)

_PFX_EXACT       = "vds:exact:"        # exact match keševi
_PFX_SEMANTIC    = "vds:semantic:"     # odgovori vezani za semantički indeks
_PFX_EMBED_INDEX = "vds:embed_index:"  # lista keširanih embeddinga po namespace-u (npr. vds:embed_index:BOOKS)


class RedisCacheService:
    """
    Singleton keš servis.

    Konekcija se pokušava pri startu; ako Redis nije dostupan servis onda 
    sve metode vraćaju None/False da ne bi blokirao normalan rad chatbota.
    """

    def __init__(self) -> None:
        self._client: redis.Redis | None = None
        self._connect()

    def _connect(self) -> None:
        try:
            self._client = redis.Redis(
                host=REDIS_HOST,
                port=REDIS_PORT,
                decode_responses=True,   
                socket_connect_timeout=5,
                socket_timeout=5,
            )
            self._client.ping()
            logger.info("Redis keš konektovan na %s:%s", REDIS_HOST, REDIS_PORT)
        except Exception as exc:
            logger.warning(
                "Redis nije dostupan (%s) — keširanje je isključeno.", exc
            )
            self._client = None

    @property
    def available(self) -> bool:
        return self._client is not None

    # Pomoćne metode

    @staticmethod
    def _normalize(text: str) -> str:
        return text.strip().lower()

    @staticmethod
    def _hash(text: str) -> str:
        return hashlib.sha256(text.encode()).hexdigest()

    @staticmethod
    def _cosine_similarity(a: list[float], b: list[float]) -> float:
        va = np.array(a, dtype=np.float32)
        vb = np.array(b, dtype=np.float32)
        norm_a = np.linalg.norm(va)
        norm_b = np.linalg.norm(vb)
        if norm_a == 0 or norm_b == 0:
            return 0.0
        return float(np.dot(va, vb) / (norm_a * norm_b))

    # 1. Exact match

    def get_exact(self, query: str, namespace: str) -> dict | None:
        """
        Vraća keširan odgovor ako postoji tačan (normalizovan) hit.

        Ključ: vds:exact:<namespace>:<sha256(normalized_query)>
        """
        if not self.available:
            return None
        try:
            key = _PFX_EXACT + namespace + ":" + self._hash(self._normalize(query))
            raw = self._client.get(key)
            if raw:
                logger.debug("Exact cache HIT za: %.60s", query)
                return json.loads(raw)
        except Exception as exc:
            logger.warning("Redis get_exact greška: %s", exc)
        return None

    def set_exact(self, query: str, response: dict, namespace: str) -> None:
        """Upisuje odgovor u exact keš."""
        if not self.available:
            return
        try:
            key = _PFX_EXACT + namespace + ":" + self._hash(self._normalize(query))
            self._client.setex(key, EXACT_CACHE_TTL, json.dumps(response))
        except Exception as exc:
            logger.warning("Redis set_exact greška: %s", exc)

    # 2. Semantic match

    def get_semantic(self, query_embedding: list[float], namespace: str) -> dict | None:
        """
        Traži semantički sličan upit u kešu.

        Algoritam:
          - Učita listu svih (embedding_key, embedding_vector) iz indeksa za dati namespace
          - Za svaki izračuna kosinusnu sličnost sa query_embedding
          - Ako je sličnost >= SEMANTIC_THRESHOLD, vrati keširan odgovor
          - Vraća odgovor koji ima najvišu sličnost (ako ih ima više)
        """
        if not self.available:
            return None
        try:
            embed_index_key = _PFX_EMBED_INDEX + namespace
            # Učitaj indeks: lista stringova "embed_key|json_vector"
            index_raw = self._client.lrange(embed_index_key, 0, -1)
            if not index_raw:
                return None

            best_score = SEMANTIC_THRESHOLD - 0.001  # inicijalno ispod praga
            best_key: str | None = None

            for entry in index_raw:
                try:
                    embed_key, vec_json = entry.split("|", 1)
                    cached_vec = json.loads(vec_json)
                    score = self._cosine_similarity(query_embedding, cached_vec)
                    if score > best_score:
                        best_score = score
                        best_key = embed_key
                except Exception:
                    continue  # oštećen unos — preskoči

            if best_key:
                raw = self._client.get(best_key)
                if raw:
                    logger.debug(
                        "Semantic cache HIT (sličnost=%.4f) ključ=%s",
                        best_score, best_key,
                    )
                    return json.loads(raw)
        except Exception as exc:
            logger.warning("Redis get_semantic greška: %s", exc)
        return None

    def set_semantic(
        self,
        query: str,
        query_embedding: list[float],
        response: dict,
        namespace: str
    ) -> None:
        """
        Upisuje odgovor u semantički keš i dodaje embedding u indeks.

        Ključ odgovora: vds:semantic:<namespace>:<sha256(normalized_query)>
        Indeks:         vds:embed_index:<namespace>  (Redis lista, odvojena po namespace-u)
        """
        if not self.available:
            return
        try:
            norm = self._normalize(query)
            embed_key = _PFX_SEMANTIC + namespace + ":" + self._hash(norm)
            embed_index_key = _PFX_EMBED_INDEX + namespace

            # Upiši odgovor
            self._client.setex(embed_key, SEMANTIC_CACHE_TTL, json.dumps(response))

            # Dodaj u indeks (embedding + ključ u jednom stringu razdvojenom sa |)
            index_entry = embed_key + "|" + json.dumps(query_embedding)
            self._client.lpush(embed_index_key, index_entry)
            self._client.expire(embed_index_key, EMBED_INDEX_TTL)

            # Ograniči veličinu indeksa na 500 unosa da ne raste neograničeno
            self._client.ltrim(embed_index_key, 0, 499)

        except Exception as exc:
            logger.warning("Redis set_semantic greška: %s", exc)

    # Upravljanje kešom

    def invalidate_all(self) -> int:
        """Briše sve VDS keš ključeve. Korisno pri re-ingestion podataka."""
        if not self.available:
            return 0
        try:
            keys = self._client.keys("vds:*")
            if keys:
                return self._client.delete(*keys)
        except Exception as exc:
            logger.warning("Redis invalidate_all greška: %s", exc)
        return 0

    def stats(self) -> dict:
        """Vraća osnovne Redis statistike za health endpoint."""
        if not self.available:
            return {"available": False}
        try:
            info = self._client.info("memory")
            # Dinamički pronađi sve namespace indekse (npr. vds:embed_index:BOOKS)
            index_keys = self._client.keys(_PFX_EMBED_INDEX + "*")
            index_sizes = {k: self._client.llen(k) for k in index_keys}
            return {
                "available": True,
                "used_memory_human": info.get("used_memory_human"),
                "semantic_index_sizes": index_sizes,
            }
        except Exception as exc:
            return {"available": False, "error": str(exc)}

redis_cache = RedisCacheService()