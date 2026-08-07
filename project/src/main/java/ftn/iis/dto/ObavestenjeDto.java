package ftn.iis.dto;

import ftn.iis.model.Obavestenje;

import java.time.LocalDate;

public class ObavestenjeDto {
    private Long idO;
    private String tipO;
    private String tekstO;
    private LocalDate datKreiran;
    private boolean procitano;

    public static ObavestenjeDto fromObavestenje(Obavestenje o) {
        ObavestenjeDto dto = new ObavestenjeDto();
        dto.idO = o.getIdO();
        dto.tipO = o.getTipO();
        dto.tekstO = o.getTekstO();
        dto.datKreiran = o.getDatKreiran();
        dto.procitano = o.isProcitano();
        return dto;
    }


    public Long getIdO() {
        return idO;
    }

    public void setIdO(Long idO) {
        this.idO = idO;
    }

    public String getTipO() {
        return tipO;
    }

    public void setTipO(String tipO) {
        this.tipO = tipO;
    }

    public String getTekstO() {
        return tekstO;
    }

    public void setTekstO(String tekstO) {
        this.tekstO = tekstO;
    }

    public LocalDate getDatKreiran() {
        return datKreiran;
    }

    public void setDatKreiran(LocalDate datKreiran) {
        this.datKreiran = datKreiran;
    }

    public boolean isProcitano() {
        return procitano;
    }

    public void setProcitano(boolean procitano) {
        this.procitano = procitano;
    }
}
