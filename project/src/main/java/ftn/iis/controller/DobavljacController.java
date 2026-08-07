package ftn.iis.controller;

import ftn.iis.dto.DobavljacDetaljniDto;
import ftn.iis.dto.DobavljacDto;
import ftn.iis.dto.DobavljacIzmenaDto;
import ftn.iis.dto.OsnovniDobavljacDto;
import ftn.iis.model.Dobavljac;
import ftn.iis.service.DobavljacService;
import org.aspectj.lang.annotation.DeclareError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dobavljaci")
public class DobavljacController {
    private final DobavljacService dobavljacService;

    public DobavljacController(DobavljacService dobavljacService){
        this.dobavljacService = dobavljacService;
    }

    @PostMapping("/unos")
    public ResponseEntity<DobavljacDetaljniDto> kreirajDobavljaca(@RequestHeader ("Authorization")
                                                   String authHeader, @RequestBody DobavljacDto dobavljacDto){
        String token = authHeader.substring(7);
        DobavljacDetaljniDto dobavljacDetaljni = dobavljacService.kreirajDobavljaca(token, dobavljacDto);
        return ResponseEntity.ok(dobavljacDetaljni);
    }

    @GetMapping("/prikaz-svih")
    public ResponseEntity<List<OsnovniDobavljacDto>> ispisiSve(@RequestHeader ("Authorization")
                                               String authHeader){
        String token = authHeader.substring(7);
        List<OsnovniDobavljacDto> dobavljaci = dobavljacService.ispisiSve(token);
        return ResponseEntity.ok(dobavljaci);
    }

    @GetMapping("/detaljan-prikaz/{id}")
    public ResponseEntity<DobavljacDetaljniDto> ispisiJednog(@RequestHeader ("Authorization")
                                       String authHeader, @PathVariable Long id){
        String token = authHeader.substring(7);
        DobavljacDetaljniDto dobavljacDetaljniDto = dobavljacService.ispisiJednog(token, id);
        return ResponseEntity.ok(dobavljacDetaljniDto);
    }

    @PatchMapping("/izmena/{id}")
    public ResponseEntity<DobavljacDetaljniDto> izmeni(@RequestHeader ("Authorization")
                                          String authHeader, @PathVariable Long id, @RequestBody DobavljacIzmenaDto dto){
        String token = authHeader.substring(7);
        DobavljacDetaljniDto dobavljacDetaljniDto = dobavljacService.izmeni(token, id, dto);
        return ResponseEntity.ok(dobavljacDetaljniDto);
    }

    @PatchMapping("/brisanje/{id}")
    public ResponseEntity<Void> obrisi(@RequestHeader ("Authorization") String authHeader, @PathVariable Long id){
        String token = authHeader.substring(7);
        dobavljacService.obrisi(token, id);
        return ResponseEntity.noContent().build();
    }
}
