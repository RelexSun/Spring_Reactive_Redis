-- Create movies table
CREATE TABLE IF NOT EXISTS movies (
                                      id BIGSERIAL PRIMARY KEY,
                                      title VARCHAR(255) NOT NULL,
    description TEXT,
    release_year INTEGER,
    genre VARCHAR(100),
    director VARCHAR(255),
    rating DECIMAL(3, 1),
    duration_minutes INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Create reviews table
CREATE TABLE IF NOT EXISTS reviews (
                                       id BIGSERIAL PRIMARY KEY,
                                       movie_id BIGINT NOT NULL,
                                       reviewer_name VARCHAR(255) NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
    );

-- Create indexes
CREATE INDEX idx_movies_title ON movies(title);
CREATE INDEX idx_movies_genre ON movies(genre);
CREATE INDEX idx_reviews_movie_id ON reviews(movie_id);

-- Insert sample data
INSERT INTO movies
    (title, description, release_year, genre, director, rating, duration_minutes)
VALUES
     ('The Shawshank Redemption', 'Two imprisoned men bond over years, finding solace and eventual redemption.', 1994, 'Drama', 'Frank Darabont', 9.3, 142),
     ('The Godfather', 'The aging patriarch of an organized crime dynasty transfers control to his reluctant son.', 1972, 'Crime', 'Francis Ford Coppola', 9.2, 175),
     ('The Dark Knight', 'When the menace known as the Joker emerges, Batman must accept one of the greatest tests.', 2008, 'Action', 'Christopher Nolan', 9.0, 152),
     ('Pulp Fiction', 'The lives of two mob hitmen, a boxer, and a pair of diner bandits intertwine.', 1994, 'Crime', 'Quentin Tarantino', 8.9, 154),
     ('Inception', 'A thief who steals corporate secrets through dream-sharing technology.', 2010, 'Sci-Fi', 'Christopher Nolan', 8.8, 148);

INSERT INTO reviews (movie_id, reviewer_name, rating, comment) VALUES
   (1, 'John Doe', 5, 'Absolutely masterpiece! One of the best films ever made.'),
   (1, 'Jane Smith', 5, 'Timeless classic with amazing performances.'),
   (2, 'Mike Johnson', 5, 'The perfect crime drama. Marlon Brando at his best.'),
   (3, 'Sarah Williams', 5, 'Heath Ledger''s Joker is legendary!'),
   (3, 'Tom Brown', 4, 'Great action and story, but a bit long.');