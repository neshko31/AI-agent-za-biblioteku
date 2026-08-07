package ftn.iis.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class SlusanjeAudioKnjigeId implements Serializable {

    @Column(name = "jmbg_clana", length = 13)
    private String jmbgClana;

    @Column(name = "isbn_audio_knjige", length = 13)
    private String isbnAudioKnjige;

    @Column(name = "datum_pocetka_sak")
    private LocalDate datumPocetka;

    public SlusanjeAudioKnjigeId() {
    }

    public SlusanjeAudioKnjigeId(String jmbgClana, String isbnAudioKnjige, LocalDate datumPocetka) {
        this.jmbgClana = jmbgClana;
        this.isbnAudioKnjige = isbnAudioKnjige;
        this.datumPocetka = datumPocetka;
    }

    public String getJmbgClana() {
        return jmbgClana;
    }

    public void setJmbgClana(String jmbgClana) {
        this.jmbgClana = jmbgClana;
    }

    public String getIsbnAudioKnjige() {
        return isbnAudioKnjige;
    }

    public void setIsbnAudioKnjige(String isbnAudioKnjige) {
        this.isbnAudioKnjige = isbnAudioKnjige;
    }

    public LocalDate getDatumPocetka() {
        return datumPocetka;
    }

    public void setDatumPocetka(LocalDate datumPocetka) {
        this.datumPocetka = datumPocetka;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlusanjeAudioKnjigeId that)) return false;
        return Objects.equals(jmbgClana, that.jmbgClana) && Objects.equals(isbnAudioKnjige, that.isbnAudioKnjige) && Objects.equals(datumPocetka, that.datumPocetka);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jmbgClana, isbnAudioKnjige, datumPocetka);
    }
}
