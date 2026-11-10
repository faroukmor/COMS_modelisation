package com.charity.service.fakes;

import com.charity.dao.UserDAO;
import com.charity.model.Role;
import com.charity.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeUserDAO implements UserDAO {
    private final List<User> users = new ArrayList<>();
    private final AtomicLong id = new AtomicLong(1);
    @Override
    public Optional<User> findByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }
    @Override
    public Long save(User user) {
        if (user.getId() == null) {
            user.setId(id.getAndIncrement());
        }
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);
        return user.getId();
    }
    @Override
    public void update(User user) {
        save(user);
    }
    @Override
    public boolean exists(String username) {
        return findByUsername(username).isPresent();
    }
    @Override
    public boolean emailExists(String email) {
        return email != null && users.stream().anyMatch(u -> email.equalsIgnoreCase(u.getEmail()));
    }
    @Override
    public List<User> findAll() { return new ArrayList<>(users); }
    public List<User> all() { return findAll(); }
}
