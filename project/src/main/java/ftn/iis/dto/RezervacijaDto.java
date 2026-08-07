package ftn.iis.dto;

import ftn.iis.model.Rezervacija;

import java.time.LocalDate;

public class RezervacijaDto {
    private Long idR;
    private LocalDate datR;
    private LocalDate rokKupljR;
    private String kanalR;
    private LocalDate datIspR;
    private LocalDate datObavR;
    private String isbn;
    private String naslovKnjige;
    private String autorKnjige;
    private String putanjaNaslovna;

    public static RezervacijaDto fromRezervacija(Rezervacija r) {
        RezervacijaDto dto = new RezervacijaDto();
        dto.idR = r.getIdR();
        dto.datR = r.getDatR();
        dto.rokKupljR = r.getRokKupljR();
        dto.kanalR = r.getKanalR();
        dto.datIspR = r.getDatIspR();
        dto.datObavR = r.getDatObavR();
        dto.isbn = r.getFizickaKnjiga().getIsbn();
        dto.naslovKnjige = r.getFizickaKnjiga().getKnjiga().getNaslov();
        dto.autorKnjige = r.getFizickaKnjiga().getKnjiga().getAutor();
        dto.putanjaNaslovna = r.getFizickaKnjiga().getKnjiga().getPutanjaNaslovna();
        return dto;
    }

    public Long getIdR() { return idR; }
    public void setIdR(Long idR) { this.idR = idR; }
    public LocalDate getDatR() { return datR; }
    public void setDatR(LocalDate datR) { this.datR = datR; }
    public LocalDate getRokKupljR() { return rokKupljR; }
    public void setRokKupljR(LocalDate rokKupljR) { this.rokKupljR = rokKupljR; }
    public String getKanalR() { return kanalR; }
    public void setKanalR(String kanalR) { this.kanalR = kanalR; }
    public LocalDate getDatIspR() { return datIspR; }
    public void setDatIspR(LocalDate datIspR) { this.datIspR = datIspR; }
    public LocalDate getDatObavR() { return datObavR; }
    public void setDatObavR(LocalDate datObavR) { this.datObavR = datObavR; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getNaslovKnjige() { return naslovKnjige; }
    public void setNaslovKnjige(String naslovKnjige) { this.naslovKnjige = naslovKnjige; }
    public String getAutorKnjige() { return autorKnjige; }
    public void setAutorKnjige(String autorKnjige) { this.autorKnjige = autorKnjige; }
    public String getPutanjaNaslovna() { return putanjaNaslovna; }
    public void setPutanjaNaslovna(String putanjaNaslovna) { this.putanjaNaslovna = putanjaNaslovna; }
}
