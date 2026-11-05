"""
Script that is run once, in order to preapre datasets into local PARQUET files that will be later used for running LLM model but also for vector database ingestion.
Script's task is to load data for books that can be found both at Project Gutenberg and GoodReads dataset.
Reviews are only used for those extracted books. 

Project Gutenberg dataset: https://www.kaggle.com/datasets/lokeshparab/gutenberg-books-and-metadata-2025
GoodReads dataset: https://cseweb.ucsd.edu/~jmcauley/datasets/goodreads.html
    - Meta-Data of Books: Detailed book graph
    - Meta-Data of Books: Detailed information of authors
    - Book Reviews: Complete book reviews
GoodReads authors dataset: https://www.kaggle.com/datasets/choobani/goodread-authors
"""
# pyarrow is also needed

import pandas as pd
import gzip, json
import base64
import requests
from PIL import Image
from io import BytesIO
from tqdm import tqdm
from datetime import datetime
from collections import defaultdict
import numpy as np
from pathlib import Path
import shutil
from rapidfuzz import fuzz
import unicodedata
import re

# Book is kept only if its Project Gutenberg author is similar enough to a GoodReads author.
AUTHOR_THRESHOLD = 80

# Path for data saving
dir_path = Path("../data/")
dir_path.mkdir(parents=True, exist_ok=True)

#
#
# --- 1. Load Project Gutenberg metadata ---
#
#

print("\n[1/7] Loading Project Gutenberg metadata...")
books_pg_df = pd.read_csv("data/project_gutenberg/gutenberg_metadata.csv")
print(f"      Number of rows in CSV: {len(books_pg_df)}")

books_pg_df["Title"] = books_pg_df["Title"].astype(str).str.replace("\n", " ", regex=False)

books_pg = (
    books_pg_df
    .drop(["Read online (web)", "Kindle", "Kindle (E-readers incl. Send-to-Kindle)", "Kindle (no images, older E-readers)", "Plain Text UTF-8", "Download HTML(zip)", "Resource Description Framework (RDF)", "Other Links"], axis=1)
    .query("Type == 'Text'")
    .dropna(subset=["EPUB3 (E-readers incl. Send-to-Kindle)", "EPUB (older E-readers)", "EPUB (no images, older E-readers)"])
    .reset_index(drop=True)
)
print(f"      After dropping irellevant rows and columns: {len(books_pg)} books")

# Only keeps the books where the etext file actually exists.
books_texts_dir = Path("./data/project_gutenberg/books")
available_etext_numbers = { p.name for p in books_texts_dir.iterdir() if p.is_file() }

books_pg["Etext Number"] = books_pg["Etext Number"].astype(str)
before_text_filter = len(books_pg)
books_pg = books_pg[books_pg["Etext Number"].isin(available_etext_numbers)].reset_index(drop = True)

print(f"      Dropped {before_text_filter - len(books_pg)} books with no matching e-text file, {len(books_pg)} remain")

valid_book_titles = set(books_pg["Title"].astype(str))

#
#
# --- 2. Load GoodReads books metadata and authors metadata ---
#
#
print("\n[2/7] Streaming GoodReads books metadata and authors metadata...")

print("    Streaming GoodReads authors metadata...")

author_id_to_name = {}

with gzip.open("data/goodreads_book_authors.json.gz", "rt") as f:
    pbar = tqdm(f, desc="Rows reading", unit=" lines", miniters=100_000)
    for line in pbar:
        r = json.loads(line)
        author_id = str(r.get("author_id", "")).strip()
        author_name = str(r.get("name", "")).strip()
        author_id_to_name[author_id] = author_name
    pbar.close()

print("    Streaming GoodReads books metadata...")

books_goodreads = []

