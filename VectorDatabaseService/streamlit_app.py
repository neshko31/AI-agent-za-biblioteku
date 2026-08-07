import os
import requests
import streamlit as st

API_URL = os.getenv("API_URL", "http://vector-database-service:8000")

st.set_page_config(
    page_title="AI Assistant — Vector DB Demo",
    page_icon="🤖",
    layout="centered",
)

st.title("AI Assistant")
st.caption(f"Powered by Ollama · Vector DB: Milvus · API: {API_URL}")

# ── Ollama status badge ───────────────────────────────────────────────────────
try:
    health = requests.get(f"{API_URL}/api/v1/chat/health", timeout=3).json()
    if health.get("ollama_available"):
        st.success(f"LLM online — model: `{health.get('model')}`", icon="✅")
    else:
        st.warning("LLM not ready yet — Ollama may still be loading the model.", icon="⏳")
except Exception:
    st.error("Cannot reach the API. Make sure docker compose is running.", icon="🔴")

st.divider()

# ── Tabs ──────────────────────────────────────────────────────────────────────
tab_books, tab_reviews = st.tabs([
    "📚 Book Finder",
    "⭐ Review Insights",
])


# ── Books tab ─────────────────────────────────────────────────────────────────
with tab_books:
    st.subheader("Book Finder")
    st.caption(
        "Describe what you're in the mood to read. I search the books catalog in Milvus "
        "and ask an LLM to recommend from what's available."
    )

    if "books_history" not in st.session_state:
        st.session_state.books_history = []

    for msg in st.session_state.books_history:
        with st.chat_message(msg["role"]):
            st.markdown(msg["content"])
            context_books = msg.get("context_books") or msg.get("sources") or []
            if context_books:
                with st.expander("Books retrieved from catalog", expanded=False):
                    for book in context_books:
                        title = book.get("title", "Unknown title")
                        author = book.get("author", "Unknown author")
                        score = book.get("score")
                        isbn = book.get("isbn")

                        line = f"- **{title}** by {author}"
                        details = []
                        if isbn:
                            details.append(f"ISBN: `{isbn}`")
                        if score is not None:
                            details.append(f"score: `{float(score):.4f}`")
                        if details:
                            line += " — " + " | ".join(details)
                        st.markdown(line)

    if prompt := st.chat_input(
        "e.g. Looking for a sci-fi novel about space exploration",
        key="books_input",
    ):
        st.session_state.books_history.append({"role": "user", "content": prompt})
        with st.chat_message("user"):
            st.markdown(prompt)

        with st.chat_message("assistant"):
            with st.spinner("Searching catalog and generating recommendation…"):
                try:
                    resp = requests.post(
                        f"{API_URL}/api/v1/books/chat",
                        json={"message": prompt},
                        timeout=120,
                    )
                    resp.raise_for_status()
                    data = resp.json()
                    response_text = data["response"]
                    context_books = data.get("context_books") or data.get("sources", [])

                    st.markdown(response_text)
                    if context_books:
                        with st.expander("Books retrieved from catalog", expanded=False):
                            for book in context_books:
                                title = book.get("title", "Unknown title")
                                author = book.get("author", "Unknown author")
                                score = book.get("score")
                                isbn = book.get("isbn")

                                line = f"- **{title}** by {author}"
                                details = []
                                if isbn:
                                    details.append(f"ISBN: `{isbn}`")
                                if score is not None:
                                    details.append(f"score: `{float(score):.4f}`")
                                if details:
                                    line += " — " + " | ".join(details)
                                st.markdown(line)
                except requests.exceptions.Timeout:
                    response_text = "⏳ Request timed out. The LLM may still be warming up — try again in a moment."
                    context_books = []
                    st.warning(response_text)
                except Exception as exc:
                    response_text = f"❌ Error: {exc}"
                    context_books = []
                    st.error(response_text)

        st.session_state.books_history.append({
            "role":          "assistant",
            "content":       response_text,
            "context_books": context_books,
        })

    if st.session_state.books_history:
        if st.button("Clear conversation", key="clear_books"):
            st.session_state.books_history = []
            st.rerun()


# ── Reviews tab ───────────────────────────────────────────────────────────────
with tab_reviews:
    st.subheader("Review Insights")
    st.caption(
        "Ask what readers think about a book, author, or genre. I search the reviews "
        "collection in Milvus and ask an LLM to summarise the sentiment."
    )

    if "reviews_history" not in st.session_state:
        st.session_state.reviews_history = []

    for msg in st.session_state.reviews_history:
        with st.chat_message(msg["role"]):
            st.markdown(msg["content"])
            context_reviews = msg.get("context_reviews") or msg.get("sources") or []
            if context_reviews:
                with st.expander("Retrieved reviews", expanded=False):
                    for i, src in enumerate(context_reviews, 1):
                        if isinstance(src, dict):
                            review_id = src.get("review_id", "n/a")
                            isbn = src.get("isbn", "n/a")
                            rating = src.get("rating", "n/a")
                            score = src.get("score")
                            score_part = f" | score: `{float(score):.4f}`" if score is not None else ""
                            st.markdown(
                                f"**Review {i}:** review_id=`{review_id}` | isbn=`{isbn}` | rating=`{rating}`{score_part}"
                            )
                        else:
                            st.markdown(f"**Review {i}:** {src}")

    if prompt := st.chat_input(
        "e.g. What do readers think about dystopian fiction?",
        key="reviews_input",
    ):
        st.session_state.reviews_history.append({"role": "user", "content": prompt})
        with st.chat_message("user"):
            st.markdown(prompt)

        with st.chat_message("assistant"):
            with st.spinner("Searching reviews and summarising sentiment…"):
                try:
                    resp = requests.post(
                        f"{API_URL}/api/v1/reviews/chat",
                        json={"message": prompt},
                        timeout=120,
                    )
                    resp.raise_for_status()
                    data = resp.json()
                    response_text = data["response"]
                    context_reviews = data.get("context_reviews") or data.get("sources", [])

                    st.markdown(response_text)
                    if context_reviews:
                        with st.expander("Retrieved reviews", expanded=False):
                            for i, src in enumerate(context_reviews, 1):
                                if isinstance(src, dict):
                                    review_id = src.get("review_id", "n/a")
                                    isbn = src.get("isbn", "n/a")
                                    rating = src.get("rating", "n/a")
                                    score = src.get("score")
                                    score_part = f" | score: `{float(score):.4f}`" if score is not None else ""
                                    st.markdown(
                                        f"**Review {i}:** review_id=`{review_id}` | isbn=`{isbn}` | rating=`{rating}`{score_part}"
                                    )
                                else:
                                    st.markdown(f"**Review {i}:** {src}")
                except requests.exceptions.Timeout:
                    response_text = "⏳ Request timed out. The LLM may still be warming up — try again in a moment."
                    context_reviews = []
                    st.warning(response_text)
                except Exception as exc:
                    response_text = f"❌ Error: {exc}"
                    context_reviews = []
                    st.error(response_text)

        st.session_state.reviews_history.append({
            "role":            "assistant",
            "content":         response_text,
            "context_reviews": context_reviews,
        })

    if st.session_state.reviews_history:
        if st.button("Clear conversation", key="clear_reviews"):
            st.session_state.reviews_history = []
            st.rerun()