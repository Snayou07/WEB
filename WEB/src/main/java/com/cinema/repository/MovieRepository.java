package com.cinema.repository;

import com.cinema.model.Movie;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository // Позначаємо цей клас як Spring-компонент
public class MovieRepository {

    // "База даних" у пам'яті. ConcurrentHashMap - для безпечної роботи з потоками
    private final Map<Long, Movie> movieStorage = new ConcurrentHashMap<>();
    // Лічильник для унікальних ID
    private final AtomicLong idCounter = new AtomicLong();

    // 1. Отримання списку всіх
    public List<Movie> findAll() {
        return new ArrayList<>(movieStorage.values());
    }

    // 2. Отримання за ID
    public Optional<Movie> findById(Long id) {
        return Optional.ofNullable(movieStorage.get(id));
    }

    // 3. Додавання / 4. Оновлення
    public Movie save(Movie movie) {
        if (movie.getId() == null) { // Створення нового
            movie.setId(idCounter.incrementAndGet());
        }
        movieStorage.put(movie.getId(), movie);
        return movie;
    }

    // 5. Видалення
    public void deleteById(Long id) {
        movieStorage.remove(id);
    }

    // Допоміжний метод для перевірки існування
    public boolean existsById(Long id) {
        return movieStorage.containsKey(id);
    }
}