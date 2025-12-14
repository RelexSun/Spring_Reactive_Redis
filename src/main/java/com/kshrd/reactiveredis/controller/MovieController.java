package com.kshrd.reactiveredis.controller;

import com.kshrd.reactiveredis.base.APIResponse;
import com.kshrd.reactiveredis.model.request.MovieRequest;
import com.kshrd.reactiveredis.model.response.MovieResponse;
import com.kshrd.reactiveredis.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.kshrd.reactiveredis.utils.ResponseUtil.buildResponse;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Movie API", description = "Reactive movie management endpoints")
public class MovieController {

    private final MovieService movieService;

    // ===================== CREATE =====================

    @Operation(summary = "Create a new movie")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Movie created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = APIResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public Mono<MovieResponse> createMovie(
            @Valid @RequestBody MovieRequest request) {

        log.info("Received request to create movie: {}", request.getTitle());
        return movieService.createMovie(request);

//        return buildResponse(
//                "Movie created successfully",
//                movieService.createMovie(request),
//                HttpStatus.CREATED
//        );
    }

    // ===================== READ =====================

    @Operation(summary = "Get all movies")
    @ApiResponse(
            responseCode = "200",
            description = "List of movies",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = MovieResponse.class))
            )
    )
    @GetMapping
    public ResponseEntity<APIResponse<Flux<MovieResponse>>> getAllMovies() {

        log.info("Received request to get all movies");

        return buildResponse(
                "Movies retrieved successfully",
                movieService.getAllMovies(),
                HttpStatus.OK
        );
    }

    // ===================== STREAM (SSE) =====================

    @Operation(
            summary = "Stream all movies (SSE)",
            description = "Streams movies as Server-Sent Events using reactive Flux"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Movie event stream",
            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)
    )
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<APIResponse<Flux<MovieResponse>>> streamAllMovies() {

        log.info("Streaming all movies");

        Flux<MovieResponse> stream =
                movieService.getAllMovies().delayElements(Duration.ofMillis(500));

        return buildResponse(
                "Streaming movies",
                stream,
                HttpStatus.OK
        );
    }

    // ===================== GET BY ID =====================

    @Operation(summary = "Get movie by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie found"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<Mono<MovieResponse>>> getMovieById(
            @Parameter(description = "Movie ID", example = "1")
            @PathVariable Long id) {

        log.info("Received request to get movie with ID: {}", id);

        return buildResponse(
                "Movie retrieved successfully",
                movieService.getMovieById(id),
                HttpStatus.OK
        );
    }

    // ===================== GET WITH REVIEWS =====================

    @Operation(summary = "Get movie with reviews")
    @GetMapping("/{id}/with-reviews")
    public ResponseEntity<APIResponse<Mono<MovieResponse>>> getMovieWithReviews(
            @PathVariable Long id) {

        log.info("Received request to get movie with reviews, ID: {}", id);

        return buildResponse(
                "Movie with reviews retrieved",
                movieService.getMovieWithReviews(id),
                HttpStatus.OK
        );
    }

    // ===================== UPDATE =====================

    @Operation(summary = "Update movie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie updated"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<Mono<MovieResponse>>> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequest request) {

        log.info("Received request to update movie with ID: {}", id);

        return buildResponse(
                "Movie updated successfully",
                movieService.updateMovie(id, request),
                HttpStatus.OK
        );
    }

    // ===================== DELETE =====================

    @Operation(summary = "Delete movie")
    @ApiResponse(responseCode = "204", description = "Movie deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Mono<Void>>> deleteMovie(
            @PathVariable Long id) {

        log.info("Received request to delete movie with ID: {}", id);

        return buildResponse(
                "Movie deleted successfully",
                movieService.deleteMovie(id),
                HttpStatus.NO_CONTENT
        );
    }

    // ===================== SEARCH =====================

    @Operation(summary = "Search movies by title")
    @GetMapping("/search")
    public ResponseEntity<APIResponse<Flux<MovieResponse>>> searchMovies(
            @Parameter(description = "Movie title keyword", example = "Avengers")
            @RequestParam String title) {

        log.info("Searching movies with title: {}", title);

        return buildResponse(
                "Search results",
                movieService.searchByTitle(title),
                HttpStatus.OK
        );
    }

    // ===================== FILTER =====================

    @Operation(summary = "Get movies by genre")
    @GetMapping("/genre/{genre}")
    public ResponseEntity<APIResponse<Flux<MovieResponse>>> getMoviesByGenre(
            @PathVariable String genre) {

        log.info("Getting movies by genre: {}", genre);

        return buildResponse(
                "Movies by genre retrieved",
                movieService.getMoviesByGenre(genre),
                HttpStatus.OK
        );
    }

    // ===================== TOP RATED =====================

    @Operation(summary = "Get top rated movies")
    @GetMapping("/top-rated")
    public ResponseEntity<APIResponse<Flux<MovieResponse>>> getTopRatedMovies(
            @Parameter(description = "Max number of movies", example = "10")
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Getting top {} rated movies", limit);

        return buildResponse(
                "Top rated movies retrieved",
                movieService.getTopRatedMovies(limit),
                HttpStatus.OK
        );
    }

    // ===================== CACHE =====================

    @Operation(summary = "Clear all movie caches")
    @DeleteMapping("/cache")
    public ResponseEntity<APIResponse<Mono<String>>> clearCaches() {

        log.info("Clearing all caches");

        Mono<String> result =
                movieService.clearAllCaches()
                        .map(count -> "Cleared " + count + " cache entries");

        return buildResponse(
                "Cache cleared",
                result,
                HttpStatus.OK
        );
    }
}
