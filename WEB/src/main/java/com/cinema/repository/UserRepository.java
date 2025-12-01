package com.cinema.repository;

import com.cinema.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Ці методи Spring реалізує сам автоматично по назві
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Щоб при редагуванні не сварився на свій же нік (перевірка для інших ID)
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
}