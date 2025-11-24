package com.cinema.repository;

import com.cinema.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {

    private final Map<Long, User> userStorage = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong();

    public List<User> findAll() {
        return new ArrayList<>(userStorage.values());
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userStorage.get(id));
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter.incrementAndGet());
        }
        userStorage.put(user.getId(), user);
        return user;
    }

    public void deleteById(Long id) {
        userStorage.remove(id);
    }

    public boolean existsById(Long id) {
        return userStorage.containsKey(id);
    }
}