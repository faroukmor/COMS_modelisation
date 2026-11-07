package com.charity.dao;

import com.charity.model.Donor;
import java.util.List;
import java.util.Optional;

/**
 * Data access operations for {@link Donor}.
 */
public interface DonorDAO {
    List<Donor> findAll();
    Optional<Donor> findById(Long id);
    Long save(Donor donor);
    void update(Donor donor);
    void delete(Long id);
    List<Donor> search(String keyword);
}
