#!/usr/bin/env python3
"""
Skripta koja za zadati `book_id` briše sve povezane zapise iz:
  - books_elasticsearch_relational_db.jsonl  (1 red - polje "book_id")
  - books_vectordb.parquet                    (1 red - kolona "book_id")
  - reviews.parquet                           (0..N redova - kolona "book_id")

genres.json i genres_expanded.json se NE diraju.
book_texts/ folder se NE dira.

Za svako obrisano brisanje se pravi log (JSON) sa tačno onim što je obrisano,
radi audit trail-a - fajl se snima pored ulaznog data foldera kao
`obrisano_<book_id>_<timestamp>.json`.

Upotreba:
    python obrisi_knjigu.py <book_id> --data-dir putanja/do/data

Opciono:
    --dry-run       -> samo pokaži šta bi se obrisalo, ne menjaj fajlove
    --log-dir DIR   -> gde da snimi log fajl (podrazumevano: isti kao --data-dir)
"""

import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

import pandas as pd


def obrisi_iz_jsonl(jsonl_path: Path, book_id: str, dry_run: bool):
    """
    Čita JSONL red po red, izdvaja redove čiji je book_id jednak zadatom
    (obrisani), i piše ostatak nazad u fajl (osim ako je dry_run).
    Vraća listu obrisanih objekata (kao dict).
    """
    if not jsonl_path.exists():
        print(f"[UPOZORENJE] Fajl ne postoji, preskačem: {jsonl_path}", file=sys.stderr)
        return []

    zadrzani_redovi = []
    obrisani_objekti = []

    with jsonl_path.open("r", encoding="utf-8") as f:
        for broj_reda, linija in enumerate(f, start=1):
            sirova = linija.rstrip("\n")
            if not sirova.strip():
                continue  # preskoči prazne redove, ne prepisuj ih nazad

            try:
                obj = json.loads(sirova)
            except json.JSONDecodeError as e:
                print(f"[UPOZORENJE] {jsonl_path.name} red {broj_reda}: nevalidan JSON ({e}) - zadržavam kao što jeste", file=sys.stderr)
                zadrzani_redovi.append(sirova)
                continue

            if str(obj.get("book_id")) == str(book_id):
                obrisani_objekti.append(obj)
            else:
                zadrzani_redovi.append(sirova)

    if obrisani_objekti and not dry_run:
        with jsonl_path.open("w", encoding="utf-8") as f:
            for red in zadrzani_redovi:
                f.write(red + "\n")

    return obrisani_objekti


def obrisi_iz_parquet(parquet_path: Path, book_id: str, dry_run: bool):
    """
    Učitava parquet fajl, izdvaja redove sa zadatim book_id (obrisani),
    piše ostatak nazad u fajl (osim ako je dry_run).
    Vraća listu obrisanih redova (kao list of dict).
    """
    if not parquet_path.exists():
        print(f"[UPOZORENJE] Fajl ne postoji, preskačem: {parquet_path}", file=sys.stderr)
        return []

    df = pd.read_parquet(parquet_path)

    if "book_id" not in df.columns:
        print(f"[GREŠKA] Kolona 'book_id' ne postoji u {parquet_path.name} - preskačem", file=sys.stderr)
        return []

    # Poredimo kao string da izbegnemo probleme int vs str
    maska_za_brisanje = df["book_id"].astype(str) == str(book_id)
    obrisani_df = df[maska_za_brisanje]

    if obrisani_df.empty:
        return []

    obrisani_objekti = obrisani_df.to_dict(orient="records")

    if not dry_run:
        preostali_df = df[~maska_za_brisanje]
        preostali_df.to_parquet(parquet_path, index=False)

    return obrisani_objekti


def main():
    parser = argparse.ArgumentParser(description="Briše sve zapise vezane za dati book_id iz jsonl/parquet fajlova.")
    parser.add_argument("book_id", help="book_id knjige koju treba obrisati")
    parser.add_argument("--data-dir", type=Path, default=Path("data"),
                         help="Putanja do 'data' foldera (podrazumevano: ./data)")
    parser.add_argument("--dry-run", action="store_true",
                         help="Samo prikaži šta bi se obrisalo, bez izmene fajlova")
    parser.add_argument("--log-dir", type=Path, default=None,
                         help="Gde snimiti log fajl (podrazumevano: isti kao --data-dir)")
    args = parser.parse_args()

    data_dir = args.data_dir
    if not data_dir.exists() or not data_dir.is_dir():
        print(f"[GREŠKA] Data folder ne postoji: {data_dir}", file=sys.stderr)
        sys.exit(1)

    book_id = args.book_id
    log_dir = args.log_dir if args.log_dir is not None else data_dir

    jsonl_path = data_dir / "books_elasticsearch_relational_db.jsonl"
    vectordb_path = data_dir / "books_vectordb.parquet"
    reviews_path = data_dir / "reviews.parquet"

    print(f"{'[DRY-RUN] ' if args.dry_run else ''}Brišem sve zapise za book_id={book_id!r} u '{data_dir}'...\n")

    # 1. books_elasticsearch_relational_db.jsonl
    obrisani_books = obrisi_iz_jsonl(jsonl_path, book_id, args.dry_run)
    print(f"books_elasticsearch_relational_db.jsonl: obrisano {len(obrisani_books)} red(ova)")

    # 2. books_vectordb.parquet
    obrisani_vectordb = obrisi_iz_parquet(vectordb_path, book_id, args.dry_run)
    print(f"books_vectordb.parquet: obrisano {len(obrisani_vectordb)} red(ova)")

    # 3. reviews.parquet (može biti više redova)
    obrisani_reviews = obrisi_iz_parquet(reviews_path, book_id, args.dry_run)
    print(f"reviews.parquet: obrisano {len(obrisani_reviews)} red(ova)")

    ukupno = len(obrisani_books) + len(obrisani_vectordb) + len(obrisani_reviews)
    print(f"\nUkupno obrisano: {ukupno} red(ova) preko sva 3 fajla.")

    if ukupno == 0:
        print(f"\nNapomena: book_id={book_id!r} nije pronađen ni u jednom fajlu - ništa nije obrisano.")

    # 4. Snimi log šta je obrisano (uvek, i za dry-run, radi transparentnosti)
    timestamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    log_path = log_dir / f"obrisano_{book_id}_{timestamp}.json"

    log_sadrzaj = {
        "book_id": book_id,
        "vreme_izvrsavanja_utc": timestamp,
        "dry_run": args.dry_run,
        "obrisano": {
            "books_elasticsearch_relational_db.jsonl": obrisani_books,
            "books_vectordb.parquet": obrisani_vectordb,
            "reviews.parquet": obrisani_reviews,
        },
        "ukupno_obrisanih_redova": ukupno,
    }

    with log_path.open("w", encoding="utf-8") as f:
        json.dump(log_sadrzaj, f, ensure_ascii=False, indent=2, default=str)

    print(f"\nLog sa obrisanim podacima sačuvan u: {log_path}")


if __name__ == "__main__":
    main()