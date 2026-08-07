package ftn.iis.controller;

import ftn.iis.dto.KomentarRequestDto;
import ftn.iis.dto.KomentarResponseDto;
import ftn.iis.service.KomentarService;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knjiga/{isbn}/komentari")
public class KomentarController {
    private final KomentarService komentarService;
    private final JwtService jwtService;
    private static final int BEARER_PREFIX_LENGTH = 7;

    public KomentarController(KomentarService komentarService, JwtService jwtService) {
        this.komentarService = komentarService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<KomentarResponseDto>> getKomentari(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable String isbn) {
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
        return ResponseEntity.ok(komentarService.dohvatiKomentareZaKnjigu(isbn, jmbg));
    }

    @PostMapping
    public ResponseEntity<KomentarResponseDto> dodajKomentar(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable String isbn, @RequestBody KomentarRequestDto dto) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(komentarService.dodajKomentar(isbn, jmbg, dto));
    }

    @PostMapping("/{komentarId}/lajk")
    public ResponseEntity<KomentarResponseDto> lajkuj(@RequestHeader(value = "Authorization", required = false) String authHeader, @PathVariable String isbn, @PathVariable Long komentarId) {
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
        return ResponseEntity.ok(komentarService.lajkujKomentar(komentarId, jmbg));
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