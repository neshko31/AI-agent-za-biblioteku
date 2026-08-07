package ftn.iis.dto;

import ftn.iis.enums.StatusNarudzbine;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class NarudzbinaResponseDto {
    private Long id;
    private String dobavljacNaziv;
    private Long dobavljacId;
    private Long ugovorId;
    private Double popust;
    private LocalDate datumKreiranja;
    private LocalDate datumOcekivaneIsporuke;
    private LocalDate datumStvarneIsporuke;
    private Double ukupnaCena;
    private StatusNarudzbine status;
    private String napomena;
    private List<StavkaNarudzbineResponseDto> stavke;

}
