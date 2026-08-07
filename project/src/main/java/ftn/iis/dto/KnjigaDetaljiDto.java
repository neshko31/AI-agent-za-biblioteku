package ftn.iis.dto;

import ftn.iis.model.Genre;
import ftn.iis.model.Knjiga;

import java.util.List;
import java.util.stream.Collectors;

public class KnjigaDetaljiDto {
    private String isbn;
    private String naslov;
    private String autor;
    private String sinopsis;
    private String katalog;
    private boolean fizicka;
    private boolean audio;
    private boolean elektronska;
    private Integer brojStrana;
    private Genre zanr;

    public KnjigaDetaljiDto() {
    }

    public KnjigaDetaljiDto(String isbn, String naslov, String autor, String sinopsis, String katalog, boolean fizicka, boolean audio, boolean elektronska, Integer brojStrana) {
        this.isbn = isbn;
        this.naslov = naslov;
        this.autor = autor;
        this.sinopsis = sinopsis;
        this.katalog = katalog;
        this.fizicka = fizicka;
        this.audio = audio;
        this.elektronska = elektronska;
        this.brojStrana = brojStrana;
    }

    public static KnjigaDetaljiDto fromKnjiga(Knjiga knjiga) {
        String tip = knjiga.getTipKnjige();
        boolean fizicka = tip != null && tip.length() > 0 && tip.charAt(0) == '1';
        boolean elektronska = tip != null && tip.length() > 1 && tip.charAt(1) == '1';
        boolean audio = tip != null && tip.length() > 2 && tip.charAt(2) == '1';
        String katalog = knjiga.getKatalog() != null ? knjiga.getKatalog().getKatIme() : null;
        Integer brojStrana = knjiga.geteKnjiga() != null ? knjiga.geteKnjiga().getBrojStranaEK() : null;

        Genre zanr = knjiga.getZanr();

        KnjigaDetaljiDto dto = new KnjigaDetaljiDto();
        dto.isbn = knjiga.getIsbn();
        dto.naslov = knjiga.getNaslov();
        dto.autor = knjiga.getAutor();
        dto.sinopsis = knjiga.getSinopsis();
        dto.katalog = katalog;
        dto.fizicka = fizicka;
        dto.audio = audio;
        dto.elektronska = elektronska;
        dto.brojStrana = brojStrana;
        dto.zanr = zanr;
        return dto;
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

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public String getKatalog() {
        return katalog;
    }

    public void setKatalog(String katalog) {
        this.katalog = katalog;
    }

    public boolean isFizicka() {
        return fizicka;
    }

    public void setFizicka(boolean fizicka) {
        this.fizicka = fizicka;
    }

    public boolean isAudio() {
        return audio;
    }

    public void setAudio(boolean audio) {
        this.audio = audio;
    }

    public boolean isElektronska() {
        return elektronska;
    }

    public void setElektronska(boolean elektronska) {
        this.elektronska = elektronska;
    }

    public Integer getBrojStrana() {
        return brojStrana;
    }

    public void setBrojStrana(Integer brojStrana) {
        this.brojStrana = brojStrana;
    }
}
