package com.charity.service.fakes;

import com.charity.dao.AidRequestDAO;
import com.charity.model.AidRequest;
import com.charity.model.BeneficiaryType;
import com.charity.model.RequestStatus;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeAidRequestDAO implements AidRequestDAO {
    private final List<AidRequest> reqs = new ArrayList<>();
    private final AtomicLong id = new AtomicLong(1);
    @Override
    public List<AidRequest> findAll() { return new ArrayList<>(reqs); }
    @Override
    public Optional<AidRequest> findById(Long id) {
        return reqs.stream().filter(r -> r.getId().equals(id)).findFirst();
    }
    @Override
    public Long save(AidRequest request) {
        if (request.getId() == null) request.setId(id.getAndIncrement());
        reqs.removeIf(r -> r.getId().equals(request.getId()));
        reqs.add(request);
        return request.getId();
    }
    @Override
    public void update(AidRequest request) { save(request); }
    @Override
    public void update(AidRequest request, Connection con) { update(request); }
    @Override
    public void delete(Long id) { reqs.removeIf(r -> r.getId().equals(id)); }
    @Override
    public void updateStatus(Long id, RequestStatus status) {
        findById(id).ifPresent(r -> r.setStatus(status));
    }
    @Override
    public void setApprovedAmount(Long id, BigDecimal approvedAmount) {
        findById(id).ifPresent(r -> r.setApprovedAmount(approvedAmount));
    }
    @Override
    public List<AidRequest> findByBeneficiary(Long beneficiaryId) {
        return reqs.stream().filter(r -> r.getBeneficiaryId().equals(beneficiaryId)).toList();
    }
    @Override
    public List<AidRequest> findByStatus(RequestStatus status) {
        return reqs.stream().filter(r -> r.getStatus() == status).toList();
    }
    @Override
    public BigDecimal totalDistributedByRequest(Long requestId) {
        return findById(requestId).map(AidRequest::getDistributedAmount).orElse(BigDecimal.ZERO);
    }
    @Override
    public int countByStatus(RequestStatus status) {
        return (int) reqs.stream().filter(r -> r.getStatus() == status).count();
    }
}
