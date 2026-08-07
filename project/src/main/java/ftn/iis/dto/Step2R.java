package ftn.iis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Step2R {

    @NotNull
    private Long kategorijaClanaId;

    public Long getKategorijaClanaId() {
        return kategorijaClanaId;
    }

    public void setKategorijaClanaId(Long kategorijaClanaId) {
        this.kategorijaClanaId = kategorijaClanaId;
    }
}
