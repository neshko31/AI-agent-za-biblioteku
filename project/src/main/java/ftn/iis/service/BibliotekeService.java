package ftn.iis.service;

import ftn.iis.model.Biblioteka;
import ftn.iis.repository.BibliotekaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BibliotekeService {

    private final BibliotekaRepository bibliotekaRepository;

    public BibliotekeService(BibliotekaRepository bibliotekaRepository) {
        this.bibliotekaRepository = bibliotekaRepository;
    }
    public List<Biblioteka> getAllLibraries() {
        return bibliotekaRepository.findAll();
    }

}
