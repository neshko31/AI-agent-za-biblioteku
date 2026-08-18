# NAPOMENE: 
# 1. Nije dobro kreiran sam regex pa samim tim ni statistika koja ide uz njega
# 2. Deluje da je provera za sadrzaj dobra
# 3. Zaista je proveravano da je u pitanju stvarno poglavlje dakle CHAPTER (ideja je da bi trebalo nesto po sadrzaju i ovo kombinovati)
# 4. Statistika pokazuje na neki cudne outliers
# 5. Deluje u redu.



"""
corpus_probe.py

Sonda za analizu Gutenberg korpusa PRE pisanja produkcionog pipeline-a.
Cilj nije savršena klasifikacija svake knjige, nego uvid u DISTRIBUCIJU
problema kroz ceo skup, da bi se odlučilo koliko robustan splitter treba.

Pokriva 5 pitanja iz toka:
  1. START/END marker provera
  2. Sadržaj (Table of Contents) na početku/kraju
  3. Chapter pokrivenost (koliko knjiga uopšte ima poglavlja)
  4. Distribucija dužine poglavlja (i celih knjiga bez poglavlja)
  5. Gruba proza/poezija heuristika

Upotreba:
    python corpus_probe.py --input-dir /path/do/txt/fajlova --output-dir ./probe_results

Na Kaggle-u: promeni samo --input-dir na putanju gde su ti .txt fajlovi
(obično /kaggle/input/<dataset-name>/).
"""

import argparse
import re
import statistics
from dataclasses import dataclass, field, asdict
from pathlib import Path

import pandas as pd


# ---------------------------------------------------------------------------
# Regex-i - namerno permisivni jer još ne znamo tačan raspon varijanti
# ---------------------------------------------------------------------------

# Pokriva i "START OF THIS" i "START OF THE", i sa/bez broja/naslova posle.
# Radi na jednoj liniji ili tekstu koji sadrži tu liniju (re.MULTILINE nije
# potreban jer tražimo unutar celog stringa sa DOTALL isključenim po redu).
START_MARKER_RE = re.compile(
    r"\*\*\*\s*START OF (?:THIS|THE)?\s*PROJECT GUTENBERG EBOOK.*?\*\*\*",
    re.IGNORECASE,
)
END_MARKER_RE = re.compile(
    r"\*\*\*\s*END OF (?:THIS|THE)?\s*PROJECT GUTENBERG EBOOK.*?\*\*\*",
    re.IGNORECASE,
)

# Fallback - generički pattern iz tvog predloga, širi od gornja dva.
# Koristimo ga da uhvatimo knjige koje NISU pogodile stroge regexe iznad,
# da vidimo da li uopšte imaju NEKI oblik markera (možda stariji/drugačiji format).
GENERIC_START_RE = re.compile(r"\*\*\*\s*START.+?\*\*\*", re.IGNORECASE)
GENERIC_END_RE = re.compile(r"\*\*\*\s*END.+?\*\*\*", re.IGNORECASE)

# Table of contents - tražimo reč "content" na svojoj liniji (case-insensitive),
# to je najčešći Gutenberg obrazac (kao u 11.txt: "Contents").
TOC_HEADING_RE = re.compile(r"^\s*(contents|table of contents)\s*$", re.IGNORECASE | re.MULTILINE)

# Chapter marker - rimski ILI arapski brojevi, prati tvoj predlog ali malo šire
# (npr. "Chapter 1", "CHAPTER I.", "Chapter One" bismo promašili namerno -
#  brojčane/rimske forme su najčešće, tekstualne ("One", "Two") suređe i
#  mogu se dodati kasnije ako sonda pokaže da ih ima puno).
CHAPTER_RE = re.compile(
    r"^\s*chapter\s+(?:[ivxlcdm]+|\d+)\b\.?\s*$",
    re.IGNORECASE | re.MULTILINE,
)

WORD_RE = re.compile(r"\S+")
SENTENCE_END_RE = re.compile(r"[.!?]")


