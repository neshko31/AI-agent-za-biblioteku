package ftn.iis.service;

import ftn.iis.dto.BookDto;
import ftn.iis.dto.KnjigaDetaljiDto;
import ftn.iis.dto.KnjigaOsnovnoDto;
import ftn.iis.model.Knjiga;
import ftn.iis.model.User;
import ftn.iis.repository.*;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnjigaService {
    private final KnjigaRepository knjigaRepository;
    private final UserRepository userRepository;
    private final PozajmicaRepository pozajmicaRepository;
    private final CitanjeEKnjigeRepository citanjeEKnjigeRepository;
    private final SlusanjeAudioKnjigeRepository slusanjeAudioKnjigeRepository;
    public KnjigaService(KnjigaRepository knjigaRepository, UserRepository userRepository, PozajmicaRepository pozajmicaRepository, CitanjeEKnjigeRepository citanjeEKnjigeRepository, SlusanjeAudioKnjigeRepository slusanjeAudioKnjigeRepository) {
        this.knjigaRepository = knjigaRepository;
        this.userRepository= userRepository;
        this.pozajmicaRepository=pozajmicaRepository;
        this.citanjeEKnjigeRepository=citanjeEKnjigeRepository;
        this.slusanjeAudioKnjigeRepository=slusanjeAudioKnjigeRepository;
    }

    public Optional<BookDto> getByISBN(String isbn){
        Optional<Knjiga> k = knjigaRepository.findByIsbn(isbn);
        if(k == null || k.get().isDeleted())
            return null;

        BookDto bookDto = new BookDto(k.get());
        return  Optional.of(bookDto);
    }

    public Knjiga getBookByISBN(String isbn){
        Knjiga k = knjigaRepository.findByIsbn(isbn).get();
        if(k.isDeleted())
            return null;
        return k;
    }

    public List<BookDto> getAll(){
        List<Knjiga> books = knjigaRepository.findAll();
        List<BookDto> retBooks = new ArrayList<>();
        for(Knjiga k: books){
            if(!k.isDeleted())
                retBooks.add(new BookDto(k));
        }

        return retBooks;
    }

    public void newBook(Knjiga knjiga){
        knjigaRepository.saveAndFlush(knjiga);
    }
    //DO NOT ASK ME WHAT DIFFERENCE THERE IS BETWEEN THESE TWO OK I LIKE NAMING THINGS
    public void saveBook(Knjiga knjiga) {
        knjigaRepository.saveAndFlush(knjiga);
    }

    public List<KnjigaOsnovnoDto> ispisiSveKnjige() {
        List<Knjiga> knjige = knjigaRepository.findByDeletedFalse();
        return knjige.stream().map(KnjigaOsnovnoDto::fromKnjiga).collect(Collectors.toList());
    }

    public List<KnjigaOsnovnoDto> pretraziPoNaslovu(String query) {
        List<Knjiga> knjige = knjigaRepository.findByNaslovContainingIgnoreCaseAndDeletedFalse(query);
        return knjige.stream().map(KnjigaOsnovnoDto::fromKnjiga).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<KnjigaDetaljiDto> getDetaljiByIsbn(String isbn) {
        return knjigaRepository.findByIsbn(isbn)
                .filter(k -> !k.isDeleted())
                .map(KnjigaDetaljiDto::fromKnjiga);
    }

    @Transactional
    public List<KnjigaOsnovnoDto> getPreporuceneForUser(String jmbg){
        User user = userRepository.findByJmbg(jmbg).orElse(null);
        if (user == null ) {
            return Collections.emptyList();
        }
        Set<Long> favZanrIds = user.getFavouriteGenres().stream()
                .map(ftn.iis.model.Genre::getId)
                .collect(Collectors.toSet());
        List<Knjiga> genreBooks = knjigaRepository.findByZanroviIdInAndDeletedFalse(favZanrIds);
        Set<String> genreIsbnSet = genreBooks.stream().map(Knjiga::getIsbn).collect(Collectors.toSet());

        List<String> autoriUZanru = knjigaRepository.findAutoriByZanrIds(favZanrIds);
        Set<String> favAuthors = autoriUZanru.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> historyAuthors = pozajmicaRepository.findByClan_Jmbg(jmbg)
                .stream()
                .map(p -> p.getPrimerakKnjige()
                        .getFizickaKnjiga()
                        .getKnjiga()
                        .getAutor())
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> pozajmljeniIsbns = pozajmicaRepository.findByClan_JmbgAndStatusPozTrue(jmbg)
                .stream()
                .map(p -> p.getPrimerakKnjige().getFizickaKnjiga().getIsbn())
                .collect(Collectors.toCollection(java.util.HashSet::new));

        LocalDate cutoff = java.time.LocalDate.now().minusDays(14);
        citanjeEKnjigeRepository.findActiveByJmbg(jmbg, cutoff)
                .forEach(c -> pozajmljeniIsbns.add(c.getId().getIsbnEKnjige()));
        slusanjeAudioKnjigeRepository.findActiveByJmbg(jmbg, cutoff)
                .forEach(s -> pozajmljeniIsbns.add(s.getId().getIsbnAudioKnjige()));
        List<Knjiga> sve = knjigaRepository.findByDeletedFalse();
        List<Map.Entry<Knjiga, Integer>> scored = new ArrayList<>();

        for (Knjiga k : sve) {
            if (pozajmljeniIsbns.contains(k.getIsbn())) continue;
            int score = 0;
            if (genreIsbnSet.contains(k.getIsbn())) {
                score += 3;
            }
            String autor = k.getAutor() != null ? k.getAutor().toLowerCase() : null;

            if (autor != null) {
                if (favAuthors.contains(autor)) {
                    score += 2;
                }
                if (historyAuthors.contains(autor)) {
                    score += 4;
                }
            }
            if (score > 0) {
                scored.add(Map.entry(k, score));
            }
        }
        scored.sort((a, b) -> {
            int cmp = b.getValue().compareTo(a.getValue());
            return cmp != 0 ? cmp : (new Random().nextBoolean() ? 1 : -1);
        });

        return scored.stream()
                .map(e -> KnjigaOsnovnoDto.fromKnjiga(e.getKey()))
                .collect(Collectors.toList());
    }
}
