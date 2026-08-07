package ftn.iis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KreirajNarudzbinuDto {

    @NotNull(message = "Dobavljač je obavezan.")
    private Long dobavljacId;

    @NotNull(message = "Ugovor je obavezan.")
    private Long ugovorId;

    private String napomena;

}