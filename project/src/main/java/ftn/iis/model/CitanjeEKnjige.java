package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import ftn.iis.enums.StatusCitanja;
import ftn.iis.model.id.CitanjeEKnjigeId;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "citanje_eknjige")
public class CitanjeEKnjige {

    @EmbeddedId
    private CitanjeEKnjigeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jmbgClana")
    @JoinColumn(name = "jmbg_clana")
    @JsonBackReference("user-citanja")
    private User clan;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("isbnEKnjige")
    @JoinColumn(name = "isbn_eknjige")
    private EKnjiga eKnjiga;

    @Column(name = "trenutna_stranica_ck")
    private Integer trenutnaStranica;

    @Column(name = "datum_poslednjeg_pristupa_ck")
    private LocalDate datumPoslednjegPristupa;

    @Column(name = "datum_zavrsetka_ck")
    private LocalDate datumZavrsetka;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_citanja_ck", nullable = false)
    private StatusCitanja statusCitanja;

    public CitanjeEKnjige() {
    }

    public CitanjeEKnjige(CitanjeEKnjigeId id, User clan, EKnjiga eKnjiga, Integer trenutnaStranica, LocalDate datumPoslednjegPristupa, LocalDate datumZavrsetka, StatusCitanja statusCitanja) {
        this.id = id;
        this.clan = clan;
        this.eKnjiga = eKnjiga;
        this.trenutnaStranica = trenutnaStranica;
        this.datumPoslednjegPristupa = datumPoslednjegPristupa;
        this.datumZavrsetka = datumZavrsetka;
        this.statusCitanja = statusCitanja;
    }

    public CitanjeEKnjigeId getId() {
        return id;
    }

    public void setId(CitanjeEKnjigeId id) {
        this.id = id;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public EKnjiga geteKnjiga() {
        return eKnjiga;
    }

    public void seteKnjiga(EKnjiga eKnjiga) {
        this.eKnjiga = eKnjiga;
    }

    public Integer getTrenutnaStranica() {
        return trenutnaStranica;
    }

    public void setTrenutnaStranica(Integer trenutnaStranica) {
        this.trenutnaStranica = trenutnaStranica;
    }

    public LocalDate getDatumPoslednjegPristupa() {
        return datumPoslednjegPristupa;
    }

    public void setDatumPoslednjegPristupa(LocalDate datumPoslednjegPristupa) {
        this.datumPoslednjegPristupa = datumPoslednjegPristupa;
    }

    public LocalDate getDatumZavrsetka() {
        return datumZavrsetka;
    }

    public void setDatumZavrsetka(LocalDate datumZavrsetka) {
        this.datumZavrsetka = datumZavrsetka;
    }

    public StatusCitanja getStatusCitanja() {
        return statusCitanja;
    }

    public void setStatusCitanja(StatusCitanja statusCitanja) {
        this.statusCitanja = statusCitanja;
    }
}
