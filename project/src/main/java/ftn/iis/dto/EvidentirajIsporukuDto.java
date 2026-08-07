package ftn.iis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EvidentirajIsporukuDto {

    @NotNull(message = "Datum isporuke je obavezan.")
    private LocalDate datumStvarneIsporuke;

}