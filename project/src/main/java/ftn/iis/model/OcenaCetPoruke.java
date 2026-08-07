package ftn.iis.model;

import ftn.iis.model.id.OcenaCetPorukeId;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocena_cet_poruke")
public class OcenaCetPoruke {

    @EmbeddedId
    private OcenaCetPorukeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jmbgClana")
    @JoinColumn(name = "jmbg_clana")
    private User clan;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idCetPoruke")
    @JoinColumn(name = "id_cp")
    private CetPoruka cetPoruka;

    @Column(name = "ocena_cp", nullable = false)
    private Integer ocenaCP;

    @Column(name = "komentar_cp", columnDefinition = "TEXT")
    private String komentarCP;

    @Column(name = "datum_ocenjivanja_cs", nullable = false)
    private LocalDateTime datumOcenjivanjaCS;

    public OcenaCetPoruke() {
    }

    public OcenaCetPoruke(OcenaCetPorukeId id, User clan, CetPoruka cetPoruka, Integer ocenaCP, String komentarCP, LocalDateTime datumOcenjivanjaCS) {
        this.id = id;
        this.clan = clan;
        this.cetPoruka = cetPoruka;
        this.ocenaCP = ocenaCP;
        this.komentarCP = komentarCP;
        this.datumOcenjivanjaCS = datumOcenjivanjaCS;
    }

    public OcenaCetPorukeId getId() {
        return id;
    }

    public void setId(OcenaCetPorukeId id) {
        this.id = id;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public CetPoruka getCetPoruka() {
        return cetPoruka;
    }

    public void setCetPoruka(CetPoruka cetPoruka) {
        this.cetPoruka = cetPoruka;
    }

    public Integer getOcenaCP() {
        return ocenaCP;
    }

    public void setOcenaCP(Integer ocenaCP) {
        this.ocenaCP = ocenaCP;
    }

    public String getKomentarCP() {
        return komentarCP;
    }

    public void setKomentarCP(String komentarCP) {
        this.komentarCP = komentarCP;
    }

    public LocalDateTime getDatumOcenjivanjaCS() {
        return datumOcenjivanjaCS;
    }

    public void setDatumOcenjivanjaCS(LocalDateTime datumOcenjivanjaCS) {
        this.datumOcenjivanjaCS = datumOcenjivanjaCS;
    }
}