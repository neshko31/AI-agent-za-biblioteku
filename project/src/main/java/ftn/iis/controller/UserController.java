package ftn.iis.controller;

import ftn.iis.dto.OmiljeniZanrovi;
import ftn.iis.dto.UpdateProfilDto;
import ftn.iis.dto.UserProfileDto;
import ftn.iis.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> getMyProfile(Principal principal){
        UserProfileDto profile=userService.getProfileByEmail(principal.getName());
        return ResponseEntity.ok(profile);
    }
    @PutMapping("/{jmbg}/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> updateProfile(
            @PathVariable String jmbg,
            @RequestBody UpdateProfilDto request) {
        return ResponseEntity.ok(userService.updateProfile(jmbg, request));
    }
    @PutMapping("/{jmbg}/genres")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateFavouriteGenres(
            @PathVariable String jmbg,
            @RequestBody OmiljeniZanrovi request) {
        userService.updateFavouriteGenres(jmbg, request);
        return ResponseEntity.ok().build();
    }

}
