"""
Script that is run once, in order to preapre datasets into local PARQUET files that will be later used for running LLM model but also for vector database ingestion.
Script's task is to load data for books that can be found both at Project Gutenberg and GoodReads dataset.
Reviews are only used for those extracted books. 

Project Gutenberg dataset: https://www.kaggle.com/datasets/lokeshparab/gutenberg-books-and-metadata-2025
GoodReads dataset: https://cseweb.ucsd.edu/~jmcauley/datasets/goodreads.html
    - Meta-Data of Books: Detailed book graph
    - Meta-Data of Books: Detailed information of authors
    - Book Reviews: Complete book reviews
"""

# IMPORTANT NOTICE: After running this script books with book_ids 14329911 (no image) and 20748091 (no e-text found) where deleted using 2 Claude generated scripts found in folder "generated scripts"

# After running this file there should be 8101 book saved.
# After finding books in files that say "has_image":false manually, there should be only one book without an image.
# After running the script to check which book has no e-text, there should be only one book without it.
# After running the script for deleting books twice (once for each of those books with their respected book_id), there should be 8099 books in the dataset.
# At last, the ebook for book_id 14329911 should be manually deleted (etext is 3470).

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

#
#
# --- 1. Load Project Gutenberg metadata ---
#
#
print("\n[1/6] Loading Project Gutenberg metadata...")
books_pg_df = pd.read_csv("data/project_gutenberg/gutenberg_metadata.csv")
print(f"      Number of rows in CSV: {len(books_pg_df)}")

books_pg = (
    books_pg_df
    .drop(["Read online (web)", "Kindle", "Kindle (E-readers incl. Send-to-Kindle)", "Kindle (no images, older E-readers)", "Plain Text UTF-8", "Download HTML(zip)", "Resource Description Framework (RDF)", "Other Links"], axis=1)
    .query("Type == 'Text'")
    .dropna(subset=["EPUB3 (E-readers incl. Send-to-Kindle)", "EPUB (older E-readers)", "EPUB (no images, older E-readers)"])
    .drop_duplicates(subset="Title", keep="first")
    .reset_index(drop=True)
)
print(f"      After dropping irellevant rows and columns: {len(books_pg)} books")

valid_book_titles = set(books_pg["Title"].astype(str))

#
#
# --- 2. Load GoodReads books metadata ---
#
#
print("\n[2/6] Streaming GoodReads books metadata...")

books_goodreads = []

with gzip.open("data/goodreads_books.json.gz", "rt") as f:
    pbar = tqdm(f, desc="Rows reading", unit=" lines", miniters=100_000)
    for line in pbar:
        r = json.loads(line)

        book_title = str(r.get("title", "")).strip()

        if book_title in valid_book_titles and book_title:
            books_goodreads.append(r)

    pbar.close()

print(f"      Done — books from GoodReads after filtering by exact book title: {len(books_goodreads)}.")

books_gr_all = pd.DataFrame(books_goodreads)

# For each book title, we choose that one row with the biggest ratings_count
books_gr_all["ratings_count"] = pd.to_numeric(books_gr_all["ratings_count"], errors="coerce").fillna(0)
books_gr = (
    books_gr_all
    .sort_values("ratings_count", ascending=False)
    .drop_duplicates(subset="title", keep="first")
    .reset_index(drop=True)
)

#
#
# --- 3. Merging the books across different datasets ---
#
#

#
# --- 3.1. Merging the 2 dataframes for Project Gutenberg and GoodReads into one ---
#
print(f"\n[3.1/6] Merging GoodReads and Project Gutenberg datasets by title...")
books_pg = books_pg.rename(columns={"Title": "title", "Etext Number": "etext_number", "EPUB3 (E-readers incl. Send-to-Kindle)": "epub3", "EPUB (older E-readers)": "epub_older", "EPUB (no images, older E-readers)": "epub_no_images"})

merged_books_df = pd.merge(books_pg, books_gr, on="title", how="inner", validate="one_to_one")
print(f"    Datasets have been merged: {merged_books_df.shape}")

#
# --- 3.2. Downloading cover images for those merged books ---
#
# SPORO JE, OKO 2 KNJIGE PO SEKUNDI PA JE PREKINUTO

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


