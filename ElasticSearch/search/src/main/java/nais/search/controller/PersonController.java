package nais.search.controller;

import nais.search.dto.PersonDto;
import nais.search.model.Person;
import nais.search.service.PersonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/{personId}")
    public Optional<Person> getPersonById(@PathVariable String personId) {
        Optional<Person> person = personService.getPersonById(personId);
        if (person.isEmpty())
            return Optional.empty();
        return person;
    }

    @PostMapping("/create")
    public ResponseEntity<?> newPerson(@RequestBody PersonDto personDto) {
        Person person = new Person(personDto);
        if (personService.createNewPerson(person) != null)
            return ResponseEntity.ok("Person added successfully");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add person");
    }

    @PatchMapping("/{personId}")
    public ResponseEntity<?> updatePerson(@PathVariable String personId, @RequestBody PersonDto personDto) {
        Person updated = personService.updatePerson(personId, personDto);
        if (updated != null)
            return ResponseEntity.ok("Person updated successfully");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update person: invalid id");
    }

    @DeleteMapping("/{personId}")
    public ResponseEntity<?> deletePerson(@PathVariable String personId) {
        try {
            personService.deletePerson(personId);
            return ResponseEntity.ok("Person has been deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete person");
        }
    }
}
