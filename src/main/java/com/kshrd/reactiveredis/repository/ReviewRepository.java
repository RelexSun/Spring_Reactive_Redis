package com.kshrd.reactiveredis.repository;

import com.kshrd.reactiveredis.model.entity.Review;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReviewRepository extends R2dbcRepository<Review, Long> {

    // Find all reviews for a movie
    Flux<Review> findByMovieId(Long movieId);

    // Find reviews by rating
    Flux<Review> findByMovieIdAndRating(Long movieId, Integer rating);

    // Calculate average rating for a movie
    @Query("SELECT AVG(rating) FROM reviews WHERE movie_id = :movieId")
    Mono<Double> calculateAverageRating(@Param("movieId") Long movieId);

    // Count reviews for a movie
    Mono<Long> countByMovieId(Long movieId);

    // Delete all reviews for a movie
    Mono<Void> deleteByMovieId(Long movieId);
}