print(f"\n[3.2/6] Downloading cover images for {len(merged_books_df)} books...")
merged_books_df["image_base64"] = fetch_images_with_progress(merged_books_df)
merged_books_df["has_image"]   = merged_books_df["image_base64"].notna()

ok_count = merged_books_df["has_image"].sum()
print(f"      Completed: {ok_count}/{len(merged_books_df)}")

#
# --- 3.3. Merging books with their Main Authors (used for vector DB) ---
#
# Now, from a list of authors, we get all author ids and their roles.
# First, we just get the book title and list of authors.
# Next, we create another df where a book will be found exactly many times as the number of authors.
# After all of that, we get a df that can be merged with the other one.
print(f"\n[3.3/6] Merging books with their Main Authors (useful for vector DB)...")

temp_df = merged_books_df.loc[:, ["title", "authors"]]

author_ids_df = temp_df.explode('authors').reset_index(drop = True)
author_ids_df['author_id'] = author_ids_df['authors'].apply(lambda x: x['author_id'] if isinstance(x, dict) else None)
author_ids_df['role'] = author_ids_df['authors'].apply(lambda x: x['role'] if isinstance(x, dict) else None)
author_ids_df = author_ids_df[['title', 'author_id', 'role']]
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
    .sort_values(by=['title', 'priority'])
    .drop_duplicates(subset="title", keep="first")
    .drop(columns='priority')
    .reset_index(drop=True)
)

books_vectordb_df = pd.merge(merged_books_df, author_ids_df, on="title", how="inner", validate="one_to_one")

print("    Streaming GoodReads authors metadata...")

authors_goodreads = []
author_id_to_name = {}

with gzip.open("data/goodreads_book_authors.json.gz", "rt") as f:
    pbar = tqdm(f, desc="Rows reading", unit=" lines", miniters=100_000)
    for line in pbar:
        r = json.loads(line)
        author_id = str(r.get("author_id", "")).strip()
        author_name = str(r.get("name", "")).strip()
        author_id_to_name[author_id] = author_name

    pbar.close()

books_vectordb_df['author'] = books_vectordb_df['author_id'].map(author_id_to_name)

print(f"    Main Authors have been added for each book for vector DB: {books_vectordb_df.shape}")

#
# --- 3.4. Merging books with their Authors and Genres (used for elasticsearch and relational DB) ---
#
print(f"\n[3.4/6] Merging books with their authors and genres (useful for elasticsearch and relational DB)...")
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

books_elastic_relationaldb_df['genres'] = books_elastic_relationaldb_df['book_id'].astype(str).map(genre_book_id_to_genre)
books_elastic_relationaldb_df['genres'] = books_elastic_relationaldb_df['genres'].apply(
    lambda x: x if isinstance(x, list) else []
)

books_elastic_relationaldb_df['genres_expanded'] = books_elastic_relationaldb_df['book_id'].astype(str).map(genre_book_id_to_genre_expanded)
books_elastic_relationaldb_df['genres_expanded'] = books_elastic_relationaldb_df['genres_expanded'].apply(
    lambda x: x if isinstance(x, list) else []
)

print(f"    Genres have been added for each book for elasticsearch and relational DB: {books_elastic_relationaldb_df.shape}")

#
#
# --- 4. Reviews ---
#
#
print("\n[4/6] Streaming GoodReads reviews metadata...")

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
            if r.get("rating", 0) > 2:
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
# --- 5. Saving ---
#
#
print("\n[5/6] Saving dataframes into files...")

print("     Saving books into PARQUET file for vector DB.")
books_vectordb_final = books_vectordb_df[[
    "book_id", "isbn13", "title", "author",
    "description", "country_code", "language_code", "image_url",
    "image_base64", "publisher", "num_pages", "ratings_count",
    "average_rating", "text_reviews_count", "url", "publication_day",
    "publication_month", "publication_year", "has_image", "etext_number",
    "epub3", "epub_older", "epub_no_images",
    "liked_percent", "review_count", "positive_review_count"
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

print("     Saving reviews into PARQUET file for all DBs.")
reviews_final = reviews_gr[[
    "user_id", "book_id", "review_id", "rating",
    "review_text", "date_added", "date_updated", "finished_at",
    "started_at", "n_votes", "n_comments"
]]
reviews_final.to_parquet("../data/reviews.parquet", index=False)

#
#
# --- 6. Extracting needed text documents for books ---
#
#
print("\n[6/6] Copying text documents for books...")

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