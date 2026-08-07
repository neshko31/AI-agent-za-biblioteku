package ftn.iis.controller;

import ftn.iis.dto.AktivnostiIzvestajResponseDto;
import ftn.iis.service.IzvestajAktivnostiService;
import ftn.iis.service.IzvestajService;
import ftn.iis.utils.JwtService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;

@RestController
@RequestMapping("/api/izvestaj")
public class IzvestajController {
    private static final Set<String> DOZVOLJENE_ULOGE = Set.of("MENADZER", "BIBLIOTEKAR", "ADMINISTRATOR");
    private static final int BEARER_LEN = 7;

    private final IzvestajService izvestajService;
    private final IzvestajAktivnostiService izvestajAktivnostiService;
    private final JwtService jwtService;

    public IzvestajController(IzvestajService izvestajService, IzvestajAktivnostiService izvestajAktivnostiService, JwtService jwtService) {
        this.izvestajService = izvestajService;
        this.izvestajAktivnostiService = izvestajAktivnostiService;
        this.jwtService = jwtService;
    }

    @GetMapping("aktivnosti")
    public ResponseEntity<?> generisiIzvestajTeodora(@RequestHeader("Authorization") String authHeader,
    @RequestParam
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate od,
    @RequestParam("do") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datDo){

        try{
            String token = authHeader.substring(BEARER_LEN);
            String rola  = jwtService.extractRole(token);

            if (!DOZVOLJENE_ULOGE.contains(rola)) {
                return ResponseEntity.status(403).body("Nemate pravo pristupa izvestaju.");
            }
            if (od.isAfter(datDo)) {
                return ResponseEntity.badRequest().body("Datum 'od' mora biti pre datuma 'do'.");
            }
            byte[] pdf = izvestajService.generisiIzvestaj(od, datDo);
            String filename = "izvestaj-aktivnosti-"
                    + od.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                    + "-"
                    + datDo.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                    + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("Greska pri generisanju izvestaja: " + e.getMessage());
        }
    }
  
  @GetMapping("fond")
    public ResponseEntity<?> generisiIzvestajFonda(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate od,
            @RequestParam("do") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datDo) {

        try {
            String token = authHeader.substring(BEARER_LEN);
            String rola  = jwtService.extractRole(token);

            if (!DOZVOLJENE_ULOGE.contains(rola)) {    
              return ResponseEntity.status(403).body("Nemate pravo pristupa izvestaju.");
            }
            if (od.isAfter(datDo)) {
                return ResponseEntity.badRequest().body("Datum 'od' mora biti pre datuma 'do'.");
            }
          byte[] pdf = izvestajService.generisiIzvestajFonda(od, datDo);
            String filename = "izvestaj-fonda-"
                    + od.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                    + "-"
                    + datDo.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
                    + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Greska pri generisanju izvestaja: " + e.getMessage());
        }
     }


    @GetMapping("/nabavka")
    public ResponseEntity<?> generisiIzvestajNabavka( @RequestHeader("Authorization") String authHeader, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate od,
                                                      @RequestParam("do") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datDo) {

        try {
            String token = authHeader.substring(BEARER_LEN);
            String rola = jwtService.extractRole(token);

            if (!rola.equals("MENADZER")) {
                return ResponseEntity.status(403).body("Nemate pravo pristupa izvestaju.");
            }
            if (od.isAfter(datDo)) {
                return ResponseEntity.badRequest().body("Datum 'od' mora biti pre datuma 'do'.");
            }

            byte[] pdf = izvestajService.generisiIzvestajNabavka(od, datDo);
            String fileName = "izvestaj-nabavka-" + od.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + "-" + datDo.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf";

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Greska pri generisanju izvestaja: " + e.getMessage());
        }

    }

    @GetMapping("/ai-asistent")
    public ResponseEntity<?> generisiIzvestajKoriscenjaAIAsistenta(@RequestHeader(value = "Authorization") String authHeader,
                                                                   @RequestParam("datumOd") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datumOd,
                                                                   @RequestParam("datumDo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datumDo) {
        try {
            String token = authHeader.substring(BEARER_LEN);
            String uloga = jwtService.extractRole(token);

            if (!DOZVOLJENE_ULOGE.contains(uloga)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nemate pravo pristupa izveštaju.");
            }
            if (datumOd.isAfter(datumDo)) {
                return ResponseEntity.badRequest().body("Početni datum mora biti pre krajnjeg datuma prilikom generisanja izveštaja");
            }

            byte[] pdf = izvestajService.generisiIzvestajKoriscenjaAIAsistenta(datumOd, datumDo);
            String fileName = "izvestaj-koriscenje-ai-asistenta-" + datumOd.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + "-" + datumDo.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf";

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").contentType(MediaType.APPLICATION_PDF).body(pdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Greška pri generisanju izveštaja o korišćenju AI asistenta: " + e.getMessage());

        }
    }

    @GetMapping("/aktivnosti-po-mesecima")
    public ResponseEntity<?> generisiIzvestajAktivnostiPoMesecima(@RequestHeader("Authorization") String authHeader,
                                                                  @RequestParam("godina") int godina) {
        try {
            String token = authHeader.substring(BEARER_LEN);
            String uloga = jwtService.extractRole(token);

            if (!DOZVOLJENE_ULOGE.contains(uloga)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nemate pravo pristupa izveštaju.");
            }

            AktivnostiIzvestajResponseDto izvestaj = izvestajAktivnostiService.generisiIzvestajAktivnosti(godina);
            return ResponseEntity.ok(izvestaj);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Greška pri generisanju izveštaja o aktivnostima po mesecima: " + e.getMessage());
        }
    }
}
