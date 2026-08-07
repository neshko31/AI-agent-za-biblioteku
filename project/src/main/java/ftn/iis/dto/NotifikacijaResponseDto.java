package ftn.iis.dto;
import java.time.LocalDateTime;

public class NotifikacijaResponseDto {
    private Long id;
    private String poruka;
    private LocalDateTime datum;
    private boolean procitana;

    public NotifikacijaResponseDto() {}

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }

    public void setDatum(LocalDateTime datum) {
        this.datum = datum;
    }

    public LocalDateTime getDatum() {
        return datum;
    }

    public boolean isProcitana() {
        return procitana;
    }

    public void setProcitana(boolean procitana) {
        this.procitana = procitana;
    }
}
