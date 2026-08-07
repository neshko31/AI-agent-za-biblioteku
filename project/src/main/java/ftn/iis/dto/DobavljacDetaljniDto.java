package ftn.iis.dto;

import ftn.iis.enums.StatusDobavljaca;

public class DobavljacDetaljniDto {
    private Long id;
    private String naziv;
    private StatusDobavljaca status;
    private String email;
    private String tel;
    private String pib;
    private String tipDobavljaca; // "00", "01", "10", "11"
    private String urlOnlineProdavnice; // null ako nije knjižara

    public DobavljacDetaljniDto() {
    }

    public DobavljacDetaljniDto(Long id, String naziv, String email, String tel, String pib, String tipDobavljaca, String urlOnlineProdavnice, StatusDobavljaca status) {
        this.id = id;
        this.naziv = naziv;
        this.email = email;
        this.tel = tel;
        this.pib = pib;
        this.tipDobavljaca = tipDobavljaca;
        this.urlOnlineProdavnice = urlOnlineProdavnice;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTipDobavljaca() {
        return tipDobavljaca;
    }

    public void setTipDobavljaca(String tipDobavljaca) {
        this.tipDobavljaca = tipDobavljaca;
    }

    public String getUrlOnlineProdavnice() {
        return urlOnlineProdavnice;
    }

    public void setUrlOnlineProdavnice(String urlOnlineProdavnice) {
        this.urlOnlineProdavnice = urlOnlineProdavnice;
    }

    public StatusDobavljaca getStatus() {
        return status;
    }

    public void setStatus(StatusDobavljaca status) {
        this.status = status;
    }
}
