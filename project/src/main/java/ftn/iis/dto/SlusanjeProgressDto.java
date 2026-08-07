package ftn.iis.dto;

public class SlusanjeProgressDto {
    private Integer trenutnaSekunda;
    private boolean zavrseno;

    public SlusanjeProgressDto() {
    }

    public SlusanjeProgressDto(Integer trenutnaSekunda) {
        this.trenutnaSekunda = trenutnaSekunda;
    }

    public SlusanjeProgressDto(Integer trenutnaSekunda, boolean zavrseno) {
        this.trenutnaSekunda = trenutnaSekunda;
        this.zavrseno = zavrseno;
    }

    public Integer getTrenutnaSekunda() {
        return trenutnaSekunda;
    }

    public void setTrenutnaSekunda(Integer trenutnaSekunda) {
        this.trenutnaSekunda = trenutnaSekunda;
    }

    public boolean isZavrseno() {
        return zavrseno;
    }

    public void setZavrseno(boolean zavrseno) {
        this.zavrseno = zavrseno;
    }
}
