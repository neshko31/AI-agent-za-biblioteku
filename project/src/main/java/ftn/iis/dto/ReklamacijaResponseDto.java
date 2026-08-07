package ftn.iis.dto;

import ftn.iis.enums.StatusReklamacije;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReklamacijaResponseDto {
    private Long id;
    private Long narudzbinId;
    private String dobavljacNaziv;
    private LocalDate datumPodnosenja;
    private String razlog;
    private String napomena;
    private StatusReklamacije status;
    private LocalDate datumZatvaranja;

}