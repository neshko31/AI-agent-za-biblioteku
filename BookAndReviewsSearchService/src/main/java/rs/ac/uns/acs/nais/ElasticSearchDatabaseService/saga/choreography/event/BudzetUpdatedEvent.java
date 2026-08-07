package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.saga.choreography.event;

import java.time.LocalDateTime;

public class BudzetUpdatedEvent {
    private String sagaId;
    private String bookId;
    private String zanr;
    private Double novoRaspolozivoStanje;
    private LocalDateTime timestamp;

    public BudzetUpdatedEvent() {
    }

    public BudzetUpdatedEvent(String sagaId, String bookId, String zanr,
                              Double novoRaspolozivoStanje, LocalDateTime timestamp) {
        this.sagaId = sagaId;
        this.bookId = bookId;
        this.zanr = zanr;
        this.novoRaspolozivoStanje = novoRaspolozivoStanje;
        this.timestamp = timestamp;
    }

    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getZanr() { return zanr; }
    public void setZanr(String zanr) { this.zanr = zanr; }
    public Double getNovoRaspolozivoStanje() { return novoRaspolozivoStanje; }
    public void setNovoRaspolozivoStanje(Double novoRaspolozivoStanje) { this.novoRaspolozivoStanje = novoRaspolozivoStanje; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

}
