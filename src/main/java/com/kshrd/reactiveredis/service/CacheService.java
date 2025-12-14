package com.kshrd.reactiveredis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.reactiveredis.model.response.MovieResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.kshrd.reactiveredis.common.constant.MovieConstant.ALL_MOVIES_KEY;
import static com.kshrd.reactiveredis.common.constant.MovieConstant.CACHE_KEY_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    @Qualifier("movieRedisTemplate")
    private final ReactiveRedisTemplate<String, MovieResponse> movieRedisTemplate;

    private final ReactiveRedisTemplate<String, String> stringRedisTemplate;

    private final ObjectMapper objectMapper;

    /**
     * Cache a single movie
     */
    public Mono<Boolean> cacheMovie(Long movieId, MovieResponse movie, Duration ttl) {
        return movieRedisTemplate.opsForValue()
                .set(CACHE_KEY_PREFIX + movieId, movie, ttl)
                .doOnSuccess(result ->
                        log.debug("Cached movie {}: {}", movieId, result))
                .onErrorReturn(false);
    }

    /**
     * Get cached movie
     */
    public Mono<MovieResponse> getCachedMovie(Long movieId) {
        return movieRedisTemplate.opsForValue()
                .get(CACHE_KEY_PREFIX + movieId)
                .doOnNext(movie ->
                        log.debug("Cache hit for movie {}", movieId))
                .onErrorResume(e -> {
                    log.error("Cache error for movie {}", movieId, e);
                    return Mono.empty();
                });
    }

    /**
     * Cache all movies (as a list)
     */
    public Mono<Boolean> cacheAllMovies(Flux<MovieResponse> movies, Duration ttl) {
        return movies.collectList()
                .flatMap(list -> Mono.fromCallable(() ->
                        objectMapper.writeValueAsString(list)))
                .flatMap(json ->
                        stringRedisTemplate.opsForValue()
                                .set(ALL_MOVIES_KEY, json, ttl))
                .doOnSuccess(result ->
                        log.debug("Cached all movies"))
                .onErrorReturn(false);
    }

    /**
     * Get all cached movies
     */
    public Flux<MovieResponse> getAllCachedMovies() {
        return stringRedisTemplate.opsForValue()
                .get(ALL_MOVIES_KEY)
                .flatMapMany(json -> {
                    try {
                        MovieResponse[] movies =
                                objectMapper.readValue(json, MovieResponse[].class);
                        return Flux.fromArray(movies);
                    } catch (Exception e) {
                        log.error("Failed to deserialize movie list", e);
                        return Flux.empty();
                    }
                });
    }

    /**
     * Invalidate movie cache
     */
    public Mono<Boolean> invalidateMovie(Long movieId) {
        return movieRedisTemplate.delete(CACHE_KEY_PREFIX + movieId)
                .map(count -> count > 0)
                .onErrorReturn(false);
    }

    /**
     * Invalidate all movies cache
     */
    public Mono<Boolean> invalidateAllMovies() {
        return stringRedisTemplate.delete(ALL_MOVIES_KEY)
                .map(count -> count > 0)
                .onErrorReturn(false);
    }

    /**
     * Check if movie is cached
     */
    public Mono<Boolean> isMovieCached(Long movieId) {
        return movieRedisTemplate.hasKey(CACHE_KEY_PREFIX + movieId);
    }

    /**
     * Get all movie keys
     */
    public Flux<String> getAllMovieKeys() {
        return movieRedisTemplate.keys(CACHE_KEY_PREFIX + "*");
    }

    /**
     * Clear all movie caches
     */
    public Mono<Long> clearAllMovieCaches() {
        return movieRedisTemplate.keys(CACHE_KEY_PREFIX + "*")
                .collectList()
                .flatMap(keys -> {
                    keys.add(ALL_MOVIES_KEY);
                    return movieRedisTemplate.delete(keys.toArray(new String[0]));
                })
                .doOnSuccess(count ->
                        log.info("Cleared {} movie cache entries", count));
    }

}
