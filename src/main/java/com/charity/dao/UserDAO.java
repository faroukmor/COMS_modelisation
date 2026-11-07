package com.charity.dao;

import com.charity.model.User;
import java.util.Optional;

/**
 * Data access operations for {@link User}.
 */
public interface UserDAO {
    Optional<User> findByUsername(String username);
    Long save(User user);
    void update(User user);
    boolean exists(String username);
}
