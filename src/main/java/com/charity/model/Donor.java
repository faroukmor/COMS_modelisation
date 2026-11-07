package com.charity.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Donor: a person or entity that provides financial donations.
 * Kept independent of User so donations can be recorded for anonymous donors too.
 */
public class Donor {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private BigDecimal totalDonated;
    private LocalDateTime createdAt;

    public Donor() {
        this.totalDonated = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public Donor(Long id, String fullName, String email, String phone, String address) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.totalDonated = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getTotalDonated() { return totalDonated; }
    public void setTotalDonated(BigDecimal totalDonated) { this.totalDonated = totalDonated; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Donor{id=" + id + ", fullName='" + fullName + "'}";
    }
}
