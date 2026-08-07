package ftn.iis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class KnjigeOdgovorDto {
    private String response;

    @JsonProperty("context_books")
    private List<KontekstKnjiga> contextBooks;

    @JsonProperty("cache_hit")
    private String cacheHit;

    public static class KontekstKnjiga {
        private Long id;
        private String title;
        private String author;
        private Double score;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }

    public KnjigeOdgovorDto(String response, List<KontekstKnjiga> contextBooks, String cacheHit) {
        this.response = response;
        this.contextBooks = contextBooks;
        this.cacheHit = cacheHit;
    }

    public KnjigeOdgovorDto() {
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<KontekstKnjiga> getContextBooks() {
        return contextBooks;
    }

    public void setContextBooks(List<KontekstKnjiga> contextBooks) {
        this.contextBooks = contextBooks;
    }

    public String getCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(String cacheHit) {
        this.cacheHit = cacheHit;
    }
}
