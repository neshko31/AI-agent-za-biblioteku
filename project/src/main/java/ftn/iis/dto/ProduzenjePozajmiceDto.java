package ftn.iis.dto;

import ftn.iis.model.ProduzenjePozajmice;

import java.time.LocalDate;

public class ProduzenjePozajmiceDto {
    private Long idPP;
    private String kanalPP;
    private Boolean statusPP;
    private String razlogOdb;
    private LocalDate datKrePP;
    private LocalDate datObrPP;
    private LocalDate stariDatVrac;
    private LocalDate noviDatVrac;

    public static ProduzenjePozajmiceDto fromProduzenje(ProduzenjePozajmice pp) {
        ProduzenjePozajmiceDto dto = new ProduzenjePozajmiceDto();
        dto.idPP = pp.getIdPP();
        dto.kanalPP = pp.getKanalPP();
        dto.statusPP = pp.getStatusPP();
        dto.razlogOdb = pp.getRazlogOdb();
        dto.datKrePP = pp.getDatKrePP();
        dto.datObrPP = pp.getDatObrPP();
        dto.stariDatVrac = pp.getStariDatVrac();
        dto.noviDatVrac = pp.getNoviDatVrac();
        return dto;
    }

    public Long getIdPP() {
        return idPP;
    }

    public void setIdPP(Long idPP) {
        this.idPP = idPP;
    }

    public String getKanalPP() {
        return kanalPP;
    }

    public void setKanalPP(String kanalPP) {
        this.kanalPP = kanalPP;
    }

    public Boolean getStatusPP() {
        return statusPP;
    }

    public void setStatusPP(Boolean statusPP) {
        this.statusPP = statusPP;
    }

    public String getRazlogOdb() {
        return razlogOdb;
    }

    public void setRazlogOdb(String razlogOdb) {
        this.razlogOdb = razlogOdb;
    }

    public LocalDate getDatKrePP() {
        return datKrePP;
    }

    public void setDatKrePP(LocalDate datKrePP) {
        this.datKrePP = datKrePP;
    }

    public LocalDate getDatObrPP() {
        return datObrPP;
    }

    public void setDatObrPP(LocalDate datObrPP) {
        this.datObrPP = datObrPP;
    }

    public LocalDate getStariDatVrac() {
        return stariDatVrac;
    }

    public void setStariDatVrac(LocalDate stariDatVrac) {
        this.stariDatVrac = stariDatVrac;
    }

    public LocalDate getNoviDatVrac() {
        return noviDatVrac;
    }

    public void setNoviDatVrac(LocalDate noviDatVrac) {
        this.noviDatVrac = noviDatVrac;
    }
}
