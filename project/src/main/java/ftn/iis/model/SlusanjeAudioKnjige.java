package ftn.iis.model;

import ftn.iis.enums.StatusSlusanja;
import ftn.iis.model.id.SlusanjeAudioKnjigeId;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "slusanje_audio_knjige")
public class SlusanjeAudioKnjige {

    @EmbeddedId
    private SlusanjeAudioKnjigeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jmbgClana")
    @JoinColumn(name = "jmbg_clana")
    private User clan;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("isbnAudioKnjige")
    @JoinColumn(name = "isbn_audio_knjige")
    private AudioKnjiga audioKnjiga;

    @Column(name = "trenutna_sekunda_sak")
    private Integer trenutnaSekunda;

    @Column(name = "datum_poslednjeg_pristupa_sak")
    private LocalDate datumPoslednjegPristupa;

    @Column(name = "datum_zavrsetka_sak")
    private LocalDate datumZavrsetka;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_slusanja_sak", nullable = false)
    private StatusSlusanja statusSlusanja;

    public SlusanjeAudioKnjige() {
    }

    public SlusanjeAudioKnjige(SlusanjeAudioKnjigeId id, User clan, AudioKnjiga audioKnjiga, Integer trenutnaSekunda, LocalDate datumPoslednjegPristupa, LocalDate datumZavrsetka, StatusSlusanja statusSlusanja) {
        this.id = id;
        this.clan = clan;
        this.audioKnjiga = audioKnjiga;
        this.trenutnaSekunda = trenutnaSekunda;
        this.datumPoslednjegPristupa = datumPoslednjegPristupa;
        this.datumZavrsetka = datumZavrsetka;
        this.statusSlusanja = statusSlusanja;
    }

    public SlusanjeAudioKnjigeId getId() {
        return id;
    }

    public void setId(SlusanjeAudioKnjigeId id) {
        this.id = id;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public AudioKnjiga getAudioKnjiga() {
        return audioKnjiga;
    }

    public void setAudioKnjiga(AudioKnjiga audioKnjiga) {
        this.audioKnjiga = audioKnjiga;
    }

    public Integer getTrenutnaSekunda() {
        return trenutnaSekunda;
    }

    public void setTrenutnaSekunda(Integer trenutnaSekunda) {
        this.trenutnaSekunda = trenutnaSekunda;
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

    public StatusSlusanja getStatusSlusanja() {
        return statusSlusanja;
    }

    public void setStatusSlusanja(StatusSlusanja statusSlusanja) {
        this.statusSlusanja = statusSlusanja;
    }
}