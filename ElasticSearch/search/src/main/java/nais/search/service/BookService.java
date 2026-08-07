package nais.search.service;

import nais.search.cache.BookCacheRepository;
import nais.search.dto.BookDto;
import nais.search.enums.Format;
import nais.search.model.Book;
import nais.search.model.Person;
import nais.search.ocr.OcrService;
import nais.search.repository.BookRepository;
import nais.search.repository.PersonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final PersonRepository personRepository;
    private final ElasticsearchOperations esOps;
    private final BookCacheRepository cache;
    private final OcrService ocrService;

    public BookService(BookRepository bookRepository,
                       PersonRepository personRepository,
                       ElasticsearchOperations esOps,
                       BookCacheRepository cache,
                       OcrService ocrService) {
        this.bookRepository = bookRepository;
        this.personRepository = personRepository;
        this.esOps = esOps;
        this.cache = cache;
        this.ocrService = ocrService;
    }

    public Optional<Book> getBookByRecordId(String recordId) {
        return cache.get(recordId).map(Optional::of).orElseGet(() -> {
            Optional<Book> fromEs = bookRepository.findById(recordId);
            fromEs.ifPresent(cache::put);
            return fromEs;
        });
    }

    public Optional<Book> findByIsbn(String isbn) {
        Page<Book> page = bookRepository.findByIsbns(isbn, PageRequest.of(0, 1));
        return page.isEmpty() ? Optional.empty() : Optional.of(page.getContent().get(0));
    }

    public Optional<Book> runOcr(String recordId, byte[] pdfBytes, boolean force) throws Exception {
        Optional<Book> existing = bookRepository.findById(recordId);
        if (existing.isEmpty()) return Optional.empty();

        Book book = existing.get();
        if (!force && book.getTextExcerpt() != null && !book.getTextExcerpt().isBlank()) {
            return Optional.of(book);
        }

        String text = ocrService.extractText(pdfBytes);
        book.setTextExcerpt(text);
        Book updated = bookRepository.save(book);
        cache.put(updated);
        return Optional.of(updated);
    }

    public Book createNewBook(Book book) {
        Book saved = bookRepository.save(book);
        cache.put(saved);
        return saved;
    }

    public Book updateBook(String recordId, BookDto dto) {
        Optional<Book> existing = bookRepository.findById(recordId);
        if (existing.isEmpty()) return null;

        Book book = existing.get();

        if (dto.getIsbns()         != null) book.setIsbns(dto.getIsbns());
        if (dto.getTitle()         != null) book.setTitle(dto.getTitle());
        if (dto.getOriginalTitle() != null) book.setOriginalTitle(dto.getOriginalTitle());
        if (dto.getSubtitle()      != null) book.setSubtitle(dto.getSubtitle());
        if (dto.getAuthors()       != null) book.setAuthors(dto.getAuthors());
        if (dto.getGenres()        != null) book.setGenres(dto.getGenres());
        if (dto.getLanguage()      != null) book.setLanguage(dto.getLanguage());
        if (dto.getTranslators()   != null) book.setTranslators(dto.getTranslators());
        if (dto.getIllustrators()  != null) book.setIllustrators(dto.getIllustrators());
        if (dto.getIntroductions() != null) book.setIntroductions(dto.getIntroductions());
        if (dto.getAfterwords()    != null) book.setAfterwords(dto.getAfterwords());
        if (dto.getPublisher()     != null) book.setPublisher(dto.getPublisher());
        if (dto.getPublishDate()   != null) book.setPublishDate(dto.getPublishDate());
        if (dto.getFormat()        != null) book.setFormat(dto.getFormat());
        if (dto.getSeries()        != null) book.setSeries(dto.getSeries());
        if (dto.getTextExcerpt()   != null) book.setTextExcerpt(dto.getTextExcerpt());
        if (dto.getDescription()   != null) book.setDescription(dto.getDescription());
        if (dto.getNumberOfPages() != null) book.setNumberOfPages(dto.getNumberOfPages());
        if (dto.getAwards()        != null) book.setAwards(dto.getAwards());
        if (dto.getSetting()       != null) book.setSetting(dto.getSetting());
        if (dto.getCharacters()    != null) book.setCharacters(dto.getCharacters());

        Book updated = bookRepository.save(book);
        cache.put(updated);
        return updated;
    }

    public boolean deleteBook(String recordId) {
        bookRepository.deleteById(recordId);
        cache.evict(recordId);
        return true;
    }

    public Page<Book> fullTextSearch(String query, Pageable pageable) {
        Criteria criteria = new Criteria("title").matches(query)
                .or("originalTitle").matches(query)
                .or("subtitle").matches(query)
                .or("textExcerpt").matches(query)
                .or("description").matches(query);

        Query searchQuery = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<Book> hits = esOps.search(searchQuery, Book.class);

        List<Book> books = hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(books, pageable, hits::getTotalHits);
    }

    public Page<Book> keywordFilteredSearch(
            String genre, String language, String publisher, Format format,
            String series, String award, String setting, String character,
            String isbn, Pageable pageable) {

        Criteria criteria = new Criteria();
        boolean hasCriteria = false;

        if (genre     != null) { criteria = hasCriteria ? criteria.and("genres").is(genre)         : new Criteria("genres").is(genre);         hasCriteria = true; }
        if (language  != null) { criteria = hasCriteria ? criteria.and("language").is(language)    : new Criteria("language").is(language);    hasCriteria = true; }
        if (publisher != null) { criteria = hasCriteria ? criteria.and("publisher").is(publisher)  : new Criteria("publisher").is(publisher);  hasCriteria = true; }
        if (format    != null) { criteria = hasCriteria ? criteria.and("format").is(format.name()) : new Criteria("format").is(format.name()); hasCriteria = true; }
        if (series    != null) { criteria = hasCriteria ? criteria.and("series").is(series)        : new Criteria("series").is(series);        hasCriteria = true; }
        if (award     != null) { criteria = hasCriteria ? criteria.and("awards").is(award)         : new Criteria("awards").is(award);         hasCriteria = true; }
        if (setting   != null) { criteria = hasCriteria ? criteria.and("setting").is(setting)      : new Criteria("setting").is(setting);      hasCriteria = true; }
        if (character != null) { criteria = hasCriteria ? criteria.and("characters").is(character) : new Criteria("characters").is(character); hasCriteria = true; }
        if (isbn      != null) { criteria = hasCriteria ? criteria.and("isbns").is(isbn)           : new Criteria("isbns").is(isbn);           hasCriteria = true; }

        if (!hasCriteria) return bookRepository.findAll(pageable);

        Query searchQuery = new CriteriaQuery(criteria).setPageable(pageable);
        SearchHits<Book> hits = esOps.search(searchQuery, Book.class);

        List<Book> books = hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(books, pageable, hits::getTotalHits);
    }

    public Page<Book> findBooksByAuthorName(String firstName, String lastName, Pageable pageable) {
        if (firstName == null && lastName == null) {
            return Page.empty(pageable);
        }

        Criteria personCriteria = new Criteria();
        boolean hasCriteria = false;

        if (firstName != null) {
            personCriteria = new Criteria("firstName").is(firstName);
            hasCriteria = true;
        }
        if (lastName != null) {
            personCriteria = hasCriteria
                    ? personCriteria.and("lastName").is(lastName)
                    : new Criteria("lastName").is(lastName);
        }

        Query personQuery = new CriteriaQuery(personCriteria);
        SearchHits<Person> personHits = esOps.search(personQuery, Person.class);

        List<String> personIds = personHits.stream()
                .map(hit -> hit.getContent().getPersonId())
                .collect(Collectors.toList());

        if (personIds.isEmpty()) return Page.empty(pageable);

        Criteria bookCriteria = new Criteria("authors").is(personIds.get(0));
        for (int i = 1; i < personIds.size(); i++) {
            bookCriteria = bookCriteria.or("authors").is(personIds.get(i));
        }

        Query bookQuery = new CriteriaQuery(bookCriteria).setPageable(pageable);
        SearchHits<Book> bookHits = esOps.search(bookQuery, Book.class);

        List<Book> books = bookHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(books, pageable, bookHits::getTotalHits);
    }
}
