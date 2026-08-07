package ftn.iis.dto;

import jakarta.validation.constraints.NotBlank;

public class KatalogDto {
    @NotBlank(message = "Naziv kataloga je obavezan.")
    private String naziv;
    @NotBlank(message = "Standard mora biti zadat.")
    private String standard;

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public KatalogDto(String naziv, String standard) {
        this.naziv = naziv;
        this.standard = standard;
    }

    public KatalogDto() {
    }
}
