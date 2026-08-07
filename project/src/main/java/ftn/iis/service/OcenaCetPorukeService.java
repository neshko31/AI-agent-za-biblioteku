package ftn.iis.service;

import ftn.iis.dto.OcenaCetPorukeDto;
import ftn.iis.dto.OcenaCetPorukeOdgovorDto;
import ftn.iis.enums.TipCP;
import ftn.iis.model.CetPoruka;
import ftn.iis.model.OcenaCetPoruke;
import ftn.iis.model.User;
import ftn.iis.model.id.OcenaCetPorukeId;
import ftn.iis.repository.CetPorukaRepository;
import ftn.iis.repository.OcenaCetPorukeRepository;
import ftn.iis.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
public class OcenaCetPorukeService {

    private static final int MIN_OCENA = 1;
    private static final int MAX_OCENA = 5;

    private final OcenaCetPorukeRepository ocenaCetPorukeRepository;
    private final CetPorukaRepository cetPorukaRepository;
    private final UserRepository userRepository;

    public OcenaCetPorukeService(OcenaCetPorukeRepository ocenaCetPorukeRepository,
                                 CetPorukaRepository cetPorukaRepository,
                                 UserRepository userRepository) {
        this.ocenaCetPorukeRepository = ocenaCetPorukeRepository;
        this.cetPorukaRepository = cetPorukaRepository;
        this.userRepository = userRepository;
    }

    public Optional<OcenaCetPorukeOdgovorDto> getOcenaCetPoruke(String jmbg, Long idCetPoruke) {
        CetPoruka cetPoruka = cetPorukaRepository.findById(idCetPoruke)
                .orElseThrow(() -> new NoSuchElementException("Čet poruka ne postoji: " + idCetPoruke));

        if (!Objects.equals(cetPoruka.getCetSesija().getClan().getJmbg(), jmbg)) {
            throw new NoSuchElementException("Čet poruka ne postoji: " + idCetPoruke);
        }

        OcenaCetPorukeId id = new OcenaCetPorukeId(jmbg, idCetPoruke);
        return ocenaCetPorukeRepository.findById(id).map(OcenaCetPorukeOdgovorDto::fromOcenaCetPoruke);
    }

    @Transactional(rollbackFor = Exception.class)
    public Optional<OcenaCetPorukeOdgovorDto> ocenaCetPoruke(String jmbg, Long idCetPoruke, OcenaCetPorukeDto podaci) {
        CetPoruka cetPoruka = cetPorukaRepository.findById(idCetPoruke)
                .orElseThrow(() -> new NoSuchElementException("Čet poruka ne postoji: " + idCetPoruke));

        if (!Objects.equals(cetPoruka.getCetSesija().getClan().getJmbg(), jmbg)) {
            throw new NoSuchElementException("Čet poruka ne postoji: " + idCetPoruke);
        }

        if (cetPoruka.getTipCP() != TipCP.AI_ASISTENT) {
            throw new IllegalArgumentException("Samo odgovor agenta moze biti ocenjen!");
        }

        if (podaci.getOcenaCP() == null || podaci.getOcenaCP() < MIN_OCENA || podaci.getOcenaCP() > MAX_OCENA) {
            throw new IllegalArgumentException("Ocena mora biti broj od " + MIN_OCENA + " do " + MAX_OCENA + "!");
        }

        User clan = userRepository.findByJmbg(jmbg).orElseThrow();

        OcenaCetPorukeId id = new OcenaCetPorukeId(jmbg, idCetPoruke);
        OcenaCetPoruke ocena = ocenaCetPorukeRepository.findById(id).orElseGet(OcenaCetPoruke::new);

        // Bilo da je nova ocena ili izmena postojece
        ocena.setId(id);
        ocena.setClan(clan);
        ocena.setCetPoruka(cetPoruka);
        ocena.setOcenaCP(podaci.getOcenaCP());
        ocena.setKomentarCP(podaci.getKomentarCP());
        ocena.setDatumOcenjivanjaCS(LocalDateTime.now());

        ocena = ocenaCetPorukeRepository.saveAndFlush(ocena);

        return ocenaCetPorukeRepository.findById(ocena.getId()).map(OcenaCetPorukeOdgovorDto::fromOcenaCetPoruke);
    }
}