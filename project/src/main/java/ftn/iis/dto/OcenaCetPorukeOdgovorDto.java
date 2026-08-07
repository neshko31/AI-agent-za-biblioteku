package ftn.iis.dto;

import ftn.iis.model.OcenaCetPoruke;

import java.time.LocalDateTime;

public class OcenaCetPorukeOdgovorDto {
    private Long idCetPoruke;
    private Integer ocenaCP;
    private String komentarCP;
    private LocalDateTime datumOcenjivanjaCS;

    public OcenaCetPorukeOdgovorDto() {
    }

    public OcenaCetPorukeOdgovorDto(Long idCetPoruke, Integer ocenaCP, String komentarCP, LocalDateTime datumOcenjivanjaCS) {
        this.idCetPoruke = idCetPoruke;
        this.ocenaCP = ocenaCP;
        this.komentarCP = komentarCP;
        this.datumOcenjivanjaCS = datumOcenjivanjaCS;
    }

    public static OcenaCetPorukeOdgovorDto fromOcenaCetPoruke(OcenaCetPoruke ocena) {
        return new OcenaCetPorukeOdgovorDto(
                ocena.getId().getIdCetPoruke(),
                ocena.getOcenaCP(),
                ocena.getKomentarCP(),
                ocena.getDatumOcenjivanjaCS()
        );
    }

    public Long getIdCetPoruke() {
        return idCetPoruke;
    }

    public void setIdCetPoruke(Long idCetPoruke) {
        this.idCetPoruke = idCetPoruke;
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