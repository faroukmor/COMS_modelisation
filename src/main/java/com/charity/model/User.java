package com.charity.model;

import java.time.LocalDateTime;

/**
 * System user account used for authentication and role-based access control.
 */
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private Role role;
    private String fullName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(Long id, String username, String passwordHash, Role role,
                String fullName, String email, String phone) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() { return role == Role.ADMIN; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + "}";
    }
}
