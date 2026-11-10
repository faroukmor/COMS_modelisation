package com.charity.dao;

import com.charity.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Data access operations for {@link User}.
 */
public interface UserDAO {
    /** Finds a user by username, ignoring letter case. */
    Optional<User> findByUsername(String username);
    Long save(User user);
    void update(User user);
    /** Returns {@code true} if a user with this username exists (case-insensitive). */
    boolean exists(String username);
    /** Returns {@code true} if any user already has this e-mail address (case-insensitive). */
    boolean emailExists(String email);
    /** Returns all users ordered by id. */
    List<User> findAll();
}
