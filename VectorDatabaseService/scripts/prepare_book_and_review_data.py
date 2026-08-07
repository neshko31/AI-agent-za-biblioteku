"""
Script that is run once, in order to preapre datasets into local PARQUET files.
Script's task is to load data for a 1000 books and to find 5 reviews for those books. 

Books dataset: https://zenodo.org/records/4265096
Reviews dataset: https://cseweb.ucsd.edu/~jmcauley/datasets/goodreads.html
"""

import pandas as pd
import gzip, json
import requests
from PIL import Image
from io import BytesIO
from tqdm import tqdm

BOOKS_TARGET  = 1000
REVIEWS_PER_BOOK = 5
REVIEWS_TARGET = BOOKS_TARGET * REVIEWS_PER_BOOK


def fetch_image_bytes(url: str) -> bytes | None:
    try:
        resp = requests.get(url, timeout=5)
        resp.raise_for_status()
        Image.open(BytesIO(resp.content)).verify()
        return resp.content
    except Exception:
        return None


def fetch_images_with_progress(df: pd.DataFrame) -> pd.Series:
    """Downloads images and shows progress."""
    results = []
    # tqdm wraps iterrows() — every row is one book
    for _, row in tqdm(df.iterrows(), total=len(df), desc="Image downloading", unit="book"):
        results.append(fetch_image_bytes(row["coverImg"]))
        # Checkpoint for each 100 books
        done = len(results)
        if done % 100 == 0:
            ok  = sum(1 for r in results if r is not None)
            fail = done - ok
            tqdm.write(f"  ✔ {done}/{len(df)} | succesful: {ok} | unsuccesful: {fail}")
    return pd.Series(results, index=df.index)


# ── 1. Load Zenodo dataset ───────────────────────────────────────────────────
print("\n[1/4] Loading Zenodo dataset...")
books_df = pd.read_csv("Books.csv") # Dataset is renamed!
print(f"      Number of rows in CSV: {len(books_df):,}")

books_sample = (
    books_df
    .dropna(subset=["isbn", "description", "coverImg"])
    .head(BOOKS_TARGET)
)
print(f"      After filtering (isbn+description+coverImg): {len(books_sample)} books")

books_sample = books_sample.copy()
books_sample["goodreads_id"] = (
    books_sample["bookId"].str.extract(r"^(\d+)", expand=False)
)
valid_ids = set(books_sample["goodreads_id"].astype(str))

# ── 2. Image downloading ─────────────────────────────────────────────────────
print(f"\n[2/4] Downloading cover images for {len(books_sample)} books...")
books_sample["image_bytes"] = fetch_images_with_progress(books_sample)
books_sample["has_image"]   = books_sample["image_bytes"].notna()

ok_count = books_sample["has_image"].sum()
print(f"      Completed: {ok_count}/{len(books_sample)}")

# ── 3. Load UCSD reviews (stream) ────────────────────────────────────────────
print(f"\n[3/4] Streaming reviews (goal: {REVIEWS_TARGET})...")

reviews        = []
lines_read     = 0
reviews_by_book: dict[str, int] = {}

with gzip.open("goodreads_reviews_dedup.json.gz", "rt") as f:
    pbar = tqdm(f, desc="Čitanje linija", unit=" line", miniters=100_000)
    for line in pbar:
        lines_read += 1
        r = json.loads(line)
        bid = str(r["book_id"])

        if bid in valid_ids and r.get("review_text", "").strip():
            # max 5 reviews per book
            if reviews_by_book.get(bid, 0) < REVIEWS_PER_BOOK:
                reviews.append(r)
                reviews_by_book[bid] = reviews_by_book.get(bid, 0) + 1

                # Checkpoint after 100 found reviews
                if len(reviews) % 100 == 0:
                    books_covered = len(reviews_by_book)
                    tqdm.write(
                        f"  ✔ reviews: {len(reviews)}/{REVIEWS_TARGET}"
                        f" | books covered: {books_covered}/{BOOKS_TARGET}"
                        f" | lines read: {lines_read:,}"
                    )

        if len(reviews) >= REVIEWS_TARGET:
            break

    pbar.close()

print(f"      Done — reviews: {len(reviews)} | lines read: {lines_read}")

# ── 4. Join i and writing ────────────────────────────────────────────────────
print("\n[4/4] Join and writing to PARQUET files...")

reviews_df         = pd.DataFrame(reviews)
isbn_map           = books_sample.set_index("goodreads_id")["isbn"].to_dict()
reviews_df["isbn"] = reviews_df["book_id"].astype(str).map(isbn_map)
reviews_df         = reviews_df.dropna(subset=["isbn"])

reviews_final = (
    reviews_df
    .groupby("isbn")
    .head(REVIEWS_PER_BOOK)
    .reset_index(drop=True)
)

books_final = books_sample[[
    "goodreads_id", "isbn", "title", "author",
    "description", "language",
    "coverImg", "publisher", "pages",
    "image_bytes", "has_image",
]]

reviews_final = reviews_final[[
    "review_id", "isbn", "book_id",
    "review_text", "rating", "date_added", "n_votes",
]]

books_final.to_parquet("../data/books.parquet", index=False)
reviews_final.to_parquet("../data/book_reviews.parquet", index=False)

# ── Final info ───────────────────────────────────────────────────────────────
print("\n" + "─" * 50)
print("DONE")
print("─" * 50)
print(f"  Books written:          {len(books_final)}  ->  ../data/books.parquet")
print(f"  Reviews written:        {len(reviews_final)}  ->  ../data/book_reviews.parquet")
print(f"  Books with cover image: {books_final['has_image'].sum()} / {len(books_final)}")
print(f"  Books with review:      {reviews_final['isbn'].nunique()} / {len(books_final)}")
print("─" * 50)