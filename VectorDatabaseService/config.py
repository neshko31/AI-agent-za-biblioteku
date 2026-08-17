import os

# Milvus connection settings: fetches environment variables or uses default local hostnames
MILVUS_HOST = os.getenv("MILVUS_HOST", "standalone")
MILVUS_PORT = int(os.getenv("MILVUS_PORT", "19530"))
MILVUS_URI  = f"http://{MILVUS_HOST}:{MILVUS_PORT}"

# Configuration for the visual embedding model used for image-related searches
EMBEDDING_MODEL_NAME = "Qwen/Qwen3-VL-Embedding-2B"
EMBEDDING_DIM        = 1024

# FastAPI/Uvicorn server settings
APP_HOST = "0.0.0.0"
APP_PORT = int(os.getenv("APP_PORT", "8000"))
APP_NAME = "vector-database-service"

# Service Discovery: URL for the Eureka registry to allow microservices to find each other
EUREKA_SERVER = os.getenv("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", "http://eureka-server:8761/eureka")

# Ingestion settings for processing large datasets
BATCH_SIZE       = 128 # How many items to process at once to save memory
MAX_INGEST_COUNT = 44_446 # Maximum limit of rows to import

# Indexing and search parameters (trade-off between speed and accuracy)
NLIST  = 256 # Number of clusters for the IVF index
NPROBE = 32 # Number of clusters to search through during queries

# Retrieval settings
DEFAULT_TOP_K    = 10
TWO_STAGE_RECALL = 50 # How many candidates to fetch before re-ranking

# Image processing limits to prevent network or memory hanging
IMAGE_DOWNLOAD_TIMEOUT = 10
MAX_IMAGE_SIZE_PX      = 1024

# Collection settings
BOOKS_COLLECTION                  = "books"
REVIEWS_COLLECTION                = "reviews"


#BOOKS_DESCRIPTION_EMBEDDING_MODEL = BOOKS_COVER_EMBEDDING_MODEL = "clip-ViT-B-32"
#BOOKS_DESCRIPTION_EMBEDDING_DIM   = BOOKS_COVER_EMBEDDING_DIM   = 512
#BOOKS_NLIST                       = 64
#BOOKS_NPROBE                      = 16
#BOOKS_TOP_K                       = 10

#REVIEWS_EMBEDDING_MODEL           = "sentence-transformers/all-MiniLM-L6-v2"
#REVIEWS_EMBEDDING_DIM             = 384
#REVIEWS_BM25_ANALYZER_LANGUAGE    = "english"
#REVIEWS_NLIST                     = 64
#REVIEWS_NPROBE                    = 16
#REVIEWS_TOP_K                     = 10

# Local data file paths used by preparation scripts and ingest modules
DATA_DIR                          = os.getenv("DATA_DIR", "data")
BOOKS_PARQUET_PATH                = os.getenv("BOOKS_PARQUET_PATH", os.path.join(DATA_DIR, "books.parquet"))
REVIEWS_PARQUET_PATH              = os.getenv("REVIEWS_PARQUET_PATH", os.path.join(DATA_DIR, "reviews.parquet"))