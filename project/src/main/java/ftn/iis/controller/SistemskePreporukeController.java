package ftn.iis.controller;

import ftn.iis.dto.KnjigaZaNarudzbinuDto;
import ftn.iis.dto.PrihvatiSistemskuPreporukuDto;
import ftn.iis.dto.SistemskaPreporukaResponseDto;
import ftn.iis.enums.StatusSistemskePreporuke;
import ftn.iis.enums.Uloge;
import ftn.iis.service.SistemskePreporukeService;
import ftn.iis.utils.JwtService;
import io.jsonwebtoken.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("api/sistemske-preporuke")
public class SistemskePreporukeController {
    private final SistemskePreporukeService sistemskePreporukeService;
    private final JwtService jwtService;

    public SistemskePreporukeController(SistemskePreporukeService sistemskePreporukeService, JwtService jwtService){
        this.sistemskePreporukeService = sistemskePreporukeService;
        this.jwtService = jwtService;
    }

    //rucno pokretanje
    @PostMapping("/pokreni-analizu")
    public ResponseEntity<?> pokreniAnalizu(@RequestHeader("Authorization") String authHeader ){
        String token = authHeader.substring(7);
        String role = jwtService.extractRole(token);
        if(!role.equalsIgnoreCase("MENADZER")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Jedino menadzer moze da pokrece analizu trendova.");
        }
        sistemskePreporukeService.generisiPreporuke();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/aktivne")
    public ResponseEntity<List<SistemskaPreporukaResponseDto>> pribaviAktivne(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(sistemskePreporukeService.pribaviAktivne(token));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SistemskaPreporukaResponseDto> azurirajStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam String status,
            @RequestBody(required = false) PrihvatiSistemskuPreporukuDto dto) {
        String token = authHeader.substring(7);
        StatusSistemskePreporuke noviStatus = StatusSistemskePreporuke.valueOf(status.toUpperCase());
        return ResponseEntity.ok(sistemskePreporukeService.azurirajStatus(id, noviStatus, token, dto));
    }

    @GetMapping("/za-narudzbinu")
    public ResponseEntity<List<KnjigaZaNarudzbinuDto>> getPrihvacenePreporuke(@RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        return ResponseEntity.ok(sistemskePreporukeService.preporukeZaNarudzbinu(token));
    }

}
