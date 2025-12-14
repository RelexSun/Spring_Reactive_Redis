package com.kshrd.reactiveredis.repository;

import com.kshrd.reactiveredis.model.entity.Movie;
import org.springframework.stereotype.Repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MovieRepository extends R2dbcRepository<Movie, Long> {

    // Find by title (case-insensitive)
    Flux<Movie> findByTitleContainingIgnoreCase(String title);

    // Find by genre
    Flux<Movie> findByGenre(String genre);

    // Find by release year
    Flux<Movie> findByReleaseYear(Integer year);

    // Find by director
    Flux<Movie> findByDirectorContainingIgnoreCase(String director);

    // Custom query to find movies with rating above threshold
    @Query("SELECT * FROM movies WHERE rating >= :minRating ORDER BY rating DESC")
    Flux<Movie> findByRatingGreaterThanEqual(@Param("minRating") Double minRating);

    // Find top rated movies
    @Query("SELECT * FROM movies ORDER BY rating DESC LIMIT :limit")
    Flux<Movie> findTopRatedMovies(@Param("limit") int limit);

    // Count movies by genre
    @Query("SELECT COUNT(*) FROM movies WHERE genre = :genre")
    Mono<Long> countByGenre(@Param("genre") String genre);
}
