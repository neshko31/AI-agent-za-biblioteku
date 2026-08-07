package ftn.iis.dto;

import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DobavljacDto {
    @NotBlank(message = "Naziv dobavljača je obavezan.")
    @Size(max = 255, message = "Naziv ne može biti duži od 255 karaktera.")
    private String naziv;

    @NotBlank(message = "Email je obavezan.")
    @Email(message = "Format email adrese nije validan.")
    private String email;

    @NotBlank(message = "Telefon je obavezan.")
    private String tel;

    @NotBlank(message = "PIB je obavezan.")
    @Pattern(regexp = "\\d+", message = "PIB mora sadržati samo cifre.")
    private String pib;

    @NotBlank(message = "Tip je obavezan.")
    private String tipDobavljaca;    // 00, 01, 10, 11

    private String urlOnlineProdavnice;

    public DobavljacDto() {
    }

    public DobavljacDto(String naziv, String email, String tel, String pib) {
        this.naziv = naziv;
        this.email = email;
        this.tel = tel;
        this.pib = pib;
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

    public String getUrlOnlineProdavnice(){
        return urlOnlineProdavnice;
    }

    public void setUrlOnlineProdavnice(String urlOnlineProdavnice){
        this.urlOnlineProdavnice = urlOnlineProdavnice;
    }

    public String getTipDobavljaca() {
        return tipDobavljaca;
    }

    public void setTipDobavljaca(String tipDobavljaca) {
        this.tipDobavljaca = tipDobavljaca;
    }
}
