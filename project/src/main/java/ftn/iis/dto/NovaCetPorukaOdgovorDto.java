package ftn.iis.dto;

public class NovaCetPorukaOdgovorDto {
    private CetPorukaDto porukaClana;
    private CetPorukaDto porukaAgenta;

    public NovaCetPorukaOdgovorDto() {
    }

    public NovaCetPorukaOdgovorDto(CetPorukaDto porukaClana, CetPorukaDto porukaAgenta) {
        this.porukaClana = porukaClana;
        this.porukaAgenta = porukaAgenta;
    }

    public CetPorukaDto getPorukaClana() {
        return porukaClana;
    }

    public void setPorukaClana(CetPorukaDto porukaClana) {
        this.porukaClana = porukaClana;
    }

    public CetPorukaDto getPorukaAgenta() {
        return porukaAgenta;
    }

    public void setPorukaAgenta(CetPorukaDto porukaAgenta) {
        this.porukaAgenta = porukaAgenta;
    }
}
