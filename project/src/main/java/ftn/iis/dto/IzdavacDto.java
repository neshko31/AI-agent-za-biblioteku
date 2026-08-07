package ftn.iis.dto;

import ftn.iis.model.Izdavac;

public class IzdavacDto {
    private Long izdavacId;
    private String naziv;

    public IzdavacDto() {
    }

    public IzdavacDto(Long izdavacId, String naziv) {
        this.izdavacId = izdavacId;
        this.naziv = naziv;
    }

    public Long getIzdavacId() {
        return izdavacId;
    }

    public void setIzdavacId(Long izdavacId) {
        this.izdavacId = izdavacId;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public static IzdavacDto fromIzdavac(Izdavac izdavac) {
        return new IzdavacDto(izdavac.getId(), izdavac.getDobavljac().getNaziv());
    }
}
