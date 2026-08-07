package ftn.iis.dto;

import ftn.iis.model.Knjiga;

public class KnjigaOsnovnoDto {
    private String isbn;
    private String naslov;
    private String sinopsis;
    private String autor;
    private String putanjaNaslovna;

    public KnjigaOsnovnoDto(String isbn, String naslov, String sinopsis, String autor, String putanjaNaslovna) {
        this.isbn = isbn;
        this.naslov = naslov;
        this.sinopsis = sinopsis;
        this.autor = autor;
        this.putanjaNaslovna = putanjaNaslovna;
    }

    public KnjigaOsnovnoDto() {
    }

    public static KnjigaOsnovnoDto fromKnjiga(Knjiga knjiga) {
        return new KnjigaOsnovnoDto(
                knjiga.getIsbn(),
                knjiga.getNaslov(),
                knjiga.getSinopsis(),
                knjiga.getAutor(),
                knjiga.getPutanjaNaslovna()
        );
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

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getPutanjaNaslovna() {
        return putanjaNaslovna;
    }

    public void setPutanjaNaslovna(String putanjaNaslovna) {
        this.putanjaNaslovna = putanjaNaslovna;
    }
}
