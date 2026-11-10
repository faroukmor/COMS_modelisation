package com.charity.service;

import com.charity.dao.DonorDAO;
import com.charity.model.Donor;
import com.charity.util.Validation;
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
        validate(donor);
        return donorDAO.save(donor);
    }

    public void update(Donor donor) {
        if (donor.getId() == null) {
            throw new IllegalArgumentException("Select a donor first");
        }
        Donor existing = findById(donor.getId());
        validate(donor);
        // The running total is maintained by confirmed donations; an edit form must never overwrite it.
        donor.setTotalDonated(existing.getTotalDonated());
        donorDAO.update(donor);
    }

    public void delete(Long id) {
        findById(id); // ensure exists
        donorDAO.delete(id);
    }

    public List<Donor> search(String keyword) {
        return donorDAO.search(keyword);
    }

    private void validate(Donor donor) {
        if (Validation.isBlank(donor.getFullName())) {
            throw new IllegalArgumentException("Donor full name is required");
        }
        donor.setFullName(donor.getFullName().trim());
        donor.setEmail(Validation.clean(donor.getEmail()));
        donor.setPhone(Validation.clean(donor.getPhone()));
        donor.setAddress(Validation.clean(donor.getAddress()));
        if (donor.getEmail() != null && !Validation.isValidEmail(donor.getEmail())) {
            throw new IllegalArgumentException("Email address is not valid");
        }
        if (donor.getPhone() != null && !Validation.isValidPhone(donor.getPhone())) {
            throw new IllegalArgumentException("Phone number is not valid");
        }
    }
}
