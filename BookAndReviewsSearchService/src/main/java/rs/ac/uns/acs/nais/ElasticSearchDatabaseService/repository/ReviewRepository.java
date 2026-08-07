package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Review;

@Repository
public interface ReviewRepository extends ElasticsearchRepository<Review, String> {
}
