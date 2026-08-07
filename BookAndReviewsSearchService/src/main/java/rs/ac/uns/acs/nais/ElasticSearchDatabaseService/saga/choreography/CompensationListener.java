package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.saga.choreography;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.configuration.RabbitMQConfig;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Book;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.saga.choreography.event.BudzetUpdateFailedEvent;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.saga.choreography.event.BudzetUpdatedEvent;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.impl.BookService;

import java.util.Map;

@Slf4j
@Component
public class CompensationListener {

    private final BookService bookService;
    private final Map<String, Book> sagaSnapshots;


    public CompensationListener(BookService bookService, SagaChoreographyService sagaChoreographyService) {
        this.bookService = bookService;
        this.sagaSnapshots = sagaChoreographyService.getSagaSnapshots();
    }


    @RabbitListener(queues = RabbitMQConfig.BUDGET_UPDATE_FAILED_QUEUE)
    public void handleBudgetUpdateFailed(BudzetUpdateFailedEvent event) {
        log.warn("[CHOREOGRAPHY] sagaId={} -- primljen BudgetUpdateFailedEvent za bookId={}, razlog={}",
                event.getSagaId(), event.getBookId(), event.getRazlog());

        Book snapshot = sagaSnapshots.remove(event.getSagaId());
        if (snapshot == null) {
            log.error("[CHOREOGRAPHY] sagaId={} -- nema sacuvanog snapshot-a, kompenzacija nije moguca za bookId={}",
                    event.getSagaId(), event.getBookId());
            return;
        }

        try {
            bookService.save(snapshot);
            log.info("[CHOREOGRAPHY] sagaId={} -- kompenzacija uspesna, knjiga bookId={} vracena u indeks",
                    event.getSagaId(), event.getBookId());
        } catch (Exception e) {
            log.error("[CHOREOGRAPHY] sagaId={} -- GRESKA prilikom kompenzacije za bookId={}: {}",
                    event.getSagaId(), event.getBookId(), e.getMessage(), e);
        }
    }


    @RabbitListener(queues = RabbitMQConfig.BUDGET_UPDATED_QUEUE)
    public void handleBudgetUpdated(BudzetUpdatedEvent event) {
        log.info("[CHOREOGRAPHY] sagaId={} -- saga uspesno zavrsena, budzet za zanr={} azuriran na {}",
                event.getSagaId(), event.getZanr(), event.getNovoRaspolozivoStanje());
        sagaSnapshots.remove(event.getSagaId());
    }

}