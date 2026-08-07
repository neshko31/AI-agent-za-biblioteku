package ftn.iis.dto;

import ftn.iis.enums.StatusUgovora;

import java.time.LocalDate;

public class UgovorDetaljniDto {
    private Long id;
    private Long dobavljacId;
    private String nazivDobavljaca;
    private Double popust;
    private LocalDate datumPocetka;
    private LocalDate datumIsteka;
    private LocalDate datumPotpisa;
    private Integer rokIsporuke;
    private StatusUgovora status;

    public UgovorDetaljniDto(){}

    public UgovorDetaljniDto(Long dobavljacId, String nazivDobavljaca, Double popust, LocalDate datumPocetka,
                             LocalDate datumIsteka, LocalDate datumPotpisa, Integer rokIsporuke, StatusUgovora status){
        this.dobavljacId = dobavljacId;
        this.nazivDobavljaca = nazivDobavljaca;
        this.popust = popust;
        this.datumPocetka = datumPocetka;
        this.datumIsteka = datumIsteka;
        this.datumPotpisa = datumPotpisa;
        this.rokIsporuke = rokIsporuke;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDobavljacId() {
        return dobavljacId;
    }

    public void setDobavljacId(Long dobavljacId) {
        this.dobavljacId = dobavljacId;
    }

    public String getNazivDobavljaca() {
        return nazivDobavljaca;
    }

    public void setNazivDobavljaca(String nazivDobavljaca) {
        this.nazivDobavljaca = nazivDobavljaca;
    }

    public Double getPopust() {
        return popust;
    }

    public void setPopust(Double popust) {
        this.popust = popust;
    }

    public LocalDate getDatumPocetka() {
        return datumPocetka;
    }

    public void setDatumPocetka(LocalDate datumPocetka) {
        this.datumPocetka = datumPocetka;
    }

    public LocalDate getDatumIsteka() {
        return datumIsteka;
    }

    public void setDatumIsteka(LocalDate datumIsteka) {
        this.datumIsteka = datumIsteka;
    }

    public LocalDate getDatumPotpisa() {
        return datumPotpisa;
    }

    public void setDatumPotpisa(LocalDate datumPotpisa) {
        this.datumPotpisa = datumPotpisa;
    }

    public Integer getRokIsporuke() {
        return rokIsporuke;
    }

    public void setRokIsporuke(Integer rokIsporuke) {
        this.rokIsporuke = rokIsporuke;
    }

    public StatusUgovora getStatus() {
        return status;
    }

    public void setStatus(StatusUgovora status) {
        this.status = status;
    }
}
