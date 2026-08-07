package ftn.iis.dto;

import ftn.iis.enums.NacinUplate;
import ftn.iis.model.Kazna;

import java.time.LocalDate;

public class KaznaDto {

    private Long idK;
    private Long idPozajmice;
    private String naslovKnjige;
    private String autorKnjige;
    private String isbn;
    private LocalDate datPoz;
    private LocalDate datOcVrac; // originalni rok
    private LocalDate datNastanka;
    private Integer brojDanaPrekoracenja; // null = izgubljena gradja
    private Integer iznosK;
    private boolean placena;
    private NacinUplate nacinPlacanja;
    private boolean izgubljena;

    public static KaznaDto from(Kazna k) {
        KaznaDto dto = new KaznaDto();
        dto.idK = k.getIdK();
        dto.idPozajmice = k.getPozajmica().getIdP();
        dto.naslovKnjige = k.getPozajmica().getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getNaslov();
        dto.autorKnjige = k.getPozajmica().getPrimerakKnjige().getFizickaKnjiga().getKnjiga().getAutor();
        dto.isbn = k.getPozajmica().getPrimerakKnjige().getFizickaKnjiga().getIsbn();
        dto.datPoz = k.getPozajmica().getDatPoz();
        dto.datOcVrac = k.getPozajmica().getDatOcVrac();
        dto.datNastanka = k.getDatNastanka();
        dto.brojDanaPrekoracenja = k.getBrojDanaPrekoracenja();
        dto.iznosK = k.getIznosK();
        dto.placena = k.isPlacena();
        dto.nacinPlacanja = k.getNacinPlacanja();
        dto.izgubljena = (k.getBrojDanaPrekoracenja() == null);
        return dto;
    }

    public Long getIdK() {
        return idK;
    }

    public void setIdK(Long idK) {
        this.idK = idK;
    }

    public Long getIdPozajmice() {
        return idPozajmice;
    }

    public void setIdPozajmice(Long idPozajmice) {
        this.idPozajmice = idPozajmice;
    }

    public String getNaslovKnjige() {
        return naslovKnjige;
    }

    public void setNaslovKnjige(String naslovKnjige) {
        this.naslovKnjige = naslovKnjige;
    }

    public String getAutorKnjige() {
        return autorKnjige;
    }

    public void setAutorKnjige(String autorKnjige) {
        this.autorKnjige = autorKnjige;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public LocalDate getDatPoz() {
        return datPoz;
    }

    public void setDatPoz(LocalDate datPoz) {
        this.datPoz = datPoz;
    }

    public LocalDate getDatOcVrac() {
        return datOcVrac;
    }

    public void setDatOcVrac(LocalDate datOcVrac) {
        this.datOcVrac = datOcVrac;
    }

    public LocalDate getDatNastanka() {
        return datNastanka;
    }

    public void setDatNastanka(LocalDate datNastanka) {
        this.datNastanka = datNastanka;
    }

    public Integer getBrojDanaPrekoracenja() {
        return brojDanaPrekoracenja;
    }

    public void setBrojDanaPrekoracenja(Integer brojDanaPrekoracenja) {
        this.brojDanaPrekoracenja = brojDanaPrekoracenja;
    }

    public Integer getIznosK() {
        return iznosK;
    }

    public void setIznosK(Integer iznosK) {
        this.iznosK = iznosK;
    }

    public boolean isPlacena() {
        return placena;
    }

    public void setPlacena(boolean placena) {
        this.placena = placena;
    }

    public NacinUplate getNacinPlacanja() {
        return nacinPlacanja;
    }

    public void setNacinPlacanja(NacinUplate nacinPlacanja) {
        this.nacinPlacanja = nacinPlacanja;
    }

    public boolean isIzgubljena() {
        return izgubljena;
    }

    public void setIzgubljena(boolean izgubljena) {
        this.izgubljena = izgubljena;
    }
}
