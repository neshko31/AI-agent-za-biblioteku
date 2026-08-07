package ftn.iis.dto;

import java.util.List;

public class PozajmiceRezervacijeResponseDto {
    private List<PozajmicaDto> aktivnePozajmice;
    private List<PozajmicaDto> istorijaPozajmica;
    private List<RezervacijaDto> aktivneRezervacije;
    private List<PozajmicaDto> aktivneEKnjige;
    private List<PozajmicaDto> aktivneAudioKnjige;

    public PozajmiceRezervacijeResponseDto() {}

    public List<PozajmicaDto> getAktivnePozajmice() { return aktivnePozajmice; }
    public void setAktivnePozajmice(List<PozajmicaDto> aktivnePozajmice) { this.aktivnePozajmice = aktivnePozajmice; }
    public List<PozajmicaDto> getIstorija() { return istorijaPozajmica; }
    public void setIstorija(List<PozajmicaDto> istorijaPozajmica) { this.istorijaPozajmica = istorijaPozajmica; }
    public List<RezervacijaDto> getAktivneRezervacije() { return aktivneRezervacije; }
    public void setAktivneRezervacije(List<RezervacijaDto> aktivneRezervacije) { this.aktivneRezervacije = aktivneRezervacije; }

    public List<PozajmicaDto> getIstorijaPozajmica() {
        return istorijaPozajmica;
    }

    public void setIstorijaPozajmica(List<PozajmicaDto> istorijaPozajmica) {
        this.istorijaPozajmica = istorijaPozajmica;
    }

    public List<PozajmicaDto> getAktivneEKnjige() {
        return aktivneEKnjige;
    }

    public void setAktivneEKnjige(List<PozajmicaDto> aktivneEKnjige) {
        this.aktivneEKnjige = aktivneEKnjige;
    }

    public List<PozajmicaDto> getAktivneAudioKnjige() {
        return aktivneAudioKnjige;
    }

    public void setAktivneAudioKnjige(List<PozajmicaDto> aktivneAudioKnjige) {
        this.aktivneAudioKnjige = aktivneAudioKnjige;
    }
}
