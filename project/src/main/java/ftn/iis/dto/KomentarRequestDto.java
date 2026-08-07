package ftn.iis.dto;

public class KomentarRequestDto {
    private String tekstK;
    private Long odgovorNaId;

    public KomentarRequestDto(String tekstK, Long odgovorNaId) {
        this.tekstK = tekstK;
        this.odgovorNaId = odgovorNaId;
    }

    public KomentarRequestDto() {
    }

    public String getTekstK() {
        return tekstK;
    }

    public void setTekstK(String tekstK) {
        this.tekstK = tekstK;
    }

    public Long getOdgovorNaId() {
        return odgovorNaId;
    }

    public void setOdgovorNaId(Long odgovorNaId) {
        this.odgovorNaId = odgovorNaId;
    }
}
