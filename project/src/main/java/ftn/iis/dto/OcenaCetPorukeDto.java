package ftn.iis.dto;

public class OcenaCetPorukeDto {
    private Integer ocenaCP;
    private String komentarCP;

    public OcenaCetPorukeDto() {
    }

    public OcenaCetPorukeDto(Integer ocenaCP, String komentarCP) {
        this.ocenaCP = ocenaCP;
        this.komentarCP = komentarCP;
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
}