package com.charity.service;

import com.charity.dao.DonationDAO;
import com.charity.dao.DonorDAO;
import com.charity.model.Donation;
import com.charity.model.DonationStatus;
import com.charity.model.Donor;
import com.charity.model.PaymentMethod;
import java.math.BigDecimal;

/**
 * Business logic for recording and managing financial donations.
 */
public class DonationService {
    private final DonationDAO donationDAO;
    private final DonorDAO donorDAO;

    public DonationService(DonationDAO donationDAO, DonorDAO donorDAO) {
        this.donationDAO = donationDAO;
        this.donorDAO = donorDAO;
    }

    /**
     * Records a new donation (initially PENDING). Amount must be positive.
     */
    public Long recordDonation(Long donorId, BigDecimal amount, PaymentMethod paymentMethod,
                               String description, String reference) {
        validateAmount(amount);
        Donation donation = new Donation();
        donation.setDonorId(donorId);
        donation.setAmount(amount);
        donation.setPaymentMethod(paymentMethod);
        donation.setStatus(DonationStatus.PENDING);
        donation.setDescription(description);
        donation.setReference(reference);
        Long id = donationDAO.save(donation);

        // Update donor's total donated tracking (informational only)
        donorDAO.findById(donorId).ifPresent(donor -> {
            BigDecimal current = donor.getTotalDonated() == null ? BigDecimal.ZERO : donor.getTotalDonated();
            donor.setTotalDonated(current.add(amount));
            donorDAO.update(donor);
        });
        return id;
    }

    /**
     * Confirms a pending donation. Only confirmed donations count toward the balance.
     */
    public void confirmDonation(Long id) {
        Donation donation = donationDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found: " + id));
        if (donation.getStatus() != DonationStatus.PENDING) {
            throw new IllegalStateException("Only pending donations can be confirmed");
        }
        donationDAO.updateStatus(id, DonationStatus.CONFIRMED);
    }

    /**
     * Cancels a donation.
     */
    public void cancelDonation(Long id) {
        Donation donation = donationDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found: " + id));
        if (donation.getStatus() == DonationStatus.CONFIRMED) {
            throw new IllegalStateException("A confirmed donation cannot be cancelled");
        }
        donationDAO.updateStatus(id, DonationStatus.CANCELLED);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Donation amount must be positive");
        }
    }
}

