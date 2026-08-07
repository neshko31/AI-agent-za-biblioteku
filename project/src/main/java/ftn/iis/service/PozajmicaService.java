package ftn.iis.service;

import ftn.iis.dto.*;
import ftn.iis.enums.StatusCitanja;
import ftn.iis.enums.StatusSlusanja;
import ftn.iis.model.*;
import ftn.iis.model.id.CitanjeEKnjigeId;
import ftn.iis.model.id.SlusanjeAudioKnjigeId;
import ftn.iis.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PozajmicaService {

    private final PozajmicaRepository pozajmicaRepository;
    private final PrimerakKnjigeRepository primerakKnjigeRepository;
    private final RezervacijaRepository rezervacijaRepository;
    private final ProduzenjePozajmiceRepository produzenjePozajmiceRepository;
    private final ObavestenjeRepository obavestenjeRepository;
    private final UserRepository userRepository;
    private final KnjigaRepository knjigaRepository;
    private final EKnjigaRepository eKnjigaRepository;
    private final AudioKnjigaRepository audioKnjigaRepository;
    private final ClanarinaRepository clanarinaRepository;
    private final CitanjeEKnjigeRepository citanjeEKnjigeRepository;
    private final SlusanjeAudioKnjigeRepository slusanjeAudioKnjigeRepository;

    private final KaznaService kaznaService;
    private static final int DIGITAL_LOAN_DAYS = 14;

    public PozajmicaService(PozajmicaRepository pozajmicaRepository,
                            PrimerakKnjigeRepository primerkaKnjigeRepository,
                            RezervacijaRepository rezervacijaRepository,
                            ProduzenjePozajmiceRepository produzenjePozajmiceRepository,
                            ObavestenjeRepository obavestenjeRepository,
                            UserRepository userRepository,
                            KnjigaRepository knjigaRepository,
                            EKnjigaRepository eKnjigaRepository,
                            AudioKnjigaRepository audioKnjigaRepository,
                            ClanarinaRepository clanarinaRepository,
                            CitanjeEKnjigeRepository citanjeEKnjigeRepository,
                            SlusanjeAudioKnjigeRepository slusanjeAudioKnjigeRepository,
                            KaznaService kaznaServie) {
        this.pozajmicaRepository = pozajmicaRepository;
        this.primerakKnjigeRepository = primerkaKnjigeRepository;
        this.rezervacijaRepository = rezervacijaRepository;
        this.produzenjePozajmiceRepository = produzenjePozajmiceRepository;
        this.obavestenjeRepository = obavestenjeRepository;
        this.userRepository = userRepository;
        this.knjigaRepository = knjigaRepository;
        this.eKnjigaRepository = eKnjigaRepository;
        this.audioKnjigaRepository = audioKnjigaRepository;
        this.clanarinaRepository = clanarinaRepository;
        this.citanjeEKnjigeRepository = citanjeEKnjigeRepository;
        this.slusanjeAudioKnjigeRepository = slusanjeAudioKnjigeRepository;
        this.kaznaService=kaznaServie;
    }

    // Da li korisnik ima aktivne pozajmice gde je prekoracio rok vracanja
    public boolean userHasActiveOrOverdueLoan(String jmbg) {
        return pozajmicaRepository.hasOverduePozajmica(jmbg, LocalDate.now());
    }

    // Pozajmljivanje fizickih knjiga
    @Transactional
    public Map<String, Object> borrowPhysicalBook(String jmbg, String isbn) {
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findByJmbg(jmbg).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "Korisnik nije pronađen.");
            return result;
        }
        List<Pozajmica> activeLoans = pozajmicaRepository.findByClan_JmbgAndStatusPozTrue(jmbg);
        boolean hasOverdue = activeLoans.stream().anyMatch(p -> p.getDatOcVrac().isBefore(LocalDate.now()));
        if (hasOverdue) {
            result.put("success", false);
            result.put("message", "Knjiga ne može biti pozajmljena pošto imate pozajmicu sa prekoračenim rokom vraćanja. Molimo vas da najpre vratite zakasnele knjige.");
            return result;
        }

        List<PrimerakKnjige> available = primerakKnjigeRepository.findAvailablePrimerciByIsbn(isbn);
        if (available.isEmpty()) {
            result.put("success", false);
            result.put("noAvailable", true);
            result.put("message", "Nema dostupnog primerka. Možete napraviti rezervaciju.");
            return result;
        }

        PrimerakKnjige primerak = available.get(0);
        LocalDate danas = LocalDate.now();
        LocalDate datOcVrac = danas.plusDays(14);
        Pozajmica pozajmica = new Pozajmica();
        pozajmica.setDatPoz(danas);
        pozajmica.setDatOcVrac(datOcVrac);
        pozajmica.setStatusPoz(true);
        pozajmica.setPrimerakKnjige(primerak);
        pozajmica.setClan(user);
        pozajmicaRepository.save(pozajmica);

        result.put("success", true);
        result.put("datPoz", danas.toString());
        result.put("datOcVrac", datOcVrac.toString());
        result.put("message", "Knjiga je uspešno pozajmljena i biće dostupna za preuzimanje u vašoj odabranoj biblioteci od " + formatDate(danas) + ". Važenje pozajmice od " + formatDate(danas) + " do " + formatDate(datOcVrac) + ".");
        return result;
    }

    private String formatDate(LocalDate date) {
        return String.format("%02d. %02d. %d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }


    @Transactional
    public Map<String, Object> borrowDigital(String jmbg, String isbn, String type) {
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findByJmbg(jmbg).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "Korisnik nije pronađen.");
            return result;
        }

        // Provera overdue fizickih pozajmica
        List<Pozajmica> activeLoans = pozajmicaRepository.findByClan_JmbgAndStatusPozTrue(jmbg);
        boolean hasOverdue = activeLoans.stream().anyMatch(p -> p.getDatOcVrac().isBefore(LocalDate.now()));
        if (hasOverdue) {
            result.put("success", false);
            result.put("message", "Knjiga ne može biti pozajmljena pošto imate pozajmicu sa prekoračenim rokom vraćanja. Molimo vas da najpre vratite zakasnele knjige.");
            return result;
        }

        LocalDate danas = LocalDate.now();
        LocalDate cutoff = danas.minusDays(DIGITAL_LOAN_DAYS);

        if ("audio".equals(type)) {
            // Proveri da li vec postoji aktivna audio pozajmica za ovu knjigu
            boolean hasActive = slusanjeAudioKnjigeRepository.hasActiveLoan(jmbg, isbn, cutoff);
            if (!hasActive) {
                AudioKnjiga audioKnjiga = audioKnjigaRepository.findById(isbn).orElse(null);
                if (audioKnjiga == null) {
                    result.put("success", false);
                    result.put("message", "Audio knjiga nije pronađena.");
                    return result;
                }
                SlusanjeAudioKnjige slusanje = new SlusanjeAudioKnjige();
                slusanje.setId(new SlusanjeAudioKnjigeId(jmbg, isbn, danas));
                slusanje.setClan(user);
                slusanje.setAudioKnjiga(audioKnjiga);
                slusanje.setStatusSlusanja(StatusSlusanja.U_TOKU);
                slusanje.setTrenutnaSekunda(0);
                slusanje.setDatumPoslednjegPristupa(danas);
                slusanjeAudioKnjigeRepository.save(slusanje);
            }
        } else {
            // Proveri da li vec postoji aktivna eknjiga pozajmica
            boolean hasActive = citanjeEKnjigeRepository.hasActiveLoan(jmbg, isbn, cutoff);
            if (!hasActive) {
                EKnjiga eKnjiga = eKnjigaRepository.findById(isbn).orElse(null);
                if (eKnjiga == null) {
                    result.put("success", false);
                    result.put("message", "eKnjiga nije pronađena.");
                    return result;
                }
                CitanjeEKnjige citanje = new CitanjeEKnjige();
                citanje.setId(new CitanjeEKnjigeId(jmbg, isbn, danas));
                citanje.setClan(user);
                citanje.seteKnjiga(eKnjiga);
                citanje.setStatusCitanja(StatusCitanja.U_TOKU);
                citanje.setTrenutnaStranica(1);
                citanje.setDatumPoslednjegPristupa(danas);
                citanjeEKnjigeRepository.save(citanje);
            }
        }

        result.put("success", true);
        result.put("message", "Možete pristupiti " + ("audio".equals(type) ? "audio knjizi" : "e-knjizi") + ".");
        return result;
    }

    // Pravljenje rezervacija
    @Transactional
    public Map<String, Object> makeReservation(String jmbg, String isbn) {
        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findByJmbg(jmbg).orElse(null);
        if (user == null) {
            result.put("success", false);
            result.put("message", "Korisnik nije pronađen.");
            return result;
        }
        Knjiga knjiga = knjigaRepository.findByIsbnWithFizicka(isbn).orElse(null);
        if (knjiga == null || knjiga.getFizickaKnjiga() == null) {
            result.put("success", false);
            result.put("message", "Knjiga nije pronađena.");
            return result;
        }
        FizickaKnjiga fizickaKnjiga = knjiga.getFizickaKnjiga();

        if (rezervacijaRepository.hasActiveRezervacija(jmbg, isbn)) {
            result.put("success", false);
            result.put("message", "Već imate aktivnu rezervaciju za ovu knjigu.");
            return result;
        }
        Optional<LocalDate> firstReturn = rezervacijaRepository.findFirstActivePozajmicaReturnDate(isbn);
        LocalDate datIspR = firstReturn.orElse(LocalDate.now().plusDays(14));

        Rezervacija rezervacija = new Rezervacija();
        rezervacija.setDatR(LocalDate.now());
        rezervacija.setKanalR("ONLINE");
        rezervacija.setDatIspR(datIspR);
        rezervacija.setFizickaKnjiga(fizickaKnjiga);
        rezervacija.setClan(user);
        rezervacijaRepository.save(rezervacija);

        result.put("success", true);
        result.put("datIspR", datIspR.toString());
        result.put("message", "Knjiga je uspešno rezervisana i očekuje se da će biti dostupna za preuzimanje u vašoj odabranoj biblioteci od " + formatDate(datIspR) + ".");
        return result;
    }


    @Transactional
    public PozajmiceRezervacijeResponseDto getPozajmiceAndRezervacije(String jmbg) {
        PozajmiceRezervacijeResponseDto response = new PozajmiceRezervacijeResponseDto();

        // Fizicke pozajmice
        List<Pozajmica> allPozajmice = pozajmicaRepository.findByClan_Jmbg(jmbg);
        List<PozajmicaDto> aktivne = allPozajmice.stream()
                .filter(p -> Boolean.TRUE.equals(p.getStatusPoz()))
                .map(PozajmicaDto::fromPozajmica)
                .collect(Collectors.toList());
        List<PozajmicaDto> istorija = allPozajmice.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getStatusPoz()))
                .map(PozajmicaDto::fromPozajmica)
                .collect(Collectors.toList());

        // Digitalne aktivne pozajmice (u poslednjih 14 dana, bez datuma zavrsetka)
        LocalDate cutoff = LocalDate.now().minusDays(DIGITAL_LOAN_DAYS);

        List<PozajmicaDto> aktivneEKnjige = citanjeEKnjigeRepository
                .findActiveByJmbg(jmbg, cutoff)
                .stream()
                .map(PozajmicaDto::fromCitanje)
                .collect(Collectors.toList());

        List<PozajmicaDto> aktivneAudioKnjige = slusanjeAudioKnjigeRepository
                .findActiveByJmbg(jmbg, cutoff)
                .stream()
                .map(PozajmicaDto::fromSlusanje)
                .collect(Collectors.toList());

        // Rezervacije
        List<Rezervacija> rezervacije = rezervacijaRepository.findByClan_Jmbg(jmbg);
        List<RezervacijaDto> aktivneRez = rezervacije.stream()
                .filter(r -> r.getDatObavR() == null)
                .map(RezervacijaDto::fromRezervacija)
                .collect(Collectors.toList());

        response.setAktivnePozajmice(aktivne);
        response.setIstorija(istorija);
        response.setAktivneEKnjige(aktivneEKnjige);
        response.setAktivneAudioKnjige(aktivneAudioKnjige);
        response.setAktivneRezervacije(aktivneRez);
        return response;
    }

    // Produzenje
    @Transactional
    public Map<String, Object> requestExtension(Long pozajmicaId, String jmbg) {
        Map<String, Object> result = new HashMap<>();

        Pozajmica pozajmica = pozajmicaRepository.findById(pozajmicaId).orElse(null);
        if (pozajmica == null || !pozajmica.getClan().getJmbg().equals(jmbg)) {
            result.put("success", false);
            result.put("message", "Pozajmica nije pronađena.");
            return result;
        }

        boolean hasPending = pozajmica.getProduzenja().stream().anyMatch(pp -> pp.getStatusPP() == null);
        if (hasPending) {
            result.put("success", false);
            result.put("message", "Već postoji zahtev za produženje koji čeka odobrenje.");
            return result;
        }

        ProduzenjePozajmice pp = new ProduzenjePozajmice();
        pp.setDatKrePP(LocalDate.now());
        pp.setDatObrPP(LocalDate.now().plusDays(10));
        pp.setKanalPP("ONLINE");
        pp.setStariDatVrac(pozajmica.getDatOcVrac());
        pp.setPozajmica(pozajmica);
        produzenjePozajmiceRepository.save(pp);

        result.put("success", true);
        result.put("message", "Zahtev za produženje je uspešno kreiran i čeka odobrenje bibliotekara.");
        return result;
    }

    // Samo za bibliotekara
    @Transactional
    public Map<String, Object> processExtension(Long idPP, String bibliotekarJmbg, boolean approve, String razlog) {
        Map<String, Object> result = new HashMap<>();

        ProduzenjePozajmice pp = produzenjePozajmiceRepository.findById(idPP).orElse(null);
        if (pp == null) {
            result.put("success", false);
            result.put("message", "Zahtev nije pronađen.");
            return result;
        }

        User bibliotekar = userRepository.findByJmbg(bibliotekarJmbg).orElse(null);
        String naslovKnjige = pp.getPozajmica().getPrimerakKnjige()
                .getFizickaKnjiga().getKnjiga().getNaslov();
        User clan = pp.getPozajmica().getClan();
        if (approve) {
            LocalDate noviDatVrac = pp.getPozajmica().getDatOcVrac().plusDays(14);
            pp.setStatusPP(true);
            pp.setNoviDatVrac(noviDatVrac);
            pp.setBibliotekar(bibliotekar);
            produzenjePozajmiceRepository.save(pp);

            Pozajmica pozajmica = pp.getPozajmica();
            pozajmica.setDatOcVrac(noviDatVrac);
            pozajmicaRepository.save(pozajmica);

            Obavestenje o = new Obavestenje();
            o.setTipO("PRODUZENJE_ODOBRENO");
            o.setTekstO("Vaš zahtev za produženje pozajmice knjige '" + naslovKnjige +
                    "' je odobren. Novi rok vraćanja: " +
                    String.format("%02d.%02d.%d", noviDatVrac.getDayOfMonth(),
                            noviDatVrac.getMonthValue(), noviDatVrac.getYear()) + ".");
            o.setDatKreiran(LocalDate.now());
            o.setClan(clan);
            obavestenjeRepository.save(o);

            result.put("success", true);
            result.put("noviDatVrac", noviDatVrac.toString());
        } else {
            pp.setStatusPP(false);
            pp.setRazlogOdb(razlog);
            pp.setBibliotekar(bibliotekar);
            produzenjePozajmiceRepository.save(pp);

            Obavestenje o = new Obavestenje();
            o.setTipO("PRODUZENJE_ODBIJENO");
            String tekst = "Vaš zahtev za produženje pozajmice knjige '" + naslovKnjige + "' je odbijen.";
            if (razlog != null && !razlog.isBlank()) {
                tekst += " Razlog: " + razlog;
            }
            o.setTekstO(tekst);
            o.setDatKreiran(LocalDate.now());
            o.setClan(clan);
            obavestenjeRepository.save(o);
            result.put("success", true);
        }
        return result;
    }

    // Izgubljena knjiga
    @Transactional
    public Map<String, Object> reportLostBook(Long pozajmicaId, String jmbg) {
        Map<String, Object> result = new HashMap<>();
        Pozajmica pozajmica = pozajmicaRepository.findById(pozajmicaId).orElse(null);
        if (pozajmica == null || !pozajmica.getClan().getJmbg().equals(jmbg)) {
            result.put("success", false);
            result.put("message", "Pozajmica nije pronađena.");
            return result;
        }
        pozajmica.setStatusPoz(false);
        pozajmica.setDatVrac(LocalDate.now());
        pozajmicaRepository.save(pozajmica);

        kaznaService.kreirajKaznaIzgubljena(pozajmica);

        result.put("success", true);
        result.put("message", "Prijava izgubljene knjige je evidentirana. Biće vam naplaćena naknada.");
        return result;
    }

    public List<PozajmicaDto> getAllActivePozajmice() {
        return pozajmicaRepository.findAllActivePozajmice()
                .stream()
                .map(PozajmicaDto::fromPozajmica)
                .collect(Collectors.toList());
    }

    // Vracanje knjige
    @Transactional
    public Map<String, Object> returnBookBibliotekar(Long pozajmicaId) {
        Map<String, Object> result = new HashMap<>();

        Pozajmica pozajmica = pozajmicaRepository.findById(pozajmicaId).orElse(null);
        if (pozajmica == null || !Boolean.TRUE.equals(pozajmica.getStatusPoz())) {
            result.put("success", false);
            result.put("message", "Pozajmica nije pronađena ili je već završena.");
            return result;
        }

        pozajmica.setStatusPoz(false);
        pozajmica.setDatVrac(LocalDate.now());
        pozajmicaRepository.save(pozajmica);

        checkAndNotifyReservation(pozajmica.getPrimerakKnjige().getFizickaKnjiga().getIsbn());

        result.put("success", true);
        result.put("message", "Knjiga je uspešno vraćena.");
        return result;
    }

    private void checkAndNotifyReservation(String isbn) {
        List<Rezervacija> activeRez = rezervacijaRepository.findActiveRezervacijeByIsbn(isbn);
        if (!activeRez.isEmpty()) {
            Rezervacija first = activeRez.get(0);
            first.setDatObavR(LocalDate.now());
            rezervacijaRepository.save(first);

            Obavestenje o = new Obavestenje();
            o.setTipO("REZERVACIJA_DOSTUPNA");
            o.setTekstO("Knjiga '" + first.getFizickaKnjiga().getKnjiga().getNaslov() + "' je dostupna za preuzimanje.");
            o.setDatKreiran(LocalDate.now());
            o.setClan(first.getClan());
            obavestenjeRepository.save(o);
        }
    }

    @Transactional
    public Map<String, Object> borrowFromReservation(Long rezervacijaId, String jmbg) {
        Map<String, Object> result = new HashMap<>();

        Rezervacija rezervacija = rezervacijaRepository.findById(rezervacijaId).orElse(null);
        if (rezervacija == null || !rezervacija.getClan().getJmbg().equals(jmbg)) {
            result.put("success", false);
            result.put("message", "Rezervacija nije pronađena.");
            return result;
        }

        if (pozajmicaRepository.hasOverduePozajmica(jmbg, LocalDate.now())) {
            result.put("success", false);
            result.put("message", "Knjiga ne može biti pozajmljena pošto još niste podmirili prethodna dugovanja");
            return result;
        }

        String isbn = rezervacija.getFizickaKnjiga().getIsbn();
        List<PrimerakKnjige> available = primerakKnjigeRepository.findAvailablePrimerciByIsbn(isbn);
        if (available.isEmpty()) {
            result.put("success", false);
            result.put("message", "Nema dostupnog primerka trenutno.");
            return result;
        }

        PrimerakKnjige primerak = available.get(0);
        LocalDate today = LocalDate.now();
        LocalDate datOcVrac = today.plusDays(14);

        Pozajmica pozajmica = new Pozajmica();
        pozajmica.setDatPoz(today);
        pozajmica.setDatOcVrac(datOcVrac);
        pozajmica.setStatusPoz(true);
        pozajmica.setPrimerakKnjige(primerak);
        pozajmica.setClan(rezervacija.getClan());
        pozajmica.setRezervacija(rezervacija);
        pozajmicaRepository.save(pozajmica);

        rezervacija.setDatObavR(today);
        rezervacijaRepository.save(rezervacija);

        result.put("success", true);
        result.put("datPoz", today.toString());
        result.put("datOcVrac", datOcVrac.toString());
        result.put("message", "Knjiga je uspešno pozajmljena. Datum vraćanja: " + formatDate(datOcVrac));
        return result;
    }

    public List<ObavestenjeDto> getObavestenja(String jmbg) {
        return obavestenjeRepository.findByClan_JmbgOrderByDatKreiranDesc(jmbg)
                .stream()
                .map(ObavestenjeDto::fromObavestenje)
                .collect(Collectors.toList());
    }

    public boolean userHasActiveLoanForBook(String jmbg, String isbn) {
        List<Pozajmica> activeLoans = pozajmicaRepository.findByClan_JmbgAndStatusPozTrue(jmbg);
        boolean hasPhysical = activeLoans.stream()
                .anyMatch(p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn().equals(isbn));
        if (hasPhysical) return true;

        LocalDate cutoff = LocalDate.now().minusDays(DIGITAL_LOAN_DAYS);
        if (citanjeEKnjigeRepository.hasActiveLoan(jmbg, isbn, cutoff)) return true;
        if (slusanjeAudioKnjigeRepository.hasActiveLoan(jmbg, isbn, cutoff)) return true;
        return false;
    }

    public void markObavestenjeRead(Long idO, String jmbg) {
        Obavestenje o = obavestenjeRepository.findById(idO).orElse(null);
        if (o != null && o.getClan().getJmbg().equals(jmbg)) {
            o.setProcitano(true);
            obavestenjeRepository.save(o);
        }
    }

    @Transactional
    public void deleteObavestenje(Long idO, String jmbg) {
        Obavestenje o = obavestenjeRepository.findById(idO).orElse(null);
        if (o != null && o.getClan().getJmbg().equals(jmbg)) {
            obavestenjeRepository.delete(o);
        }
    }

    public int getAvailableCopiesCount(String isbn) {
        return primerakKnjigeRepository.findAvailablePrimerciByIsbn(isbn).size();
    }
    public List<ProduzenjePozajmiceRequestDto> getPendingExtensions() {
        return produzenjePozajmiceRepository.findPendingExtensions()
                .stream()
                .map(ProduzenjePozajmiceRequestDto::from)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public void expireDigitalLoans() {
        LocalDate cutoff = LocalDate.now().minusDays(DIGITAL_LOAN_DAYS);

        List<CitanjeEKnjige> expiredCitanja = citanjeEKnjigeRepository.findExpiredActive(cutoff);
        for (CitanjeEKnjige c : expiredCitanja) {
            c.setDatumZavrsetka(c.getId().getDatumPocetka().plusDays(DIGITAL_LOAN_DAYS));
            c.setStatusCitanja(StatusCitanja.NAPUSTENO);
            citanjeEKnjigeRepository.save(c);
        }

        List<SlusanjeAudioKnjige> expiredSlusanja = slusanjeAudioKnjigeRepository.findExpiredActive(cutoff);
        for (SlusanjeAudioKnjige s : expiredSlusanja) {
            s.setDatumZavrsetka(s.getId().getDatumPocetka().plusDays(DIGITAL_LOAN_DAYS));
            s.setStatusSlusanja(StatusSlusanja.NAPUSTENO);
            slusanjeAudioKnjigeRepository.save(s);
        }
    }
    public List<Pozajmica> findAllOverdueActivePozajmice() {
        return pozajmicaRepository.findAllActivePozajmice()
                .stream()
                .filter(p -> LocalDate.now().isAfter(p.getDatOcVrac()))
                .collect(Collectors.toList());
    }
}