package ftn.iis.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "elektronska_baza_podataka")
public class ElektronskaBazaPodataka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "naziv_ebp", nullable = false)
    private String naziv;

    @Column(name = "oblast_ebp")
    private String oblast;

    @Column(name = "opis_ebp")
    private String opis;

    @Column(name = "licenca_ebp")
    private String licenca;

    @Column(name = "putanja_ebp", nullable = false)
    private String putanjaEbp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_izdavaca_ebp", nullable = false)
    private Izdavac izdavac;

    @OneToMany(mappedBy = "bazaPodataka", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PreuzimanjeBazePodataka> preuzimanja = new ArrayList<>();

    public ElektronskaBazaPodataka() {
    }

    public ElektronskaBazaPodataka(Long id, String naziv, String oblast, String opis, String licenca, Izdavac izdavac) {
        this.id = id;
        this.naziv = naziv;
        this.oblast = oblast;
        this.opis = opis;
        this.licenca = licenca;
        this.izdavac = izdavac;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getOblast() {
        return oblast;
    }

    public void setOblast(String oblast) {
        this.oblast = oblast;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public String getLicenca() {
        return licenca;
    }

    public void setLicenca(String licenca) {
        this.licenca = licenca;
    }

    public String getPutanjaEbp() {
        return putanjaEbp;
    }

    public void setPutanjaEbp(String putanjaEbp) {
        this.putanjaEbp = putanjaEbp;
    }

    public Izdavac getIzdavac() {
        return izdavac;
    }

    public void setIzdavac(Izdavac izdavac) {
        this.izdavac = izdavac;
    }

    public List<PreuzimanjeBazePodataka> getPreuzimanja() {
        return preuzimanja;
    }

    public void setPreuzimanja(List<PreuzimanjeBazePodataka> preuzimanja) {
        this.preuzimanja = preuzimanja;
    }
}
