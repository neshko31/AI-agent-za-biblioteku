package ftn.iis.model;

import ftn.iis.model.id.PristupBrojuCasopisaId;
import jakarta.persistence.*;

@Entity
@Table(name = "pristup_broju_casopisa")
public class PristupBrojuCasopisa {

    @EmbeddedId
    private PristupBrojuCasopisaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jmbgClana")
    @JoinColumn(name = "jmbg_clana")
    private User clan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "issn_casopisa", referencedColumnName = "issn", insertable = false, updatable = false),
            @JoinColumn(name = "broj_izdanja", referencedColumnName = "broj_izdanja", insertable = false, updatable = false)
    })
    private BrojCasopisa brojCasopisa;

    public PristupBrojuCasopisa() {
    }

    public PristupBrojuCasopisa(PristupBrojuCasopisaId id, User clan, BrojCasopisa brojCasopisa) {
        this.id = id;
        this.clan = clan;
        this.brojCasopisa = brojCasopisa;
    }

    public PristupBrojuCasopisaId getId() {
        return id;
    }

    public void setId(PristupBrojuCasopisaId id) {
        this.id = id;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public BrojCasopisa getBrojCasopisa() {
        return brojCasopisa;
    }

    public void setBrojCasopisa(BrojCasopisa brojCasopisa) {
        this.brojCasopisa = brojCasopisa;
    }
}