with gzip.open("data/goodreads_books.json.gz", "rt") as f:
    pbar = tqdm(f, desc="Rows reading", unit=" lines", miniters=100_000)
    for line in pbar:
        r = json.loads(line)
        book_title = str(r.get("title_without_series") or r.get("title") or "").strip()
        if book_title and book_title in valid_book_titles:
            r["title_key"] = book_title
            r["author_names"] = [
                author_id_to_name.get(str(a.get("author_id", "")).strip(), "")
                for a in r.get("authors", []) if isinstance(a, dict)
            ]
            books_goodreads.append(r)
    pbar.close()

print(f"      Done — books from GoodReads after filtering by exact book title: {len(books_goodreads)}.")

# Now, we will keep all candidates and choose the right one in step 3.1
books_gr_all = pd.DataFrame(books_goodreads)
books_gr_all["book_id"] = books_gr_all["book_id"].astype(str)
books_gr_all["ratings_count"] = pd.to_numeric(books_gr_all["ratings_count"], errors="coerce").fillna(0)

#
#
# --- 3. Merging the books across different datasets ---
#
#

#
# --- 3.1. Merging the 2 dataframes for Project Gutenberg and GoodReads into one ---
#
print(f"\n[3.1/7] Merging GoodReads and Project Gutenberg datasets by title and author...")
books_pg = books_pg.rename(columns={"Title": "title", "Etext Number": "etext_number", "EPUB3 (E-readers incl. Send-to-Kindle)": "epub3", "EPUB (older E-readers)": "epub_older", "EPUB (no images, older E-readers)": "epub_no_images"})

# Helper functions used for name cleaning
def clean_name(text: str) -> str:
    text = unicodedata.normalize("NFKD", text)
    text = "".join(c for c in text if not unicodedata.combining(c)).casefold()
    text = re.sub(r"['’`]", "", text)
    text = text.replace("&", " and ")
    return re.sub(r"[\W_]+", " ", text).strip()

def pg_author_names(raw) -> list:
    if not isinstance(raw, str):
        return []
    names = []
    for chunk in raw.split(";"):
        chunk = re.sub(r"\[[^\]]*\]|\([^)]*\)", " ", chunk) 
        parts = [p.strip() for p in chunk.split(",") if p.strip() and not re.search(r"\d", p)]
        if parts:
            names.append(clean_name(f"{parts[1]} {parts[0]}" if len(parts) > 1 else parts[0]))
    return [n for n in names if n]

def author_score(pg_names: list, gr_names: list) -> float:
    # Best similarity (0-100) between any Gutenberg author and any GoodReads author of the book.
    if not pg_names or not gr_names:
        return 0
    return max(fuzz.token_set_ratio(a, b) for a in pg_names for b in gr_names)

# GoodReads rows whose author matches the Gutenberg author are allowed to be merged.
# 1) All pairs (Gutenberg book, GoodReads book) with the identical title. Many-to-many on purpose.
candidates = pd.merge(books_pg, books_gr_all, left_on="title", right_on="title_key", how="inner", suffixes=("", "_goodreads"))

# 2) Author score for every pair
candidates["author_score"] = [
    author_score(pg_author_names(pg_authors), [clean_name(n) for n in gr_names if n])
    for pg_authors, gr_names in zip(candidates["Authors"], candidates["author_names"])
]
candidates_ok = candidates[candidates["author_score"] >= AUTHOR_THRESHOLD]

# 3) Each Gutenberg book (etext_number) keeps one GoodReads edition: the one with the best author match and then the most ratings.
# 4) Each GoodReads book (book_id) can appear only once. Gutenberg might have several e-texts of the same work, so the one with the lowest etext number is kept.
merged_books_df = (
    candidates_ok
    .sort_values(["author_score", "ratings_count"], ascending=False)
    .drop_duplicates(subset="etext_number", keep="first")
    .sort_values("etext_number", key=lambda s: pd.to_numeric(s, errors="coerce"))
    .drop_duplicates(subset="book_id", keep="first")
    .reset_index(drop=True)
)
assert merged_books_df["etext_number"].is_unique and merged_books_df["book_id"].is_unique

