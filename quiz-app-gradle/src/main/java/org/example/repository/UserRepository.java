package org.example.repository;

import org.example.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    // Create
    User save(User user);

    // Read
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    boolean existsById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Delete
    void deleteById(Long id);
    void delete(User user);
}
