package ftn.iis.service;

import ftn.iis.dto.*;
import ftn.iis.enums.NacinUplate;
import ftn.iis.enums.TipPretplate;
import ftn.iis.enums.Uloge;
import ftn.iis.model.*;
import ftn.iis.repository.*;
import ftn.iis.utils.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BibliotekaRepository bibliotekaRepository;
    private final KategorijaClanaRepository kategorijaClanaRepository;
    private final ClanarinaRepository clanarinaRepository;
    private final GenreRepository genreRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, BibliotekaRepository bibliotekaRepository, KategorijaClanaRepository kategorijaClanaRepository, ClanarinaRepository clanarinaRepository, GenreRepository genreRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.bibliotekaRepository = bibliotekaRepository;
        this.kategorijaClanaRepository = kategorijaClanaRepository;
        this.clanarinaRepository = clanarinaRepository;
        this.genreRepository = genreRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
    //logovanje korisnika
    public AuthResponse login(Login request){

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Pogrešan email ili lozinka");
        }
        User user= userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new RuntimeException("Korisnik nije pronadjen"));

        if(user.getUloge().equals(Uloge.CLAN)) {
            Clanarina clanarina = clanarinaRepository.findByUserJmbg(user.getJmbg())
                    .orElseThrow(() -> new RuntimeException("Članarina ne postoji"));

            if (!clanarina.isActive()) {
                throw new RuntimeException("Članarina je istekla");
            }
        }
        return buildAuthResponse(user);
    }
    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setJmbg(user.getJmbg());
        res.setEmail(user.getEmail());
        res.setRole(user.getUloge().name());
        res.setFirstName(user.getFirstName());
        res.setLastName(user.getLastName());
         if (user.getBiblioteka() != null) {
            res.setBid(user.getBiblioteka().getBid());
        }
        return res;
    }

    public AuthResponse registerStep1(Step1R req){
        if (userRepository.existsByJmbg(req.getJmbg())) {
            throw new RuntimeException("Korisnik sa ovim JMBG-om već postoji");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email adresa je već zauzeta");
        }
        Biblioteka biblioteka=bibliotekaRepository.findById(req.getLibraryBid())
                .orElseThrow(()->new RuntimeException("Biblioteka nije pronadjena"));
        User user = new User();

        user.setJmbg(req.getJmbg());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setUloge(Uloge.CLAN);
        user.setBiblioteka(biblioteka);
        user.setTipPretplate(req.getTipPretplate());
        user.setFavouriteGenres(new ArrayList<>());
    userRepository.save(user);
    return buildAuthResponse(user);
    }

    private User getUserByJmbg(String jmbg) {
        return userRepository.findById(jmbg)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
    }

    @Transactional
    public void  registerStep2(String jmbg, Step2R req){
        User user=getUserByJmbg(jmbg);

        KategorijaClana kategorija= kategorijaClanaRepository.findById(req.getKategorijaClanaId()).orElseThrow(()-> new RuntimeException("Kategorija clana nije pronadjena"));
        user.setKategorijaClana(kategorija);
        userRepository.save(user);
    }
    private LocalDate calcDatIsteka(LocalDate from, TipPretplate tip) {
        if (tip == TipPretplate.GODISNJA) {
            return from.plusYears(1);
        }
        return from.plusDays(30);
    }
    @Transactional
    public AuthResponse registerStep3(String jmbg, Step3R req){
        User user = getUserByJmbg(jmbg);

        if (user.getKategorijaClana() == null) {
            throw new RuntimeException("Molimo odaberite kategoriju pre uplate");
        }
        LocalDate datUplate  = LocalDate.now();
        LocalDate datIsteka  = calcDatIsteka(datUplate, user.getTipPretplate());
        LocalDate datBrisanja = datIsteka.plusDays(30);

        boolean isActive= true;
        Clanarina clanarina = new Clanarina();

        clanarina.setDatUplate(datUplate);
        clanarina.setDatIsteka(datIsteka);
        clanarina.setDatBrisanja(datBrisanja);
        clanarina.setActive(isActive);
        clanarina.setNacinUplate(req.getNacinUplate());
        clanarina.setUser(user);

        clanarinaRepository.save(clanarina);
        user.setClanarina(clanarina);

        return  buildAuthResponse(user);
    }
    @Transactional
    public void saveFavouriteGenres(String jmbg, OmiljeniZanrovi req) {
        User user = getUserByJmbg(jmbg);

        List<Genre> genres = req.getGenreIds() == null
                ? new ArrayList<>()
                : genreRepository.findAllById(req.getGenreIds());

        user.setFavouriteGenres(genres);
        userRepository.save(user);
    }

    //produzi clanarinu
    public void renewMembership(String jmbg, NacinUplate nacinUplate, TipPretplate tipPretplate){
        User user=getUserByJmbg(jmbg);
        Clanarina c=user.getClanarina();

        if (c == null) {
            throw new RuntimeException("Članarina nije pronađena");
        }
        LocalDate nesto;
        if (c.getDatIsteka().isAfter(LocalDate.now())) {
            nesto = c.getDatIsteka();
        } else {
            nesto = LocalDate.now();
        }

        LocalDate noviIstek= calcDatIsteka(nesto, tipPretplate);
        c.setDatUplate(LocalDate.now());
        c.setDatIsteka(noviIstek);
        c.setDatBrisanja(noviIstek.plusDays(30));
        c.setActive(true);
        c.setNacinUplate(nacinUplate);

        user.setTipPretplate(tipPretplate);
        clanarinaRepository.save(c); //updejtuje postojecu clanarinu
    }

}
