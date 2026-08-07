package ftn.iis.dto;

import ftn.iis.enums.StatusPredloga;

import java.time.LocalDate;

public class PredlogNabavkaZaMenadzeraDto {
    private Long id;
    private String naslov;
    private String autor;
    private LocalDate datumPodnosenja;
    private String korisnikIme;
    private String korisnikPrezime;
    private Double okvirnaCena;
    private String zanrNaziv;

    public PredlogNabavkaZaMenadzeraDto(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDate getDatumPodnosenja() {
        return datumPodnosenja;
    }

    public void setDatumPodnosenja(LocalDate datumPodnosenja) {
        this.datumPodnosenja = datumPodnosenja;
    }

    public String getKorisnikIme() {
        return korisnikIme;
    }

    public void setKorisnikIme(String korisnikIme) {
        this.korisnikIme = korisnikIme;
    }

    public String getKorisnikPrezime() {
        return korisnikPrezime;
    }

    public void setKorisnikPrezime(String korisnikPrezime) {
        this.korisnikPrezime = korisnikPrezime;
    }

    public Double getOkvirnaCena() {
        return okvirnaCena;
    }

    public void setOkvirnaCena(Double okvirnaCena) {
        this.okvirnaCena = okvirnaCena;
    }

    public String getZanrNaziv() {
        return zanrNaziv;
    }

    public void setZanrNaziv(String zanrNaziv) {
        this.zanrNaziv = zanrNaziv;
    }

}
