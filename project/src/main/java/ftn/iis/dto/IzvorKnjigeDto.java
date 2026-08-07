package ftn.iis.dto;

public class IzvorKnjigeDto {
    private Long id;
    private String naslov;
    private String autor;
    private Double skor;

    public IzvorKnjigeDto(Long id, String naslov, String autor, Double skor) {
        this.id = id;
        this.naslov = naslov;
        this.autor = autor;
        this.skor = skor;
    }

    public IzvorKnjigeDto() {
    }

    public static IzvorKnjigeDto fromKontekstKnjiga(KnjigeOdgovorDto.KontekstKnjiga kontekstKnjiga) {
        return new IzvorKnjigeDto(
                kontekstKnjiga.getId(),
                kontekstKnjiga.getTitle(),
                kontekstKnjiga.getAuthor(),
                kontekstKnjiga.getScore()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getSkor() {
        return skor;
    }

    public void setSkor(Double skor) {
        this.skor = skor;
    }
}
