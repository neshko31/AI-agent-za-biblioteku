package ftn.iis.service;

import ftn.iis.dto.OmiljeniZanrovi;
import ftn.iis.dto.UpdateProfilDto;
import ftn.iis.dto.UserProfileDto;
import ftn.iis.model.User;
import ftn.iis.model.Genre;
import ftn.iis.repository.GenreRepository;
import ftn.iis.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GenreRepository genreRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, GenreRepository genreRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.genreRepository = genreRepository;
    }

    private UserProfileDto toDto(User u) {
        return new UserProfileDto(
                u.getJmbg(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getPhone(),
                u.getDateOfBirth(),
                u.getPicturePath(),
                u.getUloge(),
                u.getBiblioteka() != null ? u.getBiblioteka().getName() : null,
                u.getTipPretplate(),
                u.getKategorijaClana() != null ? u.getKategorijaClana().getTipKC() : null,
                u.getClanarina() != null ? u.getClanarina().getDatUplate() : null,
                u.getClanarina() != null ? u.getClanarina().getDatIsteka() : null,
                u.getFavouriteGenres().stream().map(Genre::getName).toList()
        );
    }
    private User getUserByJmbg(String jmbg) {
        return userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
    }

    public Optional<User> getUserByJmbg2(String jmbg) {
        return userRepository.findById(jmbg);
    }

    public UserProfileDto getProfile(String jmbg){
        User user=getUserByJmbg(jmbg);
        return toDto(user);
    }
    public UserProfileDto getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
        return toDto(user);
    }
    @Transactional
    public UserProfileDto updateProfile(String jmbg, UpdateProfilDto req) {
        User user = getUserByJmbg(jmbg);

        if (req.getFirstName() != null)  user.setFirstName(req.getFirstName());
        if (req.getLastName()  != null)  user.setLastName(req.getLastName());
        if (req.getPhone()     != null)  user.setPhone(req.getPhone());
        if (req.getEmail()     != null)  user.setEmail(req.getEmail());
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        }

        userRepository.save(user);
        return toDto(user);
    }
    @Transactional
    public void updateFavouriteGenres(String jmbg, OmiljeniZanrovi req) {
        User user = getUserByJmbg(jmbg);
        List<Genre> genres = req.getGenreIds() == null
                ? new ArrayList<>()
                : genreRepository.findAllById(req.getGenreIds());
        user.setFavouriteGenres(genres);
        userRepository.save(user);
    }

}
