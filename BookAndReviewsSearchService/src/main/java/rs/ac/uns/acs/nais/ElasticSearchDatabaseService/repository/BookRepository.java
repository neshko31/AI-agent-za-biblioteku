package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Book;

@Repository
public interface BookRepository extends ElasticsearchRepository<Book, String> {
}
