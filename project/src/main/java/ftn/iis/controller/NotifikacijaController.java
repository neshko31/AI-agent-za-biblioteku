package ftn.iis.controller;

import ftn.iis.dto.NotifikacijaResponseDto;
import ftn.iis.service.NotifikacijaService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/notifikacije")
public class NotifikacijaController {
    private final NotifikacijaService notifikacijaService;

    public NotifikacijaController(NotifikacijaService notifikacijaService){
        this.notifikacijaService = notifikacijaService;
    }

    @GetMapping("/moje")
    public ResponseEntity<List<NotifikacijaResponseDto>> mojeNotifikacije( @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(notifikacijaService.mojeNotifikacije(token));
    }

    @PatchMapping("/procitana/{id}")
    public ResponseEntity<Void> oznаciKaoProcitanu(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        String token = authHeader.substring(7);
        notifikacijaService.oznaciKaoProcitanu(token, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/broj-neprocitanih")
    public ResponseEntity<Integer> brojNeprocitanih( @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(notifikacijaService.brojNeprocitanih(token));
    }
}
