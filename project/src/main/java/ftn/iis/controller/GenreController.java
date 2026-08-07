package ftn.iis.controller;

import ftn.iis.model.Biblioteka;
import ftn.iis.model.Genre;
import ftn.iis.service.GenreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }
    @GetMapping
    public List<Genre> getAll() {
        return genreService.getAllGenres();
    }
}
