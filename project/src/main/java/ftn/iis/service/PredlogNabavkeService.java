package ftn.iis.service;

import ftn.iis.dto.*;
import ftn.iis.enums.StatusPredloga;
import ftn.iis.enums.StatusSistemskePreporuke;
import ftn.iis.enums.Uloge;
import ftn.iis.exception.NonBiblotekarViewingSuggestionsException;
import ftn.iis.exception.NonClanGivingSuggestions;
import ftn.iis.exception.NonClanViewingSuggestions;
import ftn.iis.exception.NonManagerViewingSuggestionsException;
import ftn.iis.model.*;
import ftn.iis.repository.GenreRepository;
import ftn.iis.repository.NotifikacijaRepository;
import ftn.iis.repository.PredlogNabavkaRepository;
import ftn.iis.repository.UserRepository;
import ftn.iis.utils.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PredlogNabavkeService {

    private final PredlogNabavkaRepository predlogNabavkaRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final NotifikacijaRepository notifikacijaRepository;
    private final GenreRepository genreRepository;
    private final BudzetService budzetService;

    public PredlogNabavkeService(PredlogNabavkaRepository predlogNabavkaRepository, JwtService jwtService,
                                 UserRepository userRepository, NotifikacijaRepository notifikacijaRepository,
                                 GenreRepository genreRepository,BudzetService budzetService){
        this.predlogNabavkaRepository = predlogNabavkaRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.notifikacijaRepository = notifikacijaRepository;
        this.genreRepository= genreRepository;
        this.budzetService = budzetService;
    }

    @Transactional
    public PredlogNabavkaResponseDto kreirajPredlog(String token, PredlogNabavkaDto dto){
        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        // Validacija da li je iopste clan
        String uloga = jwtService.extractRole(token);
        if(!uloga.equalsIgnoreCase("CLAN")){
            throw new NonClanGivingSuggestions();
        }
        PredlogZaNabavku predlogZaNabavku = new PredlogZaNabavku(korisnik, dto.getNaslov(), dto.getAutor());
        predlogZaNabavku = predlogNabavkaRepository.save(predlogZaNabavku);
        return mapirajUDto(predlogZaNabavku);
    }

    @Transactional
    public List<PredlogNabavkaResponseDto> mojiPredlozi(String token){
        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        // Validacija da li je iopste clan
        String uloga = jwtService.extractRole(token);
        if(!uloga.equalsIgnoreCase("CLAN")){
            throw new NonClanViewingSuggestions();
        }

        List<PredlogZaNabavku> predlozi = predlogNabavkaRepository.findAllByKorisnikJmbgOrderByDatumPodnosenjaDesc(jmbg);

        List<PredlogNabavkaResponseDto> dtos = new ArrayList<>();
        for (PredlogZaNabavku predlog : predlozi) {
            dtos.add(mapirajUDto(predlog));
        }

        return dtos;
    }

    //menadžer vidi odobrene predloge
    @Transactional
    public List<PredlogNabavkaZaMenadzeraDto> odobreniPredlozi(String token) {
        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        String uloga = jwtService.extractRole(token);
        if (!uloga.equalsIgnoreCase("MENADZER")) {
            throw new NonManagerViewingSuggestionsException();
        }

        List<PredlogZaNabavku> predlozi = predlogNabavkaRepository
                .findAllByStatusOrderByDatumPodnosenjaDesc(StatusPredloga.ODOBRENO_BIBLIOTEKAR);

        List<PredlogNabavkaZaMenadzeraDto> dtos = new ArrayList<>();
        for (PredlogZaNabavku predlog : predlozi) {
            dtos.add(mapirajUDtoMenadzera(predlog));
        }

        return dtos;
}

//bibliotekar vidi predloge na čekanju
@Transactional
    public List<PredlogNabavkaResponseDto> predloziNaCekanju(String token) {
        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        String uloga = jwtService.extractRole(token);
        if (!uloga.equalsIgnoreCase("BIBLIOTEKAR")) {
            throw new NonBiblotekarViewingSuggestionsException();
        }

        List<PredlogZaNabavku> predlozi = predlogNabavkaRepository
                .findAllByStatusOrderByDatumPodnosenjaDesc(StatusPredloga.NA_CEKANJU);

        List<PredlogNabavkaResponseDto> dtos = new ArrayList<>();
        for (PredlogZaNabavku predlog : predlozi) {
            dtos.add(mapirajUDto(predlog));
        }

        return dtos;
    }

    @Transactional
    public PredlogNabavkaResponseDto obradiPredlog(String token, Long id, ObradiPredlogDto dto) {
        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        String uloga = jwtService.extractRole(token);
        if (!uloga.equalsIgnoreCase("BIBLIOTEKAR")) {
            throw new RuntimeException("Samo bibliotekar može da obrađuje predloge.");
        }

        PredlogZaNabavku predlog = predlogNabavkaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Predlog nije pronađen."));

        if (predlog.getStatus() != StatusPredloga.NA_CEKANJU) {
            throw new RuntimeException("Predlog je već obrađen.");
        }

        StatusPredloga noviStatus = StatusPredloga.valueOf(dto.getStatus().toUpperCase());

        if (noviStatus == StatusPredloga.ODBIJENO_BIBLIOTEKAR &&
                (dto.getObrazlozenje() == null || dto.getObrazlozenje().isBlank())) {
            throw new RuntimeException("Obrazloženje je obavezno pri odbijanju.");
        }

        if (noviStatus == StatusPredloga.ODOBRENO_BIBLIOTEKAR) {
            if (dto.getZanrId() == null) {
                throw new RuntimeException("Žanr je obavezan pri odobravanju.");
            }
            if (dto.getOkvirnaCena() == null || dto.getOkvirnaCena() <= 0) {
                throw new RuntimeException("Okvirna cena je obavezna pri odobravanju.");
            }

            Genre zanr = genreRepository.findById(dto.getZanrId())
                    .orElseThrow(() -> new RuntimeException("Žanr nije pronađen."));

            predlog.setZanr(zanr);
            predlog.setOkvirnaCena(dto.getOkvirnaCena());
        }

        predlog.setStatus(noviStatus);
        predlog.setObrazlozenje_bibliotekara(dto.getObrazlozenje());


        predlogNabavkaRepository.save(predlog);

        String poruka;
        if (noviStatus == StatusPredloga.ODOBRENO_BIBLIOTEKAR) {
            poruka = "Vaš predlog za nabavku knjige \"" + predlog.getNaslov() + "\" je odobren i uvršten u plan nabavke.";
        } else {
            poruka = "Vaš predlog za nabavku knjige \"" + predlog.getNaslov() + "\" je odbijen. Razlog: " + dto.getObrazlozenje();
        }

        Notifikacija notifikacija = new Notifikacija(predlog.getKorisnik(), poruka);
        notifikacijaRepository.save(notifikacija);

        return mapirajUDto(predlog);
    }

    @Transactional
    public void obradiPredlogMenadzer(String token, Long predlogId, ObradaPredlogaMenadzerDto dto){

        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        String uloga = jwtService.extractRole(token);
        if (!uloga.equalsIgnoreCase("MENADZER")) {
            throw new RuntimeException("Samo menadzer može da odobrava nabavke.");
        }

        PredlogZaNabavku predlog = predlogNabavkaRepository.findById(predlogId)
                .orElseThrow(() -> new RuntimeException("Predlog nije pronađen."));

        if (predlog.getStatus() != StatusPredloga.ODOBRENO_BIBLIOTEKAR) {
            throw new RuntimeException("Predlog nije odobren od strane bibliotekara.");
        }

        if(dto.getOdobren()){
            // Pre nego sto stvarno dozvolim izmenu statusa, konsultujem se sa budzetom
            if (!budzetService.imaDovoljnoSredstava(
                    predlog.getZanr().getId(), predlog.getOkvirnaCena())) {
                throw new RuntimeException("Nedovoljno sredstava u budžetu za žanr '"
                        + predlog.getZanr().getName() + "'.");
            }

            predlog.setStatus(StatusPredloga.ODOBRENO_MENADZER);
            budzetService.rezervisi(predlog.getZanr().getId(), predlog.getOkvirnaCena());
        }
        else{
            predlog.setStatus(StatusPredloga.ODBIJENO_MENADZER);
        }

        predlogNabavkaRepository.save(predlog);
    }


    @Transactional
    public List<KnjigaZaNarudzbinuDto> predloziZaNarudzbinu(String token){

        String role = jwtService.extractRole(token);

        if(!role.equalsIgnoreCase("MENADZER")){
            throw new RuntimeException();
        }

        List<PredlogZaNabavku> predlozi = predlogNabavkaRepository.findAllByStatus(StatusPredloga.ODOBRENO_MENADZER);

        List<KnjigaZaNarudzbinuDto> dto = new ArrayList<>();

        for(PredlogZaNabavku p : predlozi){

            KnjigaZaNarudzbinuDto k = new KnjigaZaNarudzbinuDto();

            k.setIsbn(null);                      // ughhhhhhhhhhhhhhhh

            k.setNaslov(p.getNaslov());
            k.setAutor(p.getAutor());
            k.setOkvirnaCena(p.getOkvirnaCena());
            k.setPredlogId(p.getId());
            k.setSistemska(false);

            dto.add(k);
        }

        return dto;
    }

    // Pomocna funkcijica
    private PredlogNabavkaResponseDto mapirajUDto(PredlogZaNabavku p) {
        PredlogNabavkaResponseDto dto = new PredlogNabavkaResponseDto();
        dto.setId(p.getId());
        dto.setNaslov(p.getNaslov());
        dto.setAutor(p.getAutor());
        dto.setDatumPodnosenja(p.getDatumPodnosenja());
        dto.setStatus(p.getStatus());
        dto.setObrazlozenje(p.getObrazlozenje_bibliotekara());
        dto.setKorisnikIme(p.getKorisnik().getFirstName());
        dto.setKorisnikPrezime(p.getKorisnik().getLastName());
        return dto;
    }

    private PredlogNabavkaZaMenadzeraDto mapirajUDtoMenadzera (PredlogZaNabavku p){
        PredlogNabavkaZaMenadzeraDto dto = new PredlogNabavkaZaMenadzeraDto();
        dto.setId(p.getId());
        dto.setNaslov(p.getNaslov());
        dto.setAutor(p.getAutor());
        dto.setDatumPodnosenja(p.getDatumPodnosenja());
        dto.setKorisnikIme(p.getKorisnik().getFirstName());
        dto.setKorisnikPrezime(p.getKorisnik().getLastName());
        dto.setOkvirnaCena(p.getOkvirnaCena());
        dto.setZanrNaziv(p.getZanr().getName());
        return dto;
    }
}
