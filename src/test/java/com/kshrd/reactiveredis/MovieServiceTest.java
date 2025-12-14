package com.kshrd.reactiveredis;

import com.kshrd.reactiveredis.model.entity.Movie;
import com.kshrd.reactiveredis.model.request.MovieRequest;
import com.kshrd.reactiveredis.model.response.MovieResponse;
import com.kshrd.reactiveredis.repository.MovieRepository;
import com.kshrd.reactiveredis.repository.ReviewRepository;
import com.kshrd.reactiveredis.service.CacheService;
import com.kshrd.reactiveredis.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;
    private MovieRequest testRequest;

    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .description("Test Description")
                .releaseYear(2024)
                .genre("Action")
                .director("Test Director")
                .rating(BigDecimal.valueOf(8.5))
                .durationMinutes(120)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRequest = MovieRequest.builder()
                .title("Test Movie")
                .description("Test Description")
                .releaseYear(2024)
                .genre("Action")
                .director("Test Director")
                .rating(BigDecimal.valueOf(8.5))
                .durationMinutes(120)
                .build();
    }

    @Test
    void createMovie_ShouldReturnMovieResponse() {
        // Given
        when(movieRepository.save(any(Movie.class))).thenReturn(Mono.just(testMovie));
        when(cacheService.cacheMovie(anyLong(), any(MovieResponse.class), any()))
                .thenReturn(Mono.just(true));
        when(cacheService.invalidateAllMovies()).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(movieService.createMovie(testRequest))
                .expectNextMatches(response ->
                        response.getTitle().equals("Test Movie") &&
                                response.getId().equals(1L)
                )
                .verifyComplete();

        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    void getAllMovies_WhenCacheEmpty_ShouldFetchFromDatabase() {
        // Given
        when(cacheService.getAllCachedMovies()).thenReturn(Flux.empty());
        when(movieRepository.findAll()).thenReturn(Flux.just(testMovie));
        when(cacheService.cacheAllMovies(any(), any())).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(movieService.getAllMovies())
                .expectNextCount(1)
                .verifyComplete();

        verify(movieRepository, times(1)).findAll();
    }

    @Test
    void getMovieById_WhenCached_ShouldReturnFromCache() {
        // Given
        MovieResponse cachedResponse = MovieResponse.builder()
                .id(1L)
                .title("Test Movie")
                .build();

        when(cacheService.getCachedMovie(1L)).thenReturn(Mono.just(cachedResponse));

        // When & Then
        StepVerifier.create(movieService.getMovieById(1L))
                .expectNext(cachedResponse)
                .verifyComplete();

        verify(movieRepository, never()).findById(anyLong());
    }

    @Test
    void getMovieById_WhenNotCached_ShouldFetchFromDatabase() {
        // Given
        when(cacheService.getCachedMovie(1L)).thenReturn(Mono.empty());
        when(movieRepository.findById(1L)).thenReturn(Mono.just(testMovie));
        when(cacheService.cacheMovie(anyLong(), any(MovieResponse.class), any()))
                .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(movieService.getMovieById(1L))
                .expectNextMatches(response -> response.getId().equals(1L))
                .verifyComplete();

        verify(movieRepository, times(1)).findById(1L);
        verify(cacheService, times(1)).cacheMovie(anyLong(), any(), any());
    }

    @Test
    void deleteMovie_ShouldInvalidateCacheAndDeleteFromDatabase() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Mono.just(testMovie));
        when(reviewRepository.deleteByMovieId(1L)).thenReturn(Mono.empty());
        when(movieRepository.deleteById(1L)).thenReturn(Mono.empty());
        when(cacheService.invalidateMovie(1L)).thenReturn(Mono.just(true));
        when(cacheService.invalidateAllMovies()).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(movieService.deleteMovie(1L))
                .verifyComplete();

        verify(movieRepository, times(1)).deleteById(1L);
        verify(cacheService, times(1)).invalidateMovie(1L);
        verify(cacheService, times(1)).invalidateAllMovies();
    }
}
