package ftn.iis.dto;

public class AzurirajElektronskuBazuDto {
    private String naziv;
    private String oblast;
    private String opis;
    private String licenca;
    private Long izdavacId;

    public AzurirajElektronskuBazuDto() {
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

    public Long getIzdavacId() {
        return izdavacId;
    }

    public void setIzdavacId(Long izdavacId) {
        this.izdavacId = izdavacId;
    }
}
