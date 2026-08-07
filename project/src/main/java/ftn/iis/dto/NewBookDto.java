package ftn.iis.dto;

import jakarta.validation.constraints.NotBlank;

public class NewBookDto {
    @NotBlank
    private String isbn;

    @NotBlank
    private String naziv;

    private String autor;

    private String sinopsis;

    @NotBlank
    private Long katId;

    public Long getKatId() {
        return katId;
    }

    public NewBookDto(String isbn, String naziv, String autor, String sinopsis, Long katId) {
        this.isbn = isbn;
        this.naziv = naziv;
        this.autor = autor;
        this.sinopsis = sinopsis;
        this.katId = katId;
    }

    public void setKatId(Long katId) {
        this.katId = katId;
    }

    public NewBookDto() {
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
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
}
