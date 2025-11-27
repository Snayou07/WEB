package com.cinema.controller;

import com.cinema.dto.UserDto;
import com.cinema.model.User;
import com.cinema.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j; // <--- 1. Логування
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "API для керування користувачами")
@Slf4j // Активує логер 'log'
public class UserController {

    @Autowired
    private UserRepository repo;

    // 2. Впроваджуємо налаштування з файлу (окреме для юзерів)
    @Value("${cinema.security.allow-user-delete:true}")
    private boolean allowUserDelete;

    @GetMapping
    @Operation(summary = "Отримати список всіх користувачів")
    @Cacheable("users") // <--- 3. Результат зберігається в кеш "users"
    public List<User> getAll() {
        log.info("Виклик getAll(): Отримання списку користувачів із БД");
        return repo.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати користувача за ID")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        log.info("Виклик getById(): пошук користувача з ID {}", id);
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Створити нового користувача")
    @CacheEvict(value = "users", allEntries = true) // <--- Очищаємо кеш при додаванні
    public ResponseEntity<User> create(@Valid @RequestBody UserDto dto) {
        log.info("Виклик create(): реєстрація нового користувача '{}'", dto.getUsername());

        User user = new User(null, dto.getUsername(), dto.getEmail(), LocalDate.now());
        return ResponseEntity.ok(repo.save(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити користувача за ID")
    @CacheEvict(value = "users", allEntries = true) // <--- Очищаємо кеш при зміні
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        log.info("Виклик update(): оновлення даних користувача ID {}", id);

        if (!repo.existsById(id)) {
            log.warn("Помилка оновлення: користувача ID {} не знайдено", id);
            return ResponseEntity.notFound().build();
        }

        // Зверни увагу: дату реєстрації залишаємо новою або беремо стару (тут спрощено)
        User user = new User(id, dto.getUsername(), dto.getEmail(), LocalDate.now());
        return ResponseEntity.ok(repo.save(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити користувача за ID")
    @CacheEvict(value = "users", allEntries = true) // <--- Очищаємо кеш при видаленні
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Перевірка дозволу на видалення (з файлу конфігурації)
        if (!allowUserDelete) {
            log.warn("Спроба видалення користувача ID {} заблокована налаштуваннями", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        log.info("Виклик delete(): видалення користувача ID {}", id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}