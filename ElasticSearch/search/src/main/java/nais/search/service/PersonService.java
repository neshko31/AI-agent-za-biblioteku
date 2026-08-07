package nais.search.service;

import nais.search.cache.PersonCacheRepository;
import nais.search.dto.PersonDto;
import nais.search.model.Person;
import nais.search.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonCacheRepository cache;

    public PersonService(PersonRepository personRepository, PersonCacheRepository cache) {
        this.personRepository = personRepository;
        this.cache = cache;
    }

    public Optional<Person> getPersonById(String personId) {
        return cache.get(personId).map(Optional::of).orElseGet(() -> {
            Optional<Person> fromEs = personRepository.findById(personId);
            fromEs.ifPresent(cache::put);
            return fromEs;
        });
    }

    public Person createNewPerson(Person person) {
        Person saved = personRepository.save(person);
        cache.put(saved);
        return saved;
    }

    public boolean deletePerson(String personId) {
        personRepository.deleteById(personId);
        cache.evict(personId);
        return true;
    }

    public Person updatePerson(String personId, PersonDto dto) {
        Optional<Person> existing = personRepository.findById(personId);
        if (existing.isEmpty()) return null;

        Person person = existing.get();

        if (dto.getFirstName()    != null) person.setFirstName(dto.getFirstName());
        if (dto.getLastName()     != null) person.setLastName(dto.getLastName());
        if (dto.getPlaceOfBirth() != null) person.setPlaceOfBirth(dto.getPlaceOfBirth());
        if (dto.getDateOfBirth()  != null) person.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getDateOfDeath()  != null) person.setDateOfDeath(dto.getDateOfDeath());
        if (dto.getRole()         != null) person.setRole(dto.getRole());
        if (dto.getGenres()       != null) person.setGenres(dto.getGenres());
        if (dto.getBooks()        != null) person.setBooks(dto.getBooks());
        if (dto.getBio()          != null) person.setBio(dto.getBio());

        Person updated = personRepository.save(person);
        cache.put(updated);
        return updated;
    }
}
