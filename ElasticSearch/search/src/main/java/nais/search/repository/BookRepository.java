package nais.search.repository;

import nais.search.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends ElasticsearchRepository<Book, String> {

    Optional<Book> findByRecordId(String recordId);

    Page<Book> findByIsbns(String isbn, Pageable pageable);

    Page<Book> findByTitleContaining(String title, Pageable pageable);

    Page<Book> findByAuthors(String author, Pageable pageable);

    Page<Book> findByGenres(String genre, Pageable pageable);
}