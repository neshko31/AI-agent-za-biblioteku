package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import ftn.iis.enums.TipPretplate;
import ftn.iis.enums.Uloge;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
@Entity
@Table(name="korisnik")
public class User implements UserDetails {

    @Id
    @Column(name = "jmbg", length = 13)
    private String jmbg;

    @Column(name = "ImeK", nullable = false)
    private String firstName;

    @Column(name = "PrzK", nullable = false)
    private String lastName;

    @Column(name = "PutanjaSlike")
    private String picturePath;

    @Column(name = "DatRodj")
    private LocalDate dateOfBirth;

    @Column(name = "SifraK" ,nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "BrojT")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = " TipK ",nullable = false)
    private Uloge uloge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid")
    private Biblioteka biblioteka;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idkc")
    private KategorijaClana kategorijaClana;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_pretplate")
    private TipPretplate tipPretplate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_favourite_genres",
            joinColumns = @JoinColumn(name = "jmbg"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> favouriteGenres = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-clan")
    private Clanarina clanarina;

    @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-citanja")
    private List<CitanjeEKnjige> citanja = new ArrayList<>();

    @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SlusanjeAudioKnjige> slusanja = new ArrayList<>();

    @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PreuzimanjeBazePodataka> preuzeteBaze = new ArrayList<>();

    @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PristupBrojuCasopisa> pristupciBrojevimaCasopisa = new ArrayList<>();

    public User() {
    }

    public String getJmbg() {
        return jmbg;
    }

    public void setJmbg(String jmbg) {
        this.jmbg = jmbg;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Nullable
    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Uloge getUloge() {
        return uloge;
    }

    public void setUloge(Uloge uloge) {
        this.uloge = uloge;
    }

    public Biblioteka getBiblioteka() {
        return biblioteka;
    }

    public void setBiblioteka(Biblioteka biblioteka) {
        this.biblioteka = biblioteka;
    }

    public KategorijaClana getKategorijaClana() {
        return kategorijaClana;
    }

    public void setKategorijaClana(KategorijaClana kategorijaClana) {
        this.kategorijaClana = kategorijaClana;
    }

    public TipPretplate getTipPretplate() {
        return tipPretplate;
    }

    public void setTipPretplate(TipPretplate tipPretplate) {
        this.tipPretplate = tipPretplate;
    }

    public List<Genre> getFavouriteGenres() {
        return favouriteGenres;
    }

    public void setFavouriteGenres(List<Genre> favouriteGenres) {
        this.favouriteGenres = favouriteGenres;
    }

    public Clanarina getClanarina() {
        return clanarina;
    }

    public void setClanarina(Clanarina clanarina) {
        this.clanarina = clanarina;
    }

    public List<CitanjeEKnjige> getCitanja() {
        return citanja;
    }

    public void setCitanja(List<CitanjeEKnjige> citanja) {
        this.citanja = citanja;
    }

    public List<SlusanjeAudioKnjige> getSlusanja() {
        return slusanja;
    }

    public void setSlusanja(List<SlusanjeAudioKnjige> slusanja) {
        this.slusanja = slusanja;
    }

    public List<PreuzimanjeBazePodataka> getPreuzeteBaze() {
        return preuzeteBaze;
    }

    public void setPreuzeteBaze(List<PreuzimanjeBazePodataka> preuzeteBaze) {
        this.preuzeteBaze = preuzeteBaze;
    }

    public List<PristupBrojuCasopisa> getPristupciBrojevimaCasopisa() {
        return pristupciBrojevimaCasopisa;
    }

    public void setPristupciBrojevimaCasopisa(List<PristupBrojuCasopisa> pristupciBrojevimaCasopisa) {
        this.pristupciBrojevimaCasopisa = pristupciBrojevimaCasopisa;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + uloge.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired()  { return true; }
    @Override
    public boolean isAccountNonLocked()   { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled()            { return true; }
}
