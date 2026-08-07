package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.saga.choreography;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.configuration.RabbitMQConfig;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Book;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.saga.choreography.event.BookReturnedToSupplierEvent;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.impl.BookService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Postupak je sledeci :
// 1) Brise se knjiga
// 2) Cuva snapshot u slucaju da bude potrebna kompenzaciona operacija
// 3) Objavljuje event


// Napomenaaa -> Snapshot se cuva u memoriji (sagaId -> Book) jer je zivotni vek sage kratak
@Slf4j
@Service
public class SagaChoreographyService {
    private final BookService bookService;
    private final RabbitTemplate rabbitTemplate;
    private final Map<String, Book> sagaSnapshots = new ConcurrentHashMap<>();

    public SagaChoreographyService(BookService bookService, RabbitTemplate rabbitTemplate) {
        this.bookService = bookService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public String vratiKnjiguDobavljacu(String bookId) {
        String sagaId = UUID.randomUUID().toString();
        log.info("[CHOREOGRAPHY] sagaId={} -- pokrecem vracanje knjige dobavljacu, bookId={}", sagaId, bookId);

        Book knjiga = bookService.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Knjiga sa ID '" + bookId + "' ne postoji"));

        sagaSnapshots.put(sagaId, knjiga);

        String zanr = (knjiga.getGenres() != null && !knjiga.getGenres().isEmpty())
                ? knjiga.getGenres().get(0) : null;

        // 1) brisanje iz Elasticsearch-a
        try {
            bookService.deleteById(bookId);
            log.info("[CHOREOGRAPHY] sagaId={} -- knjiga bookId={} obrisana iz Elasticsearch indeksa", sagaId, bookId);
        } catch (Exception e) {
            sagaSnapshots.remove(sagaId);
            log.error("[CHOREOGRAPHY] sagaId={} -- GRESKA prilikom brisanja knjige bookId={}: {}", sagaId, bookId, e.getMessage(), e);
            throw new RuntimeException("Elasticsearch brisanje neuspesno za sagaId=" + sagaId, e);
        }

        // 2) objava BookReturnedToSupplierEvent koji ce uhvatiti TimeseriesDatabaseService
        BookReturnedToSupplierEvent event = new BookReturnedToSupplierEvent(
                sagaId, bookId, knjiga.getTitle(), zanr, knjiga.getPrice(), LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CHOREOGRAPHY_EXCHANGE,
                    RabbitMQConfig.BOOK_RETURNED_TO_SUPPLIER_KEY,
                    event);
            log.info("[CHOREOGRAPHY] sagaId={} -- BookReturnedToSupplierEvent objavljen na exchange={}, key={}",
                    sagaId, RabbitMQConfig.CHOREOGRAPHY_EXCHANGE, RabbitMQConfig.BOOK_RETURNED_TO_SUPPLIER_KEY);
        } catch (Exception e) {
            log.error("[CHOREOGRAPHY] sagaId={} -- GRESKA prilikom objave BookReturnedToSupplierEvent: {}",
                    sagaId, e.getMessage(), e);
            // RabbitMQ dostava nije prosla -- odmah kompenzujemo, nema smisla cekati event koji nikad nece stici
            log.warn("[CHOREOGRAPHY] sagaId={} -- vracam knjigu nazad zbog greske u objavi eventa", sagaId);
            bookService.save(knjiga);
            sagaSnapshots.remove(sagaId);
            throw new RuntimeException("RabbitMQ dostava nije prosla za sagaId=" + sagaId, e);
        }

        return sagaId; // sagaId kako bi se identifikovala saga instanca
    }

    // pomocna funkcijica ~~~

    Map<String, Book> getSagaSnapshots() {
        return sagaSnapshots;
    }

}
