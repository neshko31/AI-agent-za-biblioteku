package rs.ac.uns.acs.nais.ElasticSearchDatabaseService.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.model.Review;
import rs.ac.uns.acs.nais.ElasticSearchDatabaseService.service.impl.ReviewService;

import java.util.List;

/**
 * REST controller for book search and management backed by Elasticsearch.
 * Base path: /books
 * Exposes a range of full-text search strategies over the books index.
 *  * ┌─────────────────────────────────────────────────────────────────────────┐
 *  * │ CRUD Method │       Path          │  Description                        │
 *  * ├─────────────────────────────────────────────────────────────────────────┤
 *  * │  POST       │  /reviews           │  Create 1 review                    │
 *  * │  POST       │  /reviews/batch     │  Create N reviews                   │
 *  * │  GET        │  /reviews/{id}      │  Get 1 review by ID                 │
 *  * │  GET        │  /reviews           │  Get all reviews                    │
 *  * │  GET        │  /reviews/batch     │  Get reviews by ID list             │
 *  * │  PUT        │  /reviews/{id}      │  Update 1 review                    │
 *  * │  DELETE     │  /reviews/{id}      │  Delete 1 review                    │
 *  * │  DELETE     │  /reviews/batch     │  Delete reviews by ID list          │
 *  * └─────────────────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        Review saved = reviewService.save(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Review>> addReviews(@RequestBody List<Review> reviews) {
        List<Review> saved = reviewService.saveAll(reviews);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable String id, @RequestBody Review review) {
        try {
            Review updated = reviewService.update(id, review);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteReviews(@RequestBody List<String> ids) {
        reviewService.deleteAllById(ids);
        return ResponseEntity.noContent().build();
    }

    // READ
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReview(@PathVariable String id) {
        return reviewService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<Review>> getAllReviews(@PageableDefault(size = 20) Pageable pageable) {
        Page<Review> reviews = reviewService.findAll(pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/batch")
    public ResponseEntity<List<Review>> getReviewsByIds(@RequestParam List<String> ids) {
        List<Review> reviews = reviewService.findAllById(ids);
        return ResponseEntity.ok(reviews);
    }

    // COMPLEX QUERIES
    @GetMapping("/complex-query-2")
    public List<ReviewService.BookReviewStats> getBookReviewStats(@RequestParam(defaultValue = "1") int minVotes, @RequestParam(defaultValue = "1") int minComments) {
        return reviewService.complexSearch2(minVotes, minComments);
    }
}
