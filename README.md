# Reactive Movie Management System

A comprehensive Spring Boot application demonstrating Reactive Programming with R2DBC (PostgreSQL) and Reactive Redis for caching.

## 📚 What You'll Learn

* **Spring WebFlux** - Building reactive REST APIs
* **R2DBC** - Reactive database access with PostgreSQL
* **Reactive Redis** - Implementing reactive caching strategies
* **Reactive Streams** - Working with `Mono` and `Flux`
* **Error Handling** - Proper error handling in reactive flows
* **Testing** - Writing tests for reactive code using `StepVerifier`

## 🏗️ Project Structure

```text
src/main/java/com/example/reactive/
├── config/
│   └── RedisConfig.java              # Redis configuration
├── controller/
│   └── MovieController.java          # REST endpoints
├── exception/
│   ├── ErrorResponse.java            # Error response DTO
│   └── GlobalErrorHandler.java       # Global error handling
├── model/
│   ├── entity/
│   │   ├── Movie.java               # Movie entity
│   │   └── Review.java              # Review entity
│   ├── request/
│   │   └── MovieRequest.java        # Request DTO
│   └── response/
│       ├── MovieResponse.java       # Response DTO
│       └── ReviewResponse.java      # Review response DTO
├── repository/
│   ├── MovieRepository.java         # R2DBC repository
│   └── ReviewRepository.java        # Review repository
├── service/
│   ├── CacheService.java            # Redis caching logic
│   └── MovieService.java            # Business logic
└── ReactiveApplication.java         # Main application
```

-----

## 🚀 Getting Started

### Prerequisites

* Java 17 or higher
* **Gradle 8.x**
* Docker and Docker Compose

### Step 1: Start Infrastructure

```bash
# Start PostgreSQL and Redis
docker-compose up -d

# Verify services are running
docker ps
```

### Step 2: Build the Project

```bash
./gradlew build
```

### Step 3: Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

-----

## 🧪 Testing the API

1.  **Create a Movie**

<!-- end list -->

```bash
curl -X POST http://localhost:8080/api/movies \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Inception",
    "description": "A thief who steals corporate secrets",
    "releaseYear": 2010,
    "genre": "Sci-Fi",
    "director": "Christopher Nolan",
    "rating": 8.8,
    "durationMinutes": 148
  }'
```

2.  **Stream Movies (SSE)**

<!-- end list -->

```bash
curl http://localhost:8080/api/movies/stream
```

*This will stream movies with a 500ms delay between each.*

-----

## 🔍 Key Concepts Explained

### 1\. Mono vs Flux

* **Mono\<T\>**: Represents 0 or 1 element.
* **Flux\<T\>**: Represents 0 to N elements.

### 2\. Caching Strategy (Cache-Aside)

The application implements a Cache-Aside pattern:

1.  **Read**: Check cache → if miss, read from DB → cache result.
2.  **Write/Update/Delete**: Perform DB operation → invalidate cache.

<!-- end list -->

```java
public Mono<MovieResponse> getMovieById(Long id) {
    return cacheService.getCachedMovie(id)
        .switchIfEmpty(
            movieRepository.findById(id)
                .map(this::toMovieResponse)
                .flatMap(response -> 
                    cacheService.cacheMovie(id, response, CACHE_TTL)
                        .thenReturn(response)
                )
        );
}
```

### 3\. R2DBC vs JPA

| Feature | R2DBC | JPA |
| :--- | :--- | :--- |
| **Programming Model** | Reactive | Blocking |
| **Return Types** | Mono / Flux | Entity / List |
| **Connection Pool** | Non-blocking | Blocking |
| **Performance** | Better under high concurrency | Good for standard CRUD |

-----

## 📊 Performance Tips

* **Use Flux for bulk operations**: `movieRepository.saveAll(movies)` is preferred over manual loops.
* **Backpressure**: Use `.buffer(n)` or `.limitRate(n)` when dealing with high-volume streams.
* **Parallelism**: Use `Mono.zip` to fetch independent data (e.g., Movie details + Reviews) simultaneously.

-----

## 🔧 Monitoring & Debugging

### Redis Check

```bash
docker exec -it redis-reactive redis-cli
KEYS movie:*
TTL movie:1
```

### Debugging Reactive Code

In `build.gradle`, ensure you have the reactor-test dependency. To debug signals:

```java
movieRepository.findAll()
    .doOnNext(movie -> log.debug("Found: {}", movie))
    .log(); // Detailed trace of all reactive signals
```

-----

## 🧪 Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests MovieServiceTest

# Run with detailed logging
./gradlew test -i
```

## 📦 Building for Production

```bash
# Build executable JAR
./gradlew clean bootJar

# Run JAR
java -jar build/libs/reactive-movie-system-1.0.0.jar
```