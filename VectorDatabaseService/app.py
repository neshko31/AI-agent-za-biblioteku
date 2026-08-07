import logging

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from config import APP_HOST, APP_PORT
from controller.books_controller import router as books_router
from controller.chat_controller import router as chat_router
from controller.reviews_controller import router as reviews_router

logging.basicConfig(level=logging.INFO, format="%(asctime)s  %(levelname)-8s  %(name)s  %(message)s")

from config import EUREKA_SERVER, APP_NAME

app = FastAPI(
    title="Vector Database Service - Library",
    description="Books and reviews vector search API with semantic, hybrid, and chat endpoints.",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])
app.include_router(books_router)
app.include_router(reviews_router)
app.include_router(chat_router)


@app.get("/health", tags=["Health"])
def health():
    return {"status": "ok", "collection": "books + reviews"}


if __name__ == "__main__":
    uvicorn.run("app:app", host=APP_HOST, port=APP_PORT, reload=False)
