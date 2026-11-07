package com.charity.dao;

import com.charity.model.Donation;
import com.charity.model.DonationStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Data access operations for {@link Donation}.
 */
public interface DonationDAO {
    List<Donation> findAll();
    Optional<Donation> findById(Long id);
    Long save(Donation donation);
    void update(Donation donation);
    void delete(Long id);
    void updateStatus(Long id, DonationStatus status);
    List<Donation> findByDonor(Long donorId);
    List<Donation> findByStatus(DonationStatus status);
    BigDecimal totalConfirmedAmount();
}
