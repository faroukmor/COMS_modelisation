package com.charity.dao;

import com.charity.model.Beneficiary;
import java.util.List;
import java.util.Optional;

/**
 * Data access operations for {@link Beneficiary} (individuals & associations).
 */
public interface BeneficiaryDAO {
    List<Beneficiary> findAll();
    Optional<Beneficiary> findById(Long id);
    Long save(Beneficiary beneficiary);
    void update(Beneficiary beneficiary);
    void delete(Long id);
    List<Beneficiary> search(String keyword);
    List<Beneficiary> findByType(com.charity.model.BeneficiaryType type);
}
