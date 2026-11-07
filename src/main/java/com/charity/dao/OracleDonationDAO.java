package com.charity.dao;

import com.charity.database.DatabaseConnection;
import com.charity.model.Donation;
import com.charity.model.DonationStatus;
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

public class OracleDonationDAO implements DonationDAO {

    private static final String FIND_ALL = "SELECT id, donor_id, amount, donation_date, payment_method, status, description, reference FROM donations ORDER BY donation_date DESC, id DESC";
    private static final String FIND_BY_ID = "SELECT id, donor_id, amount, donation_date, payment_method, status, description, reference FROM donations WHERE id = ?";
    private static final String INSERT = "INSERT INTO donations (donor_id, amount, donation_date, payment_method, status, description, reference) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE donations SET donor_id=?, amount=?, donation_date=?, payment_method=?, status=?, description=?, reference=? WHERE id = ?";
    private static final String DELETE = "DELETE FROM donations WHERE id = ?";
    private static final String UPDATE_STATUS = "UPDATE donations SET status = ? WHERE id = ?";
    private static final String FIND_BY_DONOR = "SELECT id, donor_id, amount, donation_date, payment_method, status, description, reference FROM donations WHERE donor_id = ? ORDER BY donation_date DESC";
    private static final String FIND_BY_STATUS = "SELECT id, donor_id, amount, donation_date, payment_method, status, description, reference FROM donations WHERE status = ? ORDER BY donation_date DESC";
    private static final String TOTAL_CONFIRMED = "SELECT NVL(SUM(amount), 0) FROM donations WHERE status = 'CONFIRMED'";

    @Override
    public List<Donation> findAll() {
        List<Donation> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_ALL); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all donations", e);
        }
        return list;
    }

    @Override
    public Optional<Donation> findById(Long id) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding donation id=" + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Long save(Donation donation) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(INSERT, new String[]{"id"})) {
            bind(ps, donation);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            throw new SQLException("Creating donation failed, no ID obtained.");
        } catch (SQLException e) {
            throw new RuntimeException("Error saving donation", e);
        }
    }

    @Override
    public void update(Donation donation) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
            bind(ps, donation);
            ps.setLong(8, donation.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating donation id=" + donation.getId(), e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting donation id=" + id, e);
        }
    }

    @Override
    public void updateStatus(Long id, DonationStatus status) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE_STATUS)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating donation status id=" + id, e);
        }
    }

    @Override
    public List<Donation> findByDonor(Long donorId) {
        List<Donation> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_BY_DONOR)) {
            ps.setLong(1, donorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding donations by donor", e);
        }
        return list;
    }

    @Override
    public List<Donation> findByStatus(DonationStatus status) {
        List<Donation> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_BY_STATUS)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding donations by status", e);
        }
        return list;
    }

    @Override
    public BigDecimal totalConfirmedAmount() {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(TOTAL_CONFIRMED); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error computing total confirmed donations", e);
        }
        return BigDecimal.ZERO;
    }

    private void bind(PreparedStatement ps, Donation d) throws SQLException {
        ps.setLong(1, d.getDonorId());
        ps.setBigDecimal(2, d.getAmount() == null ? BigDecimal.ZERO : d.getAmount());
        ps.setTimestamp(3, d.getDonationDate() != null ? Timestamp.valueOf(d.getDonationDate()) : null);
        ps.setString(4, d.getPaymentMethod().name());
        ps.setString(5, d.getStatus().name());
        ps.setString(6, d.getDescription());
        ps.setString(7, d.getReference());
    }

    private Donation map(ResultSet rs) throws SQLException {
        Donation d = new Donation();
        d.setId(rs.getLong("id"));
        d.setDonorId(rs.getLong("donor_id"));
        d.setAmount(rs.getBigDecimal("amount"));
        Timestamp ts = rs.getTimestamp("donation_date");
        d.setDonationDate(ts != null ? ts.toLocalDateTime() : null);
        d.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        d.setStatus(DonationStatus.valueOf(rs.getString("status")));
        d.setDescription(rs.getString("description"));
        d.setReference(rs.getString("reference"));
        return d;
    }
}
