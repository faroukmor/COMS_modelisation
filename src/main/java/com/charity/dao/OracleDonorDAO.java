package com.charity.dao;

import com.charity.database.DatabaseConnection;
import com.charity.model.Donor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Oracle JDBC implementation of {@link DonorDAO}.
 */
public class OracleDonorDAO implements DonorDAO {

    private static final String FIND_ALL =
            "SELECT id, full_name, email, phone, address, total_donated, created_at FROM donors ORDER BY id";
    private static final String FIND_BY_ID =
            "SELECT id, full_name, email, phone, address, total_donated, created_at FROM donors WHERE id = ?";
    private static final String INSERT =
            "INSERT INTO donors (full_name, email, phone, address, total_donated, created_at) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE donors SET full_name=?, email=?, phone=?, address=?, total_donated=? WHERE id = ?";
    private static final String DELETE = "DELETE FROM donors WHERE id = ?";
    private static final String SEARCH =
            "SELECT id, full_name, email, phone, address, total_donated, created_at FROM donors "
            + "WHERE LOWER(full_name) LIKE ? OR LOWER(email) LIKE ? ORDER BY id";

    @Override
    public List<Donor> findAll() {
        List<Donor> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all donors", e);
        }
        return list;
    }

    @Override
    public Optional<Donor> findById(Long id) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding donor id=" + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Long save(Donor donor) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT, new String[]{"ID"})) {
            ps.setString(1, donor.getFullName());
            ps.setString(2, donor.getEmail());
            ps.setString(3, donor.getPhone());
            ps.setString(4, donor.getAddress());
            ps.setBigDecimal(5, donor.getTotalDonated() == null ? BigDecimal.ZERO : donor.getTotalDonated());
            ps.setTimestamp(6, donor.getCreatedAt() != null
                    ? Timestamp.valueOf(donor.getCreatedAt()) : null);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            throw new SQLException("Creating donor failed, no ID obtained.");
        } catch (SQLException e) {
            throw new RuntimeException("Error saving donor", e);
        }
    }

    @Override
    public void update(Donor donor) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, donor.getFullName());
            ps.setString(2, donor.getEmail());
            ps.setString(3, donor.getPhone());
            ps.setString(4, donor.getAddress());
            ps.setBigDecimal(5, donor.getTotalDonated() == null ? BigDecimal.ZERO : donor.getTotalDonated());
            ps.setLong(6, donor.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating donor id=" + donor.getId(), e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DaoSupport.deleteFailure("donor", id, e);
        }
    }

    @Override
    public List<Donor> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        List<Donor> list = new ArrayList<>();
        String like = "%" + keyword.trim().toLowerCase() + "%";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SEARCH)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching donors", e);
        }
        return list;
    }

    private Donor map(ResultSet rs) throws SQLException {
        Donor d = new Donor();
        d.setId(rs.getLong("id"));
        d.setFullName(rs.getString("full_name"));
        d.setEmail(rs.getString("email"));
        d.setPhone(rs.getString("phone"));
        d.setAddress(rs.getString("address"));
        d.setTotalDonated(rs.getBigDecimal("total_donated"));
        Timestamp ts = rs.getTimestamp("created_at");
        d.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return d;
    }
}