@dataclass
class BookProbeResult:
    """Rezultat sonde za jednu knjigu - jedan red u finalnom DataFrame-u."""

    file_name: str
    file_size_bytes: int = 0

    # (1) START/END markeri
    has_strict_start: bool = False
    has_strict_end: bool = False
    has_generic_start_only: bool = False  # ima generički START ali ne strogi
    has_generic_end_only: bool = False
    has_no_start_marker_at_all: bool = False
    has_no_end_marker_at_all: bool = False

    # (2) Sadržaj (TOC)
    has_toc_heading: bool = False
    toc_position_ratio: float = -1.0  # 0.0 = na početku, 1.0 = na kraju, -1 = nema

    # (3) Chapter pokrivenost
    chapter_marker_count: int = 0
    has_chapters: bool = False  # >= 2 markera = smatramo da ima poglavlja

    # (4) Dužina poglavlja / cele knjige
    total_word_count: int = 0
    chapter_word_counts: list = field(default_factory=list)  # prazna lista ako nema poglavlja

    # (5) Proza/poezija heuristika
    avg_words_per_line: float = 0.0
    median_words_per_line: float = 0.0
    punctuation_density: float = 0.0  # broj interpunkcijskih znakova / broj karaktera
    short_line_ratio: float = 0.0  # % linija kraćih od 6 reči (indikator stihova)
    likely_verse: bool = False  # gruba oznaka, NE finalna klasifikacija

    # Greške/napomene - da ništa ne prođe nezapaženo
    notes: str = ""


def probe_single_book(file_path: Path) -> BookProbeResult:
    """Analizira jedan .txt fajl i vraća BookProbeResult. Ne baca izuzetke -
    greške se beleže u polju notes, jer sa 8000 fajlova očekujemo da neki
    budu oštećeni/prazni/čudno kodirani, i to samo po sebi je podatak."""

    result = BookProbeResult(file_name=file_path.name)
    notes = []

    try:
        result.file_size_bytes = file_path.stat().st_size
        raw_text = file_path.read_text(encoding="utf-8", errors="replace")
    except Exception as exc:  # noqa: BLE001 - namerno široko, ovo je sonda
        result.notes = f"READ_ERROR: {exc}"
        return result

    if not raw_text.strip():
        result.notes = "EMPTY_FILE"
        return result

    # --- (1) START/END marker provera -------------------------------------
    strict_start_match = START_MARKER_RE.search(raw_text)
    strict_end_match = END_MARKER_RE.search(raw_text)
    result.has_strict_start = strict_start_match is not None
    result.has_strict_end = strict_end_match is not None

    if not result.has_strict_start:
        generic_start_match = GENERIC_START_RE.search(raw_text)
        result.has_generic_start_only = generic_start_match is not None
        result.has_no_start_marker_at_all = generic_start_match is None
    if not result.has_strict_end:
        generic_end_match = GENERIC_END_RE.search(raw_text)
        result.has_generic_end_only = generic_end_match is not None
        result.has_no_end_marker_at_all = generic_end_match is None

    # Izdvoji "telo" knjige (između markera) za dalju analizu ako oba postoje;
    # u suprotnom radi nad celim tekstom i obeleži to u notes.
    if strict_start_match and strict_end_match:
        body = raw_text[strict_start_match.end():strict_end_match.start()]
    else:
        body = raw_text
        notes.append("NO_CLEAN_BODY_EXTRACTION_used_full_text")

    body = body.strip()
    if not body:
        result.notes = "; ".join(notes + ["EMPTY_BODY_AFTER_MARKER_STRIP"])
        return result

    # --- (2) Sadržaj (TOC) provera ------------------------------------------
    toc_match = TOC_HEADING_RE.search(body)
    result.has_toc_heading = toc_match is not None
    if toc_match:
        result.toc_position_ratio = round(toc_match.start() / max(len(body), 1), 4)

    # --- (3) Chapter pokrivenost --------------------------------------------
    chapter_matches = list(CHAPTER_RE.finditer(body))
    result.chapter_marker_count = len(chapter_matches)
    result.has_chapters = len(chapter_matches) >= 2

    # --- (4) Distribucija dužine ---------------------------------------------
    all_words = WORD_RE.findall(body)
    result.total_word_count = len(all_words)

    if result.has_chapters:
        # Deli telo po pozicijama markera i izbroj reči u svakom segmentu
        positions = [m.start() for m in chapter_matches] + [len(body)]
        chapter_word_counts = []
        for start_pos, end_pos in zip(positions[:-1], positions[1:]):
            segment = body[start_pos:end_pos]
            chapter_word_counts.append(len(WORD_RE.findall(segment)))
        result.chapter_word_counts = chapter_word_counts

    # --- (5) Proza/poezija gruba heuristika -----------------------------------
    lines = [ln for ln in body.splitlines() if ln.strip()]
    if lines:
        words_per_line = [len(WORD_RE.findall(ln)) for ln in lines]
        result.avg_words_per_line = round(statistics.mean(words_per_line), 3)
        result.median_words_per_line = round(statistics.median(words_per_line), 3)
        short_lines = sum(1 for w in words_per_line if w < 6)
        result.short_line_ratio = round(short_lines / len(lines), 4)

        punctuation_count = sum(1 for ch in body if ch in ".,;:!?")
        result.punctuation_density = round(punctuation_count / max(len(body), 1), 5)

        # Gruba odluka: kratke linije ILI visok udeo kratkih linija je tipično
        # za stih (namerni prelomi reda), proza po pravilu ima duže,
        # nasumično prelomljene linije (osim ako je fajl bez wrap-a, pazi na to).
        #
        # VAŽNA NAPOMENA: pragovi ispod (8 reči / 0.35 udeo) su kalibrisani
        # na uzorku od SVEGA 3 knjige (Alice = proza, Snowflakes = poezija).
        # To je apsolutno nedovoljno da se pragovi smatraju pouzdanim.
        # Kad ovo pokreneš nad svih 8000 knjiga, obavezno pogledaj histogram
        # 'median_words_per_line' i 'short_line_ratio' po CELOM korpusu
        # (probe_raw_results.parquet) i po potrebi pomeri ove brojeve -
        # najbolje tako što ćeš ručno pregledati par desetina knjiga oko
        # granice i videti da li ih heuristika tačno pogađa.
        result.likely_verse = (
            result.median_words_per_line <= 8 or result.short_line_ratio >= 0.35
        )

    result.notes = "; ".join(notes)
    return result


