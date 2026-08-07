package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Book;

import java.util.List;
import java.util.Optional;

public interface IBookService {
    // ── CREATE ──────────────────────────────────────────────────────────────
    Book save(Book book);
    List<Book> saveAll(List<Book> books);

    // ── UPDATE ───────────────────────────────────────────────────────────────
    Book update(String id, Book book);

    // ── DELETE ───────────────────────────────────────────────────────────────
    void deleteById(String id);
    void deleteAllById(List<String> ids);

    // ── READ ─────────────────────────────────────────────────────────────────
    Optional<Book> findById(String id);
    Page<Book> findAll(Pageable pageable);
    List<Book> findAllById(List<String> ids);
}
