package com.cinema.controller;

import com.cinema.model.Movie;
import com.cinema.repository.MovieRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/movies") // Базовий URL
@Tag(name = "Movie API", description = "API для керування фільмами")
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    // 1. Отримання списку всіх сутностей
    @GetMapping
    @Operation(summary = "Отримати список всіх фільмів")
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // 2. Отримання сутності за ідентифікатором
    @GetMapping("/{id}")
    @Operation(summary = "Отримати фільм за ID")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        Optional<Movie> movie = movieRepository.findById(id);
        return movie.map(ResponseEntity::ok) // 200 OK
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found
    }

    // 3. Додавання нової сутності
    @PostMapping
    @Operation(summary = "Додати новий фільм")
    @ResponseStatus(HttpStatus.CREATED) // 201 Created
    public Movie createMovie(@RequestBody Movie movie) {
        // Переконуємося, що ID буде згенеровано
        movie.setId(null);
        return movieRepository.save(movie);
    }

    // 4. Оновлення існуючої сутності
    @PutMapping("/{id}")
    @Operation(summary = "Оновити фільм за ID")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody Movie movieDetails) {
        if (!movieRepository.existsById(id)) {
            return ResponseEntity.notFound().build(); // 404
        }
        movieDetails.setId(id); // Встановлюємо ID, щоб оновити правильний запис
        return ResponseEntity.ok(movieRepository.save(movieDetails)); // 200 OK
    }

    // 5. Видалення сутності
    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити фільм за ID")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        if (!movieRepository.existsById(id)) {
            return ResponseEntity.notFound().build(); // 404
        }
        movieRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // 204 No Content (успішне видалення)
    }
}