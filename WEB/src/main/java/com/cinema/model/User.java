package com.cinema.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
    private LocalDate registeredAt;

    // Конструктор для легкого створення тестових даних
    public User(Long id, String username, String email, LocalDate registeredAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.registeredAt = registeredAt;
    }
}