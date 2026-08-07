package ftn.iis.dto;

import ftn.iis.enums.TipPretplate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class Step1R {
    @NotBlank
    @Size(min = 13, max = 13, message = "JMBG mora imati tačno 13 cifara")
    private String jmbg;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private LocalDate dateOfBirth;

    @Email(message = "Email nije u dobrom formatu")
    @NotBlank(message = "Email je obavezan")
    private String email;

    @NotBlank(message = "Telefon je obavezan")
    private String phone;

    @NotBlank @Size(min = 8, message = "Lozinka mora imati najmanje 8 znakova")
    private String password;

    @NotBlank
    private String libraryBid;

    @NotNull
    private TipPretplate tipPretplate;

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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLibraryBid() {
        return libraryBid;
    }

    public void setLibraryBid(String libraryBid) {
        this.libraryBid = libraryBid;
    }

    public TipPretplate getTipPretplate() {
        return tipPretplate;
    }

    public void setTipPretplate(TipPretplate tipPretplate) {
        this.tipPretplate = tipPretplate;
    }
}
