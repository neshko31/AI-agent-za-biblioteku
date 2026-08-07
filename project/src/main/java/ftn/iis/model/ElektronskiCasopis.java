package ftn.iis.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "elektronski_casopis")
public class ElektronskiCasopis {

    @Id
    @Column(name = "issn", length = 8)
    private String issn;

    @Column(name = "naziv_ec", nullable = false)
    private String naziv;

    @Column(name = "oblast_ec")
    private String oblast;

    @Column(name = "opis_ec")
    private String opis;

    @Column(name = "jezik_ec")
    private String jezik;

    @Column(name = "ucestalost_izdavanja_ec")
    private String ucestalostIzdavanja;

    @Column(name = "putanja_slike_ec")
    private String putanjaSlike;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_izdavaca_ec", nullable = false)
    private Izdavac izdavac;

    @OneToMany(mappedBy = "elektronskiCasopis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BrojCasopisa> brojeviCasopisa = new ArrayList<>();

    public ElektronskiCasopis() {
    }

    public ElektronskiCasopis(String issn, String naziv, String oblast, String opis, String jezik, String ucestalostIzdavanja, String putanjaSlike, Izdavac izdavac) {
        this.issn = issn;
        this.naziv = naziv;
        this.oblast = oblast;
        this.opis = opis;
        this.jezik = jezik;
        this.ucestalostIzdavanja = ucestalostIzdavanja;
        this.putanjaSlike = putanjaSlike;
        this.izdavac = izdavac;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
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

    public String getJezik() {
        return jezik;
    }

    public void setJezik(String jezik) {
        this.jezik = jezik;
    }

    public String getUcestalostIzdavanja() {
        return ucestalostIzdavanja;
    }

    public void setUcestalostIzdavanja(String ucestalostIzdavanja) {
        this.ucestalostIzdavanja = ucestalostIzdavanja;
    }

    public String getPutanjaSlike() {
        return putanjaSlike;
    }

    public void setPutanjaSlike(String putanjaSlike) {
        this.putanjaSlike = putanjaSlike;
    }

    public Izdavac getIzdavac() {
        return izdavac;
    }

    public void setIzdavac(Izdavac izdavac) {
        this.izdavac = izdavac;
    }

    public List<BrojCasopisa> getBrojeviCasopisa() {
        return brojeviCasopisa;
    }

    public void setBrojeviCasopisa(List<BrojCasopisa> brojeviCasopisa) {
        this.brojeviCasopisa = brojeviCasopisa;
    }
}
