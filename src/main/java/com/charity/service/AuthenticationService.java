package com.charity.service;

import com.charity.dao.UserDAO;
import com.charity.model.Role;
import com.charity.model.User;
import com.charity.util.PasswordUtil;
import java.util.Optional;

/**
 * Authenticates users and enforces role-based access control.
 */
public class AuthenticationService {
    private final UserDAO userDAO;
    private static volatile String dummyHash;

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user by username (case-insensitive, surrounding spaces ignored) and password.
     *
     * @return the authenticated {@link User}, or {@code null} if credentials are invalid
     */
    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        String name = username.trim();
        if (name.isEmpty() || password.isEmpty()) {
            return null;
        }
        Optional<User> found = userDAO.findByUsername(name);
        if (found.isEmpty()) {
            burnTime(password);
            return null;
        }
        User user = found.get();
        return PasswordUtil.verify(password, user.getPasswordHash()) ? user : null;
    }

    /**
     * Unknown usernames still pay the PBKDF2 cost, so response time does not reveal which
     * usernames exist.
     */
    private void burnTime(String password) {
        if (dummyHash == null) {
            dummyHash = PasswordUtil.hash("not-a-real-password-1");
        }
        PasswordUtil.verify(password, dummyHash);
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    public boolean isDonor(User user) {
        return user != null && user.getRole() == Role.DONOR;
    }

    public boolean isBeneficiary(User user) {
        return user != null && (user.getRole() == Role.BENEFICIARY || user.getRole() == Role.ASSOCIATION);
    }

    public void requireAdmin(User user) {
        if (!isAdmin(user)) {
            throw new SecurityException("Administrator access required");
        }
    }
}
