package ftn.iis.controller;

import ftn.iis.model.Genre;
import ftn.iis.model.KategorijaClana;
import ftn.iis.service.KategorijeClanaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kategorije")
public class KategorijaClanaController {

    private final KategorijeClanaService kategorijeClanaService;

    public KategorijaClanaController(KategorijeClanaService kategorijeClanaService) {
        this.kategorijeClanaService = kategorijeClanaService;
    }
    @GetMapping
    public List<KategorijaClana> getAll() {
        return kategorijeClanaService.getAllKC();
    }
}
