package ftn.iis.model;

import ftn.iis.model.id.PreuzimanjeBazePodatakaId;
import jakarta.persistence.*;

@Entity
@Table(name = "preuzimanje_baze_podataka")
public class PreuzimanjeBazePodataka {

    @EmbeddedId
    private PreuzimanjeBazePodatakaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jmbgClana")
    @JoinColumn(name = "jmbg_clana")
    private User clan;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idBaze")
    @JoinColumn(name = "id_baze")
    private ElektronskaBazaPodataka bazaPodataka;

    public PreuzimanjeBazePodataka() {
    }

    public PreuzimanjeBazePodataka(PreuzimanjeBazePodatakaId id, User clan, ElektronskaBazaPodataka bazaPodataka) {
        this.id = id;
        this.clan = clan;
        this.bazaPodataka = bazaPodataka;
    }

    public PreuzimanjeBazePodatakaId getId() {
        return id;
    }

    public void setId(PreuzimanjeBazePodatakaId id) {
        this.id = id;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public ElektronskaBazaPodataka getBazaPodataka() {
        return bazaPodataka;
    }

    public void setBazaPodataka(ElektronskaBazaPodataka bazaPodataka) {
        this.bazaPodataka = bazaPodataka;
    }
}
