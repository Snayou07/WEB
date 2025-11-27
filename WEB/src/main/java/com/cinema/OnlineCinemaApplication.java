package com.cinema;

import com.cinema.model.Movie;
import com.cinema.repository.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching; // <--- ДОДАНО
import org.springframework.context.annotation.Bean;
import java.util.List;

@SpringBootApplication
@EnableCaching // <--- Вмикає механізм кешування
public class OnlineCinemaApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineCinemaApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(MovieRepository repo) {
		return args -> {
			if (repo.count() == 0) {
				repo.save(new Movie(null, "Dune 2", "Epic", 2024, 9.0, List.of("Ukr")));
			}
		};
	}
}