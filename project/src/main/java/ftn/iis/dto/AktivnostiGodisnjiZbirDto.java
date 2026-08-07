package ftn.iis.dto;

import java.math.BigDecimal;

public class AktivnostiGodisnjiZbirDto {

    private Long ukupnoCitanjaEKnjiga;
    private Long ukupnoSlusanjaAudioKnjiga;
    private Long ukupnoPreuzimanjaBaza;
    private Long ukupnoCetSesija;
    private Long ukupnoPorukaKaAI;
    private BigDecimal prosekPorukaPoSesijiGodisnje;

    public AktivnostiGodisnjiZbirDto() {
    }

    public AktivnostiGodisnjiZbirDto(Long ukupnoCitanjaEKnjiga, Long ukupnoSlusanjaAudioKnjiga,
                                     Long ukupnoPreuzimanjaBaza, Long ukupnoCetSesija,
                                     Long ukupnoPorukaKaAI, BigDecimal prosekPorukaPoSesijiGodisnje) {
        this.ukupnoCitanjaEKnjiga = ukupnoCitanjaEKnjiga;
        this.ukupnoSlusanjaAudioKnjiga = ukupnoSlusanjaAudioKnjiga;
        this.ukupnoPreuzimanjaBaza = ukupnoPreuzimanjaBaza;
        this.ukupnoCetSesija = ukupnoCetSesija;
        this.ukupnoPorukaKaAI = ukupnoPorukaKaAI;
        this.prosekPorukaPoSesijiGodisnje = prosekPorukaPoSesijiGodisnje;
    }

    public Long getUkupnoCitanjaEKnjiga() {
        return ukupnoCitanjaEKnjiga;
    }

    public void setUkupnoCitanjaEKnjiga(Long ukupnoCitanjaEKnjiga) {
        this.ukupnoCitanjaEKnjiga = ukupnoCitanjaEKnjiga;
    }

    public Long getUkupnoSlusanjaAudioKnjiga() {
        return ukupnoSlusanjaAudioKnjiga;
    }

    public void setUkupnoSlusanjaAudioKnjiga(Long ukupnoSlusanjaAudioKnjiga) {
        this.ukupnoSlusanjaAudioKnjiga = ukupnoSlusanjaAudioKnjiga;
    }

    public Long getUkupnoPreuzimanjaBaza() {
        return ukupnoPreuzimanjaBaza;
    }

    public void setUkupnoPreuzimanjaBaza(Long ukupnoPreuzimanjaBaza) {
        this.ukupnoPreuzimanjaBaza = ukupnoPreuzimanjaBaza;
    }

    public Long getUkupnoCetSesija() {
        return ukupnoCetSesija;
    }

    public void setUkupnoCetSesija(Long ukupnoCetSesija) {
        this.ukupnoCetSesija = ukupnoCetSesija;
    }

    public Long getUkupnoPorukaKaAI() {
        return ukupnoPorukaKaAI;
    }

    public void setUkupnoPorukaKaAI(Long ukupnoPorukaKaAI) {
        this.ukupnoPorukaKaAI = ukupnoPorukaKaAI;
    }

    public BigDecimal getProsekPorukaPoSesijiGodisnje() {
        return prosekPorukaPoSesijiGodisnje;
    }

    public void setProsekPorukaPoSesijiGodisnje(BigDecimal prosekPorukaPoSesijiGodisnje) {
        this.prosekPorukaPoSesijiGodisnje = prosekPorukaPoSesijiGodisnje;
    }
}