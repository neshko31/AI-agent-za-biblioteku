package ftn.iis.controller;

import ftn.iis.model.Biblioteka;
import ftn.iis.repository.BibliotekaRepository;
import ftn.iis.service.BibliotekeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/biblioteka")
public class BibliotekaController {
    private final BibliotekeService bibliotekeService;

    public BibliotekaController(BibliotekeService bibliotekeService) {
        this.bibliotekeService = bibliotekeService;
    }

    @GetMapping
    public List<Biblioteka> getAll() {
        return bibliotekeService.getAllLibraries();
    }
}
