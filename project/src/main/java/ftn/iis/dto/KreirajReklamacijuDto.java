package ftn.iis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KreirajReklamacijuDto {

    @NotBlank(message = "Razlog je obavezan.")
    private String razlog;

    private String napomena;

}