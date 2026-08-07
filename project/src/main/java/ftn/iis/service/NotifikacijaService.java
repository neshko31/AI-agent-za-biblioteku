package ftn.iis.service;

import ftn.iis.dto.NotifikacijaResponseDto;
import ftn.iis.exception.NonClanGivingSuggestions;
import ftn.iis.exception.NonClanRecievingNotification;
import ftn.iis.model.Notifikacija;
import ftn.iis.model.User;
import ftn.iis.repository.NotifikacijaRepository;
import ftn.iis.repository.UserRepository;
import ftn.iis.utils.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotifikacijaService {
    private final NotifikacijaRepository notifikacijaRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public NotifikacijaService(NotifikacijaRepository notifikacijaRepository, JwtService jwtService,
                               UserRepository userRepository){
        this.notifikacijaRepository =notifikacijaRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<NotifikacijaResponseDto> mojeNotifikacije(String token) {
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        // Validacija da li je iopste clan
        String uloga = jwtService.extractRole(token);
        if(!uloga.equalsIgnoreCase("CLAN")){
            throw new NonClanRecievingNotification();
        }
        List<Notifikacija> notifikacije = notifikacijaRepository.findAllByKorisnikJmbgOrderByDatumDesc(jmbg);

        List<NotifikacijaResponseDto> dtos = new ArrayList<>();
        for (Notifikacija n : notifikacije) {
            dtos.add(mapirajUDto(n));
        }

        return dtos;
    }

    @Transactional
    public void oznaciKaoProcitanu(String token, Long id) {
        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        // Validacija da li je iopste clan
        String uloga = jwtService.extractRole(token);
        if(!uloga.equalsIgnoreCase("CLAN")){
            throw new NonClanGivingSuggestions();
        }

        // nadzi notifikaciju
        Notifikacija notifikacija = notifikacijaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notifikacija nije pronađena."));

        // Proveri jel ona vezana za ulogovanog korisnika
        if(!notifikacija.getKorisnik().getJmbg().equals(jmbg)){
            throw new RuntimeException("Nemate pristup ovoj notifikaciji.");
        }

        notifikacija.setProcitana(true);
        notifikacijaRepository.save(notifikacija);
    }

    public Integer brojNeprocitanih(String token) {
        // Validacija da li clan postoji
        String jmbg = jwtService.extractJmbg(token);
        User korisnik = userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen."));

        // Validacija da li je iopste clan
        String uloga = jwtService.extractRole(token);
        if(!uloga.equalsIgnoreCase("CLAN")){
            throw new RuntimeException("Samo clanovi biblioteke primaju notifikacije vezane za pracenje statusa predloga naslova");
        }

        Integer brNeprocitanih =  notifikacijaRepository.countByKorisnikJmbgAndProcitanaFalse(jmbg);
        return brNeprocitanih;
    }

    //pomocna funkcijicaa ~~~~
    private NotifikacijaResponseDto mapirajUDto(Notifikacija n) {
        NotifikacijaResponseDto dto = new NotifikacijaResponseDto();
        dto.setId(n.getId());
        dto.setPoruka(n.getPoruka());
        dto.setDatum(n.getDatum());
        dto.setProcitana(n.isProcitana());
        return dto;
    }
}
