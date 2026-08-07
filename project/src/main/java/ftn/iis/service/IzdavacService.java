package ftn.iis.service;

import ftn.iis.dto.IzdavacDto;
import ftn.iis.dto.KnjigaOsnovnoDto;
import ftn.iis.model.Izdavac;
import ftn.iis.model.Knjiga;
import ftn.iis.repository.IzdavacRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IzdavacService {
    private final IzdavacRepository izdavacRepository;

    public IzdavacService(IzdavacRepository izdavacRepository) {
        this.izdavacRepository = izdavacRepository;
    }

    public List<IzdavacDto> ispisiSveIzdavace() {
        List<Izdavac> knjige = izdavacRepository.findAll();
        return knjige.stream().map(IzdavacDto::fromIzdavac).collect(Collectors.toList());
    }
}
