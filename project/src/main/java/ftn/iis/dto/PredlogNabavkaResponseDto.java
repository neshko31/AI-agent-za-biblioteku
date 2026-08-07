package ftn.iis.dto;

import ftn.iis.enums.StatusPredloga;
import ftn.iis.repository.PredlogNabavkaRepository;

import java.time.LocalDate;

public class PredlogNabavkaResponseDto {
    private Long id;
    private String naslov;
    private String autor;
    private LocalDate datumPodnosenja;
    private StatusPredloga status;
    private String obrazlozenje;
    private String korisnikIme;
    private String korisnikPrezime;

    public PredlogNabavkaResponseDto(){}

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

    public StatusPredloga getStatus() {
        return status;
    }

    public void setStatus(StatusPredloga status) {
        this.status = status;
    }

    public String getObrazlozenje() {
        return obrazlozenje;
    }

    public void setObrazlozenje(String obrazlozenje) {
        this.obrazlozenje = obrazlozenje;
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
}
