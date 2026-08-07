package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.impl;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Book;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.repository.BookRepository;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.IBookService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Service layer for book search and management backed by Elasticsearch.
 * Delegates all Elasticsearch interactions to BookRepository, which uses
 * Spring Data Elasticsearch and custom @Query annotations to build various
 * search strategies.
 */
@Service
public class BookService implements IBookService {
    private final BookRepository bookRepository;

    private final ElasticsearchOperations elasticsearchOperations;

    public BookService(BookRepository bookRepository, ElasticsearchOperations elasticsearchOperations) {
        this.bookRepository = bookRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    // CREATE
    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public List<Book> saveAll(List<Book> books) {
        Iterable<Book> saved = bookRepository.saveAll(books);
        return StreamSupport.stream(saved.spliterator(), false).toList();
    }

    // UPDATE
    @Override
    public Book update(String id, Book book) {
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Book with ID '" + id + "' does not exist in index.");
        }
        book.setBookId(id);
        return bookRepository.save(book);
    }

    // DELETE
    @Override
    public void deleteById(String id) {
        bookRepository.deleteById(id);
    }

    @Override
    public void deleteAllById(List<String> ids) {
        bookRepository.deleteAllById(ids);
    }

    // READ
    @Override
    public Optional<Book> findById(String id) {
        return bookRepository.findById(id);
    }

    @Override
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Override
    public List<Book> findAllById(List<String> ids) {
        Iterable<Book> found = bookRepository.findAllById(ids);
        return StreamSupport.stream(found.spliterator(), false).toList();
    }

    // COMPLEX QUERIES

    // COMPLEX QUERY 1
    public record BookSearchResult(
            List<Book> books,
            Map<String, Long> publisherCounts  // publisher -> broj pojavljivanja
    ) {}

    /*
     * Korisnik pretražuje knjige preko teksta. Upit koristi multi_match da pretraži i naslov i opis istovremeno, pri
     * čemu naslovi imaju trostruki značaj uz fuzziness: AUTO zbog tolerisanja pravopisnih grešaka.
     * Rezultati se dodatno filtriraju — prikazuju se samo knjige na odabranom jeziku i koje imaju određen broj stranica.
     * Uz to se prikazuje i distribucija rezultata po izdavaču putem agregacije.
     * Rezultati se sortiraju po relevantnosti (score) i dobavlja se 10 njih uz prikaz nekih polja.
    */

    public BookSearchResult complexSearch1(String userInput, String language, int minPages, int maxPages) {
        Query searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m.multiMatch(mm -> mm.query(userInput).fields("title^3", "description").fuzziness("AUTO")))
                                .filter(f -> f.term(t -> t.field("language").value(language)))
                                .filter(f -> f.range(r -> r.field("pages").gte(JsonData.of(minPages)).lte(JsonData.of(maxPages))))
                        )
                )
                .withAggregation("by_publisher", Aggregation.of(a -> a.terms(t -> t.field("publisher").size(10))))
                .withSort(Sort.by(Sort.Direction.DESC, "_score"))
                .withMaxResults(10)
                .withSourceFilter(new FetchSourceFilter(new String[]{"title", "author", "pages", "publisher", "language"}, null))
                .build();

        SearchHits<Book> hits = elasticsearchOperations.search(searchQuery, Book.class);
        List<Book> books = hits.getSearchHits().stream().map(SearchHit::getContent).toList();

        Map<String, Long> publisherCounts = new LinkedHashMap<>();
        ElasticsearchAggregations aggs = (ElasticsearchAggregations) hits.getAggregations();

        aggs.get("by_publisher").aggregation().getAggregate().sterms().buckets().array()
                .forEach(bucket -> publisherCounts.put(bucket.key().stringValue(), bucket.docCount()));

        return new BookSearchResult(books, publisherCounts);
    }

    // COMPLEX QUERY 3
    public record BookSearchResult3(
            List<Book> books,
            Map<String, Double> genreAvgLiked,
            Map<String, Double> publisherAvgLiked
    ) {}

    /*
     * Korisnik pretražuje knjige slobodnim tekstom (multi_match po naslovu i opisu, fuzziness: AUTO).
     * Filtriraju se samo knjige na engleskom jeziku koje imaju likedPercent >= 95 i numRatings <= 500000.
     * Cilj je pronaći "skrivene dragulje" — visoko ocenjene knjige bez previše rejtinga.
     * Rezultati se sortiraju po relevantnosti, pa po likedPercent.
     * Agregacija po žanru prikazuje prosečni likedPercent, sortirana po prosečnom likedPercent (DESC).
     * Agregacija po izdavaču prikazuje top 5 sa prosečnim likedPercent.
     * Vraća se 10 dokumenata sa odabranim poljima.
     */

    public BookSearchResult3 complexSearch3(String userInput) {
        Query searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m.multiMatch(mm -> mm.query(userInput).fields("title^3", "description").fuzziness("AUTO")))
                                .filter(f -> f.term(t -> t.field("language").value("English")))
                                .filter(f -> f.range(t -> t.field("likedPercent").gte(JsonData.of(95))))
                                .filter(f -> f.range(r -> r.field("numRatings").lte(JsonData.of(50000))))
                        )
                )
                .withSort(Sort.by(Sort.Direction.DESC, "_score"))
                .withSort(Sort.by(Sort.Direction.DESC, "likedPercent"))
                .withAggregation("by_genre", Aggregation.of(a -> a
                        .terms(t -> t
                                .field("genres")
                                .size(10)
                                .order(List.of(NamedValue.of("avg_liked", SortOrder.Desc)))
                        )
                        .aggregations("avg_liked", Aggregation.of(inner -> inner.avg(avg -> avg.field("likedPercent"))))
                        )
                )
                .withAggregation("by_publisher", Aggregation.of(a -> a
                        .terms(t -> t
                                .field("publisher")
                                .size(5)
                        )
                        .aggregations("avg_liked", Aggregation.of(inner -> inner.avg(avg -> avg.field("likedPercent"))))
                        )
                )
                .withMaxResults(10)
                .withSourceFilter(new FetchSourceFilter(new String[]{"title", "author", "genres", "likedPercent", "numRatings", "publisher"}, null))
                .build();

        SearchHits<Book> hits = elasticsearchOperations.search(searchQuery, Book.class);
        List<Book> books = hits.getSearchHits().stream().map(SearchHit::getContent).toList();

        ElasticsearchAggregations aggs = (ElasticsearchAggregations) hits.getAggregations();

        Map<String, Double> genreAvgLiked = new LinkedHashMap<>();
        aggs.get("by_genre").aggregation().getAggregate().sterms().buckets().array()
                .forEach(bucket -> {
                    double avgLiked = bucket.aggregations().get("avg_liked").avg().value();
                    genreAvgLiked.put(bucket.key().stringValue(), avgLiked);
                });

        Map<String, Double> publisherAvgLiked = new LinkedHashMap<>();
        aggs.get("by_publisher").aggregation().getAggregate().sterms().buckets().array()
                .forEach(bucket -> {
                    double avgLiked = bucket.aggregations().get("avg_liked").avg().value();
                    publisherAvgLiked.put(bucket.key().stringValue(), avgLiked);
                });

        return new BookSearchResult3(books, genreAvgLiked, publisherAvgLiked);
    }
}
