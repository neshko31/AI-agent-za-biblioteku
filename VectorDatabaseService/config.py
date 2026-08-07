import os

# Milvus connection settings: fetches environment variables or uses default local hostnames
MILVUS_HOST = os.getenv("MILVUS_HOST", "standalone")
MILVUS_PORT = int(os.getenv("MILVUS_PORT", "19530"))
MILVUS_URI  = f"http://{MILVUS_HOST}:{MILVUS_PORT}"

# Configuration for the visual embedding model used for image-related searches
EMBEDDING_MODEL_NAME = "clip-ViT-B-32"
EMBEDDING_DIM        = 512

# FastAPI/Uvicorn server settings
APP_HOST = "0.0.0.0"
APP_PORT = int(os.getenv("APP_PORT", "8000"))
APP_NAME = "vector-database-service"

# Service Discovery: URL for the Eureka registry to allow microservices to find each other
EUREKA_SERVER = os.getenv("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", "http://eureka-server:8761/eureka")

# Redis keš konfiguracija
# Hostname Redis kontejnera — mora odgovarati imenu servisa u docker-compose
REDIS_HOST = os.getenv("REDIS_HOST", "redis-cache-books")
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))
 
# TTL-ovi u sekundama
EXACT_CACHE_TTL    = int(os.getenv("EXACT_CACHE_TTL",    str(60 * 60)))   # 60 min
SEMANTIC_CACHE_TTL = int(os.getenv("SEMANTIC_CACHE_TTL", str(60 * 60)))   # 60 min
EMBED_INDEX_TTL    = int(os.getenv("EMBED_INDEX_TTL",    str(60 * 60)))   # 60 min
 
# Prag semantičke sličnosti (0.0-1.0); 0.95 = skoro identični upiti
SEMANTIC_THRESHOLD = float(os.getenv("SEMANTIC_THRESHOLD", "0.95"))

# Indexing and search parameters (trade-off between speed and accuracy)
LAB_NLIST      = 64 # Number of clusters for the IVF index
LAB_NPROBE     = 16 # Number of clusters to search through during queries

# Ingestion settings for processing large datasets
BATCH_SIZE       = 64 # How many items to process at once to save memory
MAX_INGEST_COUNT = 44_446 # Maximum limit of rows to import

NLIST  = 256
NPROBE = 32

# Retrieval settings
DEFAULT_TOP_K    = 10
TWO_STAGE_RECALL = 50 # How many candidates to fetch before re-ranking

# Image processing limits to prevent network or memory hanging
IMAGE_DOWNLOAD_TIMEOUT = 10
MAX_IMAGE_SIZE_PX      = 1024

# LLM (Large Language Model) settings for RAG and generation tasks
OLLAMA_URL   = os.getenv("OLLAMA_URL",   "http://ollama:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "qwen2.5:1.5b")

# Books collection settings (dense text vector + dense cover vector)
BOOKS_COLLECTION                  = "books"
BOOKS_DESCRIPTION_EMBEDDING_MODEL = BOOKS_COVER_EMBEDDING_MODEL = "clip-ViT-B-32"
BOOKS_DESCRIPTION_EMBEDDING_DIM   = BOOKS_COVER_EMBEDDING_DIM   = 512
BOOKS_NLIST                       = 64
BOOKS_NPROBE                      = 16
BOOKS_TOP_K                       = 10

# Reviews collection settings (dense vector + sparse BM25 vector)
REVIEWS_COLLECTION                = "reviews"
REVIEWS_EMBEDDING_MODEL           = "sentence-transformers/all-MiniLM-L6-v2"
REVIEWS_EMBEDDING_DIM             = 384
REVIEWS_BM25_ANALYZER_LANGUAGE    = "english"
REVIEWS_NLIST                     = 64
REVIEWS_NPROBE                    = 16
REVIEWS_TOP_K                     = 10

# Local data file paths used by preparation scripts and ingest modules
DATA_DIR                          = os.getenv("DATA_DIR", "data")
BOOKS_PARQUET_PATH                = os.getenv("BOOKS_PARQUET_PATH", os.path.join(DATA_DIR, "books.parquet"))
REVIEWS_PARQUET_PATH              = os.getenv("REVIEWS_PARQUET_PATH", os.path.join(DATA_DIR, "reviews.parquet"))

# Temporary aliases to keep legacy imports stable until Phase 9 cleanup
BOOKS_EMBEDDING_MODEL             = BOOKS_DESCRIPTION_EMBEDDING_MODEL
BOOKS_EMBEDDING_DIM               = BOOKS_DESCRIPTION_EMBEDDING_DIM
BOOKS_DEFAULT_TOP_K               = BOOKS_TOP_K
REVIEWS_DEFAULT_TOP_K             = REVIEWS_TOP_K
BOOK_REVIEWS_COLLECTION           = REVIEWS_COLLECTION