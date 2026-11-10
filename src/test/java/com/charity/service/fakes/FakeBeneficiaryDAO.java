package com.charity.service.fakes;

import com.charity.dao.BeneficiaryDAO;
import com.charity.model.Beneficiary;
import com.charity.model.BeneficiaryType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeBeneficiaryDAO implements BeneficiaryDAO {
    private final List<Beneficiary> list = new ArrayList<>();
    private final AtomicLong id = new AtomicLong(1);
    @Override
    public List<Beneficiary> findAll() { return new ArrayList<>(list); }
    @Override
    public Optional<Beneficiary> findById(Long id) {
        return list.stream().filter(b -> b.getId().equals(id)).findFirst();
    }
    @Override
    public Long save(Beneficiary beneficiary) {
        if (beneficiary.getId() == null) beneficiary.setId(id.getAndIncrement());
        list.removeIf(b -> b.getId().equals(beneficiary.getId()));
        list.add(beneficiary);
        return beneficiary.getId();
    }
    @Override
    public void update(Beneficiary beneficiary) { save(beneficiary); }
    @Override
    public void delete(Long id) { list.removeIf(b -> b.getId().equals(id)); }
    @Override
    public List<Beneficiary> search(String keyword) {
        // mirrors OracleBeneficiaryDAO: matches the display name or the e-mail, blank keyword = everything
        if (keyword == null || keyword.isBlank()) return findAll();
        String k = keyword.trim().toLowerCase();
        return list.stream().filter(b -> b.getDisplayName().toLowerCase().contains(k)
                || (b.getEmail() != null && b.getEmail().toLowerCase().contains(k))).toList();
    }
    @Override
    public List<Beneficiary> findByType(BeneficiaryType type) {
        return list.stream().filter(b -> b.getType() == type).toList();
    }
}
