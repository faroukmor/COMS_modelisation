package com.charity.service.fakes;

import com.charity.dao.DistributionDAO;
import com.charity.model.Distribution;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeDistributionDAO implements DistributionDAO {
    private final List<Distribution> dists = new ArrayList<>();
    private final AtomicLong id = new AtomicLong(1);
    @Override
    public List<Distribution> findAll() { return new ArrayList<>(dists); }
    @Override
    public Optional<Distribution> findById(Long id) {
        return dists.stream().filter(d -> d.getId().equals(id)).findFirst();
    }
    @Override
    public Long save(Distribution distribution) {
        if (distribution.getId() == null) distribution.setId(id.getAndIncrement());
        dists.removeIf(d -> d.getId().equals(distribution.getId()));
        dists.add(distribution);
        return distribution.getId();
    }
    @Override
    public Long save(Distribution distribution, Connection con) { return save(distribution); }
    @Override
    public void delete(Long id) { dists.removeIf(d -> d.getId().equals(id)); }
    @Override
    public List<Distribution> findByRequest(Long requestId) {
        return dists.stream().filter(d -> d.getAidRequestId().equals(requestId)).toList();
    }
    @Override
    public BigDecimal totalAmount() {
        return dists.stream().map(Distribution::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
