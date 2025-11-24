package com.cinema.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data // Геттери, сеттери, equals, hashCode, toString
@NoArgsConstructor // Конструктор без аргументів
public class Movie {
    private Long id;
    private String title;
    private String description;
    private Integer releaseYear;
    private Double rating;
    private List<String> availableVoiceovers; // <--- Твоя нова вимога

    // Конструктор для легкого створення тестових даних
    public Movie(Long id, String title, String description, Integer releaseYear, Double rating, List<String> voiceovers) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.rating = rating;
        this.availableVoiceovers = voiceovers;
    }
}