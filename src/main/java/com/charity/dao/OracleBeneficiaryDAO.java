package com.charity.dao;

import com.charity.database.DatabaseConnection;
import com.charity.model.Beneficiary;
import com.charity.model.BeneficiaryType;
import com.charity.model.CharityAssociation;
import com.charity.model.Individual;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OracleBeneficiaryDAO implements BeneficiaryDAO {

    private static final String FIND_ALL = "SELECT id, type, full_name, national_id, organization_name, registration_number, representative_name, email, phone, address, created_at FROM beneficiaries ORDER BY id";
    private static final String FIND_BY_ID = "SELECT id, type, full_name, national_id, organization_name, registration_number, representative_name, email, phone, address, created_at FROM beneficiaries WHERE id = ?";
    private static final String INSERT = "INSERT INTO beneficiaries (type, full_name, national_id, organization_name, registration_number, representative_name, email, phone, address, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE beneficiaries SET type=?, full_name=?, national_id=?, organization_name=?, registration_number=?, representative_name=?, email=?, phone=?, address=? WHERE id = ?";
    private static final String DELETE = "DELETE FROM beneficiaries WHERE id = ?";
    private static final String SEARCH = "SELECT id, type, full_name, national_id, organization_name, registration_number, representative_name, email, phone, address, created_at FROM beneficiaries WHERE LOWER(full_name) LIKE ? OR LOWER(organization_name) LIKE ? OR LOWER(email) LIKE ? ORDER BY id";
    private static final String FIND_BY_TYPE = "SELECT id, type, full_name, national_id, organization_name, registration_number, representative_name, email, phone, address, created_at FROM beneficiaries WHERE type = ? ORDER BY id";

    @Override
    public List<Beneficiary> findAll() {
        List<Beneficiary> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_ALL); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all beneficiaries", e);
        }
        return list;
    }

    @Override
    public Optional<Beneficiary> findById(Long id) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding beneficiary id=" + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Long save(Beneficiary beneficiary) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(INSERT, new String[]{"ID"})) {
            bind(ps, beneficiary);
            ps.setTimestamp(10, beneficiary.getCreatedAt() != null
                    ? Timestamp.valueOf(beneficiary.getCreatedAt()) : null);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            throw new SQLException("Creating beneficiary failed, no ID obtained.");
        } catch (SQLException e) {
            throw new RuntimeException("Error saving beneficiary", e);
        }
    }

    @Override
    public void update(Beneficiary beneficiary) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
            bind(ps, beneficiary);
            ps.setLong(10, beneficiary.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating beneficiary id=" + beneficiary.getId(), e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DaoSupport.deleteFailure("beneficiary", id, e);
        }
    }

    @Override
    public List<Beneficiary> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        List<Beneficiary> list = new ArrayList<>();
        String like = "%" + keyword.trim().toLowerCase() + "%";
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(SEARCH)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching beneficiaries", e);
        }
        return list;
    }

    @Override
    public List<Beneficiary> findByType(BeneficiaryType type) {
        List<Beneficiary> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_BY_TYPE)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding beneficiaries by type", e);
        }
        return list;
    }

    private void bind(PreparedStatement ps, Beneficiary b) throws SQLException {
        ps.setString(1, b.getType().name());
        ps.setString(2, b instanceof Individual ? ((Individual) b).getFullName() : null);
        ps.setString(3, b instanceof Individual ? ((Individual) b).getNationalId() : null);
        ps.setString(4, b instanceof CharityAssociation ? ((CharityAssociation) b).getOrganizationName() : null);
        ps.setString(5, b instanceof CharityAssociation ? ((CharityAssociation) b).getRegistrationNumber() : null);
        ps.setString(6, b instanceof CharityAssociation ? ((CharityAssociation) b).getRepresentativeName() : null);
        ps.setString(7, b.getEmail());
        ps.setString(8, b.getPhone());
        ps.setString(9, b.getAddress());
    }

    private Beneficiary map(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String address = rs.getString("address");
        Timestamp ts = rs.getTimestamp("created_at");
        if (BeneficiaryType.INDIVIDUAL.name().equals(type)) {
            Individual ind = new Individual();
            ind.setId(rs.getLong("id"));
            ind.setType(BeneficiaryType.INDIVIDUAL);
            ind.setFullName(rs.getString("full_name"));
            ind.setNationalId(rs.getString("national_id"));
            ind.setEmail(email);
            ind.setPhone(phone);
            ind.setAddress(address);
            ind.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
            return ind;
        } else {
            CharityAssociation assoc = new CharityAssociation();
            assoc.setId(rs.getLong("id"));
            assoc.setType(BeneficiaryType.ASSOCIATION);
            assoc.setOrganizationName(rs.getString("organization_name"));
            assoc.setRegistrationNumber(rs.getString("registration_number"));
            assoc.setRepresentativeName(rs.getString("representative_name"));
            assoc.setEmail(email);
            assoc.setPhone(phone);
            assoc.setAddress(address);
            assoc.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
            return assoc;
        }
    }
}
