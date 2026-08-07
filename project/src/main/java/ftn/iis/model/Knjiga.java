package ftn.iis.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "knjiga")
public class Knjiga {
    @Id
    @Column(name = "isbn", length = 13)
    private String isbn;

    @Column(name = "putanja_naslovna")
    private String putanjaNaslovna;

    @Column(name = "naslov", nullable = false)
    private String naslov;

    @Column(name = "sinopsis")
    private String sinopsis;

    @Column(name = "tip_knjige", length = 3, nullable = false)
    @Pattern(regexp = "[01]{3}")
    private String tipKnjige = "000";
    // Napomena za mapiranje:
    // 1xx - postoji fizicka knjiga
    // x1x - postoji eknjiga
    // xx1 - postoji audio knjiga

    @OneToOne(mappedBy = "knjiga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FizickaKnjiga fizickaKnjiga;

    @OneToOne(mappedBy = "knjiga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EKnjiga eKnjiga;

    @OneToOne(mappedBy = "knjiga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AudioKnjiga audioKnjiga;

    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "katalog_id", nullable = false)
    @JsonBackReference("katalog-knjiga")
    private Katalog katalog;

    @ManyToOne
    @JoinColumn(name = "zanr_id")
    private Genre zanr;

    @Column
    private boolean deleted;

    @Column(name = "autor", nullable = true)
    private String autor;

    public Knjiga() {
        this.tipKnjige = "000";
    }

    public Knjiga(String isbn, String putanjaNaslovna, String naslov, String sinopsis) {
        this.isbn = isbn;
        this.putanjaNaslovna = putanjaNaslovna;
        this.naslov = naslov;
        this.sinopsis = sinopsis;
    }

    public Knjiga(String isbn, String putanjaNaslovna, String naslov, String sinopsis, FizickaKnjiga fizickaKnjiga, EKnjiga eKnjiga, AudioKnjiga audioKnjiga, Katalog katalog, String autor, String tipKnjige, Genre zanr) {
        this.isbn = isbn;
        this.putanjaNaslovna = putanjaNaslovna;
        this.naslov = naslov;
        this.sinopsis = sinopsis;
        this.fizickaKnjiga = fizickaKnjiga;
        this.eKnjiga = eKnjiga;
        this.audioKnjiga = audioKnjiga;
        this.katalog = katalog;
        this.deleted = false;
        this.tipKnjige = tipKnjige;
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPutanjaNaslovna() {
        return putanjaNaslovna;
    }

    public void setPutanjaNaslovna(String putanjaNaslovna) {
        this.putanjaNaslovna = putanjaNaslovna;
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public FizickaKnjiga getFizickaKnjiga() {
        return fizickaKnjiga;
    }

    public void setFizickaKnjiga(FizickaKnjiga fizickaKnjiga) {
        this.fizickaKnjiga = fizickaKnjiga;
    }

    public EKnjiga geteKnjiga() {
        return eKnjiga;
    }

    public void seteKnjiga(EKnjiga eKnjiga) {
        this.eKnjiga = eKnjiga;
    }

    public AudioKnjiga getAudioKnjiga() {
        return audioKnjiga;
    }

    public Katalog getKatalog() {
        return katalog;
    }

    public void setKatalog(Katalog katalog) {
        this.katalog = katalog;
    }

    public void setAudioKnjiga(AudioKnjiga audioKnjiga) {
        this.audioKnjiga = audioKnjiga;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
  
    public String getTipKnjige() {
        return tipKnjige;
    }

    public void setTipKnjige(String tipKnjige) {
        this.tipKnjige = tipKnjige;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    @PostPersist
    @PostUpdate
    public void izracunajTipKnjige() {
        char[] f = {'0', '0', '0'};
        if (this.fizickaKnjiga != null) f[0] = '1';
        if (this.eKnjiga != null)       f[1] = '1';
        if (this.audioKnjiga != null)  f[2] = '1';
        this.tipKnjige = new String(f);
    }
    public Genre getZanr() {
        return zanr;
    }

    public void setZanr(Genre zanr) {
        this.zanr = zanr;
    }
}
