package ftn.iis.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class PostaviBudzetDto {
    @NotNull(message = "Žanr je obavezan.")
    private Long zanrId;

    @NotNull(message = "Iznos je obavezan.")
    @DecimalMin(value = "0.0", message = "Budžet ne može biti negativan.")
    private Double ukupanBudzet;

    @NotNull(message = "Originalni budzet biblioteke je obavezan.")
    private Long budzetId;

    public Long getZanrId() {
        return zanrId;
    }

    public void setZanrId(Long zanrId) {
        this.zanrId = zanrId;
    }

    public Double getUkupanBudzet() {
        return ukupanBudzet;
    }

    public void setUkupanBudzet(Double ukupanBudzet) {
        this.ukupanBudzet = ukupanBudzet;
    }

    public Long getBudzetId() {
        return budzetId;
    }

    public void setBudzetId(Long budzetId) {
        this.budzetId = budzetId;
    }
}