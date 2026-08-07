package ftn.iis.controller;

import ftn.iis.dto.*;
import ftn.iis.enums.NacinUplate;
import ftn.iis.enums.TipPretplate;
import ftn.iis.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody Login request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/register/step1")
    public ResponseEntity<AuthResponse> registerStep1(
            @Valid @RequestBody Step1R request) {
        return ResponseEntity.ok(authService.registerStep1(request));
    }
    @PostMapping("/register/step2/{jmbg}")
    public ResponseEntity<Void> registerStep2(
            @PathVariable String jmbg,
            @Valid @RequestBody Step2R request) {
        authService.registerStep2(jmbg, request);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/register/step3/{jmbg}")
    public ResponseEntity<AuthResponse> registerStep3(
            @PathVariable String jmbg,
            @Valid @RequestBody Step3R request) {
        return ResponseEntity.ok(authService.registerStep3(jmbg, request));
    }
    @PostMapping("/register/genres/{jmbg}")
    public ResponseEntity<Void> saveFavouriteGenres(
            @PathVariable String jmbg,
            @RequestBody OmiljeniZanrovi request) {
        authService.saveFavouriteGenres(jmbg, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("renew/{jmbg}")
    public ResponseEntity<Void> renewMembership( @PathVariable String jmbg,
                                                 @RequestParam NacinUplate nacinUplate,
                                                 @RequestParam TipPretplate tipPretplate){
        authService.renewMembership(jmbg,nacinUplate,tipPretplate);
        return ResponseEntity.ok().build();
    }


}
