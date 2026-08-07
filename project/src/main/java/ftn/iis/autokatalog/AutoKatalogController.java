package ftn.iis.autokatalog;

import ftn.iis.dto.NewBookDto;
import ftn.iis.marc.MarcMappingService;
import ftn.iis.marc.dto.MarcRecordDto;
import ftn.iis.model.Katalog;
import ftn.iis.model.Knjiga;
import ftn.iis.repository.KnjigaRepository;
import ftn.iis.service.KatalogService;
import ftn.iis.service.KnjigaService;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/knjiga/autokatalog")
public class AutoKatalogController {

    private static final int BEARER_PREFIX_LENGTH = 7;

    private final KnjigaRepository knjigaRepository;
    private final KnjigaService knjigaService;
    private final KatalogService katalogService;
    private final MarcMappingService marcMappingService;
    private final JwtService jwtService;

    public AutoKatalogController(KnjigaRepository knjigaRepository,
                                  KnjigaService knjigaService,
                                  KatalogService katalogService,
                                  MarcMappingService marcMappingService,
                                  JwtService jwtService) {
        this.knjigaRepository = knjigaRepository;
        this.knjigaService = knjigaService;
        this.katalogService = katalogService;
        this.marcMappingService = marcMappingService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<?> autokatalogizuj(@RequestHeader("Authorization") String authHeader,
                                              @RequestBody NewBookDto newBookDto) {
        String token = authHeader.substring(BEARER_PREFIX_LENGTH);
        String role = jwtService.extractRole(token);
        if (!role.equals("BIBLIOTEKAR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Jedino bibliotekar moze koristiti autokatalogizaciju");

        if (knjigaRepository.findByIsbn(newBookDto.getIsbn()).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Knjiga sa ovim ISBN-om vec postoji");

        Optional<Katalog> katalog = katalogService.getByKatId(newBookDto.getKatId());
        if (katalog == null || katalog.isEmpty())
            return ResponseEntity.badRequest().body("Katalog ne postoji");

        Knjiga knjiga = new Knjiga();
        knjiga.setIsbn(newBookDto.getIsbn());
        knjiga.setAutor(newBookDto.getAutor());
        knjiga.setNaslov(newBookDto.getNaziv());
        knjiga.setSinopsis(newBookDto.getSinopsis());
        knjiga.setKatalog(katalog.get());

        try {
            knjigaService.newBook(knjiga);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add new book");
        }

        MarcRecordDto marc = marcMappingService.buildRecord(newBookDto.getIsbn()).orElse(null);
        return ResponseEntity.ok(marc);
    }
}
