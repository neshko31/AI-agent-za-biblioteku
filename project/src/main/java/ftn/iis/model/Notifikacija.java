package ftn.iis.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifikacija")
public class Notifikacija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "korisnik_jmbg", nullable = false)
    private User korisnik;

    @Column(name = "poruka", nullable = false)
    private String poruka;

    @Column(name = "datum", nullable = false)
    private LocalDateTime datum;

    @Column(name = "procitana", nullable = false)
    private boolean procitana = false;

    public Notifikacija() {}

    public Notifikacija(User korisnik, String poruka) {
        this.korisnik = korisnik;
        this.poruka = poruka;
        this.datum = LocalDateTime.now();
        this.procitana = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(User korisnik) {
        this.korisnik = korisnik;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }

    public LocalDateTime getDatum() {
        return datum;
    }

    public void setDatum(LocalDateTime datum) {
        this.datum = datum;
    }

    public boolean isProcitana() {
        return procitana;
    }

    public void setProcitana(boolean procitana) {
        this.procitana = procitana;
    }
}