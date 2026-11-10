package com.charity.service.fakes;

import com.charity.dao.DonorDAO;
import com.charity.model.Donor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeDonorDAO implements DonorDAO {
    private final List<Donor> donors = new ArrayList<>();
    private final AtomicLong id = new AtomicLong(1);
    @Override
    public List<Donor> findAll() { return new ArrayList<>(donors); }
    @Override
    public Optional<Donor> findById(Long id) {
        return donors.stream().filter(d -> d.getId().equals(id)).findFirst();
    }
    @Override
    public Long save(Donor donor) {
        if (donor.getId() == null) donor.setId(id.getAndIncrement());
        donors.removeIf(d -> d.getId().equals(donor.getId()));
        donors.add(donor);
        return donor.getId();
    }
    @Override
    public void update(Donor donor) { save(donor); }
    @Override
    public void delete(Long id) { donors.removeIf(d -> d.getId().equals(id)); }
    @Override
    public List<Donor> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        String k = keyword.trim().toLowerCase();
        return donors.stream().filter(d ->
                d.getFullName().toLowerCase().contains(k) ||
                (d.getEmail() != null && d.getEmail().toLowerCase().contains(k)))
                .toList();
    }
}
