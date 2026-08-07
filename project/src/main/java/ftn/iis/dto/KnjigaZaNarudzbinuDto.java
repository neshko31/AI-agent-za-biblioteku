package ftn.iis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KnjigaZaNarudzbinuDto {

    private String isbn;          // null ako knjiga nije u bazi

    private Long predlogId;       // null za sistemsku preporuku

    private String naslov;

    private String autor;

    private Double okvirnaCena;

    private boolean sistemska;
}
