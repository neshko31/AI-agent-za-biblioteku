package ftn.iis.dto;

public class NovaCetPorukaDto {
    private String sadrzajPoruke;
    private boolean ukloniSliku;

    public NovaCetPorukaDto() {
    }

    public NovaCetPorukaDto(String sadrzajPoruke) {
        this.sadrzajPoruke = sadrzajPoruke;
    }

    public NovaCetPorukaDto(String sadrzajPoruke, boolean ukloniSliku) {
        this.sadrzajPoruke = sadrzajPoruke;
        this.ukloniSliku = ukloniSliku;
    }

    public String getSadrzajPoruke() {
        return sadrzajPoruke;
    }

    public void setSadrzajPoruke(String sadrzajPoruke) {
        this.sadrzajPoruke = sadrzajPoruke;
    }

    public boolean isUkloniSliku() {
        return ukloniSliku;
    }

    public void setUkloniSliku(boolean ukloniSliku) {
        this.ukloniSliku = ukloniSliku;
    }
}
