package com.charity.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A financial donation made by a donor.
 * Only CONFIRMED donations contribute to the available balance.
 */
public class Donation {
    private Long id;
    private Long donorId;
    private BigDecimal amount;
    private LocalDateTime donationDate;
    private PaymentMethod paymentMethod;
    private DonationStatus status;
    private String description;
    private String reference;

    public Donation() {
        this.donationDate = LocalDateTime.now();
        this.status = DonationStatus.PENDING;
        this.amount = BigDecimal.ZERO;
    }

    public Donation(Long id, Long donorId, BigDecimal amount, LocalDateTime donationDate,
                    PaymentMethod paymentMethod, DonationStatus status,
                    String description, String reference) {
        this.id = id;
        this.donorId = donorId;
        this.amount = amount;
        this.donationDate = donationDate;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.description = description;
        this.reference = reference;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getDonationDate() { return donationDate; }
    public void setDonationDate(LocalDateTime donationDate) { this.donationDate = donationDate; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public DonationStatus getStatus() { return status; }
    public void setStatus(DonationStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public boolean isConfirmed() { return status == DonationStatus.CONFIRMED; }

    @Override
    public String toString() {
        return "Donation{id=" + id + ", donorId=" + donorId + ", amount=" + amount
                + ", status=" + status + "}";
    }
}
