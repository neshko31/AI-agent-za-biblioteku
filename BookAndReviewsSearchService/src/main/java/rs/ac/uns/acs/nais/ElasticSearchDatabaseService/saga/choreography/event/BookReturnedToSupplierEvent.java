package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.saga.choreography.event;

import java.time.LocalDateTime;

public class BookReturnedToSupplierEvent {
    private String sagaId;
    private String bookId;
    private String naslovKnjige;
    private String zanr;
    private Double iznosPovracaja;
    private LocalDateTime timestamp;

    public BookReturnedToSupplierEvent() {
    }

    public BookReturnedToSupplierEvent(String sagaId, String bookId, String naslovKnjige,
                                       String zanr, Double iznosPovracaja, LocalDateTime timestamp) {
        this.sagaId = sagaId;
        this.bookId = bookId;
        this.naslovKnjige = naslovKnjige;
        this.zanr = zanr;
        this.iznosPovracaja = iznosPovracaja;
        this.timestamp = timestamp;
    }

    public String getSagaId() {
        return sagaId;
    }
    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }
    public String getBookId() {
        return bookId;
    }
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    public String getNaslovKnjige() {
        return naslovKnjige;
    }
    public void setNaslovKnjige(String naslovKnjige) {
        this.naslovKnjige = naslovKnjige;
    }
    public String getZanr() {
        return zanr;
    }
    public void setZanr(String zanr) {
        this.zanr = zanr;
    }
    public Double getIznosPovracaja() {
        return iznosPovracaja;
    }
    public void setIznosPovracaja(Double iznosPovracaja) {
        this.iznosPovracaja = iznosPovracaja;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}