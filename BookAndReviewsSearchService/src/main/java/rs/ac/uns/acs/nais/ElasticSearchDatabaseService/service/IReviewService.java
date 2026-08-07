package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Review;

import java.util.List;
import java.util.Optional;

public interface IReviewService {
    // ── CREATE ──────────────────────────────────────────────────────────────
    Review save(Review review);
    List<Review> saveAll(List<Review> reviews);

    // ── UPDATE ───────────────────────────────────────────────────────────────
    Review update(String id, Review review);

    // ── DELETE ───────────────────────────────────────────────────────────────
    void deleteById(String id);
    void deleteAllById(List<String> ids);

    // ── READ ─────────────────────────────────────────────────────────────────
    Optional<Review> findById(String id);
    Page<Review> findAll(Pageable pageable);
    List<Review> findAllById(List<String> ids);
}
