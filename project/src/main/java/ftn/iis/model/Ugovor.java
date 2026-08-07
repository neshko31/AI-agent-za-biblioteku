package ftn.iis.model;

import ftn.iis.enums.StatusUgovora;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "ugovor")
public class Ugovor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dobavljac_id", nullable = false)
    private Dobavljac dobavljac;

    @Column(name = "popust", nullable = false)
    private Double popust; // u procentima

    @Column(name = "datum_pocetka", nullable = false)
    private LocalDate datumPocetka;

    @Column(name = "datum_isteka", nullable = false)
    private LocalDate datumIsteka;

    @Column(name = "datum_potpisa", nullable = false)
    private LocalDate datumPotpisa;

    @Column(name = "rok_isporuke", nullable = false)
    private Integer rokIsporuke; // u danima

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusUgovora status;

    public Ugovor() {}

    public Ugovor(Dobavljac dobavljac, Double popust, LocalDate datumPocetka,
                  LocalDate datumIsteka, LocalDate datumPotpisa, Integer rokIsporuke) {
        this.dobavljac = dobavljac;
        this.popust = popust;
        this.datumPocetka = datumPocetka;
        this.datumIsteka = datumIsteka;
        this.datumPotpisa = datumPotpisa;
        this.rokIsporuke = rokIsporuke;
        this.status = StatusUgovora.AKTIVAN;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Dobavljac getDobavljac() {
        return dobavljac;
    }

    public void setDobavljac(Dobavljac dobavljac) {
        this.dobavljac = dobavljac;
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
