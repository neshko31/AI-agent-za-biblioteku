package ftn.iis.service;

import ftn.iis.dto.KomentarRequestDto;
import ftn.iis.dto.KomentarResponseDto;
import ftn.iis.model.Knjiga;
import ftn.iis.model.Komentar;
import ftn.iis.model.User;
import ftn.iis.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ftn.iis.exception.MaksimalnaDubinaKomentaraException;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KomentarService {
    private final KomentarRepository komentarRepository;
    private final KnjigaRepository knjigaRepository;
    private final UserRepository userRepository;

    public KomentarService(KomentarRepository komentarRepository, KnjigaRepository knjigaRepository, UserRepository userRepository) {
        this.komentarRepository = komentarRepository;
        this.knjigaRepository = knjigaRepository;
        this.userRepository = userRepository;
    }

    public List<KomentarResponseDto> dohvatiKomentareZaKnjigu(String isbn, String jmbgKorisnika) {
        return komentarRepository.findByKnjigaIsbnAndOdgovorNaIsNull(isbn)
                .stream()
                .map(k -> KomentarResponseDto.fromKomentar(k, jmbgKorisnika))
                .collect(Collectors.toList());
    }

    @Transactional
    public KomentarResponseDto dodajKomentar(String isbn, String jmbgClana, KomentarRequestDto dto) {
        Knjiga knjiga = knjigaRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Knjiga nije pronađena"));
        User clan = userRepository.findByJmbg(jmbgClana)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        Komentar komentar = new Komentar();
        komentar.setTekstK(dto.getTekstK());
        komentar.setDatumKreiranjaK(LocalDateTime.now());
        komentar.setKnjiga(knjiga);
        komentar.setClan(clan);

        // Ako je odgovor na drugi komentar
        if (dto.getOdgovorNaId() != null) {
            Komentar roditelj = komentarRepository.findById(dto.getOdgovorNaId())
                    .orElseThrow(() -> new RuntimeException("Roditeljski komentar nije pronađen"));
            komentar.setOdgovorNa(roditelj);
        }

        try {
            Komentar sacuvan = komentarRepository.save(komentar);
            return KomentarResponseDto.fromKomentar(sacuvan, jmbgClana);
        } catch (DataAccessException ex) {
            if (ProveraPremasivanjeMaksimalneDubine(ex)) {
                throw new MaksimalnaDubinaKomentaraException();
            }
            throw ex;
        }
    }

    private static final String OZNAKA_MAKSIMALNE_DUZINE_KOMENTARA = "MAX_DUBINA_PREMASENA";

    private boolean ProveraPremasivanjeMaksimalneDubine(Throwable ex) {
        Throwable trenutni = ex;
        while (trenutni != null) {
            if (trenutni.getMessage() != null && trenutni.getMessage().contains(OZNAKA_MAKSIMALNE_DUZINE_KOMENTARA)) {
                return true;
            }
            trenutni = trenutni.getCause();
        }
        return false;
    }

    @Transactional
    public KomentarResponseDto lajkujKomentar(Long komentarId, String jmbgClana) {
        Komentar komentar = komentarRepository.findById(komentarId)
                .orElseThrow(() -> new RuntimeException("Komentar nije pronađen"));
        User clan = userRepository.findByJmbg(jmbgClana)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        boolean vecLajkovao = komentar.getLajkovali().stream()
                .anyMatch(u -> u.getJmbg().equals(jmbgClana));

        if (vecLajkovao) {
            komentar.getLajkovali().removeIf(u -> u.getJmbg().equals(jmbgClana));
        } else {
            komentar.getLajkovali().add(clan);
        }

        Komentar sacuvan = komentarRepository.save(komentar);
        return KomentarResponseDto.fromKomentar(sacuvan, jmbgClana);
    }

    @Transactional
    public void obrisiKomentar(Long komentarId, String jmbgClana) {
        Komentar komentar = komentarRepository.findById(komentarId)
                .orElseThrow(() -> new RuntimeException("Komentar nije pronađen"));

        // Samo autor može brisati
        if (!komentar.getClan().getJmbg().equals(jmbgClana)) {
            throw new RuntimeException("Nemate dozvolu za brisanje ovog komentara");
        }

        komentarRepository.delete(komentar);
    }
}
