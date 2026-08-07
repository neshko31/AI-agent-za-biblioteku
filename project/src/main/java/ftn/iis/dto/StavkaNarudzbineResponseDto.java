package ftn.iis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StavkaNarudzbineResponseDto {
    private Long id;
    private String isbn;
    private String naslov;
    private String autor;
    private Integer kolicina;
    private Double cenaPoKomadu;
    private Double ukupnaCenaStavke;
    private Long predlogId;
    private Long preporukaId;
}