# Gutenberg books that have GoodReads books with the same title, but none with a matching author
no_match = candidates[~candidates["etext_number"].isin(candidates_ok["etext_number"])]
no_match = no_match.sort_values("author_score", ascending=False).drop_duplicates(subset="etext_number", keep="first")
pd.DataFrame({
    "etext_number": no_match["etext_number"],
    "title": no_match["title"],
    "pg_authors": no_match["Authors"],
    "gr_authors": no_match["author_names"].map("; ".join),
    "author_score": no_match["author_score"],
}).to_csv("../data/matching_rejected_pairs.csv", index=False)

print(f"    Title + author pairs: {len(candidates_ok)} of {len(candidates)} | \n    Gutenberg books without author match: {len(no_match)} | merged books: {len(merged_books_df)}")

print(f"    Datasets have been merged: {merged_books_df.shape}")

#
# --- 3.2. Downloading cover images for those merged books ---
#
# It downloads around 2 book covers per second
# Helper functions for image downloading
def fetch_image_bytes(url: str):
    try:
        resp = requests.get(url, timeout=10)
        resp.raise_for_status()
        Image.open(BytesIO(resp.content)).verify()
        return base64.b64encode(resp.content).decode("utf-8")
    except Exception:
        return None

def fetch_images_with_progress(df: pd.DataFrame) -> pd.Series:
    results = []
    for _, row in tqdm(df.iterrows(), total=len(df), desc="Image downloading", unit="book"):
        results.append(fetch_image_bytes(row["image_url"]))
        done = len(results)
        if done % 1000 == 0:
            ok  = sum(1 for r in results if r is not None)
            fail = done - ok
            tqdm.write(f"  ✔ {done}/{len(df)} | succesful: {ok} | unsuccesful: {fail}")
    return pd.Series(results, index=df.index)


print(f"\n[3.2/7] Downloading cover images for {len(merged_books_df)} books...")

merged_books_df["image_base64"] = fetch_images_with_progress(merged_books_df)
merged_books_df["has_image"]   = merged_books_df["image_base64"].notna()

ok_count = merged_books_df["has_image"].sum()
print(f"      Completed: {ok_count}/{len(merged_books_df)}")

# Books without a succesfully downloaded cover image are dropped.
before_image_filter = len(merged_books_df)
merged_books_df = merged_books_df[merged_books_df["has_image"]].reset_index(drop=True) 

print(f"    Dropped {before_image_filter - len(merged_books_df)} books with no cover image, {len(merged_books_df)} remain.")

#
# --- 3.3. Merging books with their Main Authors (used for vector DB) ---
#
# Now, from a list of authors, we get all author ids and their roles.
# First, we just get the book title and list of authors.
# Next, we create another df where a book will be found exactly many times as the number of authors.
# After all of that, we get a df that can be merged with the other one.
print(f"\n[3.3/7] Merging books with their Main Authors (useful for vector DB)...")

temp_df = merged_books_df.loc[:, ["book_id", "authors"]]

author_ids_df = temp_df.explode('authors').reset_index(drop = True)
author_ids_df['author_id'] = author_ids_df['authors'].apply(lambda x: x['author_id'] if isinstance(x, dict) else None)
author_ids_df['role'] = author_ids_df['authors'].apply(lambda x: x['role'] if isinstance(x, dict) else None)
author_ids_df = author_ids_df[['book_id', 'author_id', 'role']]
author_ids_df.loc[author_ids_df["role"] == '', 'role'] = 'Main Author'

def priority(role):
    if role == 'Main Author':
        return 0
    elif pd.isna(role):
        return 2
    else:
        return 1

author_ids_df['priority'] = author_ids_df['role'].apply(priority)

author_ids_df = (
    author_ids_df
    .sort_values(by=['book_id', 'priority'])
    .drop_duplicates(subset="book_id", keep="first")
    .drop(columns='priority')
    .reset_index(drop=True)
)

