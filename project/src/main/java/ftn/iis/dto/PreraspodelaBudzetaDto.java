package ftn.iis.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class PreraspodelaBudzetaDto {
    @NotNull(message = "Izvorni žanr je obavezan.")
    private Long izvorZanrId;

    @NotNull(message = "Odredišni žanr je obavezan.")
    private Long odredisteZanrId;

    @NotNull(message = "Iznos je obavezan.")
    @DecimalMin(value = "0.01", message = "Iznos mora biti veći od 0.")
    private Double iznos;

    public Long getIzvorZanrId() {
        return izvorZanrId;
    }

    public void setIzvorZanrId(Long izvorZanrId) {
        this.izvorZanrId = izvorZanrId;
    }

    public Long getOdredisteZanrId() {
        return odredisteZanrId;
    }

    public void setOdredisteZanrId(Long odredisteZanrId) {
        this.odredisteZanrId = odredisteZanrId;
    }

    public Double getIznos() {
        return iznos;
    }

    public void setIznos(Double iznos) {
        this.iznos = iznos;
    }

}