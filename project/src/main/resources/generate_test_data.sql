-- =====================================================================
-- Skripta za generisanje test podataka radi analize plana izvrsavanja
-- upita nad tabelama knjiga / e_knjiga / citanje_eknjige
--
-- NAPOMENA: ne pokretati kroz data.sql (Spring ga izvrsava pri SVAKOM
-- pokretanju aplikacije, a ddl-auto=create-drop ionako brise sablon
-- pri restartu) - pokrenuti RUCNO kroz psql/DBeaver/pgAdmin nakon sto
-- je aplikacija vec jednom podigla semu (create-drop je napravio tabele
-- i data.sql je ubacio osnovne redove).
--
-- Pokretanje:
--   psql -h localhost -U admin -d iis_db -f generate_test_data.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. Sigurnosna provera - da li osnovni podaci iz data.sql uopste postoje
-- ---------------------------------------------------------------------
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM zanr) = 0 THEN
        RAISE EXCEPTION 'Tabela zanr je prazna - prvo pokreni aplikaciju da izvrsi data.sql';
    END IF;
    IF (SELECT COUNT(*) FROM knjiga) = 0 THEN
        RAISE EXCEPTION 'Tabela knjiga je prazna - prvo pokreni aplikaciju da izvrsi data.sql';
    END IF;
END $$;

-- ---------------------------------------------------------------------
-- 1. Dodatni zanrovi (da imamo raznovrsnije zanr_id vrednosti za WHERE)
--
--    data.sql ubacuje zanr_id 1..15 RUCNO (eksplicitne vrednosti), a ne
--    preko IDENTITY sekvence, pa sekvenca ostaje na 1. Bez setval-a bi
--    sledeci auto-generisani insert pokusao da ubaci zanr_id=1 i pukao
--    na PK konfliktu - zato prvo pomeramo sekvencu iza trenutnog MAX-a.
-- ---------------------------------------------------------------------
SELECT setval(
    pg_get_serial_sequence('zanr', 'zanr_id'),
    GREATEST((SELECT MAX(zanr_id) FROM zanr), 1)
);

INSERT INTO zanr (zanr_name)
SELECT 'Test zanr ' || g
FROM generate_series(1, 10) AS g
ON CONFLICT (zanr_name) DO NOTHING;

-- ---------------------------------------------------------------------
-- 2. Dodatni katalog (knjiga.katalog_id je NOT NULL)
--
--    ISPRAVKA: bib_id je FK ka biblioteka(bid), NE proizvoljan string -
--    koristimo postojecu biblioteku 'BIB001' iz data.sql. Kolona za ime
--    kataloga je "kat_ime", a PK kolona je "kat_id" (oboje potvrdjeno
--    direktno iz tvoje baze - information_schema.columns).
-- ---------------------------------------------------------------------
INSERT INTO katalog (kat_ime, standard, bib_id, deleted)
SELECT 'Test katalog za opterecenje', 'MARC', 'BIB001', FALSE
WHERE NOT EXISTS (SELECT 1 FROM katalog WHERE kat_ime = 'Test katalog za opterecenje');

-- ---------------------------------------------------------------------
-- 3. Generisanje 5.000 dodatnih knjiga sa nasumicnim zanrom
--    ISBN generisemo sa prefiksom '999' da se sigurno ne poklapaju
--    sa postojecim ISBN-ovima iz data.sql
-- ---------------------------------------------------------------------
INSERT INTO knjiga (isbn, naslov, putanja_naslovna, sinopsis, katalog_id, deleted, tip_knjige, autor, zanr_id)
SELECT
    '999' || lpad(g::text, 10, '0')                                   AS isbn,
    'Testna knjiga broj ' || g                                        AS naslov,
    NULL                                                               AS putanja_naslovna,
    'Automatski generisan test zapis za analizu performansi upita.'   AS sinopsis,
    (SELECT kat_id FROM katalog WHERE kat_ime = 'Test katalog za opterecenje') AS katalog_id,
    FALSE                                                              AS deleted,
    '010'                                                              AS tip_knjige, -- ima e-knjigu
    'Test Autor ' || (g % 200)                                         AS autor,
    (SELECT zanr_id FROM zanr ORDER BY random() LIMIT 1)                AS zanr_id
FROM generate_series(1, 5000) AS g
ON CONFLICT (isbn) DO NOTHING;

-- ---------------------------------------------------------------------
-- 4. Za svaku novu knjigu napravi odgovarajucu e_knjiga (1:1)
-- ---------------------------------------------------------------------
INSERT INTO e_knjiga (isbn, format_ek, datum_dodavanja_ek, broj_strana_ek, putanja_ek)
SELECT
    k.isbn,
    'EPUB',
    CURRENT_DATE - (random() * 700)::int,
    100 + (random() * 400)::int,
    NULL
FROM knjiga k
WHERE k.isbn LIKE '999%'
  AND NOT EXISTS (SELECT 1 FROM e_knjiga ek WHERE ek.isbn = k.isbn);