books_vectordb_df = pd.merge(merged_books_df, author_ids_df, on="book_id", how="inner", validate="one_to_one")

books_vectordb_df['author'] = books_vectordb_df['author_id'].map(author_id_to_name)

print(f"    Main Authors have been added for each book for vector DB: {books_vectordb_df.shape}")

#
# --- 3.4. Merging books with their Authors and Genres (used for elasticsearch and relational DB) ---
#
print(f"\n[3.4/7] Merging books with their authors and genres (useful for elasticsearch and relational DB)...")
# Authors
def add_author_names(authors_list):
    enriched = []
    for a in authors_list:
        if not isinstance(a, dict):
            continue
        a_copy = dict(a)
        author_id = str(a.get("author_id", "")).strip()
        role = str(a.get("role", "")).strip()
        a_copy['name'] = author_id_to_name.get(author_id, "")
        a_copy['role'] = role if role else "Main Author"
        enriched.append(a_copy)
    return enriched

books_elastic_relationaldb_df = merged_books_df.copy()

books_elastic_relationaldb_df['authors_enriched'] = books_elastic_relationaldb_df['authors'].apply(add_author_names)

print(f"    Main Authors have been added for each book for elasticsearch and relational DB: {books_elastic_relationaldb_df.shape}")

# Genres
print("    Streaming GoodReads genres metadata...")

genres_goodreads = set()
genres_goodreads_expanded = set()

genre_book_id_to_genre = {}
genre_book_id_to_genre_expanded = {}

with gzip.open("data/goodreads_book_genres_initial.json.gz", "rt") as f:
    pbar = tqdm(f, desc="Rows reading", unit=" lines", miniters=100_000)
    for line in pbar:
        r = json.loads(line)
        book_id = str(r.get("book_id", "")).strip()
        genres_dict = r.get("genres", {})
        genre_list = list(genres_dict.keys())
        genre_list_expanded = sorted(set(
            g.strip() for combo in genre_list for g in combo.split(",")
        ))
        genre_book_id_to_genre[book_id] = genre_list
        genre_book_id_to_genre_expanded[book_id] = genre_list_expanded
        genres_goodreads.update(genre_list)
        genres_goodreads_expanded.update(genre_list_expanded)

    pbar.close()

# For relational and elasticsearch DB
books_elastic_relationaldb_df['genres'] = books_elastic_relationaldb_df['book_id'].astype(str).map(genre_book_id_to_genre)
books_elastic_relationaldb_df['genres'] = books_elastic_relationaldb_df['genres'].apply(
    lambda x: x if isinstance(x, list) else []
)
books_elastic_relationaldb_df['genres_expanded'] = books_elastic_relationaldb_df['book_id'].astype(str).map(genre_book_id_to_genre_expanded)
books_elastic_relationaldb_df['genres_expanded'] = books_elastic_relationaldb_df['genres_expanded'].apply(
    lambda x: x if isinstance(x, list) else []
)
print(f"    Genres have been added for each book for elasticsearch and relational DB: {books_elastic_relationaldb_df.shape}")

# For vector DB
books_vectordb_df['genres'] = books_vectordb_df['book_id'].astype(str).map(genre_book_id_to_genre)
books_vectordb_df['genres'] = books_vectordb_df['genres'].apply(
    lambda x: x if isinstance(x, list) else []
)
books_vectordb_df['genres_expanded'] = books_vectordb_df['book_id'].astype(str).map(genre_book_id_to_genre_expanded)
books_vectordb_df['genres_expanded'] = books_vectordb_df['genres_expanded'].apply(
    lambda x: x if isinstance(x, list) else []
)
print(f"    Genres have been added for each book for vector DB: {books_vectordb_df.shape}")

#
#
# --- 4. Authors ---
#
#
print("\n[4/7] Streaming GoodReads authors metadata from extended dataset...")

