package ftn.iis.dto;

public class UpdateBookWithMediaDto {
    private String naslov;
    private String autor;
    private String sinopsis;
    private Long katId;
    private Integer brojStranaEK;
    private Integer trajanjeSekundeAK;

    public UpdateBookWithMediaDto() {}

    public UpdateBookWithMediaDto(String naslov, String autor, String sinopsis, Long katId, Integer brojStranaEK, Integer trajanjeSekundeAK) {
        this.naslov = naslov;
        this.autor = autor;
        this.sinopsis = sinopsis;
        this.katId = katId;
        this.brojStranaEK = brojStranaEK;
        this.trajanjeSekundeAK = trajanjeSekundeAK;
    }

    public String getNaslov() { return naslov; }
    public void setNaslov(String naslov) { this.naslov = naslov; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getSinopsis() { return sinopsis; }
    public void setSinopsis(String sinopsis) { this.sinopsis = sinopsis; }

    public Long getKatId() { return katId; }
    public void setKatId(Long katId) { this.katId = katId; }

    public Integer getBrojStranaEK() { return brojStranaEK; }
    public void setBrojStranaEK(Integer brojStranaEK) { this.brojStranaEK = brojStranaEK; }

    public Integer getTrajanjeSekundeAK() { return trajanjeSekundeAK; }
    public void setTrajanjeSekundeAK(Integer trajanjeSekundeAK) { this.trajanjeSekundeAK = trajanjeSekundeAK; }
}