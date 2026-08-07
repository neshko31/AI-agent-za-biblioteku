package ftn.iis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RecenzijeOdgovorDto {
    private String response;

    @JsonProperty("context_reviews")
    private List<KontekstRecenzija> contextReviews;

    public static class KontekstRecenzija {
        private Long id;
        @JsonProperty("review_id")
        private String reviewId;
        private String isbn;
        private Integer rating;
        private Double score;

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

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }

    public RecenzijeOdgovorDto(String response, List<KontekstRecenzija> contextReviews) {
        this.response = response;
        this.contextReviews = contextReviews;
    }

    public RecenzijeOdgovorDto () {
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<KontekstRecenzija> getContextReviews() {
        return contextReviews;
    }

    public void setContextReviews(List<KontekstRecenzija> contextReviews) {
        this.contextReviews = contextReviews;
    }
}
