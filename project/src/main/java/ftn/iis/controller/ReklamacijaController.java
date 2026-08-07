package ftn.iis.controller;

import ftn.iis.dto.KreirajReklamacijuDto;
import ftn.iis.dto.ReklamacijaResponseDto;
import ftn.iis.dto.ZatvoriReklamacijuDto;
import ftn.iis.service.ReklamacijaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/reklamacije")
public class ReklamacijaController {
    private final ReklamacijaService reklamacijaService;

    public ReklamacijaController(ReklamacijaService reklamacijaService) {
        this.reklamacijaService = reklamacijaService;
    }

    @PostMapping("/narudzbina/{narudzbinId}")
    public ResponseEntity<ReklamacijaResponseDto> kreiraj( @RequestHeader("Authorization") String authHeader,
                                                           @PathVariable Long narudzbinId,
                                                           @RequestBody @Valid KreirajReklamacijuDto dto) {
        return ResponseEntity.ok(reklamacijaService.kreirajReklamaciju(
                authHeader.substring(7), narudzbinId, dto));
    }

    @GetMapping("/sve")
    public ResponseEntity<List<ReklamacijaResponseDto>> getSve(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(reklamacijaService.getSveReklamacije(authHeader.substring(7)));
    }

    @PatchMapping("/{id}/zatvori")
    public ResponseEntity<ReklamacijaResponseDto> zatvori(@RequestHeader("Authorization") String authHeader,
                                                         @PathVariable Long id,
                                                         @RequestBody @Valid ZatvoriReklamacijuDto dto) {
        return ResponseEntity.ok(reklamacijaService.zatvoriReklamaciju(authHeader.substring(7), id, dto));
    }
}
