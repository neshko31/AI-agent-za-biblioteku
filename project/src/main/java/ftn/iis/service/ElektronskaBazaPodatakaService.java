package ftn.iis.service;

import ftn.iis.dto.ElektronskaBazaOsnovnoDto;
import ftn.iis.model.ElektronskaBazaPodataka;
import ftn.iis.model.PreuzimanjeBazePodataka;
import ftn.iis.model.User;
import ftn.iis.model.id.PreuzimanjeBazePodatakaId;
import ftn.iis.repository.ElektronskaBazaPodatakaRepository;
import ftn.iis.repository.PreuzimanjeBazePodatakaRepository;
import ftn.iis.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ElektronskaBazaPodatakaService {
    private final ElektronskaBazaPodatakaRepository bazaRepository;
    private final PreuzimanjeBazePodatakaRepository preuzimanjeRepository;
    private final UserRepository userRepository;

    public ElektronskaBazaPodatakaService(ElektronskaBazaPodatakaRepository bazaRepository,
                                          PreuzimanjeBazePodatakaRepository preuzimanjeRepository,
                                          UserRepository userRepository) {
        this.bazaRepository = bazaRepository;
        this.preuzimanjeRepository = preuzimanjeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ElektronskaBazaOsnovnoDto> ispisiSve() {
        return bazaRepository.findAll()
                .stream()
                .map(ElektronskaBazaOsnovnoDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ElektronskaBazaOsnovnoDto> pretraziPoNazivu(String query) {
        return bazaRepository.findByNazivContainingIgnoreCase(query)
                .stream()
                .map(ElektronskaBazaOsnovnoDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ElektronskaBazaOsnovnoDto> detalji(Long id) {
        return bazaRepository.findById(id)
                .map(ElektronskaBazaOsnovnoDto::fromEntity);
    }

    @Transactional
    public void sacuvajPreuzimanje(String jmbg, Long idBaze) {
        User clan = userRepository.findByJmbg(jmbg).orElse(null);
        ElektronskaBazaPodataka baza = bazaRepository.findById(idBaze).orElse(null);
        if (clan == null || baza == null) {
            return;
        }

        PreuzimanjeBazePodatakaId id = new PreuzimanjeBazePodatakaId(jmbg, idBaze, LocalDate.now());
        if (preuzimanjeRepository.existsById(id)) {
            return;
        }

        PreuzimanjeBazePodataka preuzimanje = new PreuzimanjeBazePodataka(id, clan, baza);
        preuzimanjeRepository.save(preuzimanje);
    }
}