-- ---------------------------------------------------------------------
-- 5. Dodatni test korisnici (clanovi) - potrebni zbog FK jmbg_clana
--
--    Kolone su onako kako Postgres stvarno skladisti nazive iz @Column
--    (nekvotovani identifikatori se foldiraju u lowercase, bez obzira
--    sto anotacija/data.sql pise "imeK"/"przK"/"sifraK"): imek, przk,
--    sifrak, tipk, bid, brojt. "bid" je FK ka biblioteka(bid) - koristimo
--    postojecu biblioteku BIB001 iz data.sql da izbegnemo FK gresku.
--    "brojt" (broj telefona) nije NOT NULL, ali je dodajemo radi
--    kompletnosti i da izbegnemo iznenadjenja sa drugim constraint-ima.
-- ---------------------------------------------------------------------
INSERT INTO korisnik (jmbg, imek, przk, sifrak, email, tipk, bid, brojt, dat_rodj)
SELECT
    lpad((9000000000000::bigint + g)::text, 13, '0'),
    'TestIme' || g,
    'TestPrezime' || g,
    -- Placeholder hash - ovi korisnici sluze samo za FK, ne za login
    '$2b$12$JFMqeH4J/vrU8rYlH1h7n.Vs0evx4iNNrKzPAltuFhDzQH30.QcbS',
    'test.citalac.' || g || '@example.com',
    'CLAN',
    'BIB001',
    '06' || lpad((g % 100000000)::text, 8, '0'),
    CURRENT_DATE - (18*365) - (random()*15000)::int
FROM generate_series(1, 2000) AS g
ON CONFLICT (jmbg) DO NOTHING;

-- ---------------------------------------------------------------------
-- 6. Glavna tabela za opterecenje: citanje_eknjige (~80.000 redova)
--
--    datum_poslednjeg_pristupa_ck se generise RELATIVNO na CURRENT_DATE
--    (od -90 do 0 dana), tako da priblizno 1/3 redova upada u prozor
--    "poslednjih 30 dana" iz WHERE klauzule test upita - bez obzira
--    kada se skripta pokrene.
--
--    Napomena o performansama: umesto da za SVAKI od 80.000 redova radimo
--    "ORDER BY random() LIMIT 1" nad e_knjiga/korisnik (sto bi bilo
--    O(n * m) i moglo da traje jako dugo), prvo ucitamo sve test ISBN-ove
--    i JMBG-ove u niz jednom, pa biramo nasumican indeks iz niza po redu -
--    ovo je znatno brze (par sekundi umesto nekoliko minuta).
--
--    ISPRAVKA (23505 - duplicate key na PK (datum_pocetka_ck, isbn_eknjige,
--    jmbg_clana)): jmbg_clana, isbn_eknjige i datum_pocetka_ck se biraju
--    NEZAVISNO nasumicno, a datum_pocetka_ck ima svega ~181 mogucu vrednost
--    (CURRENT_DATE - 30..210 dana). Zbog "birthday problem" efekta, kod
--    80.000 nezavisnih izvlacenja trojki kolizija sa PK-jem postaje
--    prakticno izvesna mnogo pre nego sto se iscrpi prostor kombinacija.
--    Zato dodajemo ON CONFLICT DO NOTHING na sam PK - duplikat se tiho
--    preskace umesto da obori celu skriptu (finalan broj redova ce zbog
--    toga biti malo manji od 80.000, sto je ocekivano i bezopasno za
--    potrebe generisanja test/opterecujucih podataka).
-- ---------------------------------------------------------------------
WITH test_isbn AS MATERIALIZED (
    SELECT array_agg(isbn) AS isbns FROM e_knjiga WHERE isbn LIKE '999%'
),
test_jmbg AS MATERIALIZED (
    SELECT array_agg(jmbg) AS jmbgs FROM korisnik WHERE jmbg LIKE '9%' AND length(jmbg) = 13
)
INSERT INTO citanje_eknjige (
    jmbg_clana, isbn_eknjige, datum_pocetka_ck,
    trenutna_stranica_ck, datum_poslednjeg_pristupa_ck,
    datum_zavrsetka_ck, status_citanja_ck
)
SELECT
    jmbgs[1 + floor(random() * array_length(jmbgs, 1))::int]             AS jmbg_clana,
    isbns[1 + floor(random() * array_length(isbns, 1))::int]             AS isbn_eknjige,
    CURRENT_DATE - (random() * 180)::int - 30                           AS datum_pocetka_ck,
    (random() * 300)::int                                               AS trenutna_stranica_ck,
    CURRENT_DATE - (random() * 90)::int                                 AS datum_poslednjeg_pristupa_ck,
    CASE WHEN random() < 0.4 THEN CURRENT_DATE - (random() * 30)::int ELSE NULL END AS datum_zavrsetka_ck,
    (ARRAY['U_TOKU', 'ZAVRSENO', 'NAPUSTENO'])[1 + floor(random() * 3)] AS status_citanja_ck
FROM generate_series(1, 80000) AS g
CROSS JOIN test_isbn
CROSS JOIN test_jmbg
ON CONFLICT (jmbg_clana, isbn_eknjige, datum_pocetka_ck) DO NOTHING;

-- ---------------------------------------------------------------------
-- 7. Osvezi statistiku planera nad izmenjenim tabelama
--    (VAZNO: bez ovoga EXPLAIN moze da pokaze zastarele procene)
-- ---------------------------------------------------------------------
ANALYZE knjiga;
ANALYZE e_knjiga;
ANALYZE citanje_eknjige;
ANALYZE korisnik;
ANALYZE zanr;

-- ---------------------------------------------------------------------
-- 8. Kontrolni ispis - koliko redova sada ima svaka tabela
-- ---------------------------------------------------------------------
SELECT 'knjiga' AS tabela, COUNT(*) FROM knjiga
UNION ALL
SELECT 'e_knjiga', COUNT(*) FROM e_knjiga
UNION ALL
SELECT 'citanje_eknjige', COUNT(*) FROM citanje_eknjige
UNION ALL
SELECT 'korisnik', COUNT(*) FROM korisnik
UNION ALL
SELECT 'zanr', COUNT(*) FROM zanr;