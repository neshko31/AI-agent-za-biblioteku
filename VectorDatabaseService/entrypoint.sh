#!/bin/sh
# Shebang line: Specifies that the script should be executed using the Bourne shell
set -e

# Instruction to exit the script immediately if any command returns a non-zero (error) status

# Sets a default URL for Ollama if the environment variable is not already provided
OLLAMA_URL="${OLLAMA_URL:-http://ollama:11434}"
#OLLAMA_MODEL="${OLLAMA_MODEL:-qwen2.5:1.5b}"
#OLLAMA_WAIT_TIMEOUT="${OLLAMA_WAIT_TIMEOUT:-30}"

# Starts a subshell in the background (using &) to run tasks without blocking the main server
(
  echo ">>> [0/3] Checking Ollama model..."
  # Checks if the specific AI model is already downloaded locally
  if curl -sf "${OLLAMA_URL}/api/tags" | grep -q "qwen2.5:1.5b"; then
    echo ">>> [0/3] qwen2.5:1.5b already present — skipping pull."
  else
    echo ">>> [0/3] Waiting for Ollama to be ready..."
    # A loop that pauses execution until the Ollama API becomes reachable
    until curl -sf "${OLLAMA_URL}/api/tags" >/dev/null 2>&1; do sleep 3; done
    echo ">>> [0/3] Pulling qwen2.5:1.5b (~986 MB, please wait)..."
    # Sends a POST request to download the model; stream:false waits for the full download to finish
    curl -sf -X POST "${OLLAMA_URL}/api/pull" \
        -H "Content-Type: application/json" \
        -d '{"name":"qwen2.5:1.5b","stream":false}' >/dev/null \
    && echo ">>> [0/3] Pull complete." \
    || echo ">>> [0/3] WARNING: pull failed, chat endpoints may not work."
  fi

  echo ">>> [1/3] Waiting for Milvus..."
  python -c "from utils.wait_for_milvus import wait_for_milvus; wait_for_milvus('standalone', 19530)"

  echo ">>> [2/3] Running books RAG ingestion (skipped if already done)..."
  # Executes a Python module to process and load books data into the database for RAG (Retrieval-Augmented Generation)
  python -m ingest.books_ingest || echo ">>> WARNING: books ingest failed, continuing..."

  echo ">>> [3/3] Running book reviews ingestion (skipped if already done)..."
  # Executes a Python module to process and load book reviews data into the database
  python -m ingest.reviews_ingest || echo ">>> WARNING: reviews ingest failed, continuing..."

  echo ">>> [3/3] Background setup complete."
)

echo ">>> Starting API server..."
# Replaces the shell process with the Uvicorn server, ensuring the app handles signals directly
exec uvicorn app:app --host 0.0.0.0 --port 8000
