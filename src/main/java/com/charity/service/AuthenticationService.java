package com.charity.service;

import com.charity.dao.UserDAO;
import com.charity.model.Role;
import com.charity.model.User;
import com.charity.util.PasswordUtil;

/**
 * Authenticates users and enforces role-based access control.
 */
public class AuthenticationService {
    private final UserDAO userDAO;

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user by username and password.
     *
     * @return the authenticated {@link User}, or {@code null} if credentials are invalid
     */
    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        return userDAO.findByUsername(username)
                .filter(user -> PasswordUtil.verify(password, user.getPasswordHash()))
                .orElse(null);
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

