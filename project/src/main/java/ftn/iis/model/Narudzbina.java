package ftn.iis.model;

import ftn.iis.enums.StatusNarudzbine;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "narudzbina")
public class Narudzbina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dobavljac_id", nullable = false)
    private Dobavljac dobavljac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ugovor_id", nullable = false)
    private Ugovor ugovor;

    @Column(name = "datum_kreiranja", nullable = false)
    private LocalDate datumKreiranja;

    @Column(name = "datum_ocekivane_isporuke", nullable = false)
    private LocalDate datumOcekivaneIsporuke;

    @Column(name = "datum_stvarne_isporuke")
    private LocalDate datumStvarneIsporuke;

    @Column(name = "ukupna_cena", nullable = false)
    private Double ukupnaCena = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusNarudzbine status;

    @Column(name = "napomena")
    private String napomena;

    @OneToMany(mappedBy = "narudzbina", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StavkaNarudzbine> stavke = new ArrayList<>();

    @OneToOne(mappedBy = "narudzbina", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Reklamacija reklamacija;

    public Narudzbina() {}

    public Narudzbina(Dobavljac dobavljac, Ugovor ugovor, String napomena) {
        this.dobavljac = dobavljac;
        this.ugovor = ugovor;
        this.napomena = napomena;
        this.datumKreiranja = LocalDate.now();
        this.datumOcekivaneIsporuke = LocalDate.now().plusDays(ugovor.getRokIsporuke());
        this.ukupnaCena = 0.0;
        this.status = StatusNarudzbine.KREIRANA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getUkupnaCena() {
        return ukupnaCena;
    }

    public void setUkupnaCena(Double ukupnaCena) {
        this.ukupnaCena = ukupnaCena;
    }

    public Dobavljac getDobavljac() {
        return dobavljac;
    }

    public void setDobavljac(Dobavljac dobavljac) {
        this.dobavljac = dobavljac;
    }

    public Ugovor getUgovor() {
        return ugovor;
    }

    public void setUgovor(Ugovor ugovor) {
        this.ugovor = ugovor;
    }

    public LocalDate getDatumKreiranja() {
        return datumKreiranja;
    }

    public void setDatumKreiranja(LocalDate datumKreiranja) {
        this.datumKreiranja = datumKreiranja;
    }

    public LocalDate getDatumOcekivaneIsporuke() {
        return datumOcekivaneIsporuke;
    }

    public void setDatumOcekivaneIsporuke(LocalDate datumOcekivaneIsporuke) {
        this.datumOcekivaneIsporuke = datumOcekivaneIsporuke;
    }

    public LocalDate getDatumStvarneIsporuke() {
        return datumStvarneIsporuke;
    }

    public void setDatumStvarneIsporuke(LocalDate datumStvarneIsporuke) {
        this.datumStvarneIsporuke = datumStvarneIsporuke;
    }

    public StatusNarudzbine getStatus() {
        return status;
    }

    public void setStatus(StatusNarudzbine status) {
        this.status = status;
    }

    public String getNapomena() {
        return napomena;
    }

    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }

    public List<StavkaNarudzbine> getStavke() {
        return stavke;
    }

    public void setStavke(List<StavkaNarudzbine> stavke) {
        this.stavke = stavke;
    }

    public Reklamacija getReklamacija() {
        return reklamacija;
    }

    public void setReklamacija(Reklamacija reklamacija) {
        this.reklamacija = reklamacija;
    }

}