package ftn.iis.controller;

import ftn.iis.dto.AzurirajElektronskuBazuDto;
import ftn.iis.dto.ElektronskaBazaOsnovnoDto;
import ftn.iis.dto.NovaElektronskaBazaDto;
import ftn.iis.service.ElektronskaBazaPodatakaManagementService;
import ftn.iis.service.ElektronskaBazaPodatakaMediaService;
import ftn.iis.service.ElektronskaBazaPodatakaService;
import ftn.iis.utils.JwtService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/baze-podataka")
public class ElektronskaBazaPodatakaController {

    private static final int BEARER_PREFIX_LENGTH = 7;
    private static final List<String> ALLOWED_ZIP_TYPES = Arrays.asList("application/zip", "application/octet-stream", "application/x-zip", "application/x-zip-compressed", "multipart/x-zip");

    private final ElektronskaBazaPodatakaService bazaService;
    private final ElektronskaBazaPodatakaMediaService bazaMediaService;
    private final ElektronskaBazaPodatakaManagementService bazaManagementService;
    private final JwtService jwtService;

    public ElektronskaBazaPodatakaController(ElektronskaBazaPodatakaService bazaService,
                                             ElektronskaBazaPodatakaMediaService bazaMediaService,
                                             ElektronskaBazaPodatakaManagementService bazaManagementService,
                                             JwtService jwtService) {
        this.bazaService = bazaService;
        this.bazaMediaService = bazaMediaService;
        this.bazaManagementService = bazaManagementService;
        this.jwtService = jwtService;
    }

    @GetMapping("/sve/osnovno")
    public ResponseEntity<List<ElektronskaBazaOsnovnoDto>> ispisiSve(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN", "BIBLIOTEKAR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(bazaService.ispisiSve());
    }

    @GetMapping("/pretraga")
    public ResponseEntity<List<ElektronskaBazaOsnovnoDto>> pretrazi(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "q", required = false) String query) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN", "BIBLIOTEKAR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(bazaService.ispisiSve());
        }

        return ResponseEntity.ok(bazaService.pretraziPoNazivu(query));
    }

    @GetMapping("/detalji/{id}")
    public ResponseEntity<ElektronskaBazaOsnovnoDto> detalji(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        String role = safeExtractRole(authHeader);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!isRoleAllowed(role, "CLAN", "BIBLIOTEKAR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return bazaService.detalji(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/preuzmi")
    public ResponseEntity<Resource> preuzmi(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        String role = safeExtractRole(authHeader);
        String jmbg = safeExtractJmbg(authHeader);
        if (role == null || jmbg == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!role.equalsIgnoreCase("CLAN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return bazaMediaService.getZipPath(id)
                .map(path -> {
                    bazaService.sacuvajPreuzimanje(jmbg, id);
                    return fileResponse(path);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/nova", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> novaBaza(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("podaci") NovaElektronskaBazaDto podaci,
            @RequestPart("zip") MultipartFile zip) {
        String role = safeExtractRole(authHeader);
        if (!isRoleAllowed(role, "BIBLIOTEKAR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Jedino bibliotekar moze dodavati baze podataka");
        }

        String validationError = validateRequired(podaci);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        if (!isAllowedZipFile(zip)) {
            return ResponseEntity.badRequest().body("Zip fajl mora biti validan .zip");
        }

        try {
            bazaManagementService.kreiraj(podaci, zip);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Uspesno dodata baza podataka");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Greska prilikom dodavanja baze: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> azurirajBazu(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestPart("podaci") AzurirajElektronskuBazuDto podaci,
            @RequestPart(value = "zip", required = false) MultipartFile zip) {
        String role = safeExtractRole(authHeader);
        if (!isRoleAllowed(role, "BIBLIOTEKAR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Jedino bibliotekar moze azurirati baze podataka");
        }

        String validationError = validateRequired(podaci);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        if (zip != null && !zip.isEmpty() && !isAllowedZipFile(zip)) {
            return ResponseEntity.badRequest().body("Zip fajl mora biti validan .zip");
        }

        try {
            bazaManagementService.azuriraj(id, podaci, zip);
            return ResponseEntity.ok("Uspesno azurirana baza podataka");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Greska prilikom azuriranja baze: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> obrisiBazu(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String role = safeExtractRole(authHeader);
        if (!isRoleAllowed(role, "BIBLIOTEKAR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Jedino bibliotekar moze brisati baze podataka");
        }

        try {
            bazaManagementService.obrisi(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Greska prilikom brisanja baze: " + e.getMessage());
        }
    }

    private String validateRequired(NovaElektronskaBazaDto dto) {
        if (dto == null) {
            return "Podaci su obavezni";
        }
        if (isBlank(dto.getNaziv())) return "Naziv je obavezan";
        if (isBlank(dto.getOblast())) return "Oblast je obavezna";
        if (isBlank(dto.getLicenca())) return "Licenca je obavezna";
        if (isBlank(dto.getOpis())) return "Opis je obavezan";
        return null;
    }

    private String validateRequired(AzurirajElektronskuBazuDto dto) {
        if (dto == null) {
            return "Podaci su obavezni";
        }
        if (isBlank(dto.getNaziv())) return "Naziv je obavezan";
        if (isBlank(dto.getOblast())) return "Oblast je obavezna";
        if (isBlank(dto.getLicenca())) return "Licenca je obavezna";
        if (isBlank(dto.getOpis())) return "Opis je obavezan";
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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

    private boolean isAllowedZipFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String filename = file.getOriginalFilename();
        boolean hasZipExt = filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".zip");
        if (!hasZipExt) {
            return false;
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            return true;
        }
        return ALLOWED_ZIP_TYPES.contains(contentType.toLowerCase(Locale.ROOT));
    }

    private ResponseEntity<Resource> fileResponse(Path path) {
        MediaType mediaType = MediaTypeFactory.getMediaType(path.getFileName().toString())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + path.getFileName().toString() + "\"")
                .contentType(mediaType)
                .body(new FileSystemResource(path));
    }
}
