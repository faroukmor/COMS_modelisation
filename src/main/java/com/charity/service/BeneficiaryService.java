package com.charity.service;

import com.charity.dao.BeneficiaryDAO;
import com.charity.model.Beneficiary;
import com.charity.model.BeneficiaryType;
import java.util.List;

/**
 * Business logic for managing beneficiaries (individuals and charity associations).
 */
public class BeneficiaryService {
    private final BeneficiaryDAO beneficiaryDAO;

    public BeneficiaryService(BeneficiaryDAO beneficiaryDAO) {
        this.beneficiaryDAO = beneficiaryDAO;
    }

    public List<Beneficiary> findAll() {
        return beneficiaryDAO.findAll();
    }

    public Beneficiary findById(Long id) {
        return beneficiaryDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found: " + id));
    }

    public Long create(Beneficiary beneficiary) {
        if (beneficiary.getType() == null) {
            throw new IllegalArgumentException("Beneficiary type is required");
        }
        if (beneficiary.getEmail() == null || beneficiary.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        return beneficiaryDAO.save(beneficiary);
    }

    public void update(Beneficiary beneficiary) {
        findById(beneficiary.getId());
        beneficiaryDAO.update(beneficiary);
    }

    public void delete(Long id) {
        findById(id);
        beneficiaryDAO.delete(id);
    }

    public List<Beneficiary> search(String keyword) {
        return beneficiaryDAO.search(keyword);
    }

    public List<Beneficiary> findByType(BeneficiaryType type) {
        return beneficiaryDAO.findByType(type);
    }
}

