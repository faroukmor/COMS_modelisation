package com.charity.service;

import com.charity.dao.DonationDAO;
import com.charity.dao.DonorDAO;
import com.charity.model.Donation;
import com.charity.model.DonationStatus;
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
     * Records a new donation (initially PENDING). Amount must be positive and the donor must exist.
     */
    public Long recordDonation(Long donorId, BigDecimal amount, PaymentMethod paymentMethod,
                               String description, String reference) {
        return recordDonation(donorId, amount, paymentMethod, DonationStatus.PENDING, description, reference);
    }

    /**
     * Records a donation with an explicit initial status. {@link DonationStatus#CONFIRMED} records the
     * donation and confirms it in one step (e.g. cash received at the desk); CANCELLED is not allowed.
     */
    public Long recordDonation(Long donorId, BigDecimal amount, PaymentMethod paymentMethod,
                               DonationStatus initialStatus, String description, String reference) {
        if (donorId == null) {
            throw new IllegalArgumentException("Donor is required");
        }
        validateAmount(amount);
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (initialStatus == DonationStatus.CANCELLED) {
            throw new IllegalArgumentException("A new donation cannot be recorded as cancelled");
        }
        donorDAO.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found: " + donorId));

        Donation donation = new Donation();
        donation.setDonorId(donorId);
        donation.setAmount(amount);
        donation.setPaymentMethod(paymentMethod);
        donation.setStatus(DonationStatus.PENDING);
        donation.setDescription(blankToNull(description));
        donation.setReference(blankToNull(reference));
        Long id = donationDAO.save(donation);

        if (initialStatus == DonationStatus.CONFIRMED) {
            confirmDonation(id);
        }
        return id;
    }

    /**
     * Confirms a pending donation. Only confirmed donations count toward the balance and toward
     * the donor's {@code totalDonated}.
     */
    public void confirmDonation(Long id) {
        Donation donation = donationDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found: " + id));
        if (donation.getStatus() != DonationStatus.PENDING) {
            throw new IllegalStateException("Only pending donations can be confirmed");
        }
        donationDAO.updateStatus(id, DonationStatus.CONFIRMED);

        donorDAO.findById(donation.getDonorId()).ifPresent(donor -> {
            BigDecimal current = donor.getTotalDonated() == null ? BigDecimal.ZERO : donor.getTotalDonated();
            donor.setTotalDonated(current.add(donation.getAmount()));
            donorDAO.update(donor);
        });
    }

    /**
     * Cancels a pending donation. Confirmed donations are part of the ledger and cannot be cancelled.
     */
    public void cancelDonation(Long id) {
        Donation donation = donationDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found: " + id));
        if (donation.getStatus() == DonationStatus.CONFIRMED) {
            throw new IllegalStateException("A confirmed donation cannot be cancelled");
        }
        if (donation.getStatus() == DonationStatus.CANCELLED) {
            throw new IllegalStateException("This donation is already cancelled");
        }
        donationDAO.updateStatus(id, DonationStatus.CANCELLED);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Donation amount must be positive");
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
