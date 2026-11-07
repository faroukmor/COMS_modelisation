package com.charity.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A financial distribution made against an approved aid request.
 */
public class Distribution {
    private Long id;
    private Long aidRequestId;
    private Long beneficiaryId;
    private BigDecimal amount;
    private LocalDateTime distributionDate;
    private PaymentMethod paymentMethod;
    private String reference;
    private String description;

    public Distribution() {
        this.distributionDate = LocalDateTime.now();
        this.amount = BigDecimal.ZERO;
    }

    public Distribution(Long id, Long aidRequestId, Long beneficiaryId, BigDecimal amount,
                        LocalDateTime distributionDate, PaymentMethod paymentMethod,
                        String reference, String description) {
        this.id = id;
        this.aidRequestId = aidRequestId;
        this.beneficiaryId = beneficiaryId;
        this.amount = amount;
        this.distributionDate = distributionDate;
        this.paymentMethod = paymentMethod;
        this.reference = reference;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAidRequestId() { return aidRequestId; }
    public void setAidRequestId(Long aidRequestId) { this.aidRequestId = aidRequestId; }

    public Long getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(Long beneficiaryId) { this.beneficiaryId = beneficiaryId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getDistributionDate() { return distributionDate; }
    public void setDistributionDate(LocalDateTime distributionDate) { this.distributionDate = distributionDate; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Distribution{id=" + id + ", aidRequestId=" + aidRequestId
                + ", amount=" + amount + ", date=" + distributionDate + "}";
    }
}
