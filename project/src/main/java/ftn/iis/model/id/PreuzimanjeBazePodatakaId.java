package ftn.iis.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class PreuzimanjeBazePodatakaId implements Serializable {

    @Column(name = "jmbg_clana", length = 13)
    private String jmbgClana;

    @Column(name = "id_baze")
    private Long idBaze;

    @Column(name = "datum_preuzimanja_pbp")
    private LocalDate datumPreuzimanja;

    public PreuzimanjeBazePodatakaId() {
    }

    public PreuzimanjeBazePodatakaId(String jmbgClana, Long idBaze, LocalDate datumPreuzimanja) {
        this.jmbgClana = jmbgClana;
        this.idBaze = idBaze;
        this.datumPreuzimanja = datumPreuzimanja;
    }

    public String getJmbgClana() {
        return jmbgClana;
    }

    public void setJmbgClana(String jmbgClana) {
        this.jmbgClana = jmbgClana;
    }

    public Long getIdBaze() {
        return idBaze;
    }

    public void setIdBaze(Long idBaze) {
        this.idBaze = idBaze;
    }

    public LocalDate getDatumPreuzimanja() {
        return datumPreuzimanja;
    }

    public void setDatumPreuzimanja(LocalDate datumPreuzimanja) {
        this.datumPreuzimanja = datumPreuzimanja;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreuzimanjeBazePodatakaId that)) return false;
        return Objects.equals(jmbgClana, that.jmbgClana) &&
                Objects.equals(idBaze, that.idBaze) &&
                Objects.equals(datumPreuzimanja, that.datumPreuzimanja);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jmbgClana, idBaze, datumPreuzimanja);
    }
}