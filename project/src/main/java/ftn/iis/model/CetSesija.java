package ftn.iis.model;

import ftn.iis.enums.TipAgentaCS;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cet_sesija")
public class CetSesija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cs")
    private Long id;

    @Column(name = "naslov_cs")
    private String naslovCS;

    @Column(name = "datum_kreiranja_cs", nullable = false)
    private LocalDateTime datumKreiranjaCS;

    @Column(name = "datum_azuriranja_cs", nullable = false)
    private LocalDateTime datumAzuriranjaCS;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_agenta_cs", nullable = false)
    private TipAgentaCS tipAgentaCS;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jmbg_clana", nullable = false)
    private User clan;

    @OneToMany(mappedBy = "cetSesija", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CetPoruka> poruke = new ArrayList<>();

    @Column(name = "arhivirano", nullable = false)
    private Boolean arhivirano = false;

    @Column(name = "datum_arhiviranja_cs")
    private LocalDateTime datumArhiviranjaCS;

    @Column(name = "verzija", nullable = false)
    private Integer verzija = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_roditeljske_sesije", nullable = true)
    private CetSesija roditeljskaSesija;

    @OneToMany(mappedBy = "roditeljskaSesija", fetch = FetchType.LAZY)
    private List<CetSesija> grane = new ArrayList<>();

    @Column(name = "indeks_poruke_racvanja", nullable = true)
    private Integer indeksPorukeRacvanja;

    @Column(name = "ima_grane", nullable = false)
    private Boolean imaGrane = false;

    public CetSesija() {
    }

    public CetSesija(Long id, String naslovCS, LocalDateTime datumKreiranjaCS, LocalDateTime datumAzuriranjaCS, TipAgentaCS tipAgentaCS, User clan, List<CetPoruka> poruke, Boolean arhivirano, LocalDateTime datumArhiviranjaCS, Integer verzija, CetSesija roditeljskaSesija, List<CetSesija> grane, Integer indeksPorukeRacvanja, Boolean imaGrane) {
        this.id = id;
        this.naslovCS = naslovCS;
        this.datumKreiranjaCS = datumKreiranjaCS;
        this.datumAzuriranjaCS = datumAzuriranjaCS;
        this.tipAgentaCS = tipAgentaCS;
        this.clan = clan;
        this.poruke = poruke;
        this.arhivirano = arhivirano;
        this.datumArhiviranjaCS = datumArhiviranjaCS;
        this.verzija = verzija;
        this.roditeljskaSesija = roditeljskaSesija;
        this.grane = grane;
        this.indeksPorukeRacvanja = indeksPorukeRacvanja;
        this.imaGrane = imaGrane;
    }

    public CetSesija(Long id, String naslovCS, LocalDateTime datumKreiranjaCS, LocalDateTime datumAzuriranjaCS, TipAgentaCS tipAgentaCS, User clan, List<CetPoruka> poruke, Boolean arhivirano, LocalDateTime datumArhiviranjaCS) {
        this.id = id;
        this.naslovCS = naslovCS;
        this.datumKreiranjaCS = datumKreiranjaCS;
        this.datumAzuriranjaCS = datumAzuriranjaCS;
        this.tipAgentaCS = tipAgentaCS;
        this.clan = clan;
        this.poruke = poruke;
        this.arhivirano = arhivirano;
        this.datumArhiviranjaCS = datumArhiviranjaCS;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaslovCS() {
        return naslovCS;
    }

    public void setNaslovCS(String naslovCS) {
        this.naslovCS = naslovCS;
    }

    public LocalDateTime getDatumKreiranjaCS() {
        return datumKreiranjaCS;
    }

    public void setDatumKreiranjaCS(LocalDateTime datumKreiranjaCS) {
        this.datumKreiranjaCS = datumKreiranjaCS;
    }

    public LocalDateTime getDatumAzuriranjaCS() {
        return datumAzuriranjaCS;
    }

    public void setDatumAzuriranjaCS(LocalDateTime datumAzuriranjaCS) {
        this.datumAzuriranjaCS = datumAzuriranjaCS;
    }

    public TipAgentaCS getTipAgentaCS() {
        return tipAgentaCS;
    }

    public void setTipAgentaCS(TipAgentaCS tipAgentaCS) {
        this.tipAgentaCS = tipAgentaCS;
    }

    public User getClan() {
        return clan;
    }

    public void setClan(User clan) {
        this.clan = clan;
    }

    public List<CetPoruka> getPoruke() {
        return poruke;
    }

    public void setPoruke(List<CetPoruka> poruke) {
        this.poruke = poruke;
    }

    public Boolean getArhivirano() {
        return arhivirano;
    }

    public void setArhivirano(Boolean arhivirano) {
        this.arhivirano = arhivirano;
    }

    public LocalDateTime getDatumArhiviranjaCS() {
        return datumArhiviranjaCS;
    }

    public void setDatumArhiviranjaCS(LocalDateTime datumArhiviranjaCS) {
        this.datumArhiviranjaCS = datumArhiviranjaCS;
    }

    public Integer getVerzija() {
        return verzija;
    }

    public void setVerzija(Integer verzija) {
        this.verzija = verzija;
    }

    public CetSesija getRoditeljskaSesija() {
        return roditeljskaSesija;
    }

    public void setRoditeljskaSesija(CetSesija roditeljskaSesija) {
        this.roditeljskaSesija = roditeljskaSesija;
    }

    public List<CetSesija> getGrane() {
        return grane;
    }

    public void setGrane(List<CetSesija> grane) {
        this.grane = grane;
    }

    public Integer getIndeksPorukeRacvanja() {
        return indeksPorukeRacvanja;
    }

    public void setIndeksPorukeRacvanja(Integer indeksPorukeRacvanja) {
        this.indeksPorukeRacvanja = indeksPorukeRacvanja;
    }

    public Boolean getImaGrane() {
        return imaGrane;
    }

    public void setImaGrane(Boolean imaGrane) {
        this.imaGrane = imaGrane;
    }
}