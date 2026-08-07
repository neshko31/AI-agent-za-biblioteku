package ftn.iis.service;

import ftn.iis.enums.StatusCitanja;
import ftn.iis.enums.StatusSlusanja;
import ftn.iis.model.CitanjeEKnjige;
import ftn.iis.model.SlusanjeAudioKnjige;
import ftn.iis.model.User;
import ftn.iis.model.id.CitanjeEKnjigeId;
import ftn.iis.model.id.SlusanjeAudioKnjigeId;
import ftn.iis.repository.CitanjeEKnjigeRepository;
import ftn.iis.repository.SlusanjeAudioKnjigeRepository;
import ftn.iis.repository.AudioKnjigaRepository;
import ftn.iis.repository.EKnjigaRepository;
import ftn.iis.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class KnjigaProgressService {
    private final CitanjeEKnjigeRepository citanjeRepository;
    private final SlusanjeAudioKnjigeRepository slusanjeRepository;
    private final UserRepository userRepository;
    private final EKnjigaRepository eKnjigaRepository;
    private final AudioKnjigaRepository audioKnjigaRepository;

    public KnjigaProgressService(CitanjeEKnjigeRepository citanjeRepository, SlusanjeAudioKnjigeRepository slusanjeRepository, UserRepository userRepository, EKnjigaRepository eKnjigaRepository, AudioKnjigaRepository audioKnjigaRepository) {
        this.citanjeRepository = citanjeRepository;
        this.slusanjeRepository = slusanjeRepository;
        this.userRepository = userRepository;
        this.eKnjigaRepository = eKnjigaRepository;
        this.audioKnjigaRepository = audioKnjigaRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Integer> getCitanjeProgress(String jmbg, String isbn) {
        return citanjeRepository
                .findTopByIdJmbgClanaAndIdIsbnEKnjigeOrderByDatumPoslednjegPristupaDesc(jmbg, isbn)
                .map(CitanjeEKnjige::getTrenutnaStranica);
    }

    @Transactional(readOnly = true)
    public Optional<Integer> getSlusanjeProgress(String jmbg, String isbn) {
        return slusanjeRepository
                .findTopByIdJmbgClanaAndIdIsbnAudioKnjigeOrderByDatumPoslednjegPristupaDesc(jmbg, isbn)
                .map(SlusanjeAudioKnjige::getTrenutnaSekunda);
    }

    @Transactional
    public Optional<Integer> sacuvajCitanje(String jmbg, String isbn, Integer trenutnaStranica, boolean zavrseno) {
        if (trenutnaStranica == null) {
            return Optional.empty();
        }

        User clan = userRepository.findByJmbg(jmbg).orElse(null);
        var eKnjiga = eKnjigaRepository.findById(isbn).orElse(null);
        if (clan == null || eKnjiga == null) {
            return Optional.empty();
        }

        CitanjeEKnjige citanje = citanjeRepository
                .findTopByIdJmbgClanaAndIdIsbnEKnjigeOrderByDatumPoslednjegPristupaDesc(jmbg, isbn)
                .orElse(null);

        if (citanje == null) {
            citanje = new CitanjeEKnjige();
            citanje.setId(new CitanjeEKnjigeId(jmbg, isbn, LocalDate.now()));
            citanje.setClan(clan);
            citanje.seteKnjiga(eKnjiga);
            citanje.setStatusCitanja(StatusCitanja.U_TOKU);
        }

        citanje.setTrenutnaStranica(trenutnaStranica);
        citanje.setDatumPoslednjegPristupa(LocalDate.now());

        Integer ukupnoStrana = eKnjiga.getBrojStranaEK();
        boolean isFinished = zavrseno || (ukupnoStrana != null && trenutnaStranica >= ukupnoStrana);
        if (isFinished) {
            citanje.setStatusCitanja(StatusCitanja.ZAVRSENO);
            if (citanje.getDatumZavrsetka() == null) {
                citanje.setDatumZavrsetka(LocalDate.now());
            }
        } else if (citanje.getStatusCitanja() != StatusCitanja.ZAVRSENO) {
            citanje.setStatusCitanja(StatusCitanja.U_TOKU);
        }
        citanjeRepository.save(citanje);
        return Optional.of(trenutnaStranica);
    }

    @Transactional
    public Optional<Integer> sacuvajSlusanje(String jmbg, String isbn, Integer trenutnaSekunda, boolean zavrseno) {
        if (trenutnaSekunda == null) {
            return Optional.empty();
        }

        User clan = userRepository.findByJmbg(jmbg).orElse(null);
        var audioKnjiga = audioKnjigaRepository.findById(isbn).orElse(null);
        if (clan == null || audioKnjiga == null) {
            return Optional.empty();
        }

        SlusanjeAudioKnjige slusanje = slusanjeRepository
                .findTopByIdJmbgClanaAndIdIsbnAudioKnjigeOrderByDatumPoslednjegPristupaDesc(jmbg, isbn)
                .orElse(null);

        if (slusanje == null) {
            slusanje = new SlusanjeAudioKnjige();
            slusanje.setId(new SlusanjeAudioKnjigeId(jmbg, isbn, LocalDate.now()));
            slusanje.setClan(clan);
            slusanje.setAudioKnjiga(audioKnjiga);
            slusanje.setStatusSlusanja(StatusSlusanja.U_TOKU);
        }

        slusanje.setTrenutnaSekunda(trenutnaSekunda);
        slusanje.setDatumPoslednjegPristupa(LocalDate.now());

        Integer ukupnoSekundi = audioKnjiga.getTrajanjeSekundeAK();
        boolean isFinished = zavrseno || (ukupnoSekundi != null && trenutnaSekunda >= ukupnoSekundi);
        if (isFinished) {
            slusanje.setStatusSlusanja(StatusSlusanja.ZAVRSENO);
            if (slusanje.getDatumZavrsetka() == null) {
                slusanje.setDatumZavrsetka(LocalDate.now());
            }
        } else if (slusanje.getStatusSlusanja() != StatusSlusanja.ZAVRSENO) {
            slusanje.setStatusSlusanja(StatusSlusanja.U_TOKU);
        }
        slusanjeRepository.save(slusanje);
        return Optional.of(trenutnaSekunda);
    }
}
