package com.charity.dao;

import com.charity.database.DatabaseConnection;
import com.charity.model.Role;
import com.charity.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Oracle JDBC implementation of {@link UserDAO}.
 */
public class OracleUserDAO implements UserDAO {

    private static final String FIND_BY_USERNAME =
            "SELECT id, username, password_hash, role, full_name, email, phone, created_at "
            + "FROM users WHERE LOWER(username) = LOWER(?)";

    private static final String INSERT =
            "INSERT INTO users (username, password_hash, role, full_name, email, phone, created_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE users SET username=?, password_hash=?, role=?, full_name=?, email=?, phone=? "
            + "WHERE id = ?";

    private static final String EXISTS =
            "SELECT 1 FROM users WHERE LOWER(username) = LOWER(?)";

    private static final String EMAIL_EXISTS =
            "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";

    private static final String FIND_ALL =
            "SELECT id, username, password_hash, role, full_name, email, phone, created_at "
            + "FROM users ORDER BY id";

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username: " + username, e);
        }
        return Optional.empty();
    }

    @Override
    public Long save(User user) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT, new String[]{"ID"})) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setTimestamp(7, user.getCreatedAt() != null
                    ? Timestamp.valueOf(user.getCreatedAt()) : null);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            throw new SQLException("Creating user failed, no ID obtained.");
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public void update(User user) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setLong(7, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user id=" + user.getId(), e);
        }
    }

    @Override
    public boolean exists(String username) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(EXISTS)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking user existence", e);
        }
    }

    @Override
    public boolean emailExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(EMAIL_EXISTS)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking e-mail existence", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing users", e);
        }
        return list;
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        Timestamp ts = rs.getTimestamp("created_at");
        u.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return u;
    }
}
