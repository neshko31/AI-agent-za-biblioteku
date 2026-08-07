package ftn.iis.controller;

import ftn.iis.dto.*;
import ftn.iis.service.PredlogNabavkeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/predlozi")
public class PredlogNabavkaController {
    private final PredlogNabavkeService predlogNabavkeService;

    public PredlogNabavkaController(PredlogNabavkeService predlogNabavkeService){
        this.predlogNabavkeService = predlogNabavkeService;
    }

    @PostMapping("/kreiraj")
    public ResponseEntity<PredlogNabavkaResponseDto> kreirajPredlog(@RequestHeader("Authorization") String authHeader,
                                                                    @RequestBody PredlogNabavkaDto predlogNabavkaDto){
        String token = authHeader.substring(7);
        PredlogNabavkaResponseDto predlog = predlogNabavkeService.kreirajPredlog(token, predlogNabavkaDto);
        return ResponseEntity.ok(predlog);
    }

    @GetMapping("/moji-zahtevi")
    public ResponseEntity<List<PredlogNabavkaResponseDto>> mojiPredlozi(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        List<PredlogNabavkaResponseDto> predlozi = predlogNabavkeService.mojiPredlozi(authHeader.substring(7));
        return ResponseEntity.ok(predlozi);
    }

    @GetMapping("/odobreni")
    public ResponseEntity<List<PredlogNabavkaZaMenadzeraDto>> odobreniPredlozi(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(predlogNabavkeService.odobreniPredlozi(authHeader.substring(7)));
    }

    @GetMapping("/na-cekanju")
    public ResponseEntity<List<PredlogNabavkaResponseDto>> predloziNaCekanju(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(predlogNabavkeService.predloziNaCekanju(authHeader.substring(7)));
    }

    @PatchMapping("/obradi/{id}")
    public ResponseEntity<PredlogNabavkaResponseDto> obradiPredlog(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid ObradiPredlogDto dto) {
        return ResponseEntity.ok(predlogNabavkeService.obradiPredlog(authHeader.substring(7), id, dto));
    }

    @PutMapping("/{id}/obrada-menadzer")
    public ResponseEntity<?> obradiPredlogMenadzer(@RequestHeader("Authorization") String authHeader,
                                                   @PathVariable Long id, @RequestBody ObradaPredlogaMenadzerDto dto){
        String token = authHeader.substring(7);
        predlogNabavkeService.obradiPredlogMenadzer(token, id, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/za-narudzbinu")
    public ResponseEntity<List<KnjigaZaNarudzbinuDto>> getOdobrenePredloge(@RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        return ResponseEntity.ok(predlogNabavkeService.predloziZaNarudzbinu(token));
    }

}
