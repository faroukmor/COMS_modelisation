package com.charity.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A request for financial aid submitted by a beneficiary.
 */
public class AidRequest {
    private Long id;
    private Long beneficiaryId;
    private BeneficiaryType beneficiaryType;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal distributedAmount;
    private String description;
    private LocalDateTime submissionDate;
    private RequestStatus status;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private String adminNotes;

    public AidRequest() {
        this.submissionDate = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
        this.requestedAmount = BigDecimal.ZERO;
        this.approvedAmount = BigDecimal.ZERO;
        this.distributedAmount = BigDecimal.ZERO;
    }

    public AidRequest(Long id, Long beneficiaryId, BeneficiaryType beneficiaryType,
                      BigDecimal requestedAmount, String description, LocalDateTime submissionDate,
                      RequestStatus status) {
        this.id = id;
        this.beneficiaryId = beneficiaryId;
        this.beneficiaryType = beneficiaryType;
        this.requestedAmount = requestedAmount;
        this.approvedAmount = BigDecimal.ZERO;
        this.distributedAmount = BigDecimal.ZERO;
        this.description = description;
        this.submissionDate = submissionDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(Long beneficiaryId) { this.beneficiaryId = beneficiaryId; }

    public BeneficiaryType getBeneficiaryType() { return beneficiaryType; }
    public void setBeneficiaryType(BeneficiaryType beneficiaryType) { this.beneficiaryType = beneficiaryType; }

    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }

    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }

    public BigDecimal getDistributedAmount() { return distributedAmount; }
    public void setDistributedAmount(BigDecimal distributedAmount) { this.distributedAmount = distributedAmount; }

    public BigDecimal getRemainingAmount() {
        return approvedAmount.subtract(distributedAmount);
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public boolean isApproved() { return status == RequestStatus.APPROVED; }
    public boolean isFullyDistributed() {
        return distributedAmount.compareTo(approvedAmount) >= 0;
    }

    @Override
    public String toString() {
        return "AidRequest{id=" + id + ", beneficiaryId=" + beneficiaryId
                + ", requested=" + requestedAmount + ", approved=" + approvedAmount
                + ", distributed=" + distributedAmount + ", status=" + status + "}";
    }
}
