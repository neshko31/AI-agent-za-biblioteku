package ftn.iis.dto;

import java.math.BigDecimal;

public class AktivnostiMesecniRedDto {

    private Integer mesec;
    private Long brojCitanjaEKnjiga;
    private Long brojSlusanjaAudioKnjiga;
    private Long brojPreuzimanjaBaza;
    private Long brojCetSesija;
    private Long ukupnoPorukaKaAI;
    private BigDecimal prosekPorukaPoSesiji;

    public AktivnostiMesecniRedDto() {
    }

    public AktivnostiMesecniRedDto(Integer mesec, Long brojCitanjaEKnjiga, Long brojSlusanjaAudioKnjiga,
                                   Long brojPreuzimanjaBaza, Long brojCetSesija, Long ukupnoPorukaKaAI,
                                   BigDecimal prosekPorukaPoSesiji) {
        this.mesec = mesec;
        this.brojCitanjaEKnjiga = brojCitanjaEKnjiga;
        this.brojSlusanjaAudioKnjiga = brojSlusanjaAudioKnjiga;
        this.brojPreuzimanjaBaza = brojPreuzimanjaBaza;
        this.brojCetSesija = brojCetSesija;
        this.ukupnoPorukaKaAI = ukupnoPorukaKaAI;
        this.prosekPorukaPoSesiji = prosekPorukaPoSesiji;
    }

    public Integer getMesec() {
        return mesec;
    }

    public void setMesec(Integer mesec) {
        this.mesec = mesec;
    }

    public Long getBrojCitanjaEKnjiga() {
        return brojCitanjaEKnjiga;
    }

    public void setBrojCitanjaEKnjiga(Long brojCitanjaEKnjiga) {
        this.brojCitanjaEKnjiga = brojCitanjaEKnjiga;
    }

    public Long getBrojSlusanjaAudioKnjiga() {
        return brojSlusanjaAudioKnjiga;
    }

    public void setBrojSlusanjaAudioKnjiga(Long brojSlusanjaAudioKnjiga) {
        this.brojSlusanjaAudioKnjiga = brojSlusanjaAudioKnjiga;
    }

    public Long getBrojPreuzimanjaBaza() {
        return brojPreuzimanjaBaza;
    }

    public void setBrojPreuzimanjaBaza(Long brojPreuzimanjaBaza) {
        this.brojPreuzimanjaBaza = brojPreuzimanjaBaza;
    }

    public Long getBrojCetSesija() {
        return brojCetSesija;
    }

    public void setBrojCetSesija(Long brojCetSesija) {
        this.brojCetSesija = brojCetSesija;
    }

    public Long getUkupnoPorukaKaAI() {
        return ukupnoPorukaKaAI;
    }

    public void setUkupnoPorukaKaAI(Long ukupnoPorukaKaAI) {
        this.ukupnoPorukaKaAI = ukupnoPorukaKaAI;
    }

    public BigDecimal getProsekPorukaPoSesiji() {
        return prosekPorukaPoSesiji;
    }

    public void setProsekPorukaPoSesiji(BigDecimal prosekPorukaPoSesiji) {
        this.prosekPorukaPoSesiji = prosekPorukaPoSesiji;
    }
}