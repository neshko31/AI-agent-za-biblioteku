package ftn.iis.controller;

import ftn.iis.dto.KaznaDto;
import ftn.iis.enums.NacinUplate;
import ftn.iis.service.KaznaService;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kazne")
public class KaznaController {
    private final KaznaService kaznaService;
    private final JwtService jwtService;
    private static final int BEARER_PREFIX_LENGTH = 7;

    public KaznaController(KaznaService kaznaService, JwtService jwtService) {
        this.kaznaService = kaznaService;
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

    @GetMapping("/moje")
    public ResponseEntity<List<KaznaDto>> getMojeKazne(
            @RequestHeader("Authorization") String authHeader) {
        String jmbg= extractJmbg(authHeader);
        if(jmbg==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(kaznaService.getMojeKazne(jmbg));
    }

    @PostMapping("/{idK}/plati")
    public ResponseEntity<?> platiKaznu( @RequestHeader("Authorization") String authHeader,
                                         @PathVariable Long idK,
                                         @RequestParam NacinUplate nacinPlacanja){

        String jmbg = extractJmbg(authHeader);
        if (jmbg == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            KaznaDto dto = kaznaService.platiKaznu(idK, jmbg, nacinPlacanja);
            return ResponseEntity.ok(dto);

        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
