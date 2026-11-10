package com.charity.dao;

import com.charity.database.DatabaseConnection;
import com.charity.model.Distribution;
import com.charity.model.PaymentMethod;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OracleDistributionDAO implements DistributionDAO {

    private static final String FIND_ALL = "SELECT id, aid_request_id, beneficiary_id, amount, distribution_date, payment_method, reference, description FROM distributions ORDER BY distribution_date DESC, id DESC";
    private static final String FIND_BY_ID = "SELECT id, aid_request_id, beneficiary_id, amount, distribution_date, payment_method, reference, description FROM distributions WHERE id = ?";
    private static final String INSERT = "INSERT INTO distributions (aid_request_id, beneficiary_id, amount, distribution_date, payment_method, reference, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE = "DELETE FROM distributions WHERE id = ?";
    private static final String FIND_BY_REQUEST = "SELECT id, aid_request_id, beneficiary_id, amount, distribution_date, payment_method, reference, description FROM distributions WHERE aid_request_id = ? ORDER BY distribution_date DESC";
    private static final String TOTAL_AMOUNT = "SELECT NVL(SUM(amount), 0) FROM distributions";

    @Override
    public List<Distribution> findAll() {
        List<Distribution> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_ALL); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all distributions", e);
        }
        return list;
    }

    @Override
    public Optional<Distribution> findById(Long id) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding distribution id=" + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Long save(Distribution distribution) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(INSERT, new String[]{"ID"})) {
            bind(ps, distribution);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            throw new SQLException("Creating distribution failed, no ID obtained.");
        } catch (SQLException e) {
            throw new RuntimeException("Error saving distribution", e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DaoSupport.deleteFailure("distribution", id, e);
        }
    }

    @Override
    public List<Distribution> findByRequest(Long requestId) {
        List<Distribution> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_BY_REQUEST)) {
            ps.setLong(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding distributions by request", e);
        }
        return list;
    }

    @Override
    public BigDecimal totalAmount() {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(TOTAL_AMOUNT); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error computing total distributions", e);
        }
        return BigDecimal.ZERO;
    }

    private void bind(PreparedStatement ps, Distribution d) throws SQLException {
        ps.setLong(1, d.getAidRequestId());
        ps.setLong(2, d.getBeneficiaryId());
        ps.setBigDecimal(3, d.getAmount() == null ? BigDecimal.ZERO : d.getAmount());
        ps.setTimestamp(4, d.getDistributionDate() != null ? Timestamp.valueOf(d.getDistributionDate()) : null);
        ps.setString(5, d.getPaymentMethod().name());
        ps.setString(6, d.getReference());
        ps.setString(7, d.getDescription());
    }

    private Distribution map(ResultSet rs) throws SQLException {
        Distribution d = new Distribution();
        d.setId(rs.getLong("id"));
        d.setAidRequestId(rs.getLong("aid_request_id"));
        d.setBeneficiaryId(rs.getLong("beneficiary_id"));
        d.setAmount(rs.getBigDecimal("amount"));
        Timestamp ts = rs.getTimestamp("distribution_date");
        d.setDistributionDate(ts != null ? ts.toLocalDateTime() : null);
        d.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        d.setReference(rs.getString("reference"));
        d.setDescription(rs.getString("description"));
        return d;
    }
    @Override
    public Long save(Distribution distribution, Connection con) {
        try (PreparedStatement ps = con.prepareStatement(INSERT, new String[]{"ID"})) {
            bind(ps, distribution);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            throw new SQLException("Creating distribution failed, no ID obtained.");
        } catch (SQLException e) {
            throw new RuntimeException("Error saving distribution in transaction", e);
        }
    }
}
