package ftn.iis.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import ftn.iis.enums.*;
import ftn.iis.model.Kazna;
import ftn.iis.model.Knjiga;
import ftn.iis.model.Pozajmica;
import ftn.iis.repository.KaznaRepository;
import ftn.iis.repository.KnjigaRepository;
import ftn.iis.repository.PozajmicaRepository;
import ftn.iis.repository.PrimerakKnjigeRepository;
import ftn.iis.model.*;
import ftn.iis.repository.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class IzvestajService {
    private final KnjigaRepository        knjigaRepository;
    private final PrimerakKnjigeRepository primerakKnjigeRepository;
    private final KaznaRepository         kaznaRepository;
    private final PozajmicaRepository pozajmicaRepository;
    private final NarudzbinaRepository narudzbinaRepository;
    private final StavkaNarudzbineRepository stavkaNarudzbineRepository;
    private final ReklamacijaRepository reklamacijaRepository;
    private final BudzetPoZanruRepository budzetPoZanruRepository;
    private final PredlogNabavkaRepository predlogRepository;
    private final FizickaKnjigaRepository fizickaKnjigaRepository;
    private final SistemskePreporukeRepository sistemskePreporukeRepository;
    private final CetSesijaRepository cetSesijaRepository;
    private final CetPorukaRepository cetPorukaRepository;

    private static final Color BROWN_DARK   = new Color(94,  68,  54);
    private static final Color BROWN_MED    = new Color(122, 92,  72);
    private static final Color BROWN_LIGHT  = new Color(168, 144, 128);
    private static final Color BEIGE_LIGHT  = new Color(250, 248, 246);
    private static final Color BEIGE_MED    = new Color(240, 232, 224);
    private static final Color TEXT_DARK    = new Color(40,  35,  30);
    private static final Color TEXT_MID     = new Color(100, 90,  80);
    private static final Color WHITE        = Color.WHITE;
    private static final Color GREEN_LIGHT  = new Color(212, 221, 184);
    private static final Color GREEN_DARK   = new Color(74,  103,  65);
    private static final Color AMBER_DARK   = new Color(160, 100,   0);
    private static final Color GREEN_MED   = new Color(122, 144, 104);
    private static final Color CARD_BG     = new Color(200, 213, 170);
    private static final Color CARD_LIGHT  = new Color(234, 239, 220);
    private static final Color TEXT_DARK_GREEN   = new Color(30, 45, 20);
    private static final Color TEXT_MID_GREEN   = new Color(58, 78, 48);

    private static final String[] MESECI = {
            "", "Januar", "Februar", "Mart", "April", "Maj", "Jun",
            "Jul", "Avgust", "Septembar", "Oktobar", "Novembar", "Decembar"
    };

    public IzvestajService(PozajmicaRepository pozajmicaRepository,
                           KnjigaRepository knjigaRepository,
                           PrimerakKnjigeRepository primerakKnjigeRepository,
                           KaznaRepository kaznaRepository, NarudzbinaRepository narudzbinaRepository,
                           StavkaNarudzbineRepository stavkaNarudzbineRepository,
                           ReklamacijaRepository reklamacijaRepository,
                           BudzetPoZanruRepository budzetPoZanruRepository,
                           PredlogNabavkaRepository predlogRepository,
                           FizickaKnjigaRepository fizickaKnjigaRepository,
                           SistemskePreporukeRepository sistemskePreporukeRepository,
                           CetSesijaRepository cetSesijaRepository,
                           CetPorukaRepository cetPorukaRepository) {
        this.knjigaRepository         = knjigaRepository;
        this.primerakKnjigeRepository = primerakKnjigeRepository;
        this.kaznaRepository          = kaznaRepository;                           
        this.pozajmicaRepository = pozajmicaRepository;
        this.narudzbinaRepository = narudzbinaRepository;
        this.stavkaNarudzbineRepository = stavkaNarudzbineRepository;
        this.reklamacijaRepository = reklamacijaRepository;
        this.budzetPoZanruRepository = budzetPoZanruRepository;
        this.predlogRepository = predlogRepository;
        this.fizickaKnjigaRepository = fizickaKnjigaRepository;
        this.sistemskePreporukeRepository = sistemskePreporukeRepository;
        this.cetSesijaRepository = cetSesijaRepository;
        this.cetPorukaRepository = cetPorukaRepository;
    }





    // ===================================================================================
    //               TEODORA
    // ===================================================================================





    public byte[] generisiIzvestaj(LocalDate od, LocalDate datDo) throws DocumentException, IOException {
        java.util.List<Pozajmica> pozajmice = pozajmicaRepository.findAllInPeriodWithDetails(od, datDo);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 45, 45, 55, 45);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                try {
                    BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1250", BaseFont.NOT_EMBEDDED);
                    Font footer = new Font(bf, 8, Font.NORMAL, TEXT_MID_GREEN);
                    PdfContentByte cb = w.getDirectContent();
                    cb.saveState();
                    cb.setColorFill(CARD_BG);
                    cb.rectangle(d.left(), d.bottom() - 15, d.right() - d.left(), 1);
                    cb.fill();
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                            new Phrase("Strana " + w.getPageNumber(), footer),
                            (d.left() + d.right()) / 2, d.bottom() - 25, 0);
                    cb.restoreState();
                } catch (Exception ignored) {}
            }
        });

        doc.open();

        BaseFont bf     = BaseFont.createFont(BaseFont.HELVETICA,      "Cp1250", BaseFont.NOT_EMBEDDED);
        BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, "Cp1250", BaseFont.NOT_EMBEDDED);

        Font fTitle    = new Font(bfBold, 20, Font.BOLD,   WHITE);
        Font fSubtitle = new Font(bf,     10, Font.NORMAL, GREEN_LIGHT);
        Font fSection  = new Font(bfBold, 13, Font.BOLD,   GREEN_DARK);
        Font fTblHead  = new Font(bfBold,  9, Font.BOLD,   WHITE);
        Font fTblCell  = new Font(bf,      9, Font.NORMAL, TEXT_DARK_GREEN);
        Font fTblSmall = new Font(bf,      8, Font.NORMAL, TEXT_MID_GREEN);
        Font fMetVal   = new Font(bfBold, 15, Font.BOLD,   GREEN_MED);
        Font fMetLbl   = new Font(bf,      9, Font.NORMAL, TEXT_MID_GREEN);
        Font fNormal   = new Font(bf,      10, Font.NORMAL, TEXT_DARK_GREEN);



        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(20);

        PdfPCell hCell = new PdfPCell();
        hCell.setBackgroundColor(GREEN_DARK);
        hCell.setPadding(22);
        hCell.setBorder(Rectangle.NO_BORDER);

        Paragraph t1 = new Paragraph("Izvestaj o aktivnostima biblioteke", fTitle);
        t1.setAlignment(Element.ALIGN_CENTER);
        Paragraph t2 = new Paragraph(
                "Period: " + fmt(od) + "  \u2014  " + fmt(datDo) +
                        "    |    Generisano: " + fmt(LocalDate.now()), fSubtitle);
        t2.setAlignment(Element.ALIGN_CENTER);
        t2.setSpacingBefore(5);

        hCell.addElement(t1);
        hCell.addElement(t2);
        header.addCell(hCell);
        doc.add(header);


        int total = pozajmice.size();
        LocalDate danas = LocalDate.now();

        // Aktivni clanovi
        long aktivnihClanova = pozajmice.stream()
                .map(p -> p.getClan().getJmbg()).distinct().count();

        // Prosecno trajanje (samo vracene)
        OptionalDouble prosecnoTrajanje = pozajmice.stream()
                .filter(p -> p.getDatVrac() != null)
                .mapToLong(p -> ChronoUnit.DAYS.between(p.getDatPoz(), p.getDatVrac()))
                .average();

        // Stopa prekoracenja
        long prekoraciloRok = pozajmice.stream()
                .filter(p -> p.getDatVrac() != null && p.getDatVrac().isAfter(p.getDatOcVrac()))
                .count();
        long josUPrekoracenju = pozajmice.stream()
                .filter(p -> p.getDatVrac() == null && danas.isAfter(p.getDatOcVrac()))
                .count();
        long ukupnoPrekoracilo = prekoraciloRok + josUPrekoracenju;

        // Top clanovi
        Map<String, Long> countByJmbg = pozajmice.stream()
                .collect(Collectors.groupingBy(p -> p.getClan().getJmbg(), Collectors.counting()));
        List<Map.Entry<String, Long>> topClanovi = countByJmbg.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5).collect(Collectors.toList());
        Map<String, String> jmbgToName = pozajmice.stream()
                .collect(Collectors.toMap(p -> p.getClan().getJmbg(),
                        p -> p.getClan().getFirstName() + " " + p.getClan().getLastName(),
                        (a, b) -> a));

        // Po kategoriji clana
        Map<String, Long> poKategoriji = pozajmice.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getClan().getKategorijaClana() != null
                                ? p.getClan().getKategorijaClana().getTipKC().name()
                                : "Bez kategorije",
                        Collectors.counting()));

        // Top knjige (by isbn)
        Map<String, Long> countByIsbn = pozajmice.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn(), Collectors.counting()));
        Map<String, Knjiga> isbnToKnjiga = pozajmice.stream()
                .collect(Collectors.toMap(
                        p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn(),
                        p -> p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga(),
                        (a, b) -> a));
        List<Map.Entry<String, Long>> topKnjige = countByIsbn.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10).collect(Collectors.toList());

        // Po zanru
        Map<String, Long> poZanru = pozajmice.stream()
                .collect(Collectors.groupingBy(
                        p -> {
                            var zanr = p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getZanr();
                            return zanr != null ? zanr.getName() : "Bez zanra";
                        }, Collectors.counting()));

        // Mesecni trend
        Map<YearMonth, Long> mesecniTrend = new TreeMap<>(pozajmice.stream()
                .collect(Collectors.groupingBy(
                        p -> YearMonth.of(p.getDatPoz().getYear(), p.getDatPoz().getMonthValue()),
                        Collectors.counting())));

        doc.add(sectionHeader("1. Aktivnost clanova", fSection, bfBold));

        // Karticice sa sumama
        PdfPTable metrics1 = new PdfPTable(3);
        metrics1.setWidthPercentage(100);
        metrics1.setSpacingBefore(10);
        metrics1.setSpacingAfter(16);
        metrics1.setWidths(new float[]{1, 1, 1});

        metrics1.addCell(metricCell("" + total,          "Ukupno pozajmica",      fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell("" + aktivnihClanova,"Aktivnih clanova",       fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell(
                prosecnoTrajanje.isPresent()
                        ? String.format("%.1f dan", prosecnoTrajanje.getAsDouble()) : "—",
                "Prosecno trajanje poz.", fMetVal, fMetLbl, bfBold));
        doc.add(metrics1);

        // Top 5 clanova
        doc.add(subheading("Top 5 najaktivnijih clanova", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblClanovi = new PdfPTable(3);
        tblClanovi.setWidthPercentage(100);
        tblClanovi.setSpacingBefore(6);
        tblClanovi.setSpacingAfter(14);
        tblClanovi.setWidths(new float[]{0.5f, 3f, 1.5f});

        addTableHeader(tblClanovi, fTblHead, GREEN_DARK, "#", "Ime i prezime", "Br. pozajmica");
        int rank = 1;
        for (Map.Entry<String, Long> e : topClanovi) {
            Color bg = (rank % 2 == 0) ? CARD_LIGHT : WHITE;
            addRow(tblClanovi, fTblCell, bg, Element.ALIGN_CENTER, "" + rank,
                    jmbgToName.getOrDefault(e.getKey(), e.getKey()),
                    "" + e.getValue());
            rank++;
        }
        if (topClanovi.isEmpty()) addEmptyRow(tblClanovi, fTblSmall, 3, "Nema podataka za izabrani period");
        doc.add(tblClanovi);

        // Po kategoriji
        doc.add(subheading("Pozajmice po kategoriji clana", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblKat = new PdfPTable(3);
        tblKat.setWidthPercentage(70);
        tblKat.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblKat.setSpacingBefore(6);
        tblKat.setSpacingAfter(20);
        tblKat.setWidths(new float[]{3f, 1.5f, 1f});

        addTableHeader(tblKat, fTblHead, GREEN_DARK, "Kategorija", "Br. pozajmica", "%");
        int rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(poKategoriji)) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String pct = total > 0 ? String.format("%.1f%%", 100.0 * e.getValue() / total) : "—";
            addRow(tblKat, fTblCell, bg, Element.ALIGN_LEFT, labelKategorije(e.getKey()), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (poKategoriji.isEmpty()) addEmptyRow(tblKat, fTblSmall, 3, "Nema podataka");
        doc.add(tblKat);


        doc.add(sectionHeader("2. Popularnost naslova", fSection, bfBold));

        doc.add(subheading("Top 10 najpozajmljivanijih knjiga", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblKnjige = new PdfPTable(5);
        tblKnjige.setWidthPercentage(100);
        tblKnjige.setSpacingBefore(6);
        tblKnjige.setSpacingAfter(14);
        tblKnjige.setWidths(new float[]{0.5f, 3.5f, 2f, 1.5f, 1f});

        addTableHeader(tblKnjige, fTblHead, GREEN_DARK, "#", "Naslov", "Autor", "Zanr", "Br.");
        int rk = 1;
        for (Map.Entry<String, Long> e : topKnjige) {
            Color bg = (rk % 2 == 0) ? CARD_LIGHT : WHITE;
            Knjiga k = isbnToKnjiga.get(e.getKey());
            String naslov = truncate(k.getNaslov(), 40);
            String autor  = truncate(k.getAutor() != null ? k.getAutor() : "—", 25);
            String zanr   = k.getZanr() != null ? k.getZanr().getName() : "—";
            addRow(tblKnjige, fTblCell, bg, Element.ALIGN_CENTER, "" + rk, naslov, autor, zanr, "" + e.getValue());
            rk++;
        }
        if (topKnjige.isEmpty()) addEmptyRow(tblKnjige, fTblSmall, 5, "Nema podataka za izabrani period");
        doc.add(tblKnjige);

        // Po zanru
        doc.add(subheading("Pozajmice po zanru", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblZanr = new PdfPTable(3);
        tblZanr.setWidthPercentage(65);
        tblZanr.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblZanr.setSpacingBefore(6);
        tblZanr.setSpacingAfter(20);
        tblZanr.setWidths(new float[]{3f, 1.5f, 1f});

        addTableHeader(tblZanr, fTblHead, GREEN_DARK, "Zanr", "Br. pozajmica", "%");
        rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(poZanru)) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String pct = total > 0 ? String.format("%.1f%%", 100.0 * e.getValue() / total) : "—";
            addRow(tblZanr, fTblCell, bg, Element.ALIGN_LEFT, e.getKey(), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (poZanru.isEmpty()) addEmptyRow(tblZanr, fTblSmall, 3, "Nema podataka");
        doc.add(tblZanr);


        doc.add(sectionHeader("3. Trendovi citanja", fSection, bfBold));

        // Sumarni metrik — prekoracenja
        PdfPTable metrics3 = new PdfPTable(2);
        metrics3.setWidthPercentage(60);
        metrics3.setHorizontalAlignment(Element.ALIGN_LEFT);
        metrics3.setSpacingBefore(10);
        metrics3.setSpacingAfter(16);

        String stopaStr = total > 0
                ? String.format("%.1f%%", 100.0 * ukupnoPrekoracilo / total)
                : "—";
        metrics3.addCell(metricCell(stopaStr, "Stopa prekoracenja roka", fMetVal, fMetLbl, bfBold));
        metrics3.addCell(metricCell("" + ukupnoPrekoracilo, "Ukupno prekoraceno", fMetVal, fMetLbl, bfBold));
        doc.add(metrics3);

        // Mesecni pregled
        doc.add(subheading("Mesecni pregled pozajmica", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblMes = new PdfPTable(3);
        tblMes.setWidthPercentage(65);
        tblMes.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblMes.setSpacingBefore(6);
        tblMes.setSpacingAfter(14);
        tblMes.setWidths(new float[]{2.5f, 1.5f, 1.5f});

        addTableHeader(tblMes, fTblHead, GREEN_DARK, "Mesec", "Br. pozajmica", "Promena");
        rowIdx = 1;
        Long prevCount = null;
        for (Map.Entry<YearMonth, Long> e : mesecniTrend.entrySet()) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String label = MESECI[e.getKey().getMonthValue()] + " " + e.getKey().getYear();
            String promena;
            if (prevCount == null) {
                promena = "—";
            } else {
                long diff = e.getValue() - prevCount;
                promena = (diff >= 0 ? "+" : "") + diff;
            }
            addRow(tblMes, fTblCell, bg, Element.ALIGN_LEFT, label, "" + e.getValue(), promena);
            prevCount = e.getValue();
            rowIdx++;
        }
        if (mesecniTrend.isEmpty()) addEmptyRow(tblMes, fTblSmall, 3, "Nema podataka za izabrani period");
        doc.add(tblMes);


        doc.close();
        return baos.toByteArray();
    }







    // ===================================================================================
    //               MARIJA
    // ===================================================================================






    public byte[] generisiIzvestajNabavka(LocalDate od, LocalDate datDo) throws DocumentException, IOException {

        // Ucitavamm podatke
        List<Narudzbina> narudzbine = narudzbinaRepository.findAllByDatumKreiranjaBetween(od, datDo);
        List<BudzetPoZanru> budzeti = budzetPoZanruRepository.findAll();
        List<PredlogZaNabavku> predlozi = predlogRepository.findAllByDatumPodnosenjaBetween(od, datDo);
        List<SistemskaPreporuka> preporuke = sistemskePreporukeRepository.findAll();
        List<FizickaKnjiga> sveFizicke = fizickaKnjigaRepository.findAll();

        // Generisanje dokumenta, velicine A4, sa marginama 45, 45, 55, 45
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 45, 45, 55, 45);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);


        // Footer - koristim Teodorin
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                try {
                    BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1250", BaseFont.NOT_EMBEDDED);
                    Font footer = new Font(bf, 8, Font.NORMAL, TEXT_MID_GREEN);
                    PdfContentByte cb = w.getDirectContent();
                    cb.saveState();
                    cb.setColorFill(CARD_BG);
                    cb.rectangle(d.left(), d.bottom() - 15, d.right() - d.left(), 1);
                    cb.fill();
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                            new Phrase("Strana " + w.getPageNumber(), footer),
                            (d.left() + d.right()) / 2, d.bottom() - 25, 0);
                    cb.restoreState();
                } catch (Exception ignored) {}
            }
        });


        doc.open();

        // Samo fontovi, svi koristimo iste
        BaseFont bf     = BaseFont.createFont(BaseFont.HELVETICA,      "Cp1250", BaseFont.NOT_EMBEDDED);
        BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, "Cp1250", BaseFont.NOT_EMBEDDED);

        Font fTitle    = new Font(bfBold, 20, Font.BOLD,   WHITE);
        Font fSubtitle = new Font(bf,     10, Font.NORMAL, GREEN_LIGHT);
        Font fSection  = new Font(bfBold, 13, Font.BOLD,   GREEN_DARK);
        Font fTblHead  = new Font(bfBold,  9, Font.BOLD,   WHITE);
        Font fTblCell  = new Font(bf,      9, Font.NORMAL, TEXT_DARK_GREEN);
        Font fTblSmall = new Font(bf,      8, Font.NORMAL, TEXT_MID_GREEN);
        Font fMetVal   = new Font(bfBold, 15, Font.BOLD,   GREEN_MED);
        Font fMetLbl   = new Font(bf,      9, Font.NORMAL, TEXT_MID_GREEN);


        // Header
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(20);
        PdfPCell hCell = new PdfPCell();
        hCell.setBackgroundColor(GREEN_DARK);
        hCell.setPadding(22);
        hCell.setBorder(Rectangle.NO_BORDER);
        Paragraph t1 = new Paragraph("Izvestaj o nabavci, fondovima biblioteke i zadovoljenju korisnika", fTitle);
        t1.setAlignment(Element.ALIGN_CENTER);
        Paragraph t2 = new Paragraph(
                "Period: " + fmt(od) + "  \u2014  " + fmt(datDo) +
                        "    |    Generisano: " + fmt(LocalDate.now()), fSubtitle);
        t2.setAlignment(Element.ALIGN_CENTER);
        t2.setSpacingBefore(5);
        hCell.addElement(t1);
        hCell.addElement(t2);
        header.addCell(hCell);
        doc.add(header);


        // ================================================
        // Sekcija 1 - Troskovi nabavke
        // ===============================================

        doc.add(sectionHeader("1. Troskovi nabavke", fSection, bfBold));

        // Metrike

        double ukupnoPotrošeno = 0.0;
        double ukupnoKreirano = 0.0;
        double sumaSvihCena = 0.0;
        long brojReklamacija = 0;


        // Mapa za grupisanje po statusu (inicijalizujem sve statuse na 0 radi sigurnosti)
        Map<StatusNarudzbine, Long> poStatusu = new HashMap<>();
        for (StatusNarudzbine status : StatusNarudzbine.values()) {
            poStatusu.put(status, 0L);
        }

        for (Narudzbina n : narudzbine) {

            StatusNarudzbine status = n.getStatus();
            double cena = n.getUkupnaCena();

            sumaSvihCena += cena;

            // Azuriranje brojaca po statusu
            poStatusu.put(status, poStatusu.get(status) + 1);

            // Kategorizacija po statusima
            if (status == StatusNarudzbine.ISPORUCENA || status == StatusNarudzbine.REKLAMIRANA || status == StatusNarudzbine.POTVRDJENA) {
                ukupnoPotrošeno += cena;
            }

            if (status == StatusNarudzbine.KREIRANA) {
                ukupnoKreirano += cena;
            }

            if (status == StatusNarudzbine.REKLAMIRANA) {
                brojReklamacija++;
            }
        }

        // 3. Izracunavanje izvedenih vrednosti nakon petlje
        double stopaReklamacija = narudzbine.isEmpty() ? 0.0 : (100.0 * brojReklamacija / narudzbine.size());
        double prosecnaVrednost = narudzbine.isEmpty() ? 0.0 : (sumaSvihCena / narudzbine.size());

        // 4. Kreiranje PDF tabele
        PdfPTable met1 = new PdfPTable(3);
        met1.setWidthPercentage(100);
        met1.setSpacingBefore(10);
        met1.setSpacingAfter(16);

        met1.addCell(metricCell(String.format("%.2f RSD", ukupnoPotrošeno), "Ukupno potroseno", fMetVal, fMetLbl, bfBold));
        met1.addCell(metricCell(String.format("%.2f RSD", ukupnoKreirano), "U toku (kreirane)", fMetVal, fMetLbl, bfBold));
        met1.addCell(metricCell(
                !narudzbine.isEmpty() ? String.format("%.2f RSD", prosecnaVrednost) : "—",
                "Prosecna vrednost narudzbine", fMetVal, fMetLbl, bfBold));

        doc.add(met1);

        // Dodavanje podnaslova za grupisanje
        doc.add(subheading("Narudzbine po statusu", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));

        PdfPTable tblStatus = new PdfPTable(3);
        tblStatus.setWidthPercentage(65);
        tblStatus.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblStatus.setSpacingBefore(6);
        tblStatus.setSpacingAfter(14);
        tblStatus.setWidths(new float[]{3f, 1.5f, 1f});
        addTableHeader(tblStatus, fTblHead, GREEN_DARK, "Status", "Broj", "%");
        int rowIdx = 1;
        for (StatusNarudzbine status : StatusNarudzbine.values()) {
            long count = poStatusu.getOrDefault(status, 0L);
            String pct = narudzbine.isEmpty() ? "—" :
                    String.format("%.1f%%", 100.0 * count / narudzbine.size());
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            addRow(tblStatus, fTblCell, bg, Element.ALIGN_LEFT,
                    labelStatusaNarudzbine(status.name()), "" + count, pct);
            rowIdx++;
        }
        if (narudzbine.isEmpty()) addEmptyRow(tblStatus, fTblSmall, 3, "Nema narudzbina u periodu");
        doc.add(tblStatus);


        // Dobavljaci
        doc.add(subheading("Top 5 dobavljaca po vrednosti narudzbina", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));

        // Grupisanje vrednosti po dobavljaču
        Map<String, Double> poDobavljacu = new HashMap<>();
        for (Narudzbina n : narudzbine) {
            String nazivDobavljaca = n.getDobavljac().getNaziv();
            double trenutnaSuma = poDobavljacu.getOrDefault(nazivDobavljaca, 0.0);
            poDobavljacu.put(nazivDobavljaca, trenutnaSuma + n.getUkupnaCena());
        }

        // Sortiranje mape i uzimanje top 5 elemenata
        List<Map.Entry<String, Double>> topDobavljaci = new ArrayList<>(poDobavljacu.entrySet());
        topDobavljaci.sort(new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
                return e2.getValue().compareTo(e1.getValue()); // Opadajući poredak
            }
        });
        if (topDobavljaci.size() > 5) {
            topDobavljaci = topDobavljaci.subList(0, 5);
        }

        PdfPTable tblDob = new PdfPTable(3);
        tblDob.setWidthPercentage(100);
        tblDob.setSpacingBefore(6);
        tblDob.setSpacingAfter(14);
        tblDob.setWidths(new float[]{0.5f, 3.5f, 2f});
        addTableHeader(tblDob, fTblHead, GREEN_DARK, "#", "Dobavljac", "Ukupna vrednost");

        int rnk = 1;
        for (Map.Entry<String, Double> e : topDobavljaci) {
            Color bg = (rnk % 2 == 0) ? CARD_LIGHT : WHITE;
            addRow(tblDob, fTblCell, bg, Element.ALIGN_CENTER,
                    "" + rnk, e.getKey(), String.format("%.2f RSD", e.getValue()));
            rnk++;
        }
        if (topDobavljaci.isEmpty()) addEmptyRow(tblDob, fTblSmall, 3, "Nema podataka");
        doc.add(tblDob);

        // Stopa reklamacija
        doc.add(subheading("Stopa reklamacija", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable met1b = new PdfPTable(2);
        met1b.setWidthPercentage(60);
        met1b.setHorizontalAlignment(Element.ALIGN_LEFT);
        met1b.setSpacingBefore(10);
        met1b.setSpacingAfter(16);
        met1b.addCell(metricCell("" + brojReklamacija, "Narudzbina sa reklamacijom", fMetVal, fMetLbl, bfBold));
        met1b.addCell(metricCell(String.format("%.1f%%", stopaReklamacija), "Stopa reklamacija", fMetVal, fMetLbl, bfBold));
        doc.add(met1b);


        // ================================================
        // Sekcija 2 - Popunjenost fondova
        // ===============================================


        doc.add(sectionHeader("2. Popunjenost fondova", fSection, bfBold));

        // Primerci po žanru i ukupno primeraka
        Map<String, Long> primerciPoZanru = new HashMap<>();
        long ukupnoPrimeraka = 0;

        for (FizickaKnjiga fk : sveFizicke) {
            String zanr = "Bez zanra";
            if (fk.getKnjiga().getZanr() != null) {
                zanr = fk.getKnjiga().getZanr().getName();
            }

            long brojPrimeraka = 0;
            if (fk.getPrimerci() != null) {
                brojPrimeraka = fk.getPrimerci().size();
            }

            primerciPoZanru.put(zanr, primerciPoZanru.getOrDefault(zanr, 0L) + brojPrimeraka);
            ukupnoPrimeraka += brojPrimeraka;
        }

        PdfPTable met2 = new PdfPTable(2);
        met2.setWidthPercentage(60);
        met2.setHorizontalAlignment(Element.ALIGN_LEFT);
        met2.setSpacingBefore(10);
        met2.setSpacingAfter(16);
        met2.addCell(metricCell("" + sveFizicke.size(), "Naslova u fondu", fMetVal, fMetLbl, bfBold));
        met2.addCell(metricCell("" + ukupnoPrimeraka, "Ukupno primeraka", fMetVal, fMetLbl, bfBold));
        doc.add(met2);

        doc.add(subheading("Primerci po zanru", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblFond = new PdfPTable(3);
        tblFond.setWidthPercentage(70);
        tblFond.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblFond.setSpacingBefore(6);
        tblFond.setSpacingAfter(14);
        tblFond.setWidths(new float[]{3f, 1.5f, 1f});
        addTableHeader(tblFond, fTblHead, GREEN_DARK, "Zanr", "Primeraka", "%");

        rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(primerciPoZanru)) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String pct = ukupnoPrimeraka > 0 ?
                    String.format("%.1f%%", 100.0 * e.getValue() / ukupnoPrimeraka) : "—";
            addRow(tblFond, fTblCell, bg, Element.ALIGN_LEFT, e.getKey(), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (primerciPoZanru.isEmpty()) addEmptyRow(tblFond, fTblSmall, 3, "Nema podataka");
        doc.add(tblFond);


        // Knjige sa malo primeraka (filtriranje i sortiranje ručno)
        doc.add(subheading("Knjige sa malo primeraka (manje od 2)", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));

        List<FizickaKnjiga> maloPrimeraka = new ArrayList<>();
        for (FizickaKnjiga fk : sveFizicke) {
            if (fk.getPrimerci() == null || fk.getPrimerci().size() < 2) {
                maloPrimeraka.add(fk);
            }
        }

        maloPrimeraka.sort(new Comparator<FizickaKnjiga>() {
            @Override
            public int compare(FizickaKnjiga fk1, FizickaKnjiga fk2) {
                int p1 = fk1.getPrimerci() == null ? 0 : fk1.getPrimerci().size();
                int p2 = fk2.getPrimerci() == null ? 0 : fk2.getPrimerci().size();
                return Integer.compare(p1, p2);
            }
        });

        if (maloPrimeraka.size() > 10) {
            maloPrimeraka = maloPrimeraka.subList(0, 10);
        }

        PdfPTable tblMalo = new PdfPTable(4);
        tblMalo.setWidthPercentage(100);
        tblMalo.setSpacingBefore(6);
        tblMalo.setSpacingAfter(20);
        tblMalo.setWidths(new float[]{3.5f, 2f, 2f, 1f});
        addTableHeader(tblMalo, fTblHead, GREEN_DARK, "Naslov", "Autor", "Zanr", "Primeraka");

        rowIdx = 1;
        for (FizickaKnjiga fk : maloPrimeraka) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            Knjiga k = fk.getKnjiga();
            addRow(tblMalo, fTblCell, bg, Element.ALIGN_LEFT,
                    truncate(k.getNaslov(), 35),
                    truncate(k.getAutor() != null ? k.getAutor() : "—", 20),
                    k.getZanr() != null ? k.getZanr().getName() : "—",
                    "" + (fk.getPrimerci() != null ? fk.getPrimerci().size() : 0));
            rowIdx++;
        }
        if (maloPrimeraka.isEmpty()) addEmptyRow(tblMalo, fTblSmall, 4, "Sve knjige imaju dovoljno primeraka");
        doc.add(tblMalo);

        // Budzet po zanrovima
        doc.add(subheading("Stanje budzeta po zanrovima", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));
        PdfPTable tblBudzet = new PdfPTable(5);
        tblBudzet.setWidthPercentage(100);
        tblBudzet.setSpacingBefore(6);
        tblBudzet.setSpacingAfter(20);
        tblBudzet.setWidths(new float[]{2.5f, 2f, 2f, 2f, 1.5f});
        addTableHeader(tblBudzet, fTblHead, GREEN_DARK, "Zanr", "Ukupno", "Potroseno", "Dostupno", "Iskorisc.");

        rowIdx = 1;
        for (BudzetPoZanru b : budzeti) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            double iskorisc = b.getUkupanBudzet() > 0 ? 100.0 * b.getPotroseno() / b.getUkupanBudzet() : 0;
            addRow(tblBudzet, fTblCell, bg, Element.ALIGN_LEFT,
                    b.getZanr().getName(),
                    String.format("%.2f", b.getUkupanBudzet()),
                    String.format("%.2f", b.getPotroseno()),
                    String.format("%.2f", b.getDostupno()),
                    String.format("%.1f%%", iskorisc));
            rowIdx++;
        }
        if (budzeti.isEmpty()) addEmptyRow(tblBudzet, fTblSmall, 5, "Nema definisanog budzeta");
        doc.add(tblBudzet);

        // ==============================================
        //     Sekcija 3 - Zadovoljenje potreba korisnika
        // ==============================================


        doc.add(sectionHeader("3. Zadovoljenje potreba korisnika", fSection, bfBold));

        // Logika -> odbijeni predlozi su oni koji su ili odmah odbijeni od strane bibliotekara ili kasnije odbijeni od strane menadzera
        // Odobreni su samo oni koji su odobreni od strane menadzera, a na cekanju su oni koji su ili odobreni od strane bibliotekara ili jos uvek nisu obradjeni

        long ukupnoPredloga = predlozi.size();
        long odobrenoMenadzer = 0;
        long odbijenoBibliotekar = 0;
        long odbijenoMenadzer = 0;
        long naCekanju = 0;

        Map<String, Long> zanroviIzPredloga = new HashMap<>();

        // Jedan prolaz kroz sve predloge za računanje svih statusa i grupisanje žanrova
        for (PredlogZaNabavku p : predlozi) {
            StatusPredloga status = p.getStatus();

            if (status == StatusPredloga.ODOBRENO_MENADZER) {
                odobrenoMenadzer++;
            } else if (status == StatusPredloga.ODBIJENO_BIBLIOTEKAR) {
                odbijenoBibliotekar++;
            } else if (status == StatusPredloga.ODBIJENO_MENADZER) {
                odbijenoMenadzer++;
            } else if (status == StatusPredloga.NA_CEKANJU || status == StatusPredloga.ODOBRENO_BIBLIOTEKAR) {
                naCekanju++;
            }

            if (p.getZanr() != null) {
                String nazivZanra = p.getZanr().getName();
                zanroviIzPredloga.put(nazivZanra, zanroviIzPredloga.getOrDefault(nazivZanra, 0L) + 1);
            }
        }

        long odbijeno = odbijenoMenadzer + odbijenoBibliotekar;

        PdfPTable met3 = new PdfPTable(4);
        met3.setWidthPercentage(100);
        met3.setSpacingBefore(10);
        met3.setSpacingAfter(16);
        met3.addCell(metricCell("" + ukupnoPredloga, "Ukupno predloga", fMetVal, fMetLbl, bfBold));
        met3.addCell(metricCell("" + odobrenoMenadzer, "Odobreno", fMetVal, fMetLbl, bfBold));
        met3.addCell(metricCell("" + odbijeno, "Odbijeno", fMetVal, fMetLbl, bfBold));
        met3.addCell(metricCell("" + naCekanju, "Na cekanju", fMetVal, fMetLbl, bfBold));
        doc.add(met3);


        // Top zanrovi iz predloga
        doc.add(subheading("Najpopularniji zanrovi iz predloga korisnika", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));

        PdfPTable tblZanrPred = new PdfPTable(3);
        tblZanrPred.setWidthPercentage(65);
        tblZanrPred.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblZanrPred.setSpacingBefore(6);
        tblZanrPred.setSpacingAfter(14);
        tblZanrPred.setWidths(new float[]{3f, 1.5f, 1f});
        addTableHeader(tblZanrPred, fTblHead, GREEN_DARK, "Zanr", "Br. predloga", "%");

        rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(zanroviIzPredloga)) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String pct = ukupnoPredloga > 0 ? String.format("%.1f%%", 100.0 * e.getValue() / ukupnoPredloga) : "—";
            addRow(tblZanrPred, fTblCell, bg, Element.ALIGN_LEFT, e.getKey(), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (zanroviIzPredloga.isEmpty()) addEmptyRow(tblZanrPred, fTblSmall, 3, "Nema predloga sa definisanim zanrom");
        doc.add(tblZanrPred);


        // Sistemske preporuke — računanje brojaca kroz jednu petlju
        doc.add(subheading("Sistemske preporuke — obrada", new Font(bfBold, 10, Font.BOLD, TEXT_MID_GREEN)));

        long prihvacenoPreporuke = 0;
        long ignorisanoPreporuke = 0;
        long aktivnePreporuke = 0;

        for (SistemskaPreporuka p : preporuke) {
            if (p.getStatus() == StatusSistemskePreporuke.PRIHVACENO) {
                prihvacenoPreporuke++;
            } else if (p.getStatus() == StatusSistemskePreporuke.IGNORISANO) {
                ignorisanoPreporuke++;
            } else if (p.getStatus() == StatusSistemskePreporuke.AKTIVNA) {
                aktivnePreporuke++;
            }
        }

        PdfPTable met3b = new PdfPTable(3);
        met3b.setWidthPercentage(80);
        met3b.setHorizontalAlignment(Element.ALIGN_LEFT);
        met3b.setSpacingBefore(10);
        met3b.setSpacingAfter(20);
        met3b.addCell(metricCell("" + prihvacenoPreporuke, "Prihvaceno", fMetVal, fMetLbl, bfBold));
        met3b.addCell(metricCell("" + ignorisanoPreporuke, "Ignorisano", fMetVal, fMetLbl, bfBold));
        met3b.addCell(metricCell("" + aktivnePreporuke, "Aktivnih (neobradenih)", fMetVal, fMetLbl, bfBold));
        doc.add(met3b);

        doc.close();
        return baos.toByteArray();

    }

    private String labelStatusaNarudzbine(String status) {
        return switch (status) {
            case "KREIRANA"    -> "Kreirana";
            case "ISPORUCENA"  -> "Isporucena";
            case "REKLAMIRANA" -> "Reklamirana";
            case "POTVRDJENA"    -> "Potvrdjena";
            default -> status;
        };
    }









    // ===================================================================================
    //               SRDJAN
    // ===================================================================================




    public byte[] generisiIzvestajFonda(LocalDate od, LocalDate datDo) throws DocumentException, IOException {

        // ── DATA FETCHING ────────────────────────────────────────────────
        List<Knjiga>    sveKnjige          = knjigaRepository.findAllActiveWithZanrAndFizicka();
        long            ukupnoPrimeraka    = primerakKnjigeRepository.countAllPrimerci();
        long            dostupnoPrimeraka  = primerakKnjigeRepository.countAvailablePrimerci();
        List<Pozajmica> pozajmiceUPeriodu  = pozajmicaRepository.findAllInPeriodWithDetails(od, datDo);
        List<String>    sveIkadPozajmljene = pozajmicaRepository.findAllEverBorrowedIsbns();
        List<Pozajmica> uPrekoracenjuSad   = pozajmicaRepository.findAllCurrentlyOverdue(LocalDate.now());
        List<Kazna>     izgubljenaPozajmica = kaznaRepository.findAllLostBookKazne();

        // ── SECTION 1 AGGREGATIONS ───────────────────────────────────────
        long ukupnoNaslova = sveKnjige.size();
        long nFizicke = sveKnjige.stream().filter(k -> tip(k, 0)).count();
        long nEKnjige = sveKnjige.stream().filter(k -> tip(k, 1)).count();
        long nAudio   = sveKnjige.stream().filter(k -> tip(k, 2)).count();

        Map<String, Long> poZanruNaslovi = sveKnjige.stream()
            .collect(Collectors.groupingBy(
                k -> k.getZanr() != null ? k.getZanr().getName() : "Bez zanra",
                Collectors.counting()));

        // ── SECTION 2 AGGREGATIONS ───────────────────────────────────────
        Set<String> pozajmljeneIsbnUPeriodu = pozajmiceUPeriodu.stream()
            .map(p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn())
            .collect(Collectors.toSet());

        Map<String, Long> cirkulacija = pozajmiceUPeriodu.stream()
            .collect(Collectors.groupingBy(
                p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn(),
                Collectors.counting()));

        Map<String, Knjiga> isbnToKnjigaCirc = pozajmiceUPeriodu.stream()
            .collect(Collectors.toMap(
                p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn(),
                p -> p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga(),
                (a, b) -> a));

        List<Map.Entry<String, Long>> top10 = cirkulacija.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10).collect(Collectors.toList());

        List<Knjiga> bezCirkulacijeSve = sveKnjige.stream()
            .filter(k -> k.getFizickaKnjiga() != null && !pozajmljeneIsbnUPeriodu.contains(k.getIsbn()))
            .collect(Collectors.toList());
        int ukupnoBezCirk = bezCirkulacijeSve.size();
        List<Knjiga> bezCirkulacijePrikaz = bezCirkulacijeSve.stream().limit(20).collect(Collectors.toList());

        // ── SECTION 3 AGGREGATIONS ───────────────────────────────────────
        Set<String> ikadPozajmljeneSet = new HashSet<>(sveIkadPozajmljene);
        List<Knjiga> nikadPozajmleneSve = sveKnjige.stream()
            .filter(k -> k.getFizickaKnjiga() != null && !ikadPozajmljeneSet.contains(k.getIsbn()))
            .collect(Collectors.toList());
        int ukupnoNikad = nikadPozajmleneSve.size();
        List<Knjiga> nikadPozajmlenePrikaz = nikadPozajmleneSve.stream().limit(15).collect(Collectors.toList());

        // ── PDF SETUP ────────────────────────────────────────────────────
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 45, 45, 55, 45);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                try {
                    BaseFont bf2 = BaseFont.createFont(BaseFont.HELVETICA, "Cp1250", BaseFont.NOT_EMBEDDED);
                    Font footer2 = new Font(bf2, 8, Font.NORMAL, TEXT_MID);
                    PdfContentByte cb = w.getDirectContent();
                    cb.saveState();
                    cb.setColorFill(BROWN_LIGHT);
                    cb.rectangle(d.left(), d.bottom() - 15, d.right() - d.left(), 1);
                    cb.fill();
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                            new Phrase("Strana " + w.getPageNumber(), footer2),
                            (d.left() + d.right()) / 2, d.bottom() - 25, 0);
                    cb.restoreState();
                } catch (Exception ignored) {}
            }
        });

        doc.open();

        BaseFont bf     = BaseFont.createFont(BaseFont.HELVETICA,      "Cp1250", BaseFont.NOT_EMBEDDED);
        BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, "Cp1250", BaseFont.NOT_EMBEDDED);

        Font fTitle    = new Font(bfBold, 20, Font.BOLD,   WHITE);
        Font fSubtitle = new Font(bf,     10, Font.NORMAL, new Color(220, 200, 180));
        Font fSection  = new Font(bfBold, 13, Font.BOLD,   BROWN_DARK);
        Font fSectionA = new Font(bfBold, 13, Font.BOLD,   AMBER_DARK);
        Font fTblHead  = new Font(bfBold,  9, Font.BOLD,   WHITE);
        Font fTblCell  = new Font(bf,      9, Font.NORMAL, TEXT_DARK);
        Font fTblSmall = new Font(bf,      8, Font.NORMAL, TEXT_MID);
        Font fMetVal   = new Font(bfBold, 15, Font.BOLD,   BROWN_MED);
        Font fMetLbl   = new Font(bf,      9, Font.NORMAL, TEXT_MID);
        Font fNote     = new Font(bf,      8, Font.ITALIC, TEXT_MID);
        Font fSubSec   = new Font(bfBold, 10, Font.BOLD,   TEXT_MID);

        // ── HEADER ───────────────────────────────────────────────────────
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(20);
        PdfPCell hCell = new PdfPCell();
        hCell.setBackgroundColor(BROWN_MED);
        hCell.setPadding(22);
        hCell.setBorder(Rectangle.NO_BORDER);
        Paragraph t1 = new Paragraph(
            "Izvestaj o stanju fonda, cirkulaciji naslova i potrebama za revizijom", fTitle);
        t1.setAlignment(Element.ALIGN_CENTER);
        Paragraph t2 = new Paragraph(
            "Period cirkulacije: " + fmt(od) + "  \u2014  " + fmt(datDo) +
            "    |    Generisano: " + fmt(LocalDate.now()), fSubtitle);
        t2.setAlignment(Element.ALIGN_CENTER);
        t2.setSpacingBefore(5);
        hCell.addElement(t1);
        hCell.addElement(t2);
        header.addCell(hCell);
        doc.add(header);

        // ════════════════════════════════════════════════════════════════
        // SEKCIJA 1 — PREGLED FONDA
        // ════════════════════════════════════════════════════════════════
        doc.add(sectionHeader("1. Pregled fonda", fSection, bfBold));

        PdfPTable metrics1 = new PdfPTable(3);
        metrics1.setWidthPercentage(100);
        metrics1.setSpacingBefore(10);
        metrics1.setSpacingAfter(16);
        metrics1.setWidths(new float[]{1, 1, 1});
        metrics1.addCell(metricCell("" + ukupnoNaslova,    "Ukupno naslova",      fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell("" + ukupnoPrimeraka,  "Ukupno primeraka",    fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell("" + dostupnoPrimeraka,"Dostupnih primeraka", fMetVal, fMetLbl, bfBold));
        doc.add(metrics1);

        doc.add(subheading("Raspodela po formatu", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblFormat = new PdfPTable(3);
        tblFormat.setWidthPercentage(62);
        tblFormat.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblFormat.setSpacingBefore(6);
        tblFormat.setSpacingAfter(14);
        tblFormat.setWidths(new float[]{3f, 1.5f, 1f});
        addTableHeader(tblFormat, fTblHead, BROWN_DARK, "Format", "Br. naslova", "%");
        String[][] formati = {
            {"Fizicka knjiga", "" + nFizicke,
                ukupnoNaslova > 0 ? String.format("%.1f%%", 100.0 * nFizicke / ukupnoNaslova) : "-"},
            {"E-knjiga",       "" + nEKnjige,
                ukupnoNaslova > 0 ? String.format("%.1f%%", 100.0 * nEKnjige / ukupnoNaslova) : "-"},
            {"Audioknjiga",    "" + nAudio,
                ukupnoNaslova > 0 ? String.format("%.1f%%", 100.0 * nAudio   / ukupnoNaslova) : "-"},
        };
        int rowIdx = 1;
        for (String[] row : formati) {
            addRow(tblFormat, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                   row[0], row[1], row[2]);
            rowIdx++;
        }
        doc.add(tblFormat);

        doc.add(subheading("Raspodela po zanru", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblZanrFond = new PdfPTable(3);
        tblZanrFond.setWidthPercentage(70);
        tblZanrFond.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblZanrFond.setSpacingBefore(6);
        tblZanrFond.setSpacingAfter(20);
        tblZanrFond.setWidths(new float[]{3f, 1.5f, 1f});
        addTableHeader(tblZanrFond, fTblHead, BROWN_DARK, "Zanr", "Br. naslova", "%");
        rowIdx = 1;
        for (Map.Entry<String, Long> e : sortedDesc(poZanruNaslovi)) {
            String pct = ukupnoNaslova > 0
                ? String.format("%.1f%%", 100.0 * e.getValue() / ukupnoNaslova) : "-";
            addRow(tblZanrFond, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                   e.getKey(), "" + e.getValue(), pct);
            rowIdx++;
        }
        if (poZanruNaslovi.isEmpty()) addEmptyRow(tblZanrFond, fTblSmall, 3, "Nema podataka");
        doc.add(tblZanrFond);

        // ════════════════════════════════════════════════════════════════
        // SEKCIJA 2 — CIRKULACIJA NASLOVA
        // ════════════════════════════════════════════════════════════════
        doc.add(sectionHeader(
            "2. Cirkulacija naslova  (" + fmt(od) + " \u2014 " + fmt(datDo) + ")", fSection, bfBold));

        PdfPTable metrics2 = new PdfPTable(3);
        metrics2.setWidthPercentage(100);
        metrics2.setSpacingBefore(10);
        metrics2.setSpacingAfter(16);
        metrics2.setWidths(new float[]{1, 1, 1});
        metrics2.addCell(metricCell("" + pozajmiceUPeriodu.size(),       "Ukupno pozajmica u periodu", fMetVal, fMetLbl, bfBold));
        metrics2.addCell(metricCell("" + pozajmljeneIsbnUPeriodu.size(), "Aktivnih naslova",           fMetVal, fMetLbl, bfBold));
        metrics2.addCell(metricCell("" + ukupnoBezCirk,                  "Naslova bez cirkulacije",    fMetVal, fMetLbl, bfBold));
        doc.add(metrics2);

        doc.add(subheading("Top 10 najpozajmljivanijih naslova", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblTop = new PdfPTable(5);
        tblTop.setWidthPercentage(100);
        tblTop.setSpacingBefore(6);
        tblTop.setSpacingAfter(14);
        tblTop.setWidths(new float[]{0.5f, 3.5f, 2f, 1.5f, 1f});
        addTableHeader(tblTop, fTblHead, BROWN_DARK, "#", "Naslov", "Autor", "Zanr", "Br. poz.");
        int rk = 1;
        for (Map.Entry<String, Long> e : top10) {
            Color bg = rk % 2 == 0 ? BEIGE_LIGHT : WHITE;
            Knjiga k = isbnToKnjigaCirc.get(e.getKey());
            addRow(tblTop, fTblCell, bg, Element.ALIGN_CENTER,
                "" + rk,
                truncate(k.getNaslov(), 40),
                truncate(k.getAutor() != null ? k.getAutor() : "-", 25),
                k.getZanr() != null ? k.getZanr().getName() : "-",
                "" + e.getValue());
            rk++;
        }
        if (top10.isEmpty()) addEmptyRow(tblTop, fTblSmall, 5, "Nema pozajmica u izabranom periodu");
        doc.add(tblTop);

        doc.add(subheading("Naslovi bez cirkulacije u periodu", new Font(bfBold, 10, Font.BOLD, TEXT_MID)));
        PdfPTable tblBezCirk = new PdfPTable(3);
        tblBezCirk.setWidthPercentage(100);
        tblBezCirk.setSpacingBefore(6);
        tblBezCirk.setSpacingAfter(4);
        tblBezCirk.setWidths(new float[]{3.5f, 2.5f, 2f});
        addTableHeader(tblBezCirk, fTblHead, BROWN_DARK, "Naslov", "Autor", "Zanr");
        rowIdx = 1;
        for (Knjiga k : bezCirkulacijePrikaz) {
            addRow(tblBezCirk, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                truncate(k.getNaslov(), 45),
                truncate(k.getAutor() != null ? k.getAutor() : "-", 30),
                k.getZanr() != null ? k.getZanr().getName() : "-");
            rowIdx++;
        }
        if (bezCirkulacijePrikaz.isEmpty())
            addEmptyRow(tblBezCirk, fTblSmall, 3, "Svi naslovi su imali cirkulaciju u ovom periodu");
        doc.add(tblBezCirk);
        if (ukupnoBezCirk > 20) {
            Paragraph noteCirc = new Paragraph(
                "* Prikazano prvih 20 od ukupno " + ukupnoBezCirk + " naslova bez cirkulacije u periodu.", fNote);
            noteCirc.setSpacingBefore(4);
            doc.add(noteCirc);
        }
        doc.add(new Paragraph(" "));

        // ════════════════════════════════════════════════════════════════
        // SEKCIJA 3 — POTREBE ZA REVIZIJOM
        // ════════════════════════════════════════════════════════════════
        doc.add(sectionHeader("3. Potrebe za revizijom", fSectionA, bfBold));

        // 3.1 Nikad pozajmljeni
        doc.add(subheading("3.1  Naslovi koji nikada nisu pozajmljeni", fSubSec));
        PdfPTable tblNikad = new PdfPTable(3);
        tblNikad.setWidthPercentage(100);
        tblNikad.setSpacingBefore(6);
        tblNikad.setSpacingAfter(4);
        tblNikad.setWidths(new float[]{3.5f, 2.5f, 2f});
        addTableHeader(tblNikad, fTblHead, BROWN_DARK, "Naslov", "Autor", "Zanr");
        rowIdx = 1;
        for (Knjiga k : nikadPozajmlenePrikaz) {
            addRow(tblNikad, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                truncate(k.getNaslov(), 45),
                truncate(k.getAutor() != null ? k.getAutor() : "-", 30),
                k.getZanr() != null ? k.getZanr().getName() : "-");
            rowIdx++;
        }
        if (nikadPozajmlenePrikaz.isEmpty())
            addEmptyRow(tblNikad, fTblSmall, 3, "Svi naslovi su imali barem jednu pozajmicu");
        doc.add(tblNikad);
        if (ukupnoNikad > 15) {
            Paragraph noteNikad = new Paragraph(
                "* Prikazano prvih 15 od ukupno " + ukupnoNikad + " naslova koji nikada nisu pozajmljeni.", fNote);
            noteNikad.setSpacingBefore(4);
            doc.add(noteNikad);
        }
        doc.add(new Paragraph(" "));

        // 3.2 Aktivne pozajmice u prekoracenju
        doc.add(subheading("3.2  Aktivne pozajmice u prekoracenju roka", fSubSec));
        PdfPTable tblPrekor = new PdfPTable(4);
        tblPrekor.setWidthPercentage(100);
        tblPrekor.setSpacingBefore(6);
        tblPrekor.setSpacingAfter(14);
        tblPrekor.setWidths(new float[]{3f, 2.5f, 1.5f, 1.5f});
        addTableHeader(tblPrekor, fTblHead, BROWN_DARK, "Naslov", "Clan", "Rok vracanja", "Dana kasnjenja");
        rowIdx = 1;
        for (Pozajmica p : uPrekoracenjuSad) {
            long dana = ChronoUnit.DAYS.between(p.getDatOcVrac(), LocalDate.now());
            String naslov = truncate(
                p.getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getNaslov(), 40);
            String clan = p.getClan().getFirstName() + " " + p.getClan().getLastName();
            addRow(tblPrekor, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                naslov, truncate(clan, 30), fmt(p.getDatOcVrac()), "+" + dana);
            rowIdx++;
        }
        if (uPrekoracenjuSad.isEmpty())
            addEmptyRow(tblPrekor, fTblSmall, 4, "Nema aktivnih pozajmica u prekoracenju roka");
        doc.add(tblPrekor);

        // 3.3 Izgubljena gradja
        doc.add(subheading("3.3  Izgubljena gradja (evidentirana kazna za gubitak)", fSubSec));
        PdfPTable tblIzg = new PdfPTable(5);
        tblIzg.setWidthPercentage(100);
        tblIzg.setSpacingBefore(6);
        tblIzg.setSpacingAfter(14);
        tblIzg.setWidths(new float[]{3f, 2.5f, 1.5f, 1.2f, 1f});
        addTableHeader(tblIzg, fTblHead, BROWN_DARK, "Naslov", "Clan", "Dat. kazne", "Iznos (RSD)", "Placena");
        rowIdx = 1;
        for (Kazna kaz : izgubljenaPozajmica) {
            String naslov = truncate(
                kaz.getPozajmica().getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getNaslov(), 40);
            String clan = kaz.getClan().getFirstName() + " " + kaz.getClan().getLastName();
            addRow(tblIzg, fTblCell, rowIdx % 2 == 0 ? BEIGE_LIGHT : WHITE, Element.ALIGN_LEFT,
                naslov, truncate(clan, 30), fmt(kaz.getDatNastanka()),
                "" + kaz.getIznosK(), kaz.isPlacena() ? "Da" : "Ne");
            rowIdx++;
        }
        if (izgubljenaPozajmica.isEmpty())
            addEmptyRow(tblIzg, fTblSmall, 5, "Nema evidentiranih izgubljenih primeraka");
        doc.add(tblIzg);

        Paragraph napomena = new Paragraph(
            "* Izvestaj obuhvata aktivni fond (neobrisan). " +
            "Sekcija cirkulacije odnosi se na fizicke pozajmice u navedenom periodu.",
            fNote);
        napomena.setSpacingBefore(20);
        doc.add(napomena);

        doc.close();
        return baos.toByteArray();
    }





    // ===================================================================================
    //               NENAD
    // ===================================================================================





    public byte[] generisiIzvestajKoriscenjaAIAsistenta(LocalDate datumOd, LocalDate datumDo)
            throws DocumentException, IOException {

        // ── 0. Podaci iz baze ──────────────────────────────────────────────────────
        LocalDateTime odDT = datumOd.atStartOfDay();
        LocalDateTime doDT = datumDo.atTime(23, 59, 59);

        // Sve čet-sesije u periodu (po datumu kreiranja)
        List<CetSesija> sveSesije = cetSesijaRepository
                .findByDatumKreiranjaCSBetween(odDT, doDT);

        // Sve poruke iz tih sesija
        List<Long> idSesija = sveSesije.stream().map(CetSesija::getId).collect(Collectors.toList());
        List<CetPoruka> svePoruke = idSesija.isEmpty()
                ? Collections.emptyList()
                : cetPorukaRepository.findByCetSesijaIdIn(idSesija);

        // ── 1. Agregirani podaci ───────────────────────────────────────────────────
        long ukupnoSesija   = sveSesije.size();
        long ukupnoClanskeP = svePoruke.stream().filter(p -> p.getTipCP() == TipCP.CLAN).count();
        long ukupnoAIPorukaL= svePoruke.stream().filter(p -> p.getTipCP() == TipCP.AI_ASISTENT).count();
        long ukupnoPorukaL  = svePoruke.size();

        double prosecnoPorukaPoSesiji = ukupnoSesija == 0 ? 0
                : (double) ukupnoPorukaL / ukupnoSesija;

        // Top-10 korisnika po broju sesija
        Map<String, Long> sesijePoClanJmbg = sveSesije.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getClan().getJmbg(), Collectors.counting()));
        Map<String, String> jmbgToIme = sveSesije.stream()
                .collect(Collectors.toMap(
                        s -> s.getClan().getJmbg(),
                        s -> s.getClan().getFirstName() + " " + s.getClan().getLastName(),
                        (a, b) -> a));
        // Za svakog korisnika – broj korisničkih poruka
        Map<String, Long> porukePoClanJmbg = svePoruke.stream()
                .filter(p -> p.getTipCP() == TipCP.CLAN)
                .collect(Collectors.groupingBy(
                        p -> p.getCetSesija().getClan().getJmbg(), Collectors.counting()));

        List<Map.Entry<String, Long>> top10 = sesijePoClanJmbg.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Ocene čet-poruka (raspoređene 1–5)
        List<Integer> sveOcene = svePoruke.stream()
                .flatMap(p -> p.getOcene().stream())
                .map(o -> o.getOcenaCP())
                .collect(Collectors.toList());
        long ukupnoOcena = sveOcene.size();
        Map<Integer, Long> ocenePoBroju = new TreeMap<>();
        for (int i = 1; i <= 5; i++) {
            final int ocena = i;
            ocenePoBroju.put(ocena, sveOcene.stream().filter(o -> o == ocena).count());
        }

        // Slike uz poruke
        long porukesSlikama = svePoruke.stream()
                .filter(p -> p.getTipCP() == TipCP.CLAN && p.getSlikaBase64() != null && !p.getSlikaBase64().isBlank())
                .count();

        // Arhivirane sesije (sve vreme, ne samo period – to je "trenutno stanje")
        long arhiviraneSesijeSvukupno = cetSesijaRepository.countByArhiviranoIsTrueAndDatumArhiviranjaCSBetween(odDT, doDT);
        long sveSesijeIkad            = cetSesijaRepository.count();
        double udelArhiviranih = sveSesijeIkad == 0 ? 0
                : 100.0 * arhiviraneSesijeSvukupno / sveSesijeIkad;
        // Arhivirane u periodu
        long arhiviraneUPeriodu = sveSesije.stream()
                .filter(s -> Boolean.TRUE.equals(s.getArhivirano())).count();

        // Mesečni trend sesija u periodu
        Map<YearMonth, Long> mesecniTrendSesija = new TreeMap<>(sveSesije.stream()
                .collect(Collectors.groupingBy(
                        s -> YearMonth.of(
                                s.getDatumKreiranjaCS().getYear(),
                                s.getDatumKreiranjaCS().getMonthValue()),
                        Collectors.counting())));

        // Broj jedinstvenih korisnika
        long jedinstveniKorisnici = sveSesije.stream().map(s -> s.getClan().getJmbg()).distinct().count();

        // Sesije po samom clanu ukupno
        Map<String, Long> sesijePoClanJmbgUkupno = sveSesije.stream().collect(Collectors.groupingBy(s -> s.getClan().getJmbg(), Collectors.counting()));

        // Raspodela po tipu agenta
        Map<TipAgentaCS, Long> poTipuAgenta = sveSesije.stream().collect(Collectors.groupingBy(CetSesija::getTipAgentaCS, Collectors.counting()));

        // Grananje sesija
        long sesijaSaGranama = sveSesije.stream().filter(s -> s.getImaGrane()).count();
        long graneSesija = sveSesije.stream().filter(s -> s.getRoditeljskaSesija() != null).count();

        // ── 2. PDF dokument ────────────────────────────────────────────────────────
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 45, 45, 55, 45);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        // Footer sa brojem stranice
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                try {
                    BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1250", BaseFont.NOT_EMBEDDED);
                    Font footer = new Font(bf, 8, Font.NORMAL, TEXT_MID_GREEN);
                    PdfContentByte cb = w.getDirectContent();
                    cb.saveState();
                    cb.setColorFill(CARD_BG);
                    cb.rectangle(d.left(), d.bottom() - 15, d.right() - d.left(), 1);
                    cb.fill();
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                            new Phrase("Strana " + w.getPageNumber(), footer),
                            (d.left() + d.right()) / 2, d.bottom() - 25, 0);
                    cb.restoreState();
                } catch (Exception ignored) {}
            }
        });

        doc.open();

        BaseFont bf     = BaseFont.createFont(BaseFont.HELVETICA,      "Cp1250", BaseFont.NOT_EMBEDDED);
        BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, "Cp1250", BaseFont.NOT_EMBEDDED);
        BaseFont bfUnicode = BaseFont.createFont(IzvestajService.class.getResource("/fonts/DejaVuSans.ttf").toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);


        Font fTitle    = new Font(bfBold, 20, Font.BOLD,   WHITE);
        Font fSubtitle = new Font(bf,     10, Font.NORMAL, GREEN_LIGHT);
        Font fSection  = new Font(bfBold, 13, Font.BOLD,   GREEN_DARK);
        Font fTblHead  = new Font(bfBold,  9, Font.BOLD,   WHITE);
        Font fTblCell  = new Font(bf,      9, Font.NORMAL, TEXT_DARK_GREEN);
        Font fTblSmall = new Font(bf,      8, Font.NORMAL, TEXT_MID_GREEN);
        Font fMetVal   = new Font(bfBold, 15, Font.BOLD,   GREEN_MED);
        Font fMetLbl   = new Font(bf,      9, Font.NORMAL, TEXT_MID_GREEN);
        Font fSubSec   = new Font(bfBold, 10, Font.BOLD,   TEXT_MID_GREEN);
        Font fNote     = new Font(bf,      8, Font.ITALIC, TEXT_MID_GREEN);
        Font fZvezdice = new Font(bfUnicode, 9, Font.NORMAL, TEXT_DARK_GREEN);

        // ── Zaglavlje ─────────────────────────────────────────────────────────────
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(20);
        PdfPCell hCell = new PdfPCell();
        hCell.setBackgroundColor(GREEN_DARK);
        hCell.setPadding(22);
        hCell.setBorder(Rectangle.NO_BORDER);
        Paragraph t1 = new Paragraph("Izveštaj o korišćenju AI asistenta", fTitle);
        t1.setAlignment(Element.ALIGN_CENTER);
        Paragraph t2 = new Paragraph(
                "Period: " + fmt(datumOd) + "  \u2014  " + fmt(datumDo) +
                        "    |    Generisano: " + fmt(LocalDate.now()), fSubtitle);
        t2.setAlignment(Element.ALIGN_CENTER);
        t2.setSpacingBefore(5);
        hCell.addElement(t1);
        hCell.addElement(t2);
        headerTable.addCell(hCell);
        doc.add(headerTable);

        // ══════════════════════════════════════════════════════════════════════════
        // SEKCIJA 1 – Opsti pregled
        // ══════════════════════════════════════════════════════════════════════════
        doc.add(sectionHeader("1. Opšti pregled korišćenja", fSection, bfBold));

        PdfPTable metrics1 = new PdfPTable(5);
        metrics1.setWidthPercentage(100);
        metrics1.setSpacingBefore(10);
        metrics1.setSpacingAfter(16);
        metrics1.setWidths(new float[]{1f, 1f, 1f, 1f, 1f});
        metrics1.addCell(metricCell("" + ukupnoSesija,      "Čet sesija",            fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell("" + jedinstveniKorisnici,"Jedinstvenih korisnika", fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell("" + ukupnoClanskeP,    "Poruka korisnika",      fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell("" + ukupnoAIPorukaL,   "Odgovora AI asistenta", fMetVal, fMetLbl, bfBold));
        metrics1.addCell(metricCell(String.format("%.1f", prosecnoPorukaPoSesiji), "Prosečno poruka po sesiji", fMetVal, fMetLbl, bfBold));
        doc.add(metrics1);

        // Mesecni trend sesija
        doc.add(subheading("Mesečni trend kreiranja sesija", fSubSec));
        PdfPTable tblMes = new PdfPTable(3);
        tblMes.setWidthPercentage(65);
        tblMes.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblMes.setSpacingBefore(6);
        tblMes.setSpacingAfter(20);
        tblMes.setWidths(new float[]{2.5f, 1.5f, 1.5f});
        addTableHeader(tblMes, fTblHead, GREEN_DARK, "Mesec", "Broj kreiranih sesija", "Promena");
        int rowIdx = 1;
        Long prevCount = null;
        for (Map.Entry<YearMonth, Long> e : mesecniTrendSesija.entrySet()) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String label = MESECI[e.getKey().getMonthValue()] + " " + e.getKey().getYear();
            String promena;
            if (prevCount == null) {
                promena = "-";
            } else {
                long diff = e.getValue() - prevCount;
                promena = (diff >= 0 ? "+" : "") + diff;
            }
            addRow(tblMes, fTblCell, bg, Element.ALIGN_LEFT, label, "" + e.getValue(), promena);
            prevCount = e.getValue();
            rowIdx++;
        }
        if (mesecniTrendSesija.isEmpty())
            addEmptyRow(tblMes, fTblSmall, 3, "Nema podataka za izabrani period");
        doc.add(tblMes);

        // ══════════════════════════════════════════════════════════════════════════
        // SEKCIJA 2 – Top 10 korisnika
        // ══════════════════════════════════════════════════════════════════════════
        doc.add(sectionHeader("2. Top 10 korisnika po korišćenju AI asistenta", fSection, bfBold));
        doc.add(subheading("Rangirano po ukupnom broju kreiranih čet sesija", fSubSec));

        PdfPTable tblTop = new PdfPTable(5);
        tblTop.setWidthPercentage(100);
        tblTop.setSpacingBefore(6);
        tblTop.setSpacingAfter(20);
        tblTop.setWidths(new float[]{0.5f, 3f, 1.5f, 1.5f, 1.5f});
        addTableHeader(tblTop, fTblHead, GREEN_DARK, "#", "Korisnik", "Broj sesija", "Broj poruka", "Prosečan broj poruka po sesiji");
        int rank = 1;
        for (Map.Entry<String, Long> e : top10) {
            Color bg = (rank % 2 == 0) ? CARD_LIGHT : WHITE;
            long sesijKorisnika = e.getValue();
            long porKorisnika   = porukePoClanJmbg.getOrDefault(e.getKey(), 0L);
            String prosek = sesijKorisnika == 0 ? "-"
                    : String.format("%.1f", (double) porKorisnika / sesijKorisnika);
            addRow(tblTop, fTblCell, bg, Element.ALIGN_CENTER,
                    "" + rank,
                    truncate(jmbgToIme.getOrDefault(e.getKey(), e.getKey()), 35),
                    "" + sesijKorisnika,
                    "" + porKorisnika,
                    prosek);
            rank++;
        }
        if (top10.isEmpty())
            addEmptyRow(tblTop, fTblSmall, 5, "Nema podataka za izabrani period");
        doc.add(tblTop);

        // ══════════════════════════════════════════════════════════════════════════
        // SEKCIJA 3 – Ocene cet poruka
        // ══════════════════════════════════════════════════════════════════════════
        doc.add(sectionHeader("3. Ocene odgovora AI asistenta", fSection, bfBold));

        PdfPTable metrics3 = new PdfPTable(2);
        metrics3.setWidthPercentage(50);
        metrics3.setHorizontalAlignment(Element.ALIGN_LEFT);
        metrics3.setSpacingBefore(10);
        metrics3.setSpacingAfter(16);
        metrics3.addCell(metricCell("" + ukupnoOcena, "Ukupno ocena", fMetVal, fMetLbl, bfBold));

        double prosecnaOcena = sveOcene.isEmpty() ? 0 : sveOcene.stream().mapToInt(Integer::intValue).average().orElse(0);
        metrics3.addCell(metricCell(sveOcene.isEmpty() ? "-" : String.format("%.2f / 5", prosecnaOcena), "Prosečna ocena", fMetVal, fMetLbl, bfBold));
        doc.add(metrics3);

        doc.add(subheading("Distribucija ocena (1 – 5 zvezdica)", fSubSec));
        PdfPTable tblOcene = new PdfPTable(3);
        tblOcene.setWidthPercentage(55);
        tblOcene.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblOcene.setSpacingBefore(6);
        tblOcene.setSpacingAfter(20);
        tblOcene.setWidths(new float[]{1.5f, 1.5f, 1f});
        addTableHeader(tblOcene, fTblHead, GREEN_DARK, "Ocena", "Broj ocena", "Udeo u ukupnom broju ocena");
        rowIdx = 1;
        for (Map.Entry<Integer, Long> e : ocenePoBroju.entrySet()) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String zvezdice = "★".repeat(e.getKey()) + "☆".repeat(5 - e.getKey());
            String pct = ukupnoOcena > 0 ? String.format("%.1f%%", 100.0 * e.getValue() / ukupnoOcena) : "-";

            // Ćelija sa zvezdicama koristi Unicode font
            PdfPCell celZvezdice = new PdfPCell(new Phrase(zvezdice, fZvezdice));
            celZvezdice.setBackgroundColor(bg);
            celZvezdice.setHorizontalAlignment(Element.ALIGN_LEFT);
            celZvezdice.setPaddingTop(5);
            celZvezdice.setPaddingBottom(5);
            celZvezdice.setPaddingLeft(6);
            celZvezdice.setPaddingRight(6);
            celZvezdice.setBorderColor(GREEN_LIGHT);
            tblOcene.addCell(celZvezdice);

            // Ostale dve ćelije normalno sa postojećim fontom
            PdfPCell celBroj = new PdfPCell(new Phrase("" + e.getValue(), fTblCell));
            celBroj.setBackgroundColor(bg);
            celBroj.setHorizontalAlignment(Element.ALIGN_CENTER);
            celBroj.setPaddingTop(5); celBroj.setPaddingBottom(5);
            celBroj.setPaddingLeft(6); celBroj.setPaddingRight(6);
            celBroj.setBorderColor(GREEN_LIGHT);
            tblOcene.addCell(celBroj);

            PdfPCell celPct = new PdfPCell(new Phrase(pct, fTblCell));
            celPct.setBackgroundColor(bg);
            celPct.setHorizontalAlignment(Element.ALIGN_CENTER);
            celPct.setPaddingTop(5); celPct.setPaddingBottom(5);
            celPct.setPaddingLeft(6); celPct.setPaddingRight(6);
            celPct.setBorderColor(GREEN_LIGHT);
            tblOcene.addCell(celPct);

            rowIdx++;
        }
        doc.add(tblOcene);

        // ══════════════════════════════════════════════════════════════════════════
        // SEKCIJA 4 – Slike uz poruke
        // ══════════════════════════════════════════════════════════════════════════
        doc.add(sectionHeader("4. Korišćenje slika u razgovoru", fSection, bfBold));

        String udeoSlika = ukupnoClanskeP > 0 ? String.format("%.1f%%", 100.0 * porukesSlikama / ukupnoClanskeP) : "-";

        PdfPTable metrics4 = new PdfPTable(3);
        metrics4.setWidthPercentage(75);
        metrics4.setHorizontalAlignment(Element.ALIGN_LEFT);
        metrics4.setSpacingBefore(10);
        metrics4.setSpacingAfter(20);
        metrics4.setWidths(new float[]{1f, 1f, 1f});
        metrics4.addCell(metricCell("" + ukupnoClanskeP, "Ukupno poruka korisnika", fMetVal, fMetLbl, bfBold));
        metrics4.addCell(metricCell("" + porukesSlikama, "Poruka sa slikom",        fMetVal, fMetLbl, bfBold));
        metrics4.addCell(metricCell(udeoSlika, "Udeo poruka sa slikom",   fMetVal, fMetLbl, bfBold));
        doc.add(metrics4);

        Paragraph notaSlika = new Paragraph("* Slanje slika je podržano samo za agenta za knjige.", fNote);
        notaSlika.setSpacingBefore(2);
        notaSlika.setSpacingAfter(16);
        doc.add(notaSlika);

        // ══════════════════════════════════════════════════════════════════════════
        // SEKCIJA 5 – Arhiviranje
        // ══════════════════════════════════════════════════════════════════════════
        doc.add(sectionHeader("5. Arhiviranje čet sesija", fSection, bfBold));

        PdfPTable metrics5 = new PdfPTable(3);
        metrics5.setWidthPercentage(100);
        metrics5.setSpacingBefore(10);
        metrics5.setSpacingAfter(16);
        metrics5.setWidths(new float[]{1f, 1f, 1f});
        metrics5.addCell(metricCell("" + arhiviraneSesijeSvukupno, "Arhivirano (ukupno)", fMetVal, fMetLbl, bfBold));
        metrics5.addCell(metricCell("" + arhiviraneUPeriodu, "Arhivirano u periodu", fMetVal, fMetLbl, bfBold));
        metrics5.addCell(metricCell(String.format("%.1f%%", udelArhiviranih), "Udeo arhiviranih sesija u odnosu na sve sesije", fMetVal, fMetLbl, bfBold));
        doc.add(metrics5);

        Paragraph notaArh = new Paragraph("* 'Arhivirano (ukupno)' i udeo odnose se na sve sesije u sistemu, a ne samo na one iz odabranog perioda.", fNote);
        notaArh.setSpacingBefore(2);
        notaArh.setSpacingAfter(20);
        doc.add(notaArh);

        // ══════════════════════════════════════════════════════════════════════════
        // SEKCIJA 6 – Raspodela po tipu agenta
        // ══════════════════════════════════════════════════════════════════════════
        doc.add(sectionHeader("6. Raspodela sesija po tipu agenta", fSection, bfBold));

        PdfPTable tblAgenti = new PdfPTable(3);
        tblAgenti.setWidthPercentage(60);
        tblAgenti.setHorizontalAlignment(Element.ALIGN_LEFT);
        tblAgenti.setSpacingBefore(6);
        tblAgenti.setSpacingAfter(20);
        tblAgenti.setWidths(new float[]{3f, 1.5f, 1.5f});
        addTableHeader(tblAgenti, fTblHead, GREEN_DARK, "Tip agenta", "Broj sesija", "Udeo");
        rowIdx = 1;
        for (Map.Entry<TipAgentaCS, Long> e : poTipuAgenta.entrySet()) {
            Color bg = (rowIdx % 2 == 0) ? CARD_LIGHT : WHITE;
            String naziv = e.getKey() == TipAgentaCS.AGENT_KNJIGE ? "Agent za knjige" : "Agent za recenzije";
            String pct = ukupnoSesija > 0 ? String.format("%.1f%%", 100.0 * e.getValue() / ukupnoSesija) : "-";
            addRow(tblAgenti, fTblSmall, bg, Element.ALIGN_LEFT, naziv, "" + e.getValue(), pct);
            rowIdx++;
        }
        if (poTipuAgenta.isEmpty()) {
            addEmptyRow(tblAgenti, fTblSmall, 3, "Nema podataka za izabrani period!");
        }
        doc.add(tblAgenti);

        // ══════════════════════════════════════════════════════════════════════════
        // SEKCIJA 7 – Grananje sesija
        // ══════════════════════════════════════════════════════════════════════════
        doc.add(sectionHeader("7. Grananje čet sesija", fSection, bfBold));
        PdfPTable metrics7 = new PdfPTable(3);
        metrics7.setWidthPercentage(75);
        metrics7.setHorizontalAlignment(Element.ALIGN_LEFT);
        metrics7.setSpacingBefore(10);
        metrics7.setSpacingAfter(16);
        metrics7.setWidths(new float[]{1f, 1f, 1f});
        metrics7.addCell(metricCell("" + sesijaSaGranama, "Sesija sa granama",   fMetVal, fMetLbl, bfBold));
        metrics7.addCell(metricCell("" + graneSesija,      "Sesija koje su grane", fMetVal, fMetLbl, bfBold));
        String udeoGrananja = ukupnoSesija > 0 ? String.format("%.1f%%", 100.0 * graneSesija / ukupnoSesija) : "-";
        metrics7.addCell(metricCell(udeoGrananja, "Udeo grana od svih sesija", fMetVal, fMetLbl, bfBold));
        doc.add(metrics7);

        Paragraph napomenaGrananje = new Paragraph("* Sesija sa granama je ona iz koje je korisnik modifikovao poruku i time kreirao novu verziju razgovora.", fNote);
        napomenaGrananje.setSpacingBefore(2);
        napomenaGrananje.setSpacingAfter(20);
        doc.add(napomenaGrananje);

        // ══════════════════════════════════════════════════════════════════════════
        // Napomena na kraju
        // ══════════════════════════════════════════════════════════════════════════
        Paragraph napomena = new Paragraph("* Izvestaj obuhvata čet sesije kreirane u navedenom periodu. Statistike ocena i slika vezane su za poruke iz tih sesija.", fNote);
        napomena.setSpacingBefore(20);
        doc.add(napomena);

        doc.close();
        return baos.toByteArray();
    }



    // ── Pomocne metode za PDF ────────────────────────────────────────────

    private boolean tip(Knjiga k, int pos) {
        String t = k.getTipKnjige();
        return t != null && t.length() > pos && t.charAt(pos) == '1';
    }

    private Paragraph sectionHeader(String tekst, Font fSection, BaseFont bfBold) throws DocumentException {
        Paragraph p = new Paragraph();
        p.setSpacingBefore(14);
        p.setSpacingAfter(2);

        Chunk c = new Chunk(tekst, fSection);
        p.add(c);
        return p;
    }

    private Paragraph subheading(String tekst, Font font) {
        Paragraph p = new Paragraph(tekst, font);
        p.setSpacingBefore(6);
        p.setSpacingAfter(2);
        return p;
    }

    private PdfPCell metricCell(String value, String label, Font fVal, Font fLbl, BaseFont bfBold) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(CARD_BG);
        cell.setPadding(14);
        cell.setBorderColor(GREEN_LIGHT);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph val = new Paragraph(value, fVal);
        val.setAlignment(Element.ALIGN_CENTER);
        Paragraph lbl = new Paragraph(label, fLbl);
        lbl.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(val);
        cell.addElement(lbl);
        return cell;
    }

    private void addTableHeader(PdfPTable table, Font font, Color bg, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setBackgroundColor(bg);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(7);
            cell.setPaddingBottom(7);
            cell.setPaddingLeft(6);
            cell.setPaddingRight(6);
            cell.setBorderColor(GREEN_DARK);
            table.addCell(cell);
        }
    }

    private void addRow(PdfPTable table, Font font, Color bg, int firstAlign, String... values) {
        boolean first = true;
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v != null ? v : "—", font));
            cell.setBackgroundColor(bg);
            cell.setHorizontalAlignment(first ? firstAlign : Element.ALIGN_CENTER);
            cell.setPaddingTop(5);
            cell.setPaddingBottom(5);
            cell.setPaddingLeft(6);
            cell.setPaddingRight(6);
            cell.setBorderColor(GREEN_LIGHT);
            table.addCell(cell);
            first = false;
        }
    }

    private void addEmptyRow(PdfPTable table, Font font, int colspan, String message) {
        PdfPCell cell = new PdfPCell(new Phrase(message, font));
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10);
        cell.setBorderColor(GREEN_LIGHT);
        table.addCell(cell);
    }

    private List<Map.Entry<String, Long>> sortedDesc(Map<String, Long> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());
    }

    private String fmt(LocalDate d) {
        return d != null ? d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")) : "—";
    }

    private String truncate(String s, int max) {
        if (s == null) return "—";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private String labelKategorije(String tip) {
        return switch (tip) {
            case "REGULARNA"     -> "Regularna";
            case "DECIJA"        -> "Decija";
            case "STUDENTSKA"    -> "Studentska";
            case "PENZIONERSKA"  -> "Penzionerska";
            case "PORODICNA"     -> "Porodicna";
            default              -> tip;
        };
    }
}
