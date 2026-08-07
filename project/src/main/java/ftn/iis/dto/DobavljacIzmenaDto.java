package ftn.iis.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DobavljacIzmenaDto {
    private String naziv;

    private String email;

    private String tel;

    private String pib;

    private String urlOnlineProdavnice;

    public DobavljacIzmenaDto() {
    }

    public DobavljacIzmenaDto(String naziv, String email, String tel, String pib, String urlOnlineProdavnice) {
        this.naziv = naziv;
        this.email = email;
        this.tel = tel;
        this.pib = pib;
        this.urlOnlineProdavnice = urlOnlineProdavnice;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPib() {
        return pib;
    }

    public void setPib(String pib) {
        this.pib = pib;
    }

    public String getUrlOnlineProdavnice() {
        return urlOnlineProdavnice;
    }

    public void setUrlOnlineProdavnice(String urlOnlineProdavnice) {
        this.urlOnlineProdavnice = urlOnlineProdavnice;
    }
}
