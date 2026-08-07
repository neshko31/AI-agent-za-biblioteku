package ftn.iis.dto;

import ftn.iis.enums.TipAgentaCS;
import ftn.iis.enums.TipCP;
import ftn.iis.model.CetPoruka;
import ftn.iis.model.CetSesija;
import ftn.iis.model.OcenaCetPoruke;
import jakarta.persistence.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CetPorukaDto {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private Long id;
    private TipCP tipCP;
    private LocalDateTime datumKreiranjaCP;
    private String sadrzajCP;
    private List<IzvorKnjigeDto> izvoriKnjige;
    private List<IzvorRecenzijeDto> izvoriRecenzije;
    private String slikaBase64;

    public CetPorukaDto(Long id, TipCP tipCP, LocalDateTime datumKreiranjaCP, String sadrzajCP, List<IzvorKnjigeDto> izvoriKnjige, List<IzvorRecenzijeDto> izvoriRecenzije, String slikaBase64) {
        this.id = id;
        this.tipCP = tipCP;
        this.datumKreiranjaCP = datumKreiranjaCP;
        this.sadrzajCP = sadrzajCP;
        this.izvoriKnjige = izvoriKnjige;
        this.izvoriRecenzije = izvoriRecenzije;
        this.slikaBase64 = slikaBase64;
    }

    public static CetPorukaDto fromCetPoruka(CetPoruka cetPoruka) {
        TipAgentaCS tipAgenta = cetPoruka.getCetSesija() != null ? cetPoruka.getCetSesija().getTipAgentaCS() : null;
        String izvoriJson = cetPoruka.getIzvoriCP();

        List<IzvorKnjigeDto> izvoriKnjige = Collections.emptyList();
        List<IzvorRecenzijeDto> izvoriRecenzije = Collections.emptyList();

        if (tipAgenta == TipAgentaCS.AGENT_KNJIGE) {
            izvoriKnjige = parsirajIzvore(izvoriJson, new TypeReference<List<IzvorKnjigeDto>>() {
            });
        } else if (tipAgenta == TipAgentaCS.AGENT_RECENZIJE) {
            izvoriRecenzije = parsirajIzvore(izvoriJson, new TypeReference<List<IzvorRecenzijeDto>>() {
            });
        }

        return new CetPorukaDto(
                cetPoruka.getId(),
                cetPoruka.getTipCP(),
                cetPoruka.getDatumKreiranjaCP(),
                cetPoruka.getSadrzajCP(),
                izvoriKnjige,
                izvoriRecenzije,
                cetPoruka.getSlikaBase64()
        );
    }

    public CetPorukaDto() {
    }

    private static <T> List<T> parsirajIzvore(String izvoriJson, TypeReference<List<T>> tip) {
        if (izvoriJson == null || izvoriJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(izvoriJson, tip);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipCP getTipCP() {
        return tipCP;
    }

    public void setTipCP(TipCP tipCP) {
        this.tipCP = tipCP;
    }

    public LocalDateTime getDatumKreiranjaCP() {
        return datumKreiranjaCP;
    }

    public void setDatumKreiranjaCP(LocalDateTime datumKreiranjaCP) {
        this.datumKreiranjaCP = datumKreiranjaCP;
    }

    public String getSadrzajCP() {
        return sadrzajCP;
    }

    public void setSadrzajCP(String sadrzajCP) {
        this.sadrzajCP = sadrzajCP;
    }

    public List<IzvorKnjigeDto> getIzvoriKnjige() {
        return izvoriKnjige;
    }

    public void setIzvoriKnjige(List<IzvorKnjigeDto> izvoriKnjige) {
        this.izvoriKnjige = izvoriKnjige;
    }

    public List<IzvorRecenzijeDto> getIzvoriRecenzije() {
        return izvoriRecenzije;
    }

    public void setIzvoriRecenzije(List<IzvorRecenzijeDto> izvoriRecenzije) {
        this.izvoriRecenzije = izvoriRecenzije;
    }

    public String getSlikaBase64() {
        return slikaBase64;
    }

    public void setSlikaBase64(String slikaBase64) {
        this.slikaBase64 = slikaBase64;
    }
}
