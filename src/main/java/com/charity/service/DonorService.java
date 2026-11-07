package com.charity.service;

import com.charity.dao.DonorDAO;
import com.charity.model.Donor;
import java.util.List;

/**
 * Business logic for managing donors.
 */
public class DonorService {
    private final DonorDAO donorDAO;

    public DonorService(DonorDAO donorDAO) {
        this.donorDAO = donorDAO;
    }

    public List<Donor> findAll() {
        return donorDAO.findAll();
    }

    public Donor findById(Long id) {
        return donorDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found: " + id));
    }

    public Long create(Donor donor) {
        if (donor.getFullName() == null || donor.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Donor full name is required");
        }
        return donorDAO.save(donor);
    }

    public void update(Donor donor) {
        findById(donor.getId()); // ensure exists
        donorDAO.update(donor);
    }

    public void delete(Long id) {
        findById(id); // ensure exists
        donorDAO.delete(id);
    }

    public List<Donor> search(String keyword) {
        return donorDAO.search(keyword);
    }
}

