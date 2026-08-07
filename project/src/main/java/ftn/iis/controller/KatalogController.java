package ftn.iis.controller;

import ftn.iis.dto.KatalogDto;
import ftn.iis.model.Katalog;
import ftn.iis.model.User;
import ftn.iis.service.KatalogService;
import ftn.iis.service.UserService;
import ftn.iis.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/katalog")
public class KatalogController {
    private static final int BEARER_PREFIX_LENGTH = 7;
    private final KatalogService katalogService;
    private final JwtService jwtService;
    private final UserService userService;

    public KatalogController(KatalogService katalogService, JwtService jwtService, UserService userService) {
        this.katalogService = katalogService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/novi")
    public ResponseEntity<?> newCatalog(@RequestHeader("Authorization") String authHeader, @RequestBody KatalogDto katalogDto){
        String token = authHeader.substring(BEARER_PREFIX_LENGTH);
        String jmbg = jwtService.extractJmbg(token);
        String role = jwtService.extractRole(token);
        if(!role.equals("BIBLIOTEKAR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Jedino bibliotekar moze praviti kataloge");

        Optional<User> user = userService.getUserByJmbg2(jmbg);
        Katalog kat = new Katalog();

        kat.setKatIme(katalogDto.getNaziv());
        kat.setStandard(katalogDto.getStandard());
        kat.setBiblioteka(user.get().getBiblioteka());

        if(katalogService.addNewCatalog(kat))
            return ResponseEntity.ok(katalogDto);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add catalog");
    }

    @GetMapping("/all")
    public List<Katalog> allCatalogs(){
        return katalogService.getAll();
    }

    @GetMapping("/{id}")
    public Optional<Katalog> getCatalogById(@PathVariable Long id){
        return katalogService.getByKatId(id);
    }

    @PutMapping("delete/{id}")
    public ResponseEntity<?> deleteCatalog(@RequestHeader("Authorization") String authHeader, @PathVariable Long id){
        String token = authHeader.substring(BEARER_PREFIX_LENGTH);
        String jmbg = jwtService.extractJmbg(token);
        String role = jwtService.extractRole(token);
        if(!role.equals("BIBLIOTEKAR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Jedino bibliotekar moze brisati kataloge");
        if(!katalogService.deleteCatlog(id)){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete");
        }
        return ResponseEntity.ok("Deleted");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCatalogById(@RequestHeader("Authorization") String authHeader, @PathVariable Long id, @RequestBody KatalogDto katalogDto){
        String token = authHeader.substring(BEARER_PREFIX_LENGTH);
        String jmbg = jwtService.extractJmbg(token);
        String role = jwtService.extractRole(token);
        if(!role.equals("BIBLIOTEKAR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Jedino bibliotekar moze menjati kataloge");
        KatalogDto k = katalogService.updateCatalog(katalogDto, id);
        if(k==null)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update");
        return ResponseEntity.ok("Updated");
    }
}
