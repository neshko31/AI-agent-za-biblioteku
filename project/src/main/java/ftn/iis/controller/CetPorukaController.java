package ftn.iis.controller;

import ftn.iis.dto.CetSesijaDetaljnoDto;
import ftn.iis.dto.NovaCetPorukaDto;
import ftn.iis.dto.NovaCetPorukaOdgovorDto;
import ftn.iis.dto.OcenaCetPorukeDto;
import ftn.iis.exception.VektorskiServisException;
import ftn.iis.model.OcenaCetPoruke;
import ftn.iis.service.CetPorukaService;
import ftn.iis.service.OcenaCetPorukeService;
import ftn.iis.service.SlikaIzmena;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Set;

@RestController
@RequestMapping("/api/cet-poruka")
public class CetPorukaController {
    private final CetPorukaService cetPorukaService;
    private final OcenaCetPorukeService ocenaCetPorukeService;
    private final JwtService jwtService;
    private static final int BEARER_PREFIX_LENGTH = 7;
    private static final long MAX_VELICINA_SLIKE_BYTES = 10L * 1024 * 1024; // 10MB
    private static final Set<String> DOZVOLJENI_TIPOVI_SLIKE = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    public CetPorukaController(CetPorukaService cetPorukaService, OcenaCetPorukeService ocenaCetPorukeService, JwtService jwtService) {
        this.cetPorukaService = cetPorukaService;
        this.ocenaCetPorukeService = ocenaCetPorukeService;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/cet-sesija/{idCetSesije}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> postNovaCetPoruka(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idCetSesije,
            @RequestPart("podaci") NovaCetPorukaDto podaci,
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
            return ResponseEntity.badRequest().body("Poruka mora da ima sadržaj!");
        }
        if (slika == null || slika.isEmpty()) {
            return ResponseEntity.badRequest().body("SLIKA mora da ima sadržaj!");
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
            NovaCetPorukaOdgovorDto odgovor = cetPorukaService.postNovaCetPoruka(jmbg, idCetSesije, podaci.getSadrzajPoruke(), slikaBase64);
            return ResponseEntity.status(HttpStatus.CREATED).body(odgovor);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Greška prilikom slanja poruke: " + e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (VektorskiServisException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Agent trenutno nije dostupan: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom slanja poruke: " + e.getMessage());
        }
    }

    @GetMapping("/{idCetPoruke}/ocena")
    public ResponseEntity<?> getOcenaCetPoruke(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idCetPoruke) {

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
            return ocenaCetPorukeService.getOcenaCetPoruke(jmbg, idCetPoruke)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom dobavljanja ocene: " + e.getMessage());
        }
    }

    @PostMapping("/{idCetPoruke}/ocena")
    public ResponseEntity<?> postOcenaCetPoruke(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idCetPoruke,
            @RequestBody OcenaCetPorukeDto podaci) {

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
            return ResponseEntity.status(HttpStatus.CREATED).body(ocenaCetPorukeService.ocenaCetPoruke(jmbg, idCetPoruke, podaci).get());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom ocenjivanja poruke: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{idCetPoruke}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> azurirajCetPoruku(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long idCetPoruke,
            @RequestPart("podaci") NovaCetPorukaDto podaci,
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
            return ResponseEntity.badRequest().body("Poruka mora da ima sadržaj!");
        }

        String novaSlikaBase64;
        try {
            novaSlikaBase64 = ucitajSlikuKaoBase64(slika);
        } catch (NeispravnaSlikaException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom čitanja slike: " + e.getMessage());
        }

        if (novaSlikaBase64 != null && podaci.isUkloniSliku()) {
            return ResponseEntity.badRequest().body("Nije moguće istovremeno priložiti novu sliku i tražiti uklanjanje slike.");
        }

        // Tri moguća stanja za sliku kod editovanja, prosleđena servisu:
        //   - novaSlikaBase64 != null         -> slika se zamenjuje novom
        //   - podaci.isUkloniSliku() == true  -> slika se briše
        //   - ni jedno ni drugo                -> stara slika (ako postoji) ostaje nepromenjena
        SlikaIzmena slikaIzmena = new SlikaIzmena(novaSlikaBase64, podaci.isUkloniSliku());

        try {
            CetSesijaDetaljnoDto novaSesija = cetPorukaService.azurirajCetPoruku(jmbg, idCetPoruke, podaci.getSadrzajPoruke(), slikaIzmena);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaSesija);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (VektorskiServisException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Agent trenutno nije dostupan: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška prilikom ažuriranja poruke: " + e.getMessage());
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
