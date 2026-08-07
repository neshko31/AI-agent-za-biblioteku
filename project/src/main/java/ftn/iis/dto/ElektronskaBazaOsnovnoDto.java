package ftn.iis.dto;

import ftn.iis.model.ElektronskaBazaPodataka;

public class ElektronskaBazaOsnovnoDto {
    private Long id;
    private String naziv;
    private String oblast;
    private String opis;
    private String licenca;

    public ElektronskaBazaOsnovnoDto() {
    }

    public ElektronskaBazaOsnovnoDto(Long id, String naziv, String oblast, String opis, String licenca) {
        this.id = id;
        this.naziv = naziv;
        this.oblast = oblast;
        this.opis = opis;
        this.licenca = licenca;
    }

    public static ElektronskaBazaOsnovnoDto fromEntity(ElektronskaBazaPodataka baza) {
        return new ElektronskaBazaOsnovnoDto(
                baza.getId(),
                baza.getNaziv(),
                baza.getOblast(),
                baza.getOpis(),
                baza.getLicenca()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getOblast() {
        return oblast;
    }

    public void setOblast(String oblast) {
        this.oblast = oblast;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public String getLicenca() {
        return licenca;
    }

    public void setLicenca(String licenca) {
        this.licenca = licenca;
    }
}
