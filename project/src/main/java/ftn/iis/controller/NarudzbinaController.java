package ftn.iis.controller;

import ftn.iis.dto.DodajStavkuDto;
import ftn.iis.dto.EvidentirajIsporukuDto;
import ftn.iis.dto.KreirajNarudzbinuDto;
import ftn.iis.dto.NarudzbinaResponseDto;
import ftn.iis.repository.DobavljacRepository;
import ftn.iis.repository.FizickaKnjigaRepository;
import ftn.iis.repository.StavkaNarudzbineRepository;
import ftn.iis.repository.UgovorRepository;
import ftn.iis.service.BudzetService;
import ftn.iis.service.NarudzbinaService;
import ftn.iis.utils.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/narudzbine")
public class NarudzbinaController {
    private final NarudzbinaService narudzbinaService;

    public NarudzbinaController(NarudzbinaService narudzbinaService) {
        this.narudzbinaService = narudzbinaService;
    }

    @PostMapping("/kreiraj")
    public ResponseEntity<NarudzbinaResponseDto> kreiraj( @RequestHeader("Authorization") String authHeader, 
                                                          @RequestBody @Valid KreirajNarudzbinuDto dto) {
        return ResponseEntity.ok(narudzbinaService.kreirajNarudzbinu(authHeader.substring(7), dto));
    }

    @PostMapping("/{id}/stavke")
    public ResponseEntity<NarudzbinaResponseDto> dodajStavku( @RequestHeader("Authorization") String authHeader,
                                                              @PathVariable Long id,
                                                              @RequestBody @Valid DodajStavkuDto dto) {
        return ResponseEntity.ok(narudzbinaService.dodajStavku(authHeader.substring(7), id, dto));
    }

    @DeleteMapping("/{narudzbinaId}/stavke/{stavkaId}")
    public ResponseEntity<NarudzbinaResponseDto> ukloniStavku( @RequestHeader("Authorization") String authHeader,
                                                               @PathVariable Long narudzbinaId,
                                                               @PathVariable Long stavkaId) {
        return ResponseEntity.ok(narudzbinaService.ukloniStavku(authHeader.substring(7), narudzbinaId, stavkaId));
    }

    @PatchMapping("/{id}/potvrdi")
    public ResponseEntity<NarudzbinaResponseDto> potvrdi( @RequestHeader("Authorization") String authHeader,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(narudzbinaService.potvrdiNarudzbinu(authHeader.substring(7), id));
    }

    @GetMapping("/sve")
    public ResponseEntity<List<NarudzbinaResponseDto>> getSve( @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(narudzbinaService.getSveNarudzbine(authHeader.substring(7)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NarudzbinaResponseDto> getJednaNarudzbina( @RequestHeader("Authorization") String authHeader,
                                                           @PathVariable Long id) {
        return ResponseEntity.ok(narudzbinaService.getJednaNarudzbina(authHeader.substring(7), id));
    }

    @PatchMapping("/{id}/isporuka")
    public ResponseEntity<NarudzbinaResponseDto> evidentirajIsporuku(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid EvidentirajIsporukuDto dto) {
        return ResponseEntity.ok(narudzbinaService.evidentirajIsporuku(authHeader.substring(7), id, dto));
    }


}
