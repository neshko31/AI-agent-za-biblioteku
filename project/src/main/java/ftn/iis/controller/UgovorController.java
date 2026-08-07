package ftn.iis.controller;

import ftn.iis.dto.UgovorDetaljniDto;
import ftn.iis.dto.UgovorDto;
import ftn.iis.model.Ugovor;
import ftn.iis.service.UgovorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ugovori")
public class UgovorController {
    private final UgovorService ugovorService;

    public UgovorController(UgovorService ugovorService){
        this.ugovorService = ugovorService;
    }

    @PostMapping("/kreiranje")
    public ResponseEntity<UgovorDetaljniDto> kreirajUgovor(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UgovorDto dto) {
        String token = authHeader.substring(7);
        UgovorDetaljniDto rezultat = ugovorService.kreirajUgovor(token, dto);
        return ResponseEntity.ok(rezultat);
    }

    @GetMapping("/ispisi-sve/{id}")
    public ResponseEntity<List<UgovorDto>> ispisiSveZaDobavljaca(@RequestHeader("Authorization") String authHeader,
                                                                         @PathVariable Long id){
        String token = authHeader.substring(7);
        List<UgovorDto> rezultat = ugovorService.ispisiSveZaDobavljaca(token, id);
        return ResponseEntity.ok(rezultat);
    }

    @PatchMapping("/raskid/{id}")
    public ResponseEntity<UgovorDetaljniDto> raskiniUgovor(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable Long id){
        String token = authHeader.substring(7);
        UgovorDetaljniDto rezultat = ugovorService.raskiniUgovor(token, id);
        return ResponseEntity.ok(rezultat);
    }

}
