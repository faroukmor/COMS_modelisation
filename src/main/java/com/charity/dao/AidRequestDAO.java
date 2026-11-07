package com.charity.dao;

import com.charity.model.AidRequest;
import com.charity.model.BeneficiaryType;
import com.charity.model.RequestStatus;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data access operations for {@link AidRequest}.
 */
public interface AidRequestDAO {
    List<AidRequest> findAll();
    Optional<AidRequest> findById(Long id);
    Long save(AidRequest request);
    void update(AidRequest request);
    void update(AidRequest request, Connection con);
    void delete(Long id);
    void updateStatus(Long id, RequestStatus status);
    void setApprovedAmount(Long id, BigDecimal approvedAmount);
    List<AidRequest> findByBeneficiary(Long beneficiaryId);
    List<AidRequest> findByStatus(RequestStatus status);
    BigDecimal totalDistributedByRequest(Long requestId);
}
