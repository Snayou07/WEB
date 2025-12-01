package com.cinema.repository;

import com.cinema.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    // Перевірка: чи є фільм з такою назвою?
    boolean existsByTitle(String title);

    // Перевірка для редагування: чи є така назва у КОГОСЬ ІНШОГО (крім мене)?
    boolean existsByTitleAndIdNot(String title, Long id);
}