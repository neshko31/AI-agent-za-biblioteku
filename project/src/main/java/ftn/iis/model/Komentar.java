package ftn.iis.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "komentar")
public class Komentar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_k")
    private Long id;

    @Column(name = "tekst_k", nullable = false, columnDefinition = "TEXT")
    private String tekstK;

    @Column(name = "datum_kreiranja_k", nullable = false)
    private LocalDateTime datumKreiranjaK;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "isbn", nullable = false)
    private Knjiga knjiga;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jmbg_clana", nullable = false)
    private User clan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ok")
    private Komentar odgovorNa;

    @OneToMany(mappedBy = "odgovorNa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Komentar> odgovori = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "komentar_lajk",
            joinColumns = @JoinColumn(name = "id_k"),
            inverseJoinColumns = @JoinColumn(name = "jmbg_clana")
    )
    private List<User> lajkovali = new ArrayList<>();

    public Komentar() {
    }

    public Komentar(Long id, String tekstK, LocalDateTime datumKreiranjaK, Knjiga knjiga, User clan, Komentar odgovorNa, List<Komentar> odgovori, List<User> lajkovali) {
        this.id = id;
        this.tekstK = tekstK;
        this.datumKreiranjaK = datumKreiranjaK;
        this.knjiga = knjiga;
        this.clan = clan;
        this.odgovorNa = odgovorNa;
        this.odgovori = odgovori;
        this.lajkovali = lajkovali;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTekstK() {
        return tekstK;
    }

    public void setTekstK(String tekstK) {
        this.tekstK = tekstK;
    }

    public LocalDateTime getDatumKreiranjaK() {
        return datumKreiranjaK;
    }

    public void setDatumKreiranjaK(LocalDateTime datumKreiranjaK) {
        this.datumKreiranjaK = datumKreiranjaK;
    }

    public Knjiga getKnjiga() {
        return knjiga;
    }

    public void setKnjiga(Knjiga knjiga) {
        this.knjiga = knjiga;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public Komentar getOdgovorNa() {
        return odgovorNa;
    }

    public void setOdgovorNa(Komentar odgovorNa) {
        this.odgovorNa = odgovorNa;
    }

    public List<Komentar> getOdgovori() {
        return odgovori;
    }

    public void setOdgovori(List<Komentar> odgovori) {
        this.odgovori = odgovori;
    }

    public List<User> getLajkovali() {
        return lajkovali;
    }

    public void setLajkovali(List<User> lajkovali) {
        this.lajkovali = lajkovali;
    }
}