package ftn.iis.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class PrihvatiSistemskuPreporukuDto {
    @NotNull(message = "Okvirna cena je obavezna.")
    @DecimalMin(value = "0.01", message = "Cena mora biti veća od 0.")
    private Double okvirnaCena;

    public Double getOkvirnaCena() { return okvirnaCena; }
    public void setOkvirnaCena(Double okvirnaCena) { this.okvirnaCena = okvirnaCena; }
}