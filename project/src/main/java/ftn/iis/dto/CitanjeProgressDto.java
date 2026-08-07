package ftn.iis.dto;

public class CitanjeProgressDto {
    private Integer trenutnaStranica;
    private boolean zavrseno;

    public CitanjeProgressDto() {
    }

    public CitanjeProgressDto(Integer trenutnaStranica) {
        this.trenutnaStranica = trenutnaStranica;
    }

    public CitanjeProgressDto(Integer trenutnaStranica, boolean zavrseno) {
        this.trenutnaStranica = trenutnaStranica;
        this.zavrseno = zavrseno;
    }

    public Integer getTrenutnaStranica() {
        return trenutnaStranica;
    }

    public void setTrenutnaStranica(Integer trenutnaStranica) {
        this.trenutnaStranica = trenutnaStranica;
    }

    public boolean isZavrseno() {
        return zavrseno;
    }

    public void setZavrseno(boolean zavrseno) {
        this.zavrseno = zavrseno;
    }
}
