package ftn.iis.service;

import ftn.iis.model.Genre;
import ftn.iis.model.KategorijaClana;
import ftn.iis.repository.KategorijaClanaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KategorijeClanaService {
    private final KategorijaClanaRepository kategorijaClanaRepository;

    public KategorijeClanaService(KategorijaClanaRepository kategorijaClanaRepository) {
        this.kategorijaClanaRepository = kategorijaClanaRepository;
    }
    public List<KategorijaClana> getAllKC() {
        return kategorijaClanaRepository.findAll();
    }

}
