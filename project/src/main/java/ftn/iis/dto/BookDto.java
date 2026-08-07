package ftn.iis.dto;

import ftn.iis.model.Knjiga;

public class BookDto {
    private String naziv;

    private String autor;

    private String sinopsis;

    private boolean fizicka;

    private boolean audio;

    private boolean elektronska;

    public BookDto(String naziv, String autor, String sinopsis, boolean fizicka, boolean audio, boolean elektronska) {
        this.naziv = naziv;
        this.autor = autor;
        this.sinopsis = sinopsis;
        this.fizicka = fizicka;
        this.audio = audio;
        this.elektronska = elektronska;
    }

    public BookDto() {
    }

    public BookDto(Knjiga knjiga){
        naziv = knjiga.getNaslov();
        autor = knjiga.getAutor();
        sinopsis = knjiga.getSinopsis();
        fizicka = knjiga.getFizickaKnjiga()!= null;
        audio = knjiga.getAudioKnjiga()!=null;
        elektronska = knjiga.geteKnjiga()!=null;
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
}