authors_extra_df = pd.read_csv("./data/final_dataset.csv")
authors_extra_df = authors_extra_df.rename(columns={"authorid": "author_id"})
authors_extra_df["author_id"] = authors_extra_df["author_id"].astype(str).str.strip()

authors_extra_df["about"] = (
    authors_extra_df["about"]
    .fillna("")
    .astype(str)
    .str.replace(r"<[^>]+>", "", regex=True)
)

authors_extra_df = authors_extra_df[["author_id", "name", "image_url", "about", "born", "died"]]

book_author_ids = set(
    books_elastic_relationaldb_df["authors_enriched"]
    .explode()
    .dropna()
    .apply(lambda a: str(a.get("author_id", "")).strip() if isinstance(a, dict) else None)
    .dropna()
)
book_author_ids.discard("")

authors_final_df = pd.DataFrame({"author_id": sorted(book_author_ids)})
authors_final_df = pd.merge(authors_final_df, authors_extra_df, on="author_id", how="left")

print(f"    Authors appearing in books: {len(authors_final_df)} | matched in prior dataset: {authors_final_df['name'].notna().sum()}")

before_fallback_na = authors_final_df["name"].isna().sum()
# Fallback: for authors missing a name from the extended dataset (final_dataset.csv),
# use the name already resolved from the GoodReads authors dataset (author_id_to_name,
# built in step 2) instead of leaving it null.
missing_name_mask = authors_final_df["name"].isna() | (authors_final_df["name"].astype(str).str.strip() == "")
authors_final_df.loc[missing_name_mask, "name"] = (
    authors_final_df.loc[missing_name_mask, "author_id"].map(author_id_to_name)
)
after_fallback_na = authors_final_df["name"].isna().sum()

print(f"    Filled {before_fallback_na - after_fallback_na} missing names via GoodReads authors fallback, {after_fallback_na} still null")

#
#
# --- 5. Reviews ---
#
#
print("\n[5/7] Streaming GoodReads reviews metadata...")

valid_ids = set(books_vectordb_df["book_id"].astype(str))

reviews_goodreads = []

review_counts = defaultdict(int)
positive_counts = defaultdict(int)

with gzip.open("data/goodreads_reviews_dedup.json.gz", "rt") as f:
    pbar = tqdm(f, desc="Rows reading", unit=" lines", miniters=100_000)
    for line in pbar:
        r = json.loads(line)

        read_at_raw = r.get("read_at")
        started_at_raw = r.get("started_at")

        read_at = None
        started_at = None

        if read_at_raw not in (None, ""):
            try:
                read_at = datetime.strptime(str(read_at_raw), "%a %b %d %H:%M:%S %z %Y")
            except (TypeError, ValueError):
                pass
        if started_at_raw not in (None, ""):
            try:
                started_at = datetime.strptime(str(started_at_raw), "%a %b %d %H:%M:%S %z %Y")
            except (TypeError, ValueError):
                pass

        if read_at is not None and started_at is not None:
            if read_at < started_at:
                continue

        book_id = str(r.get("book_id", "")).strip() 
        if book_id in valid_ids:
            reviews_goodreads.append(r)
            review_counts[book_id] += 1
            if r.get("rating", 0) > 3:
                positive_counts[book_id] += 1

    pbar.close()

reviews_gr = pd.DataFrame(reviews_goodreads)
reviews_gr = reviews_gr.rename(columns={"read_at": "finished_at"})

books_vectordb_df["review_count"] = books_vectordb_df["book_id"].astype(str).map(review_counts).fillna(0).astype(int)
books_vectordb_df["positive_review_count"] = books_vectordb_df["book_id"].astype(str).map(positive_counts).fillna(0).astype(int)
books_vectordb_df["liked_percent"] = np.where(
    books_vectordb_df["review_count"] > 0,
    (books_vectordb_df["positive_review_count"] / books_vectordb_df["review_count"]) * 100,
    0
)

