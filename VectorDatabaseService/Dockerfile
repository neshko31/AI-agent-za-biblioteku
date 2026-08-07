# Specifies the lightweight Python 3.11 base image
FROM python:3.11-slim

# Disables bytecode (.pyc) files and enables unbuffered logging for cleaner container output
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Defines the filesystem path where the application will reside
WORKDIR /app

# Installs system-level dependencies for networking (curl) and image processing (libgl)
RUN apt-get update && apt-get install -y --no-install-recommends \
        curl libgl1 libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Installs the CPU-optimized version of PyTorch to significantly reduce image size
# CPU-only torch (~200 MB vs ~800 MB CUDA) — must be installed before sentence-transformers
RUN pip install --no-cache-dir \
    --extra-index-url https://download.pytorch.org/whl/cpu \
    "torch==2.3.1+cpu"

# Installs remaining Python dependencies listed in the requirements file
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Manually overrides a specific package version to ensure compatibility with Milvus components
# Force correct marshmallow version (pymilvus -> environs -> marshmallow requires 3.13+)
RUN pip install --no-cache-dir "marshmallow==3.20.2"

# Pre-bake CLIP model weights into the image layer (~350 MB, cached across rebuilds)
RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('clip-ViT-B-32')"

# Pre-bake MiniLM model weights for reviews semantic/hybrid retrieval (~90 MB)
RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')"

# Copies all application code folders and logic into the working directory
COPY config.py app.py ./
COPY schema/     ./schema/
COPY services/   ./services/
COPY model/      ./model/
COPY repository/ ./repository/
COPY service/    ./service/
COPY controller/ ./controller/
COPY ingest/     ./ingest/
COPY data/       ./data/
COPY utils/      ./utils/
COPY extraction/ ./extraction/

COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

EXPOSE 8000

# start_period covers ingestion time (~2-5 min for CLIP encoding 1000 rows)
HEALTHCHECK --interval=30s --timeout=10s --start-period=300s --retries=5 \
    CMD curl --fail http://localhost:8000/health || exit 1

CMD ["./entrypoint.sh"]
