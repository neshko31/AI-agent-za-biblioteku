package ftn.iis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZatvoriReklamaciju {
    @NotBlank(message = "Status je obavezan.")
    private String status;
}
