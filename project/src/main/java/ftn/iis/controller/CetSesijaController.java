package ftn.iis.controller;

import ftn.iis.dto.CetSesijaDetaljnoDto;
import ftn.iis.dto.CetSesijaOsnovnoDto;
import ftn.iis.dto.NovaCetSesijaDto;
import ftn.iis.service.CetPorukaService;
import ftn.iis.service.CetSesijaService;
import ftn.iis.service.OcenaCetPorukeService;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@RestController
@RequestMapping("/api/cet-sesija")
public class CetSesijaController {
    private final CetSesijaService cetSesijaService;
    private final CetPorukaService cetPorukaService;
    private final OcenaCetPorukeService ocenaCetPorukeService;
    private final JwtService jwtService;
    private static final int BEARER_PREFIX_LENGTH = 7;
    private static final long MAX_VELICINA_SLIKE_BYTES = 10L * 1024 * 1024; // 10MB
    private static final Set<String> DOZVOLJENI_TIPOVI_SLIKE = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    public CetSesijaController(CetSesijaService cetSesijaService, CetPorukaService cetPorukaService, OcenaCetPorukeService ocenaCetPorukeService, JwtService jwtService) {
        this.cetSesijaService = cetSesijaService;
        this.cetPorukaService = cetPorukaService;
        this.ocenaCetPorukeService = ocenaCetPorukeService;
        this.jwtService = jwtService;
    }

    @GetMapping("/sve")
    public ResponseEntity<List<CetSesijaOsnovnoDto>> ispisiSveCetSesije(@RequestHeader(value = "Authorization", required = false) String authHeader){
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String jmbg = safeExtractJmbg(authHeader);
        if (jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CetSesijaOsnovnoDto> rezultat = cetSesijaService.ispisiSveCetSesije(jmbg);
        return ResponseEntity.ok(rezultat);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CetSesijaDetaljnoDto> getCetSesijaPoId(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String jmbg = safeExtractJmbg(authHeader);
        if (jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return cetSesijaService.getCetSesijaPoId(jmbg, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/nova")
    public ResponseEntity<?> postNovaCetSesija(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("podaci") NovaCetSesijaDto podaci,
            @RequestPart(value = "slika", required = false) MultipartFile slika) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String jmbg = safeExtractJmbg(authHeader);
        if (jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (podaci.getSadrzajPoruke() == null || podaci.getSadrzajPoruke().isBlank()) {
            return ResponseEntity.badRequest().body("Prva poruka mora da ima sadržaj!");
        }
        if (podaci.getTipAgentaCS() == null) {
            return ResponseEntity.badRequest().body("Obavezan je izbor tipa agenta!");
        }

        String slikaBase64;
        try {
            slikaBase64 = ucitajSlikuKaoBase64(slika);
        } catch (NeispravnaSlikaException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom čitanja slike: " + e.getMessage());
        }

        try {
            CetSesijaDetaljnoDto cetSesijaDetaljnoDto = CetSesijaDetaljnoDto.fromCetSesija(cetSesijaService.postNovaCetSesija(jmbg, podaci, slikaBase64));
            return ResponseEntity.status(HttpStatus.CREATED).body(cetSesijaDetaljnoDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom kreiranja čet sesije: " + e.getMessage());
        }
    }

    // Arhivira čet sesiju. Sesija i dalje postoji i može se čitati, ali slanje novih poruka nije dozvoljeno dok se ne vrati iz arhive.
    // Automatski se briše čet sesija 30 dana od arhiviranja.
    // Brisanje se radi automatski, u ponoć svakog dana se to pokreće.
    @PatchMapping("/{id}/arhiviraj")
    public ResponseEntity<?> arhivirajCetSesiju(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable Long id) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String jmbg = safeExtractJmbg(authHeader);
        if (jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            CetSesijaDetaljnoDto rezultat = cetSesijaService.arhivirajCetSesiju(jmbg, id);
            return ResponseEntity.ok(rezultat);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Greška prilikom arhiviranja čet sesije: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom arhiviranja čet sesije: " + e.getMessage());
        }
    }

    // Vraća arhiviranu čet sesiju u normalan rad. Nakon vraćanja, korisnik može ponovo slati poruke.
    @PatchMapping("/{id}/vrati")
    public ResponseEntity<?> vratiCetSesijuIzArhive(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String jmbg = safeExtractJmbg(authHeader);
        if (jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            CetSesijaDetaljnoDto rezultat = cetSesijaService.vratiCetSesijuIzArhive(jmbg, id);
            return ResponseEntity.ok(rezultat);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Greška prilikom arhiviranja čet sesije: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom vraćanja čet sesije iz arhive: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCetSesija(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String jmbg = safeExtractJmbg(authHeader);
        if (jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            cetSesijaService.deleteCetSesija(jmbg, id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom brisanja čet sesije: " + e.getMessage());
        }
    }

    private String ucitajSlikuKaoBase64(MultipartFile slika) throws IOException, NeispravnaSlikaException {
        if (slika == null || slika.isEmpty()) {
            return null;
        }
        if (slika.getSize() > MAX_VELICINA_SLIKE_BYTES) {
            throw new NeispravnaSlikaException("Slika ne može biti veća od 5MB.");
        }
        String contentType = slika.getContentType();
        if (contentType == null || !DOZVOLJENI_TIPOVI_SLIKE.contains(contentType.toLowerCase())) {
            throw new NeispravnaSlikaException("Dozvoljene su samo JPEG, PNG i WebP slike.");
        }
        return Base64.getEncoder().encodeToString(slika.getBytes());
    }

    private static class NeispravnaSlikaException extends Exception {
        NeispravnaSlikaException(String message) {
            super(message);
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX_LENGTH);
    }

    private String safeExtractRole(String authHeader) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return null;
            }
            return jwtService.extractRole(token);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isRoleAllowed(String role, String... allowedRoles) {
        if (role == null) {
            return false;
        }
        for (String allowed : allowedRoles) {
            if (role.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }

    private String safeExtractJmbg(String authHeader) {
        try {
            String token = extractToken(authHeader);
            if (token == null) {
                return null;
            }
            return jwtService.extractJmbg(token);
        } catch (Exception e) {
            return null;
        }
    }
}
