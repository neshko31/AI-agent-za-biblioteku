package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.saga.choreography.event;

import java.time.LocalDateTime;

public class BudzetUpdateFailedEvent {
    private String sagaId;
    private String bookId;
    private String razlog;
    private LocalDateTime timestamp;

    public BudzetUpdateFailedEvent() {
    }

    public BudzetUpdateFailedEvent(String sagaId, String bookId, String razlog, LocalDateTime timestamp) {
        this.sagaId = sagaId;
        this.bookId = bookId;
        this.razlog = razlog;
        this.timestamp = timestamp;
    }

    public String getSagaId() { return sagaId; }
    public void setSagaId(String sagaId) { this.sagaId = sagaId; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getRazlog() { return razlog; }
    public void setRazlog(String razlog) { this.razlog = razlog; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

}
