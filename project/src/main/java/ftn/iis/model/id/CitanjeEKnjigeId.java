package ftn.iis.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class CitanjeEKnjigeId implements Serializable {

    @Column(name = "jmbg_clana", length = 13)
    private String jmbgClana;

    @Column(name = "isbn_eknjige", length = 13)
    private String isbnEKnjige;

    @Column(name = "datum_pocetka_ck")
    private LocalDate datumPocetka;

    public CitanjeEKnjigeId() {
    }

    public CitanjeEKnjigeId(String jmbgClana, String isbnEKnjige, LocalDate datumPocetka) {
        this.jmbgClana = jmbgClana;
        this.isbnEKnjige = isbnEKnjige;
        this.datumPocetka = datumPocetka;
    }

    public String getJmbgClana() {
        return jmbgClana;
    }

    public void setJmbgClana(String jmbgClana) {
        this.jmbgClana = jmbgClana;
    }

    public String getIsbnEKnjige() {
        return isbnEKnjige;
    }

    public void setIsbnEKnjige(String isbnEKnjige) {
        this.isbnEKnjige = isbnEKnjige;
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
        if (!(o instanceof CitanjeEKnjigeId that)) return false;
        return Objects.equals(jmbgClana, that.jmbgClana) && Objects.equals(isbnEKnjige, that.isbnEKnjige) && Objects.equals(datumPocetka, that.datumPocetka);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jmbgClana, isbnEKnjige, datumPocetka);
    }
}