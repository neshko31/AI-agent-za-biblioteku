package ftn.iis.dto;

import ftn.iis.model.ProduzenjePozajmice;

import java.time.LocalDate;

public class ProduzenjePozajmiceRequestDto {
    private Long idPP;
    private LocalDate datKrePP;
    private LocalDate stariDatVrac;
    private String naslovKnjige;
    private String autorKnjige;
    private String imeClan;
    private String jmbgClan;

    public static ProduzenjePozajmiceRequestDto from(ProduzenjePozajmice pp) {
        ProduzenjePozajmiceRequestDto dto = new ProduzenjePozajmiceRequestDto();
        dto.idPP = pp.getIdPP();
        dto.datKrePP = pp.getDatKrePP();
        dto.stariDatVrac = pp.getStariDatVrac();
        var knjiga = pp.getPozajmica().getPrimerakKnjige().getFizickaKnjiga().getKnjiga();
        dto.naslovKnjige = knjiga.getNaslov();
        dto.autorKnjige = knjiga.getAutor();
        var clan = pp.getPozajmica().getClan();
        dto.imeClan = clan.getFirstName() + " " + clan.getLastName();
        dto.jmbgClan = clan.getJmbg();
        return dto;
    }

    public Long getIdPP() {
        return idPP;
    }

    public void setIdPP(Long idPP) {
        this.idPP = idPP;
    }

    public LocalDate getDatKrePP() {
        return datKrePP;
    }

    public void setDatKrePP(LocalDate datKrePP) {
        this.datKrePP = datKrePP;
    }

    public LocalDate getStariDatVrac() {
        return stariDatVrac;
    }

    public void setStariDatVrac(LocalDate stariDatVrac) {
        this.stariDatVrac = stariDatVrac;
    }

    public String getNaslovKnjige() {
        return naslovKnjige;
    }

    public void setNaslovKnjige(String naslovKnjige) {
        this.naslovKnjige = naslovKnjige;
    }

    public String getAutorKnjige() {
        return autorKnjige;
    }

    public void setAutorKnjige(String autorKnjige) {
        this.autorKnjige = autorKnjige;
    }

    public String getImeClan() {
        return imeClan;
    }

    public void setImeClan(String imeClan) {
        this.imeClan = imeClan;
    }

    public String getJmbgClan() {
        return jmbgClan;
    }

    public void setJmbgClan(String jmbgClan) {
        this.jmbgClan = jmbgClan;
    }
}
