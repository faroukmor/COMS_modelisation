package com.charity.service;

import com.charity.dao.AidRequestDAO;
import com.charity.dao.BeneficiaryDAO;
import com.charity.model.AidRequest;
import com.charity.model.Beneficiary;
import com.charity.model.RequestStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Business logic for receiving and reviewing financial aid requests.
 */
public class AidRequestService {
    private final AidRequestDAO requestDAO;
    private final BeneficiaryDAO beneficiaryDAO;

    public AidRequestService(AidRequestDAO requestDAO, BeneficiaryDAO beneficiaryDAO) {
        this.requestDAO = requestDAO;
        this.beneficiaryDAO = beneficiaryDAO;
    }

    /**
     * Submits a new aid request (PENDING). Requested amount must be positive.
     */
    public Long submitRequest(Long beneficiaryId, BigDecimal requestedAmount, String description) {
        if (requestedAmount == null || requestedAmount.signum() <= 0) {
            throw new IllegalArgumentException("Requested amount must be positive");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        Beneficiary beneficiary = beneficiaryDAO.findById(beneficiaryId)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found: " + beneficiaryId));

        AidRequest request = new AidRequest();
        request.setBeneficiaryId(beneficiaryId);
        request.setBeneficiaryType(beneficiary.getType());
        request.setRequestedAmount(requestedAmount);
        request.setDescription(description);
        request.setSubmissionDate(LocalDateTime.now());
        request.setStatus(RequestStatus.PENDING);
        return requestDAO.save(request);
    }

    /**
     * Reviews a request: approve, reject, or mark under review.
     * Only PENDING or UNDER_REVIEW requests can be reviewed.
     */
    public void reviewRequest(Long id, RequestStatus status, Long reviewedBy, String adminNotes, BigDecimal approvedAmount) {
        AidRequest request = requestDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aid request not found: " + id));
        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only pending or under-review requests can be reviewed");
        }
        if (status != RequestStatus.APPROVED && status != RequestStatus.REJECTED && status != RequestStatus.UNDER_REVIEW) {
            throw new IllegalArgumentException("Invalid review status: " + status);
        }

        request.setStatus(status);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(reviewedBy);
        request.setAdminNotes(adminNotes);

        if (status == RequestStatus.APPROVED) {
            if (approvedAmount == null || approvedAmount.signum() <= 0) {
                throw new IllegalArgumentException("Approved amount must be positive for approved requests");
            }
            if (approvedAmount.compareTo(request.getRequestedAmount()) > 0) {
                throw new IllegalArgumentException("Approved amount cannot exceed requested amount");
            }
            request.setApprovedAmount(approvedAmount);
            request.setDistributedAmount(BigDecimal.ZERO);
        } else if (status == RequestStatus.REJECTED) {
            request.setApprovedAmount(BigDecimal.ZERO);
            request.setDistributedAmount(BigDecimal.ZERO);
        }

        requestDAO.update(request);
    }

    public void markUnderReview(Long id, Long reviewedBy) {
        AidRequest request = requestDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aid request not found: " + id));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be marked under review");
        }
        request.setStatus(RequestStatus.UNDER_REVIEW);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(reviewedBy);
        requestDAO.update(request);
    }
}

