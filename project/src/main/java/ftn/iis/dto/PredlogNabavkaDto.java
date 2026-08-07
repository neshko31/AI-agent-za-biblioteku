package ftn.iis.dto;

import jakarta.validation.constraints.NotBlank;

public class PredlogNabavkaDto {
    @NotBlank(message = "Naslov je obavezan.")
    private String naslov;

    @NotBlank(message = "Autor je obavezan.")
    private String autor;

    public PredlogNabavkaDto() {
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }
}
