package ftn.iis.dto;

public class IzvorRecenzijeDto {
    private Long id;
    private String reviewId;
    private String isbn;
    private Integer rating;
    private Double skor;

    public IzvorRecenzijeDto(Long id, String reviewId, String isbn, Integer rating, Double skor) {
        this.id = id;
        this.reviewId = reviewId;
        this.isbn = isbn;
        this.rating = rating;
        this.skor = skor;
    }

    public IzvorRecenzijeDto() {
    }

    public static IzvorRecenzijeDto fromKontekstRecenzija(RecenzijeOdgovorDto.KontekstRecenzija kontekstRecenzija) {
        return new IzvorRecenzijeDto(
                kontekstRecenzija.getId(),
                kontekstRecenzija.getReviewId(),
                kontekstRecenzija.getIsbn(),
                kontekstRecenzija.getRating(),
                kontekstRecenzija.getScore()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Double getSkor() {
        return skor;
    }

    public void setSkor(Double skor) {
        this.skor = skor;
    }
}
