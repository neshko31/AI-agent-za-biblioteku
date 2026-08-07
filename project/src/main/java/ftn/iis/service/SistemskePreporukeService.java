package ftn.iis.service;

import ftn.iis.dto.KnjigaZaNarudzbinuDto;
import ftn.iis.dto.PrihvatiSistemskuPreporukuDto;
import ftn.iis.dto.SistemskaPreporukaResponseDto;
import ftn.iis.enums.StatusSistemskePreporuke;
import ftn.iis.exception.NonManagerCreatingContractException;
import ftn.iis.exception.NonManagerStartingAnalysisException;
import ftn.iis.model.FizickaKnjiga;
import ftn.iis.model.Genre;
import ftn.iis.model.SistemskaPreporuka;
import ftn.iis.repository.FizickaKnjigaRepository;
import ftn.iis.repository.PozajmicaRepository;
import ftn.iis.repository.RezervacijaRepository;
import ftn.iis.repository.SistemskePreporukeRepository;
import ftn.iis.utils.JwtService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SistemskePreporukeService {
    private final SistemskePreporukeRepository sistemskePreporukeRepository;
    private final FizickaKnjigaRepository fizickaKnjigaRepository;
    private final PozajmicaRepository pozajmicaRepository;
    private final JwtService jwtService;
    private final BudzetService budzetService;

    private static final int BROJ_DANA_ANALIZE = 30;

    public SistemskePreporukeService(SistemskePreporukeRepository sistemskePreporukeRepository, FizickaKnjigaRepository fizickaKnjigaRepository,
                                     PozajmicaRepository pozajmicaRepository, JwtService jwtService, BudzetService budzetService) {
        this.sistemskePreporukeRepository = sistemskePreporukeRepository;
        this.fizickaKnjigaRepository = fizickaKnjigaRepository;
        this.pozajmicaRepository = pozajmicaRepository;
        this.jwtService = jwtService;
        this.budzetService = budzetService;
    }

    // rucno pokretanje analize trendova
    @Transactional
    public void generisiPreporuke () {
        LocalDate od = LocalDate.now().minusDays(BROJ_DANA_ANALIZE);

        List<FizickaKnjiga> sveFizickeKnjige = fizickaKnjigaRepository.findAll();

        for(FizickaKnjiga fk : sveFizickeKnjige) {
            String isbn = fk.getIsbn();

            // pribavi info o br pozajmica i rezervacija i primeraka
            Integer brPozajmica = pozajmicaRepository.countByIsbnAndDatPozAfter(isbn, od);
            Integer brPrimeraka = fizickaKnjigaRepository.countPrimerciByIsbn(isbn);

            Optional<SistemskaPreporuka> aktivna = sistemskePreporukeRepository
                    .findByFizickaKnjigaIsbnAndStatus(isbn, StatusSistemskePreporuke.AKTIVNA);

            if (brPozajmica >= brPrimeraka * 1.3 && brPrimeraka > 0) {
                String predlog = "Knjiga beleži nagli porast pozajmica — preporučuje se nabavka dodatnih primeraka.";

                Optional<SistemskaPreporuka> prihvacena = sistemskePreporukeRepository
                        .findByFizickaKnjigaIsbnAndStatus(isbn, StatusSistemskePreporuke.PRIHVACENO);

                Optional<SistemskaPreporuka> ignorisana = sistemskePreporukeRepository
                        .findByFizickaKnjigaIsbnAndStatus(isbn, StatusSistemskePreporuke.IGNORISANO);

                // ako postoji predlog, samo azuriram broj pozajmica i to
                if (aktivna.isPresent()) {
                    SistemskaPreporuka preporuka = aktivna.get();
                    preporuka.setBrojPozajmica(brPozajmica);
                    preporuka.setTrenutniBrojPrimeraka(brPrimeraka);
                    preporuka.setPredlog(predlog);
                    preporuka.setDatumGenerisanja(LocalDateTime.now());
                    sistemskePreporukeRepository.save(preporuka);

                // e ako uopste ne postoji kao preporuka, pravim je ~~~
                } else if (!aktivna.isPresent() && !prihvacena.isPresent() && !ignorisana.isPresent()) {
                    SistemskaPreporuka preporuka = new SistemskaPreporuka();
                    preporuka.setStatus(StatusSistemskePreporuke.AKTIVNA);
                    preporuka.setDatumGenerisanja(LocalDateTime.now());
                    preporuka.setFizickaKnjiga(fk);
                    preporuka.setBrojPozajmica(brPozajmica);
                    preporuka.setTrenutniBrojPrimeraka(brPrimeraka);
                    preporuka.setPredlog(predlog);
                    sistemskePreporukeRepository.save(preporuka);
                }
            }
        }
    }

    // automatsko pokretanje analize svakog ponedeljka u ponoc ~~~
    @Scheduled(cron = "0 */4 * * * *")
    public void automatskaAnaliza() {
        generisiPreporuke();
    }


    @Transactional
    public List<SistemskaPreporukaResponseDto> pribaviAktivne(String token) {
        String role = jwtService.extractRole(token);
        if (!role.equalsIgnoreCase("MENADZER")) {
            throw new NonManagerStartingAnalysisException();
        }

        List<SistemskaPreporuka> preporuke = sistemskePreporukeRepository
                .findAllByStatusOrderByDatumGenerisanjaDesc(StatusSistemskePreporuke.AKTIVNA);

        List<SistemskaPreporukaResponseDto> dtos = new ArrayList<>();
        for (SistemskaPreporuka p : preporuke) {
            dtos.add(mapirajUDto(p));
        }
        return dtos;
    }

    @Transactional
    public SistemskaPreporukaResponseDto azurirajStatus(Long id, StatusSistemskePreporuke noviStatus,
                                                        String token, PrihvatiSistemskuPreporukuDto dto) {
        String role = jwtService.extractRole(token);
        if (!role.equalsIgnoreCase("MENADZER")) {
            throw new NonManagerStartingAnalysisException();
        }

        SistemskaPreporuka preporuka = sistemskePreporukeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preporuka nije pronađena."));

        if (preporuka.getStatus() != StatusSistemskePreporuke.AKTIVNA) {
            throw new RuntimeException("Preporuka je već obrađena.");
        }

        if (noviStatus == StatusSistemskePreporuke.PRIHVACENO) {
            if (dto == null || dto.getOkvirnaCena() == null) {
                throw new RuntimeException("Okvirna cena je obavezna pri prihvatanju.");
            }

            Genre zanr = preporuka.getFizickaKnjiga().getKnjiga().getZanr();
            if (zanr == null) {
                throw new RuntimeException("Knjiga nema definisan žanr.");
            }

            Genre zanrPreporuke = preporuka.getFizickaKnjiga().getKnjiga().getZanr();

            // Pre nego sto stvarno dozvolim izmenu statusa, konsultujem se sa budzetom
            if (!budzetService.imaDovoljnoSredstava(
                    zanrPreporuke.getId(), preporuka.getOkvirnaCena())) {
                throw new RuntimeException("Nedovoljno sredstava u budžetu za žanr '"
                        + zanrPreporuke.getName() + "'.");
            }

            budzetService.rezervisi(zanr.getId(), dto.getOkvirnaCena());
            preporuka.setOkvirnaCena(dto.getOkvirnaCena());
        }

        preporuka.setStatus(noviStatus);
        sistemskePreporukeRepository.save(preporuka);
        return mapirajUDto(preporuka);
    }

    @Transactional
    public List<KnjigaZaNarudzbinuDto> preporukeZaNarudzbinu(String token){

        String role = jwtService.extractRole(token);

        if(!role.equalsIgnoreCase("MENADZER")){
            throw new RuntimeException();
        }

        List<SistemskaPreporuka> preporuke = sistemskePreporukeRepository.findAllByStatus(StatusSistemskePreporuke.PRIHVACENO);

        List<KnjigaZaNarudzbinuDto> dto = new ArrayList<>();

        for(SistemskaPreporuka p : preporuke){

            KnjigaZaNarudzbinuDto k = new KnjigaZaNarudzbinuDto();

            k.setIsbn(p.getFizickaKnjiga().getIsbn());
            k.setNaslov(p.getFizickaKnjiga().getKnjiga().getNaslov());
            k.setAutor(p.getFizickaKnjiga().getKnjiga().getAutor());
            k.setOkvirnaCena(p.getOkvirnaCena());
            k.setPredlogId(null);
            k.setSistemska(true);

            dto.add(k);
        }

        return dto;
    }


    // pomocne metodice ~~~

    private SistemskaPreporukaResponseDto mapirajUDto(SistemskaPreporuka p) {
        SistemskaPreporukaResponseDto dto = new SistemskaPreporukaResponseDto();
        dto.setId(p.getId());
        dto.setIsbn(p.getFizickaKnjiga().getIsbn());
        dto.setNaslov(p.getFizickaKnjiga().getKnjiga().getNaslov());
        dto.setAutor(p.getFizickaKnjiga().getKnjiga().getAutor());
        dto.setBrojPozajmica(p.getBrojPozajmica());
        dto.setTrenutniBrojPrimeraka(p.getTrenutniBrojPrimeraka());
        dto.setPredlog(p.getPredlog());
        dto.setDatumGenerisanja(p.getDatumGenerisanja());
        dto.setStatus(p.getStatus());
        dto.setOkvirnaCena(p.getOkvirnaCena());
        Genre zanr = p.getFizickaKnjiga().getKnjiga().getZanr();
        if (zanr != null) {
            dto.setZanrId(zanr.getId());
            dto.setZanrNaziv(zanr.getName());
        }
        return dto;
    }

}
