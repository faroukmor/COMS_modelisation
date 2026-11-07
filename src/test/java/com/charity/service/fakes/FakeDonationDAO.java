package com.charity.service.fakes;

import com.charity.dao.DonationDAO;
import com.charity.model.Donation;
import com.charity.model.DonationStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeDonationDAO implements DonationDAO {
    private final List<Donation> donations = new ArrayList<>();
    private final AtomicLong id = new AtomicLong(1);
    @Override
    public List<Donation> findAll() { return new ArrayList<>(donations); }
    @Override
    public Optional<Donation> findById(Long id) {
        return donations.stream().filter(d -> d.getId().equals(id)).findFirst();
    }
    @Override
    public Long save(Donation donation) {
        if (donation.getId() == null) donation.setId(id.getAndIncrement());
        donations.removeIf(d -> d.getId().equals(donation.getId()));
        donations.add(donation);
        return donation.getId();
    }
    @Override
    public void update(Donation donation) { save(donation); }
    @Override
    public void delete(Long id) { donations.removeIf(d -> d.getId().equals(id)); }
    @Override
    public void updateStatus(Long id, DonationStatus status) {
        findById(id).ifPresent(d -> d.setStatus(status));
    }
    @Override
    public List<Donation> findByDonor(Long donorId) {
        return donations.stream().filter(d -> d.getDonorId().equals(donorId)).toList();
    }
    @Override
    public List<Donation> findByStatus(DonationStatus status) {
        return donations.stream().filter(d -> d.getStatus() == status).toList();
    }
    @Override
    public BigDecimal totalConfirmedAmount() {
        return donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.CONFIRMED)
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
