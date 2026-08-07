package ftn.iis.dto;

import ftn.iis.enums.TipAgentaCS;
import ftn.iis.model.CetPoruka;
import ftn.iis.model.CetSesija;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CetSesijaDetaljnoDto {
    private Long id;
    private String naslovCS;
    private LocalDateTime datumKreiranjaCS;
    private LocalDateTime datumAzuriranjaCS;
    private TipAgentaCS tipAgentaCS;
    private String jmbgClana;
    private List<CetPorukaDto> poruke;
    private Boolean arhivirano;
    private LocalDateTime datumArhiviranjaCS;
    private Integer verzija;
    private Long idRoditeljskeSesije;
    private Integer indeksPorukeRacvanja;
    private Boolean imaGrane;

    public CetSesijaDetaljnoDto(Long id, String naslovCS, LocalDateTime datumKreiranjaCS, LocalDateTime datumAzuriranjaCS, TipAgentaCS tipAgentaCS, String jmbgClana, List<CetPorukaDto> poruke, Boolean arhivirano, LocalDateTime datumArhiviranjaCS, Integer verzija, Long idRoditeljskeSesije, Integer indeksPorukeRacvanja, Boolean imaGrane) {
        this.id = id;
        this.naslovCS = naslovCS;
        this.datumKreiranjaCS = datumKreiranjaCS;
        this.datumAzuriranjaCS = datumAzuriranjaCS;
        this.tipAgentaCS = tipAgentaCS;
        this.jmbgClana = jmbgClana;
        this.poruke = poruke;
        this.arhivirano = arhivirano;
        this.datumArhiviranjaCS = datumArhiviranjaCS;
        this.verzija = verzija;
        this.idRoditeljskeSesije = idRoditeljskeSesije;
        this.indeksPorukeRacvanja = indeksPorukeRacvanja;
        this.imaGrane = imaGrane;
    }

    public CetSesijaDetaljnoDto() {
    }

    public static CetSesijaDetaljnoDto fromCetSesija(CetSesija cetSesija) {
        return new CetSesijaDetaljnoDto(
                cetSesija.getId(),
                cetSesija.getNaslovCS(),
                cetSesija.getDatumKreiranjaCS(),
                cetSesija.getDatumAzuriranjaCS(),
                cetSesija.getTipAgentaCS(),
                cetSesija.getClan().getJmbg(),
                cetSesija.getPoruke().stream()
                        .sorted(Comparator.comparing(CetPoruka::getDatumKreiranjaCP))
                        .map(CetPorukaDto::fromCetPoruka)
                        .collect(Collectors.toList()),
                cetSesija.getArhivirano(),
                cetSesija.getDatumArhiviranjaCS(),
                cetSesija.getVerzija(),
                cetSesija.getRoditeljskaSesija() != null ? cetSesija.getRoditeljskaSesija().getId() : null,
                cetSesija.getIndeksPorukeRacvanja(),
                cetSesija.getImaGrane()
        );
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

    public String getJmbgClana() {
        return jmbgClana;
    }

    public void setJmbgClana(String jmbgClana) {
        this.jmbgClana = jmbgClana;
    }

    public List<CetPorukaDto> getPoruke() {
        return poruke;
    }

    public void setPoruke(List<CetPorukaDto> poruke) {
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

    public Long getIdRoditeljskeSesije() {
        return idRoditeljskeSesije;
    }

    public void setIdRoditeljskeSesije(Long idRoditeljskeSesije) {
        this.idRoditeljskeSesije = idRoditeljskeSesije;
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