def probe_corpus(input_dir: Path, limit: int | None = None) -> pd.DataFrame:
    """Prolazi kroz sve .txt fajlove u input_dir i vraća DataFrame sa
    jednim redom po knjizi. limit je koristan za brzo testiranje na uzorku
    pre pokretanja nad svih 8000 fajlova."""

    txt_files = sorted(input_dir.glob("*"))
    if limit:
        txt_files = txt_files[:limit]

    if not txt_files:
        raise FileNotFoundError(f"Nema .txt fajlova u {input_dir}")

    results = [probe_single_book(fp) for fp in txt_files]
    df = pd.DataFrame([asdict(r) for r in results])
    return df


# ---------------------------------------------------------------------------
# Rezime po 5 pitanja - agregacija DataFrame-a u čitljiv izveštaj
# ---------------------------------------------------------------------------

def summarize_corpus(df: pd.DataFrame) -> str:
    n = len(df)
    lines = []
    lines.append(f"UKUPNO ANALIZIRANO KNJIGA: {n}\n")

    # (1) START/END markeri
    lines.append("=" * 70)
    lines.append("1) START/END MARKER PROVERA")
    lines.append("=" * 70)
    strict_both = ((df["has_strict_start"]) & (df["has_strict_end"])).sum()
    lines.append(f"  Strogi START marker (THIS/THE PROJECT GUTENBERG EBOOK): {df['has_strict_start'].sum()} / {n} ({100*df['has_strict_start'].sum()/n:.1f}%)")
    lines.append(f"  Strogi END marker:                                     {df['has_strict_end'].sum()} / {n} ({100*df['has_strict_end'].sum()/n:.1f}%)")
    lines.append(f"  Oba stroga markera prisutna (čisto izdvajanje moguć):  {strict_both} / {n} ({100*strict_both/n:.1f}%)")
    lines.append(f"  Generički START pogodak ali NE strogi (druga varijanta teksta): {df['has_generic_start_only'].sum()}")
    lines.append(f"  Generički END pogodak ali NE strogi:                            {df['has_generic_end_only'].sum()}")
    lines.append(f"  NEMA NIKAKAV START marker (ni strogi ni generički):    {df['has_no_start_marker_at_all'].sum()}")
    lines.append(f"  NEMA NIKAKAV END marker:                               {df['has_no_end_marker_at_all'].sum()}")
    if df['has_no_start_marker_at_all'].sum() > 0:
        problem_files = df[df['has_no_start_marker_at_all']]['file_name'].tolist()
        lines.append(f"  >> Fajlovi bez ikakvog START markera (pregledaj ručno): {problem_files[:10]}{'...' if len(problem_files) > 10 else ''}")

    # (2) TOC
    lines.append("")
    lines.append("=" * 70)
    lines.append("2) SADRŽAJ (TABLE OF CONTENTS) NA POČETKU/KRAJU")
    lines.append("=" * 70)
    toc_count = df["has_toc_heading"].sum()
    lines.append(f"  Ima prepoznatljiv 'Contents' heading: {toc_count} / {n} ({100*toc_count/n:.1f}%)")
    if toc_count > 0:
        toc_positions = df[df["has_toc_heading"]]["toc_position_ratio"]
        near_start = (toc_positions <= 0.15).sum()
        near_end = (toc_positions >= 0.85).sum()
        lines.append(f"  Od toga, na početku knjige (pozicija <= 15%): {near_start}")
        lines.append(f"  Od toga, na kraju knjige (pozicija >= 85%):   {near_end}")
        lines.append(f"  Napomena: TOC odsustvo NE znači da knjiga nema poglavlja - vidi tačku 3.")

    # (3) Chapter pokrivenost
    lines.append("")
    lines.append("=" * 70)
    lines.append("3) CHAPTER POKRIVENOST")
    lines.append("=" * 70)
    has_ch = df["has_chapters"].sum()
    lines.append(f"  Knjige sa >= 2 CHAPTER markera (smatramo da imaju poglavlja): {has_ch} / {n} ({100*has_ch/n:.1f}%)")
    lines.append(f"  Knjige BEZ formalnih poglavlja (0-1 marker):                  {n - has_ch} / {n} ({100*(n-has_ch)/n:.1f}%)")
    lines.append(f"  >> Ovaj drugi broj ide u granu 'tretiraj celu knjigu kao jedno poglavlje'.")

    # (4) Distribucija dužine
    lines.append("")
    lines.append("=" * 70)
    lines.append("4) DISTRIBUCIJA DUŽINE POGLAVLJA / CELIH KNJIGA")
    lines.append("=" * 70)
    all_chapter_lengths = [wc for lst in df["chapter_word_counts"] for wc in lst]
    if all_chapter_lengths:
        lines.append(f"  Broj poglavlja ukupno (iz knjiga koje imaju poglavlja): {len(all_chapter_lengths)}")
        lines.append(f"  Dužina poglavlja (reči) - min/median/mean/max: "
                      f"{min(all_chapter_lengths)} / {statistics.median(all_chapter_lengths):.0f} / "
                      f"{statistics.mean(all_chapter_lengths):.0f} / {max(all_chapter_lengths)}")
        very_long = sum(1 for wc in all_chapter_lengths if wc > 15000)
        lines.append(f"  Poglavlja duža od 15000 reči (kandidati za fallback chunking u koraku 2): {very_long}")
    no_chapters_df = df[~df["has_chapters"]]
    if len(no_chapters_df) > 0:
        wc_no_ch = no_chapters_df["total_word_count"]
        lines.append(f"  Za knjige BEZ poglavlja, dužina cele knjige (reči) - min/median/mean/max: "
                      f"{wc_no_ch.min()} / {wc_no_ch.median():.0f} / {wc_no_ch.mean():.0f} / {wc_no_ch.max()}")

    # (5) Proza/poezija heuristika
    lines.append("")
    lines.append("=" * 70)
    lines.append("5) GRUBA PROZA/POEZIJA HEURISTIKA")
    lines.append("=" * 70)
    verse_count = df["likely_verse"].sum()
    lines.append(f"  Knjige koje heuristika označava kao 'verovatno stih/poezija': {verse_count} / {n} ({100*verse_count/n:.1f}%)")
    lines.append(f"  Prosečna medijana reči-po-liniji (ceo korpus): {df['median_words_per_line'].mean():.2f}")
    lines.append(f"  Prosečna gustina interpunkcije (ceo korpus): {df['punctuation_density'].mean():.5f}")
    lines.append(f"  NAPOMENA: ovo je gruba signalna heuristika, ne pouzdana klasifikacija.")
    lines.append(f"  Služi da se proceni KOLIKO knjiga verovatno nije standardna proza,")
    lines.append(f"  ne da se svaka knjiga tačno etiketira.")

    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description="Sonda za Gutenberg korpus")
    parser.add_argument("--input-dir", type=str, required=True, help="Direktorijum sa .txt fajlovima")
    parser.add_argument("--output-dir", type=str, default="./probe_results", help="Gde sačuvati rezultate")
    parser.add_argument("--limit", type=int, default=None, help="Ograniči broj fajlova (za brzo testiranje)")
    args = parser.parse_args()

    input_dir = Path(args.input_dir)
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    print(f"Analiziram fajlove iz: {input_dir}")
    df = probe_corpus(input_dir, limit=args.limit)

    # Sačuvaj sirovi DataFrame (bez liste-kolone chapter_word_counts jer parquet
    # to podržava, ali za CSV bi trebalo pretvoriti u string - ovde biramo parquet).
    parquet_path = output_dir / "probe_raw_results.parquet"
    df.to_parquet(parquet_path, index=False)
    print(f"Sirovi rezultati sačuvani: {parquet_path}")

    summary_text = summarize_corpus(df)
    summary_path = output_dir / "probe_summary.txt"
    summary_path.write_text(summary_text, encoding="utf-8")
    print(f"Rezime sačuvan: {summary_path}")
    print("\n" + summary_text)


if __name__ == "__main__":
    main()