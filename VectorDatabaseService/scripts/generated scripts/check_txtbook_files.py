#!/usr/bin/env python3
"""
Skripta koja:
1. Čita JSONL fajl (red po red, pogodno za velike fajlove)
2. Izvlači polja `book_id` i `etext_number` iz svakog objekta
3. Proverava koji fajlovi (imenovani po etext_number, bez ekstenzije)
   nedostaju u zadatom folderu
4. Ispisuje rezultat i snima ga u CSV

Upotreba:
    python proveri_nedostajuce.py putanja/do/fajla.jsonl putanja/do/foldera

Opciono:
    --output nedostajuci.csv     -> gde da snimi CSV sa rezultatom
    --encoding utf-8              -> encoding JSONL fajla (podrazumevano utf-8)
"""

import argparse
import csv
import json
import sys
from pathlib import Path


def ucitaj_zapise(jsonl_path: Path, encoding: str = "utf-8"):
    """
    Generator koji čita JSONL fajl red po red i vraća (book_id, etext_number)
    za svaki validan red. Redove koji nisu validan JSON ili im nedostaje
    etext_number preskačemo uz upozorenje na stderr.
    """
    with jsonl_path.open("r", encoding=encoding) as f:
        for broj_reda, linija in enumerate(f, start=1):
            linija = linija.strip()
            if not linija:
                continue  # preskoči prazne redove

            try:
                obj = json.loads(linija)
            except json.JSONDecodeError as e:
                print(f"[UPOZORENJE] Red {broj_reda}: nevalidan JSON ({e}) - preskačem", file=sys.stderr)
                continue

            book_id = obj.get("book_id")
            etext_number = obj.get("etext_number")

            if etext_number is None:
                print(f"[UPOZORENJE] Red {broj_reda}: nema 'etext_number' - preskačem", file=sys.stderr)
                continue

            # etext_number normalizujemo u string (u fajlu je broj, npr. 3470)
            yield book_id, str(etext_number)


def main():
    parser = argparse.ArgumentParser(description="Proverava koji fajlovi (etext_number) nedostaju u folderu.")
    parser.add_argument("jsonl_fajl", type=Path, help="Putanja do JSONL fajla")
    parser.add_argument("folder", type=Path, help="Putanja do foldera koji se proverava")
    parser.add_argument("--output", type=Path, default=Path("nedostajuci_fajlovi.csv"),
                         help="Putanja do CSV fajla u koji se snima rezultat (podrazumevano: nedostajuci_fajlovi.csv)")
    parser.add_argument("--encoding", default="utf-8", help="Encoding JSONL fajla (podrazumevano: utf-8)")
    args = parser.parse_args()

    if not args.jsonl_fajl.exists():
        print(f"[GREŠKA] JSONL fajl ne postoji: {args.jsonl_fajl}", file=sys.stderr)
        sys.exit(1)

    if not args.folder.exists() or not args.folder.is_dir():
        print(f"[GREŠKA] Folder ne postoji ili nije direktorijum: {args.folder}", file=sys.stderr)
        sys.exit(1)

    # 1. Skupi sve nazive fajlova koji trenutno postoje u folderu (bez ekstenzije, tačan naziv)
    postojeci_fajlovi = {p.name for p in args.folder.iterdir() if p.is_file()}
    print(f"Pronađeno {len(postojeci_fajlovi)} fajlova u folderu '{args.folder}'.")

    # 2. Prođi kroz JSONL i za svaki zapis proveri da li odgovarajući fajl postoji
    ukupno_zapisa = 0
    nedostaju = []

    for book_id, etext_number in ucitaj_zapise(args.jsonl_fajl, encoding=args.encoding):
        ukupno_zapisa += 1
        if etext_number not in postojeci_fajlovi:
            nedostaju.append({"book_id": book_id, "etext_number": etext_number})

    print(f"Ukupno obrađeno zapisa iz JSONL: {ukupno_zapisa}")
    print(f"Nedostaje fajlova: {len(nedostaju)}")

    # 3. Snimi rezultat u CSV
    if nedostaju:
        with args.output.open("w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=["book_id", "etext_number"])
            writer.writeheader()
            writer.writerows(nedostaju)
        print(f"Lista nedostajućih fajlova sačuvana u: {args.output}")

        # Ispiši i prvih par primera u konzoli, radi brzog uvida
        print("\nPrimeri (prvih 10):")
        for red in nedostaju[:10]:
            print(f"  book_id={red['book_id']}  etext_number={red['etext_number']}")
    else:
        print("Svi fajlovi postoje - ništa ne nedostaje. 🎉")


if __name__ == "__main__":
    main()