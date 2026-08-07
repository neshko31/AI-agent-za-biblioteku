package ftn.iis.dto;

import java.util.List;

public class AktivnostiIzvestajResponseDto {

    private Integer godina;
    private List<AktivnostiMesecniRedDto> meseci;
    private AktivnostiGodisnjiZbirDto ukupnoZaGodinu;

    public AktivnostiIzvestajResponseDto() {
    }

    public AktivnostiIzvestajResponseDto(Integer godina, List<AktivnostiMesecniRedDto> meseci,
                                         AktivnostiGodisnjiZbirDto ukupnoZaGodinu) {
        this.godina = godina;
        this.meseci = meseci;
        this.ukupnoZaGodinu = ukupnoZaGodinu;
    }

    public Integer getGodina() {
        return godina;
    }

    public void setGodina(Integer godina) {
        this.godina = godina;
    }

    public List<AktivnostiMesecniRedDto> getMeseci() {
        return meseci;
    }

    public void setMeseci(List<AktivnostiMesecniRedDto> meseci) {
        this.meseci = meseci;
    }

    public AktivnostiGodisnjiZbirDto getUkupnoZaGodinu() {
        return ukupnoZaGodinu;
    }

    public void setUkupnoZaGodinu(AktivnostiGodisnjiZbirDto ukupnoZaGodinu) {
        this.ukupnoZaGodinu = ukupnoZaGodinu;
    }
}