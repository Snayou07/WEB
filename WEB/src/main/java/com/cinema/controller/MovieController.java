package com.cinema.controller;

import com.cinema.dto.MovieDto;
import com.cinema.model.Movie;
import com.cinema.repository.MovieRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@Slf4j
public class MovieController {

    @Autowired private MovieRepository repo;

    @Value("${cinema.security.allow-delete:true}")
    private boolean allowDelete;

    @GetMapping
    @Operation(summary = "Отримати всі фільми")
    @Cacheable("movies")
    public List<Movie> getAll() {
        log.info("Отримання списку фільмів");
        return repo.findAll();
    }

    // ✅ ДОДАНО: Отримання одного фільму за ID
    @GetMapping("/{id}")
    @Operation(summary = "Отримати фільм за ID")
    public ResponseEntity<Movie> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Додати новий фільм")
    @CacheEvict(value = "movies", allEntries = true)
    public ResponseEntity<?> create(@Valid @RequestBody MovieDto dto) {
        if (repo.existsByTitle(dto.getTitle())) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("title", "Фільм з такою назвою вже існує!"));
        }

        Movie movie = new Movie(null, dto.getTitle(), dto.getDescription(),
                dto.getReleaseYear(), dto.getRating(), dto.getAvailableVoiceovers());

        log.info("Додано новий фільм: {}", dto.getTitle());
        return ResponseEntity.ok(repo.save(movie));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити дані фільму")
    @CacheEvict(value = "movies", allEntries = true)
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody MovieDto dto) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        if (repo.existsByTitleAndIdNot(dto.getTitle(), id)) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("title", "Фільм з такою назвою вже існує!"));
        }

        Movie movie = new Movie(id, dto.getTitle(), dto.getDescription(),
                dto.getReleaseYear(), dto.getRating(), dto.getAvailableVoiceovers());

        log.info("Оновлено фільм ID {}: {}", id, dto.getTitle());
        return ResponseEntity.ok(repo.save(movie));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити фільм")
    @CacheEvict(value = "movies", allEntries = true)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!allowDelete) {
            log.warn("Спроба видалення відхилена конфігурацією");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        log.info("Видалено фільм ID: {}", id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}