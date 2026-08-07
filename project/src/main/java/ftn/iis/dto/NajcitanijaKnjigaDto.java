package ftn.iis.dto;

/**
 * DTO koji se vraca klijentu za izvestaj "najcitanije knjige po zanru
 * u poslednjih 30 dana".
 */
public class NajcitanijaKnjigaDto {
    private String isbn;
    private String naslov;
    private long brojCitanja;

    public NajcitanijaKnjigaDto() {
    }

    public NajcitanijaKnjigaDto(String isbn, String naslov, long brojCitanja) {
        this.isbn = isbn;
        this.naslov = naslov;
        this.brojCitanja = brojCitanja;
    }

    public static NajcitanijaKnjigaDto fromProjection(NajcitanijaKnjigaProjection p) {
        return new NajcitanijaKnjigaDto(p.getIsbn(), p.getNaslov(), p.getBrojCitanja());
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public long getBrojCitanja() {
        return brojCitanja;
    }

    public void setBrojCitanja(long brojCitanja) {
        this.brojCitanja = brojCitanja;
    }
}
