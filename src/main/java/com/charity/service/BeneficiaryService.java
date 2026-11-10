package com.charity.service;

import com.charity.dao.BeneficiaryDAO;
import com.charity.model.Beneficiary;
import com.charity.model.BeneficiaryType;
import com.charity.model.CharityAssociation;
import com.charity.model.Individual;
import com.charity.util.Validation;
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
        validate(beneficiary);
        return beneficiaryDAO.save(beneficiary);
    }

    public void update(Beneficiary beneficiary) {
        if (beneficiary.getId() == null) {
            throw new IllegalArgumentException("Select a beneficiary first");
        }
        findById(beneficiary.getId());
        validate(beneficiary);
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

    private void validate(Beneficiary b) {
        if (b.getType() == null) {
            throw new IllegalArgumentException("Beneficiary type is required");
        }
        b.setEmail(Validation.clean(b.getEmail()));
        b.setPhone(Validation.clean(b.getPhone()));
        b.setAddress(Validation.clean(b.getAddress()));
        if (b.getEmail() == null) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!Validation.isValidEmail(b.getEmail())) {
            throw new IllegalArgumentException("Email address is not valid");
        }
        if (b.getPhone() != null && !Validation.isValidPhone(b.getPhone())) {
            throw new IllegalArgumentException("Phone number is not valid");
        }
        if (b instanceof Individual ind) {
            if (Validation.isBlank(ind.getFullName())) {
                throw new IllegalArgumentException("Full name is required");
            }
            if (Validation.isBlank(ind.getNationalId())) {
                throw new IllegalArgumentException("National ID is required");
            }
            ind.setFullName(ind.getFullName().trim());
            ind.setNationalId(ind.getNationalId().trim());
        } else if (b instanceof CharityAssociation assoc) {
            if (Validation.isBlank(assoc.getOrganizationName())) {
                throw new IllegalArgumentException("Organization name is required");
            }
            if (Validation.isBlank(assoc.getRegistrationNumber())) {
                throw new IllegalArgumentException("Registration number is required");
            }
            assoc.setOrganizationName(assoc.getOrganizationName().trim());
            assoc.setRegistrationNumber(assoc.getRegistrationNumber().trim());
            assoc.setRepresentativeName(Validation.clean(assoc.getRepresentativeName()));
        }
    }
}
