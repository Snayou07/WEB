package com.cinema.controller;

import com.cinema.dto.MovieDto;
import com.cinema.model.Movie;
import com.cinema.repository.MovieRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j; // <--- Для логування
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
@Slf4j // <--- 1. Ломбок створює логер автоматично
public class MovieController {

    @Autowired private MovieRepository repo;

    // 3. Впровадження конфігурації з файлу
    @Value("${cinema.security.allow-delete:true}")
    private boolean allowDelete;

    @GetMapping
    @Operation(summary = "Отримати всі фільми")
    @Cacheable("movies") // <--- 2. Результат збережеться в кеш
    public List<Movie> getAll() {
        log.info("Отримання списку фільмів (із БД, якщо не в кеші)");
        return repo.findAll();
    }

    @PostMapping
    @Operation(summary = "Додати новий фільм")
    // Додаємо `?` щоб повертати або Movie, або Map з помилками
    public ResponseEntity<?> create(@Valid @RequestBody MovieDto dto) {
        // РУЧНА ПЕРЕВІРКА НА ДУБЛІКАТИ
        if (repo.existsByTitle(dto.getTitle())) {
            // Формуємо JSON помилку: {"title": "текст помилки"}
            return ResponseEntity.badRequest().body(java.util.Map.of("title", "Фільм з такою назвою вже існує!"));
        }

        Movie movie = new Movie(null, dto.getTitle(), dto.getDescription(),
                dto.getReleaseYear(), dto.getRating(), dto.getAvailableVoiceovers());
        return ResponseEntity.ok(repo.save(movie));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити дані фільму")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody MovieDto dto) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();

        // Перевірка: чи зайнята така назва іншим фільмом?
        if (repo.existsByTitleAndIdNot(dto.getTitle(), id)) {
            return ResponseEntity.badRequest().body(java.util.Map.of("title", "Фільм з такою назвою вже існує!"));
        }

        Movie movie = new Movie(id, dto.getTitle(), dto.getDescription(),
                dto.getReleaseYear(), dto.getRating(), dto.getAvailableVoiceovers());
        return ResponseEntity.ok(repo.save(movie));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити фільм")
    @CacheEvict(value = "movies", allEntries = true) // <--- Очистити кеш
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // 3. Перевірка конфігурації
        if (!allowDelete) {
            log.warn("Спроба видалення відхилена налаштуваннями конфігурації");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        }

        log.info("Видалення фільму з ID: {}", id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}