books_elastic_relationaldb_df["review_count"] = books_elastic_relationaldb_df["book_id"].astype(str).map(review_counts).fillna(0).astype(int)
books_elastic_relationaldb_df["positive_review_count"] = books_elastic_relationaldb_df["book_id"].astype(str).map(positive_counts).fillna(0).astype(int)
books_elastic_relationaldb_df["liked_percent"] = np.where(
    books_elastic_relationaldb_df["review_count"] > 0,
    (books_elastic_relationaldb_df["positive_review_count"] / books_elastic_relationaldb_df["review_count"]) * 100,
    0
)

print(f"      Done — reviews from GoodReads after filtering by book_id: {len(reviews_goodreads)}.")

#
#
# --- 6. Saving ---
#
#
print("\n[6/7] Saving dataframes into files...")

print("     Saving books into PARQUET file for vector DB.")
books_vectordb_final = books_vectordb_df[[
    "book_id", "isbn13", "title", "author",
    "description", "country_code", "language_code", "image_url",
    "image_base64", "publisher", "num_pages", "ratings_count",
    "average_rating", "text_reviews_count", "url", "publication_day",
    "publication_month", "publication_year", "has_image", "etext_number",
    "epub3", "epub_older", "epub_no_images",
    "genres", "genres_expanded", "liked_percent", "review_count", 
    "positive_review_count"
]]
books_vectordb_final.to_parquet("../data/books_vectordb.parquet", index=False)

print("     Saving books into JSON file for elasticsearch and relational DB.")
books_elastic_relationaldb_final = books_elastic_relationaldb_df[[
    "book_id", "isbn13", "title", "authors_enriched",
    "description", "country_code", "language_code", "image_url",
    "image_base64", "publisher", "num_pages", "ratings_count",
    "average_rating", "text_reviews_count", "url", "publication_day",
    "publication_month", "publication_year", "has_image", "etext_number",
    "epub3", "epub_older", "epub_no_images",
    "genres", "genres_expanded", "liked_percent", "review_count", 
    "positive_review_count"
]]
books_elastic_relationaldb_final.to_json(
    "../data/books_elasticsearch_relational_db.jsonl",
    orient="records",
    lines=True,
    force_ascii=False
)

print("     Saving genres into JSON files for relational DB.")
genre_relationaldb_final = pd.DataFrame(genres_goodreads)
genre_relationaldb_final.to_json(
    "../data/genres.jsonl",
    orient="records",
    lines=True,
    force_ascii=False
)
genre_expanded_relationaldb_final = pd.DataFrame(genres_goodreads_expanded)
genre_expanded_relationaldb_final.to_json(
    "../data/genres_expanded.jsonl",
    orient="records",
    lines=True,
    force_ascii=False
)

print("     Saving authors into JSON file for relational DB.")
authors_final_df.to_json(
    "../data/authors.jsonl",
    orient="records",
    lines=True,
    force_ascii=False
)

print("     Saving reviews into PARQUET file for all DBs.")
reviews_final = reviews_gr[[
    "user_id", "book_id", "review_id", "rating",
    "review_text", "date_added", "date_updated", "finished_at",
    "started_at", "n_votes", "n_comments"
]]
reviews_final.to_parquet("../data/reviews.parquet", index=False)

#
#
# --- 7. Extracting needed text documents for books ---
#
#
print("\n[7/7] Copying text documents for books...")

def copy_files_by_rule(src_dir: str, dst_dir: str):
    source = Path(src_dir)
    destination = Path(dst_dir)

    valid_etext_numbers = set(books_vectordb_final["etext_number"].astype(str))
    
    destination.mkdir(parents=True, exist_ok=True)

    counter = 0
    
    for file_path in source.iterdir():
        if file_path.is_file() and file_path.name.lower() in valid_etext_numbers:
            target_path = destination / file_path.name
            
            shutil.copy2(file_path, target_path)
            counter += 1

    print(f"     Copied {counter} files.")

copy_files_by_rule("./data/project_gutenberg/books", "../data/book_texts")