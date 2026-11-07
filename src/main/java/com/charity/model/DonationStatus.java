package com.charity.model;

/**
 * Lifecycle status of a donation.
 * Only CONFIRMED donations contribute to the available balance.
 */
public enum DonationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
