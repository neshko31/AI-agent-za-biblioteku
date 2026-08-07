package ftn.iis.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OcenaCetPorukeId implements Serializable {

    @Column(name = "jmbg_clana", length = 13)
    private String jmbgClana;

    @Column(name = "id_cp")
    private Long idCetPoruke;

    public OcenaCetPorukeId() {
    }

    public OcenaCetPorukeId(String jmbgClana, Long idCetPoruke) {
        this.jmbgClana = jmbgClana;
        this.idCetPoruke = idCetPoruke;
    }

    public String getJmbgClana() {
        return jmbgClana;
    }

    public void setJmbgClana(String jmbgClana) {
        this.jmbgClana = jmbgClana;
    }

    public Long getIdCetPoruke() {
        return idCetPoruke;
    }

    public void setIdCetPoruke(Long idCetPoruke) {
        this.idCetPoruke = idCetPoruke;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OcenaCetPorukeId that)) return false;
        return Objects.equals(jmbgClana, that.jmbgClana) && Objects.equals(idCetPoruke, that.idCetPoruke);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jmbgClana, idCetPoruke);
    }
}