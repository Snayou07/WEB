package com.cinema.controller;

import com.cinema.model.User;
import com.cinema.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "API для керування користувачами")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 1. Отримання списку всіх
    @GetMapping
    @Operation(summary = "Отримати список всіх користувачів")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. Отримання за ID
    @GetMapping("/{id}")
    @Operation(summary = "Отримати користувача за ID")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Додавання нового
    @PostMapping
    @Operation(summary = "Створити нового користувача")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        user.setId(null);
        return userRepository.save(user);
    }

    // 4. Оновлення
    @PutMapping("/{id}")
    @Operation(summary = "Оновити користувача за ID")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userDetails.setId(id);
        return ResponseEntity.ok(userRepository.save(userDetails));
    }

    // 5. Видалення
    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити користувача за ID")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}