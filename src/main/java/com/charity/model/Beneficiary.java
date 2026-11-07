package com.charity.model;

import java.time.LocalDateTime;

/**
 * Base class for a beneficiary (person or association in need of aid).
 * Implements table-per-class single-table inheritance via the type discriminator.
 */
public abstract class Beneficiary {
    private Long id;
    private BeneficiaryType type;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;

    public Beneficiary() {
        this.createdAt = LocalDateTime.now();
    }

    public Beneficiary(Long id, BeneficiaryType type, String email, String phone, String address) {
        this.id = id;
        this.type = type;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BeneficiaryType getType() { return type; }
    public void setType(BeneficiaryType type) { this.type = type; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public abstract String getDisplayName();

    @Override
    public String toString() {
        return "Beneficiary{id=" + id + ", type=" + type + ", displayName='" + getDisplayName() + "'}";
    }
}
