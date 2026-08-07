package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.impl;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Review;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.repository.ReviewRepository;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.IReviewService;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Service layer for review search and management backed by Elasticsearch.
 * Delegates all Elasticsearch interactions to ReviewRepository, which uses
 * Spring Data Elasticsearch and custom @Query annotations to build various
 * search strategies.
 */
@Service
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;

    private final ElasticsearchOperations elasticsearchOperations;

    public ReviewService(ReviewRepository reviewRepository, ElasticsearchOperations elasticsearchOperations) {
        this.reviewRepository = reviewRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    // CREATE
    @Override
    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> saveAll(List<Review> reviews) {
        Iterable<Review> saved = reviewRepository.saveAll(reviews);
        return StreamSupport.stream(saved.spliterator(), false).toList();
    }

    // UPDATE
    @Override
    public Review update(String id, Review review) {
        if (!reviewRepository.existsById(id)) {
            throw new IllegalArgumentException("Review with ID '" + id + "' does not exist in index.");
        }
        review.setReviewId(id);
        return reviewRepository.save(review);
    }

    // DELETE
    @Override
    public void deleteById(String id) {
        reviewRepository.deleteById(id);
    }

    @Override
    public void deleteAllById(List<String> ids) {
        reviewRepository.deleteAllById(ids);
    }

    // READ
    @Override
    public Optional<Review> findById(String id) {
        return reviewRepository.findById(id);
    }

    @Override
    public Page<Review> findAll(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    @Override
    public List<Review> findAllById(List<String> ids) {
        Iterable<Review> found = reviewRepository.findAllById(ids);
        return StreamSupport.stream(found.spliterator(), false).toList();
    }

    // COMPLEX QUERIES

    // COMPLEX QUERY 2
    public record BookReviewStats(
            String bookId,
            long reviewCount,
            double avgRating,
            long totalHelpfulVotes,
            long totalComments,
            Map<String, Long> ratingDistribution
    ) {}

    /*
     * Prikazuje statistiku recenzija grupisanu po knjizi (bookId).
     * Filtriraju se samo recenzije koje su korisne — imaju nVotes >= 1 i nComments >= 1,
     * čime se osigurava da su recenzije provereno vredne čitanja.
     * Za svaku knjigu, grupisanje se radi po bookId, koja ima minimum 3 takve recenzije prikazuje se:
     *   - prosečna ocena (avg_rating)
     *   - ukupan broj glasova (total_helpful_votes)
     *   - ukupan broj komentara (total_comments)
     *   - distribucija ocena (koliko recenzija ima svaku od ocena 1–5)
     * Rezultati su sortirani opadajuće po prosečnoj oceni, prikazuje se top 20 knjiga.
     * Nisu potrebni konkretni dokumenti (size: 0) — samo agregacioni podaci.
     */

    public List<BookReviewStats> complexSearch2(int minVotes, int minComments) {
        Query searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .filter(f -> f.range(r -> r.field("nVotes").gte(JsonData.of(minVotes))))
                                .filter(f -> f.range(r -> r.field("nComments").gte(JsonData.of(minComments))))
                        )
                )
                .withAggregation("by_book", Aggregation.of(a -> a
                        .terms(t -> t.field("bookId").size(20).minDocCount(3))
                                .aggregations("avg_rating", Aggregation.of(inner -> inner.avg(avg -> avg.field("rating"))))
                                .aggregations("total_helpful_votes", Aggregation.of(inner -> inner.sum(sum -> sum.field("nVotes"))))
                                .aggregations("total_comments", Aggregation.of(inner -> inner.sum(sum -> sum.field("nComments"))))
                                .aggregations("rating_distribution", Aggregation.of(inner -> inner.terms(t -> t.field("rating").size(5))))
                        )
                )
                .withSort(Sort.by(Sort.Direction.DESC, "rating"))
                .withMaxResults(0)
                .build();

        SearchHits<Review> hits = elasticsearchOperations.search(searchQuery, Review.class);
        ElasticsearchAggregations aggs = (ElasticsearchAggregations) hits.getAggregations();

        return aggs.get("by_book").aggregation().getAggregate().sterms().buckets().array().stream()
                .map(bucket -> {
                    String bookId = bucket.key().stringValue();
                    long reviewCount = bucket.docCount();

                    double avgRating = bucket.aggregations().get("avg_rating").avg().value();
                    long totalHelpfulVotes  = (long) bucket.aggregations().get("total_helpful_votes").sum().value();
                    long totalComments      = (long) bucket.aggregations().get("total_comments").sum().value();

                    Map<String, Long> ratingDistribution = new LinkedHashMap<>();
                    bucket.aggregations().get("rating_distribution").lterms().buckets().array()
                            .forEach(rb -> ratingDistribution.put(String.valueOf(rb.key()), rb.docCount()));

                    return new BookReviewStats(bookId, reviewCount, avgRating, totalHelpfulVotes, totalComments, ratingDistribution);
                }).toList();
    }
}
