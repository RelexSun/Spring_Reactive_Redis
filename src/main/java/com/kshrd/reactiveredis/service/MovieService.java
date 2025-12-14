package com.kshrd.reactiveredis.service;

import com.kshrd.reactiveredis.model.entity.Movie;
import com.kshrd.reactiveredis.model.request.MovieRequest;
import com.kshrd.reactiveredis.model.response.MovieResponse;
import com.kshrd.reactiveredis.model.response.ReviewResponse;
import com.kshrd.reactiveredis.repository.MovieRepository;
import com.kshrd.reactiveredis.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final CacheService cacheService;

    private static final Duration MOVIE_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration ALL_MOVIES_CACHE_TTL = Duration.ofMinutes(15);

    /**
     * Create a new movie
     */
    @Transactional
    public Mono<MovieResponse> createMovie(MovieRequest request) {
        log.info("Creating new movie: {}", request.getTitle());

        return Mono.just(request)
                .map(MovieRequest::toEntity)
                .flatMap(movieRepository::save)
                .doOnSuccess(movie -> log.info("Movie created with ID: {}", movie.getId()))
                .flatMap(movie -> {
                    MovieResponse response = toMovieResponse(movie);
                    // Cache the newly created movie
                    return cacheService.cacheMovie(movie.getId(), response, MOVIE_CACHE_TTL)
                            .thenReturn(response);
                })
                // Invalidate all movies cache since the list changed
                .flatMap(response ->
                        cacheService.invalidateAllMovies()
                                .thenReturn(response)
                )
                .onErrorResume(e -> {
                    log.error("Failed to create movie", e);
                    return Mono.error(new RuntimeException("Failed to create movie: " + e.getMessage()));
                });
    }

    /**
     * Get all movies with caching
     */
    public Flux<MovieResponse> getAllMovies() {
        log.info("Fetching all movies");

        return cacheService.getAllCachedMovies()
                .switchIfEmpty(
                        movieRepository.findAll()
                                .map(this::toMovieResponse)
                                .collectList()
                                .flatMapMany(movies -> {
                                    // Cache the results
                                    return cacheService.cacheAllMovies(
                                            Flux.fromIterable(movies),
                                            ALL_MOVIES_CACHE_TTL
                                    ).thenMany(Flux.fromIterable(movies));
                                })
                )
                .doOnComplete(() -> log.info("Finished fetching all movies"))
                .onErrorResume(e -> {
                    log.error("Failed to fetch movies", e);
                    return Flux.empty();
                });
    }

    /**
     * Get movie by ID with caching
     */
    public Mono<MovieResponse> getMovieById(Long id) {
        log.info("Fetching movie with ID: {}", id);

        return cacheService.getCachedMovie(id)
                .switchIfEmpty(
                        movieRepository.findById(id)
                                .map(this::toMovieResponse)
                                .flatMap(response ->
                                        cacheService.cacheMovie(id, response, MOVIE_CACHE_TTL)
                                                .thenReturn(response)
                                                .onErrorResume(e -> {
                                                    log.warn("Failed to cache movie {}", id, e);
                                                    return Mono.just(response);
                                                })
                                )
                )
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Movie not found with ID: " + id)
                ))
                .doOnSuccess(movie -> log.info("Found movie: {}", movie.getTitle()))
                .onErrorResume(e -> {
                    log.error("Error fetching movie {}", id, e);
                    return Mono.error(e);
                });
    }

    /**
     * Get movie with reviews
     */
    public Mono<MovieResponse> getMovieWithReviews(Long id) {
        log.info("Fetching movie with reviews, ID: {}", id);

        return movieRepository.findById(id)
                .flatMap(movie -> {
                    MovieResponse response = toMovieResponse(movie);

                    return reviewRepository.findByMovieId(id)
                            .map(review -> ReviewResponse.builder()
                                    .id(review.getId())
                                    .movieId(review.getMovieId())
                                    .reviewerName(review.getReviewerName())
                                    .rating(review.getRating())
                                    .comment(review.getComment())
                                    .createdAt(review.getCreatedAt())
                                    .build())
                            .collectList()
                            .map(reviews -> {
                                response.setReviews(reviews);
                                return response;
                            });
                })
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Movie not found with ID: " + id)
                ));
    }

    /**
     * Update movie
     */
    @Transactional
    public Mono<MovieResponse> updateMovie(Long id, MovieRequest request) {
        log.info("Updating movie with ID: {}", id);

        return movieRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Movie not found with ID: " + id)
                ))
                .map(existing -> {
                    Movie updated = request.toEntity();
                    updated.setId(id);
                    updated.setCreatedAt(existing.getCreatedAt());
                    updated.setUpdatedAt(LocalDateTime.now());
                    return updated;
                })
                .flatMap(movieRepository::save)
                .map(this::toMovieResponse)
                .flatMap(response ->
                        // Invalidate both single movie and all movies cache
                        cacheService.invalidateMovie(id)
                                .then(cacheService.invalidateAllMovies())
                                .thenReturn(response)
                )
                .doOnSuccess(movie -> log.info("Updated movie: {}", movie.getTitle()))
                .onErrorResume(e -> {
                    log.error("Failed to update movie {}", id, e);
                    return Mono.error(e);
                });
    }

    /**
     * Delete movie
     */
    @Transactional
    public Mono<Void> deleteMovie(Long id) {
        log.info("Deleting movie with ID: {}", id);

        return movieRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Movie not found with ID: " + id)
                ))
                .flatMap(movie ->
                        // Delete reviews first (due to foreign key)
                        reviewRepository.deleteByMovieId(id)
                                .then(movieRepository.deleteById(id))
                )
                .then(cacheService.invalidateMovie(id))
                .then(cacheService.invalidateAllMovies())
                .then()
                .doOnSuccess(v -> log.info("Deleted movie with ID: {}", id))
                .onErrorResume(e -> {
                    log.error("Failed to delete movie {}", id, e);
                    return Mono.error(e);
                });
    }

    /**
     * Search movies by title
     */
    public Flux<MovieResponse> searchByTitle(String title) {
        log.info("Searching movies by title: {}", title);

        return movieRepository.findByTitleContainingIgnoreCase(title)
                .map(this::toMovieResponse)
                .doOnComplete(() -> log.info("Search completed for title: {}", title));
    }

    /**
     * Get movies by genre
     */
    public Flux<MovieResponse> getMoviesByGenre(String genre) {
        log.info("Fetching movies by genre: {}", genre);

        return movieRepository.findByGenre(genre)
                .map(this::toMovieResponse)
                .doOnComplete(() -> log.info("Fetched movies for genre: {}", genre));
    }

    /**
     * Get top rated movies
     */
    public Flux<MovieResponse> getTopRatedMovies(int limit) {
        log.info("Fetching top {} rated movies", limit);

        return movieRepository.findTopRatedMovies(limit)
                .map(this::toMovieResponse)
                .doOnComplete(() -> log.info("Fetched top rated movies"));
    }

    /**
     * Clear all caches
     */
    public Mono<Long> clearAllCaches() {
        log.info("Clearing all movie caches");
        return cacheService.clearAllMovieCaches();
    }

    private MovieResponse toMovieResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .releaseYear(movie.getReleaseYear())
                .genre(movie.getGenre())
                .director(movie.getDirector())
                .rating(movie.getRating())
                .durationMinutes(movie.getDurationMinutes())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }
}
