package com.cinema;

import com.cinema.model.Movie;
import com.cinema.model.User;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class OnlineCinemaApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineCinemaApplication.class, args);
	}

	// Цей @Bean завантажить тестові дані в наші репозиторії при старті
	@Bean
	public CommandLineRunner loadData(MovieRepository movieRepository, UserRepository userRepository) {
		return args -> {
			// Додаємо фільми
			movieRepository.save(new Movie(
					12L,
					"Дюна: Частина друга",
					"Пол Атрід об'єднується з Чані та Фременами...",
					2024,
					9.2,
					List.of("Озвучка 1", "Озвучка 2", "Субтитри")
			));
			movieRepository.save(new Movie(
					13L,
					"Інтерстеллар",
					"Команда дослідників подорожує крізь чортовину...",
					2014,
					9.5,
					List.of("Озвучка 1")
			));

			// Додаємо користувачів
			userRepository.save(new User(
					1L,
					"paul_atrid",
					"paul@arrakis.com",
					LocalDate.now().minusDays(10)
			));
			userRepository.save(new User(
					2L,
					"cooper",
					"coop@nasa.gov",
					LocalDate.now().minusDays(150)
			));
		};
	}
}