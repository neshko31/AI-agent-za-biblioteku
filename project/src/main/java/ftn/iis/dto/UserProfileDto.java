package ftn.iis.dto;

import ftn.iis.enums.Uloge;
import ftn.iis.enums.TipPretplate;
import ftn.iis.enums.TipKC;
import java.time.LocalDate;
import java.util.List;

public class UserProfileDto {
    //licni podaci
    private String jmbg;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String picturePath;
    private Uloge uloge;

    //biblioteka

    private String libraryName;

    //clanarina
    private TipPretplate tipPretplate;
    private TipKC kategorijaClana;
    private LocalDate datUplate;
    private LocalDate datIsteka;
    //zanrovi
    private List<String> favouriteGenres;

    public UserProfileDto(String jmbg, String firstName, String lastName, String email, String phone, LocalDate dateOfBirth, String picturePath, Uloge uloge, String libraryName, TipPretplate tipPretplate, TipKC kategorijaClana, LocalDate datUplate, LocalDate datIsteka, List<String> favouriteGenres) {
        this.jmbg = jmbg;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.picturePath = picturePath;
        this.uloge = uloge;
        this.libraryName = libraryName;
        this.tipPretplate = tipPretplate;
        this.kategorijaClana = kategorijaClana;
        this.datUplate = datUplate;
        this.datIsteka = datIsteka;
        this.favouriteGenres = favouriteGenres;
    }

    public UserProfileDto() {
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public Uloge getUloge() {
        return uloge;
    }

    public void setUloge(Uloge uloge) {
        this.uloge = uloge;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public TipPretplate getTipPretplate() {
        return tipPretplate;
    }

    public void setTipPretplate(TipPretplate tipPretplate) {
        this.tipPretplate = tipPretplate;
    }

    public TipKC getKategorijaClana() {
        return kategorijaClana;
    }

    public void setKategorijaClana(TipKC kategorijaClana) {
        this.kategorijaClana = kategorijaClana;
    }

    public LocalDate getDatUplate() {
        return datUplate;
    }

    public void setDatUplate(LocalDate datUplate) {
        this.datUplate = datUplate;
    }

    public LocalDate getDatIsteka() {
        return datIsteka;
    }

    public void setDatIsteka(LocalDate datIsteka) {
        this.datIsteka = datIsteka;
    }

    public List<String> getFavouriteGenres() {
        return favouriteGenres;
    }

    public void setFavouriteGenres(List<String> favouriteGenres) {
        this.favouriteGenres = favouriteGenres;
    }
}
