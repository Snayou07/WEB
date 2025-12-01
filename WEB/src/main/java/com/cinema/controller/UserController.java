package com.cinema.controller;

import com.cinema.dto.UserDto;
import com.cinema.model.User;
import com.cinema.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API")
@Slf4j
public class UserController {

    @Autowired
    private UserRepository repo;

    @GetMapping
    public List<User> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserDto dto) {
        // РУЧНА ПЕРЕВІРКА НА ДУБЛІКАТИ
        Map<String, String> errors = new HashMap<>();

        if (repo.existsByUsername(dto.getUsername())) {
            errors.put("username", "Цей нікнейм вже зайнятий!");
        }
        if (repo.existsByEmail(dto.getEmail())) {
            errors.put("email", "Ця пошта вже використовується!");
        }

        // Якщо є помилки - повертаємо їх (код 400 Bad Request)
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        User user = new User(null, dto.getUsername(), dto.getEmail(), LocalDate.now());
        return ResponseEntity.ok(repo.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();

        // Перевірка дублікатів (виключаючи поточного юзера)
        Map<String, String> errors = new HashMap<>();

        if (repo.existsByUsernameAndIdNot(dto.getUsername(), id)) {
            errors.put("username", "Цей нікнейм вже зайнятий іншим користувачем!");
        }
        if (repo.existsByEmailAndIdNot(dto.getEmail(), id)) {
            errors.put("email", "Ця пошта вже зайнята!");
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        // Оновлення (дату реєстрації залишаємо стару, тут спрощено ставимо нову або шукаємо стару в БД)
        User user = new User(id, dto.getUsername(), dto.getEmail(), LocalDate.now());
        return ResponseEntity.ok(repo.save(user));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}