package ftn.iis.controller;

import ftn.iis.dto.IzdavacDto;
import ftn.iis.dto.KnjigaOsnovnoDto;
import ftn.iis.service.IzdavacService;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/izdavaci")
public class IzdavacController {
    private final IzdavacService izdavacService;
    private final JwtService jwtService;

    public IzdavacController(IzdavacService izdavacService, JwtService jwtService) {
        this.izdavacService = izdavacService;
        this.jwtService = jwtService;
    }

    @GetMapping("/sve")
    public ResponseEntity<List<IzdavacDto>> ispisiSve(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "BIBLIOTEKAR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<IzdavacDto> rezultat = izdavacService.ispisiSveIzdavace();
        return ResponseEntity.ok(rezultat);
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

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
