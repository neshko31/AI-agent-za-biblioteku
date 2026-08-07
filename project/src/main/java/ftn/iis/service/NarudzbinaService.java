package ftn.iis.service;

import ftn.iis.dto.*;
import ftn.iis.enums.StatusDobavljaca;
import ftn.iis.enums.StatusNarudzbine;
import ftn.iis.enums.StatusUgovora;
import ftn.iis.model.*;
import ftn.iis.repository.*;
import ftn.iis.utils.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NarudzbinaService {
    private final StavkaNarudzbineRepository stavkaRepository;
    private final NarudzbinaRepository narudzbinaRepository;
    private final DobavljacRepository dobavljacRepository;
    private final UgovorRepository ugovorRepository;
    private final FizickaKnjigaRepository fizickaKnjigaRepository;
    private final BudzetService budzetService;
    private final JwtService jwtService;
    private final PredlogNabavkaRepository predlogNabavkaRepository;
    private final SistemskePreporukeRepository sistemskePreporukeRepository;

    public NarudzbinaService(StavkaNarudzbineRepository stavkaRepository,
                             DobavljacRepository dobavljacRepository, UgovorRepository ugovorRepository,
                             FizickaKnjigaRepository fizickaKnjigaRepository, BudzetService budzetService,
                             JwtService jwtService, NarudzbinaRepository narudzbinaRepository,
                             PredlogNabavkaRepository predlogNabavkaRepository,
                             SistemskePreporukeRepository sistemskePreporukeRepository) {
        this.stavkaRepository = stavkaRepository;
        this.dobavljacRepository = dobavljacRepository;
        this.ugovorRepository = ugovorRepository;
        this.fizickaKnjigaRepository = fizickaKnjigaRepository;
        this.budzetService = budzetService;
        this.jwtService = jwtService;
        this.narudzbinaRepository = narudzbinaRepository;
        this.predlogNabavkaRepository = predlogNabavkaRepository;
        this.sistemskePreporukeRepository = sistemskePreporukeRepository;
    }



    // kreiranje narudzbine
    @Transactional
    public NarudzbinaResponseDto kreirajNarudzbinu(String token, KreirajNarudzbinuDto dto) {

        proveriMenadzera(token);
        Dobavljac dobavljac = dobavljacRepository.findById(dto.getDobavljacId())
                .orElseThrow(() -> new RuntimeException("Dobavljač nije pronađen."));

        if (dobavljac.getStatus() != StatusDobavljaca.AKTIVAN) {
            throw new RuntimeException("Dobavljač nije aktivan. Nije moguce raditi nabavku od njega.");
        }

        Ugovor ugovor = ugovorRepository.findById(dto.getUgovorId())
                .orElseThrow(() -> new RuntimeException("Ugovor nije pronađen."));

        if (ugovor.getStatus() != StatusUgovora.AKTIVAN) {
            throw new RuntimeException("Ugovor nije aktivan.");
        }

        Narudzbina narudzbina = new Narudzbina(dobavljac, ugovor, dto.getNapomena());
        narudzbina = narudzbinaRepository.save(narudzbina);
        return mapirajUDto(narudzbina);

    }



    // Dodavanje stavke narudzbine
    @Transactional
    public NarudzbinaResponseDto dodajStavku(String token, Long narudzbinaId, DodajStavkuDto dto) {
        proveriMenadzera(token);

        Narudzbina narudzbina = narudzbinaRepository.findById(narudzbinaId)
                .orElseThrow(() -> new RuntimeException("Narudžbina nije pronađena."));

        if (narudzbina.getStatus() != StatusNarudzbine.KREIRANA) {
            throw new RuntimeException("Stavke se mogu dodavati samo u narudžbine sa statusom KREIRANA.");
        }

        Double popust = narudzbina.getUgovor().getPopust();

        StavkaNarudzbine stavka;

        if (dto.getIsbn() != null) {
            // Sistemska preporuka -> knjiga postoji u bazi
            FizickaKnjiga knjiga = fizickaKnjigaRepository.findById(dto.getIsbn())
                    .orElseThrow(() -> new RuntimeException("Knjiga nije pronađena"));

            SistemskaPreporuka preporuka = sistemskePreporukeRepository.findByFizickaKnjigaIsbn(dto.getIsbn())
                    .orElseThrow(() -> new RuntimeException("Sistemska preporuka za ovaj ISBN nije pronađena."));

            Double cenaStavke = preporuka.getOkvirnaCena();

            // Pošto stavka traži preporukaId, mogu ga dinamički postaviti ovde iz nađene preporuke!!!
            dto.setPreporukaId(preporuka.getId());

            stavka = new StavkaNarudzbine(narudzbina, knjiga, dto.getKolicina(), cenaStavke, popust);
            stavka.setPreporukaId(dto.getPreporukaId());

        }
        else if(dto.getPredlogId() != null){
            // Korisnicki predlog -> knjiga ne postji u bazi
            PredlogZaNabavku predlog = predlogNabavkaRepository.findById(dto.getPredlogId())
                    .orElseThrow(() -> new RuntimeException("Predlog nije pronađen"));

            Double cenaStavke = predlog.getOkvirnaCena();

            stavka = new StavkaNarudzbine(narudzbina, predlog.getNaslov(), predlog.getAutor(),
                    predlog.getId(), dto.getKolicina(), cenaStavke, popust);

            stavka.setPredlogId(predlog.getId());

        }
        else{
            throw new IllegalArgumentException("Mora biti postavljen isbn ili predlogId.");
        }

        stavkaRepository.save(stavka);

        // Ažuriranje ukupne cene narudžbine
        narudzbina.setUkupnaCena(narudzbina.getUkupnaCena() + stavka.getUkupnaCenaStavke());
        narudzbinaRepository.save(narudzbina);

        return mapirajUDto(narudzbina);
    }



    // Uklanjanje stavke
    @Transactional
    public NarudzbinaResponseDto ukloniStavku(String token, Long narudzbinId, Long stavkaId) {
        proveriMenadzera(token);

        Narudzbina narudzbina = narudzbinaRepository.findById(narudzbinId)
                .orElseThrow(() -> new RuntimeException("Narudžbina nije pronađena."));

        if (narudzbina.getStatus() != StatusNarudzbine.KREIRANA) {
            throw new RuntimeException("Stavke se mogu uklanjati samo iz narudžbina sa statusom KREIRANA.");
        }

        StavkaNarudzbine stavka = stavkaRepository.findById(stavkaId)
                .orElseThrow(() -> new RuntimeException("Stavka nije pronađena."));

        if (!stavka.getNarudzbina().getId().equals(narudzbinId)) {
            throw new RuntimeException("Stavka ne pripada ovoj narudžbini.");
        }

        // Azuriram cenu
        narudzbina.setUkupnaCena(Math.max(0.0, narudzbina.getUkupnaCena() - stavka.getUkupnaCenaStavke()));

        // Brisem stavku iz liste
        if (narudzbina.getStavke() != null) {
            narudzbina.getStavke().remove(stavka);
        }

        // Ovim brisem stavku iz baze
        stavkaRepository.delete(stavka);
        narudzbinaRepository.save(narudzbina);

        return mapirajUDto(narudzbina);
    }



    // Potvrda narudžbine — rezervacija postaje potrošnjaa
    @Transactional
    public NarudzbinaResponseDto potvrdiNarudzbinu(String token, Long id) {
        proveriMenadzera(token);

        Narudzbina narudzbina = narudzbinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Narudžbina nije pronađena."));

        if (narudzbina.getStatus() != StatusNarudzbine.KREIRANA) {
            throw new RuntimeException("Narudžbina je već potvrđena. Zao nam je :( .");
        }

        List<StavkaNarudzbine> stavke = stavkaRepository.findAllByNarudzbinaId(id);

        if (stavke.isEmpty()) {
            throw new RuntimeException("Narudžbina mora imati bar jednu stavku.");
        }

        for (StavkaNarudzbine stavka : stavke) {

            if (stavka.getFizickaKnjiga() != null) {
                Genre zanr = stavka.getFizickaKnjiga().getKnjiga().getZanr();
                if (zanr != null) {
                    budzetService.potrosi(zanr.getId(), stavka.getUkupnaCenaStavke());
                }
            }
            else{
                PredlogZaNabavku predlog = predlogNabavkaRepository.findById(stavka.getPredlogId()).orElseThrow(() -> new RuntimeException("Predlog nije pronađen."));

                if(predlog != null){
                    // Ako je predlog
                    Genre zanr = predlog.getZanr();
                    if(zanr != null){
                        budzetService.potrosi(zanr.getId(), stavka.getUkupnaCenaStavke());
                    }

                }

            }

        }
        narudzbina.setStatus(StatusNarudzbine.POTVRDJENA);
        narudzbinaRepository.save(narudzbina);
        return mapirajUDto(narudzbina);
    }



    // Pregled svih narudžbina
    @Transactional
    public List<NarudzbinaResponseDto> getSveNarudzbine(String token) {
        proveriMenadzera(token);

        List<Narudzbina> narudzbine = narudzbinaRepository.findAllByOrderByDatumKreiranjaDesc();

        List<NarudzbinaResponseDto> dtos = new ArrayList<>();
        for (Narudzbina n : narudzbine) {
            dtos.add(mapirajUDto(n));
        }
        return dtos;
    }



    // Detalji jedne narudžbine
    @Transactional
    public NarudzbinaResponseDto getJednaNarudzbina(String token, Long id) {
        proveriMenadzera(token);

        Narudzbina narudzbina = narudzbinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Narudžbina nije pronađena."));

        return mapirajUDto(narudzbina);
    }



    // Evidentiranje isporuke
    @Transactional
    public NarudzbinaResponseDto evidentirajIsporuku(String token, Long id, EvidentirajIsporukuDto dto) {
        proveriMenadzera(token);

        Narudzbina narudzbina = narudzbinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Narudžbina nije pronađena."));

        if (narudzbina.getStatus() != StatusNarudzbine.POTVRDJENA) {
            throw new RuntimeException("Isporuka se može evidentirati samo za narudžbine sa statusom POTVRDJENA.");
        }

        List<StavkaNarudzbine> stavke = stavkaRepository.findAllByNarudzbinaId(id);
        if (stavke.isEmpty()) {
            throw new RuntimeException("Ne možete evidentirati isporuku narudžbine bez stavki.");
        }

        narudzbina.setDatumStvarneIsporuke(dto.getDatumStvarneIsporuke());
        narudzbina.setStatus(StatusNarudzbine.ISPORUCENA);
        narudzbinaRepository.save(narudzbina);
        return mapirajUDto(narudzbina);
    }


    // Pomocne metodice ~~~
    private void proveriMenadzera(String token) {
        String uloga = jwtService.extractRole(token);
        if (!uloga.equalsIgnoreCase("MENADZER")) {
            throw new RuntimeException("Samo menadžer može upravljati narudzbinama.");
        }
    }

    private NarudzbinaResponseDto mapirajUDto(Narudzbina n) {
        NarudzbinaResponseDto dto = new NarudzbinaResponseDto();
        dto.setId(n.getId());
        dto.setDobavljacNaziv(n.getDobavljac().getNaziv());
        dto.setDobavljacId(n.getDobavljac().getId());
        dto.setUgovorId(n.getUgovor().getId());
        dto.setPopust(n.getUgovor().getPopust());
        dto.setDatumKreiranja(n.getDatumKreiranja());
        dto.setDatumOcekivaneIsporuke(n.getDatumOcekivaneIsporuke());
        dto.setDatumStvarneIsporuke(n.getDatumStvarneIsporuke());
        dto.setUkupnaCena(n.getUkupnaCena());
        dto.setStatus(n.getStatus());
        dto.setNapomena(n.getNapomena());

        List<StavkaNarudzbineResponseDto> stavkeDtos = new ArrayList<>();
        for (StavkaNarudzbine s : n.getStavke()) {
            stavkeDtos.add(mapirajStavkuUDto(s));
        }
        dto.setStavke(stavkeDtos);
        return dto;
    }

    private StavkaNarudzbineResponseDto mapirajStavkuUDto(StavkaNarudzbine s) {
        StavkaNarudzbineResponseDto dto = new StavkaNarudzbineResponseDto();
        dto.setId(s.getId());
        dto.setKolicina(s.getKolicina());
        dto.setCenaPoKomadu(s.getCenaPoKomadu());
        dto.setUkupnaCenaStavke(s.getUkupnaCenaStavke());
        dto.setPredlogId(s.getPredlogId());
        dto.setPreporukaId(s.getPreporukaId());

        dto.setNaslov(s.getNaslov());
        dto.setAutor(s.getAutor());

        if (s.getFizickaKnjiga() != null) {
            dto.setIsbn(s.getFizickaKnjiga().getIsbn());
        }

        return dto;
    }

}