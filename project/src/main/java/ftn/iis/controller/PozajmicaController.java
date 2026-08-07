package ftn.iis.controller;

import ftn.iis.dto.ObavestenjeDto;
import ftn.iis.dto.PozajmicaDto;
import ftn.iis.dto.PozajmiceRezervacijeResponseDto;
import ftn.iis.dto.ProduzenjePozajmiceRequestDto;
import ftn.iis.service.PozajmicaService;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pozajmice")
public class PozajmicaController {
    private final PozajmicaService pozajmicaService;
    private final JwtService jwtService;
    private static final int BEARER_PREFIX_LENGTH = 7;

    public PozajmicaController(PozajmicaService pozajmicaService, JwtService jwtService) {
        this.pozajmicaService = pozajmicaService;
        this.jwtService = jwtService;
    }
    private String extractJmbg(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
            return jwtService.extractJmbg(authHeader.substring(BEARER_PREFIX_LENGTH));
        } catch (Exception e) {
            return null;
        }
    }
    @PostMapping("/pozajmi/{isbn}")
    public ResponseEntity<?> pozajmiFizicku(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String isbn) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Map<String, Object> result = pozajmicaService.borrowPhysicalBook(jmbg, isbn);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
    @PostMapping("/pozajmi-digitalno/{isbn}")
    public ResponseEntity<?> pozajmiDigitalno(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String isbn,
            @RequestParam(defaultValue = "ebook") String tip) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Map<String, Object> result = pozajmicaService.borrowDigital(jmbg, isbn, tip);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
    @GetMapping("/mozePozajmiti")
    public ResponseEntity<?> mozePozajmiti(@RequestHeader("Authorization") String authHeader) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean hasActive = pozajmicaService.userHasActiveOrOverdueLoan(jmbg);
        return ResponseEntity.ok(Map.of("moze", !hasActive, "imaAktivnih", hasActive));
    }
    @PostMapping("/rezervisi/{isbn}")
    public ResponseEntity<?> rezervisi(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String isbn) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Map<String, Object> result = pozajmicaService.makeReservation(jmbg, isbn);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
    @GetMapping("/moje")
    public ResponseEntity<PozajmiceRezervacijeResponseDto> getMoje(
            @RequestHeader("Authorization") String authHeader) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(pozajmicaService.getPozajmiceAndRezervacije(jmbg));
    }
    @PostMapping("/produzenje/{idP}")
    public ResponseEntity<?> produzenjePozajmice(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long idP) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Map<String, Object> result = pozajmicaService.requestExtension(idP, jmbg);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
    @PostMapping("/izgubljena/{idP}")
    public ResponseEntity<?> izgubljenjaKnjiga(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long idP) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Map<String, Object> result = pozajmicaService.reportLostBook(idP, jmbg);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
    @PostMapping("/iz-rezervacije/{idR}")
    public ResponseEntity<?> pozajmiIzRezervacije(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long idR) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Map<String, Object> result = pozajmicaService.borrowFromReservation(idR, jmbg);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
    @GetMapping("/dostupno/{isbn}")
    public ResponseEntity<?> getDostupno(@PathVariable String isbn) {
        int count = pozajmicaService.getAvailableCopiesCount(isbn);
        return ResponseEntity.ok(Map.of("dostupno", count > 0, "brojDostupnih", count));
    }
    @GetMapping("/imam-pozajmicu/{isbn}")
    public ResponseEntity<?> imamAktivnuPozajmicu(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String isbn) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean hasLoan = pozajmicaService.userHasActiveLoanForBook(jmbg, isbn);
        return ResponseEntity.ok(Map.of("imaPozajmicu", hasLoan));
    }

    @GetMapping("/obavestenja")
    public ResponseEntity<List<ObavestenjeDto>> getObavestenja(
            @RequestHeader("Authorization") String authHeader) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(pozajmicaService.getObavestenja(jmbg));
    }
    @PutMapping("/obavestenja/{idO}/procitano")
    public ResponseEntity<?> markObavestenjeRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long idO) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        pozajmicaService.markObavestenjeRead(idO, jmbg);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/obavestenja/{idO}")
    public ResponseEntity<?> deleteObavestenje(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long idO) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        pozajmicaService.deleteObavestenje(idO, jmbg);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/produzenja/na-cekanju")
    public ResponseEntity<List<ProduzenjePozajmiceRequestDto>> getProduzenjaNaCekanju(
            @RequestHeader("Authorization") String authHeader) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(pozajmicaService.getPendingExtensions());
    }
    @GetMapping("/sve-aktivne")
    public ResponseEntity<List<PozajmicaDto>> getSveAktivne(
            @RequestHeader("Authorization") String authHeader) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(pozajmicaService.getAllActivePozajmice());
    }

    @PostMapping("/vrati/{idP}")
    public ResponseEntity<?> vratiKnjigu(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long idP) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Map<String, Object> result = pozajmicaService.returnBookBibliotekar(idP);
        return Boolean.TRUE.equals(result.get("success"))
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/produzenja/{idPP}/obradi")
    public ResponseEntity<?> obradiProduzenje(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long idPP,
            @RequestParam boolean approve,
            @RequestParam(required = false, defaultValue = "") String razlog) {
        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Map<String, Object> result = pozajmicaService.processExtension(idPP, jmbg, approve, razlog);
        return Boolean.TRUE.equals(result.get("success"))
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

}
