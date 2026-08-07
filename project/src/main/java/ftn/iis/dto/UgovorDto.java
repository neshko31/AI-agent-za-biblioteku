package ftn.iis.dto;

import ftn.iis.enums.StatusUgovora;
import ftn.iis.model.Ugovor;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class UgovorDto {

    private Long id;

    @NotNull(message = "ID dobavljača je obavezan.")
    private Long dobavljacId;

    @NotNull(message = "Popust je obavezan.")
    @DecimalMin(value = "0.0", message = "Popust ne može biti negativan.")
    @DecimalMax(value = "100.0", message = "Popust ne može biti veći od 100%.")
    private Double popust;

    @NotNull(message = "Datum početka je obavezan.")
    private LocalDate datumPocetka;

    @NotNull(message = "Datum isteka je obavezan.")
    private LocalDate datumIsteka;

    @NotNull(message = "Datum potpisa je obavezan.")
    private LocalDate datumPotpisa;

    @NotNull(message = "Rok isporuke je obavezan.")
    @Min(value = 1, message = "Rok isporuke mora biti najmanje 1 dan.")
    private Integer rokIsporuke;

    private StatusUgovora status;

    public UgovorDto() {
    }

    public UgovorDto(Ugovor ugovor){
        this.dobavljacId = ugovor.getDobavljac().getId();
        this.popust = ugovor.getPopust();
        this.datumPocetka = ugovor.getDatumPocetka();
        this.datumIsteka = ugovor.getDatumIsteka();
        this.datumPotpisa = ugovor.getDatumPotpisa();
        this.rokIsporuke = ugovor.getRokIsporuke();
        this.status = ugovor.getStatus();
        this.id = ugovor.getId();
    }

    public UgovorDto(Long dobavljacId, Double popust, LocalDate datumPocetka,
                  LocalDate datumIsteka, LocalDate datumPotpisa, Integer rokIsporuke, StatusUgovora status) {
        this.dobavljacId = dobavljacId;
        this.popust = popust;
        this.datumPocetka = datumPocetka;
        this.datumIsteka = datumIsteka;
        this.datumPotpisa = datumPotpisa;
        this.rokIsporuke = rokIsporuke;
        this.status =status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getDobavljacId() {
        return dobavljacId;
    }

    public void setDobavljacId(Long dobavljacId) {
        this.dobavljacId = dobavljacId;
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

    public void setStatus(StatusUgovora status) {
        this.status = status;
    }

    public StatusUgovora getStatus() {
        return status;
    }
}
