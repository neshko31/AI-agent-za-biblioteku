package nais.search.repository;

import nais.search.model.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends ElasticsearchRepository<Person, String> {
    Optional<Person> findByPersonId(String personId);

    Page<Person> findByFirstName(String firstName, Pageable pageable);

    Page<Person> findByLastName(String lastName, Pageable pageable);

    Page<Person> findByBioContaining(String bio, Pageable pageable);
}
