package com.charity.dao;

import com.charity.model.Distribution;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Data access operations for {@link Distribution}.
 */
public interface DistributionDAO {
    List<Distribution> findAll();
    Optional<Distribution> findById(Long id);
    Long save(Distribution distribution);
    void delete(Long id);
    List<Distribution> findByRequest(Long requestId);
    BigDecimal totalAmount();
}
