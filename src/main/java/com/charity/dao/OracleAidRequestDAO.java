package com.charity.dao;

import com.charity.database.DatabaseConnection;
import com.charity.model.AidRequest;
import com.charity.model.BeneficiaryType;
import com.charity.model.RequestStatus;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OracleAidRequestDAO implements AidRequestDAO {

    private static final String FIND_ALL = "SELECT id, beneficiary_id, beneficiary_type, requested_amount, approved_amount, distributed_amount, description, submission_date, status, reviewed_at, reviewed_by, admin_notes FROM aid_requests ORDER BY submission_date DESC, id DESC";
    private static final String FIND_BY_ID = "SELECT id, beneficiary_id, beneficiary_type, requested_amount, approved_amount, distributed_amount, description, submission_date, status, reviewed_at, reviewed_by, admin_notes FROM aid_requests WHERE id = ?";
    private static final String INSERT = "INSERT INTO aid_requests (beneficiary_id, beneficiary_type, requested_amount, approved_amount, distributed_amount, description, submission_date, status, reviewed_at, reviewed_by, admin_notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE aid_requests SET beneficiary_id=?, beneficiary_type=?, requested_amount=?, approved_amount=?, distributed_amount=?, description=?, submission_date=?, status=?, reviewed_at=?, reviewed_by=?, admin_notes=? WHERE id = ?";
    private static final String DELETE = "DELETE FROM aid_requests WHERE id = ?";
    private static final String UPDATE_STATUS = "UPDATE aid_requests SET status = ? WHERE id = ?";
    private static final String SET_APPROVED_AMOUNT = "UPDATE aid_requests SET approved_amount = ? WHERE id = ?";
    private static final String FIND_BY_BENEFICIARY = "SELECT id, beneficiary_id, beneficiary_type, requested_amount, approved_amount, distributed_amount, description, submission_date, status, reviewed_at, reviewed_by, admin_notes FROM aid_requests WHERE beneficiary_id = ? ORDER BY submission_date DESC";
    private static final String FIND_BY_STATUS = "SELECT id, beneficiary_id, beneficiary_type, requested_amount, approved_amount, distributed_amount, description, submission_date, status, reviewed_at, reviewed_by, admin_notes FROM aid_requests WHERE status = ? ORDER BY submission_date DESC";
    private static final String COUNT_BY_STATUS = "SELECT COUNT(*) FROM aid_requests WHERE status = ?";
    private static final String TOTAL_DISTRIBUTED_BY_REQUEST = "SELECT NVL(SUM(distributed_amount), 0) FROM aid_requests WHERE id = ?";

    @Override
    public List<AidRequest> findAll() {
        List<AidRequest> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_ALL); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all aid requests", e);
        }
        return list;
    }

    @Override
    public Optional<AidRequest> findById(Long id) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding aid request id=" + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Long save(AidRequest request) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(INSERT, new String[]{"ID"})) {
            bind(ps, request);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            throw new SQLException("Creating aid request failed, no ID obtained.");
        } catch (SQLException e) {
            throw new RuntimeException("Error saving aid request", e);
        }
    }

    @Override
    public void update(AidRequest request) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE)) {
            bind(ps, request);
            ps.setLong(12, request.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating aid request id=" + request.getId(), e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DaoSupport.deleteFailure("aid request", id, e);
        }
    }

    @Override
    public void updateStatus(Long id, RequestStatus status) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE_STATUS)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating aid request status id=" + id, e);
        }
    }

    @Override
    public void setApprovedAmount(Long id, BigDecimal approvedAmount) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(SET_APPROVED_AMOUNT)) {
            ps.setBigDecimal(1, approvedAmount == null ? BigDecimal.ZERO : approvedAmount);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error setting approved amount id=" + id, e);
        }
    }

    @Override
    public List<AidRequest> findByBeneficiary(Long beneficiaryId) {
        List<AidRequest> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_BY_BENEFICIARY)) {
            ps.setLong(1, beneficiaryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding aid requests by beneficiary", e);
        }
        return list;
    }

    @Override
    public List<AidRequest> findByStatus(RequestStatus status) {
        List<AidRequest> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(FIND_BY_STATUS)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding aid requests by status", e);
        }
        return list;
    }

    @Override
    public int countByStatus(RequestStatus status) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(COUNT_BY_STATUS)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting aid requests by status", e);
        }
        return 0;
    }

    @Override
    public BigDecimal totalDistributedByRequest(Long requestId) {
        try (Connection con = DatabaseConnection.getConnection(); PreparedStatement ps = con.prepareStatement(TOTAL_DISTRIBUTED_BY_REQUEST)) {
            ps.setLong(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error computing distributed amount for request", e);
        }
        return BigDecimal.ZERO;
    }

    private void bind(PreparedStatement ps, AidRequest r) throws SQLException {
        ps.setLong(1, r.getBeneficiaryId());
        ps.setString(2, r.getBeneficiaryType().name());
        ps.setBigDecimal(3, r.getRequestedAmount() == null ? BigDecimal.ZERO : r.getRequestedAmount());
        ps.setBigDecimal(4, r.getApprovedAmount() == null ? BigDecimal.ZERO : r.getApprovedAmount());
        ps.setBigDecimal(5, r.getDistributedAmount() == null ? BigDecimal.ZERO : r.getDistributedAmount());
        ps.setString(6, r.getDescription());
        ps.setTimestamp(7, r.getSubmissionDate() != null ? Timestamp.valueOf(r.getSubmissionDate()) : null);
        ps.setString(8, r.getStatus().name());
        ps.setTimestamp(9, r.getReviewedAt() != null ? Timestamp.valueOf(r.getReviewedAt()) : null);
        if (r.getReviewedBy() == null) {
            ps.setNull(10, java.sql.Types.NUMERIC);
        } else {
            ps.setLong(10, r.getReviewedBy());
        }
        ps.setString(11, r.getAdminNotes());
    }

    private AidRequest map(ResultSet rs) throws SQLException {
        AidRequest r = new AidRequest();
        r.setId(rs.getLong("id"));
        r.setBeneficiaryId(rs.getLong("beneficiary_id"));
        r.setBeneficiaryType(BeneficiaryType.valueOf(rs.getString("beneficiary_type")));
        r.setRequestedAmount(rs.getBigDecimal("requested_amount"));
        r.setApprovedAmount(rs.getBigDecimal("approved_amount"));
        r.setDistributedAmount(rs.getBigDecimal("distributed_amount"));
        r.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("submission_date");
        r.setSubmissionDate(ts != null ? ts.toLocalDateTime() : null);
        r.setStatus(RequestStatus.valueOf(rs.getString("status")));
        Timestamp rts = rs.getTimestamp("reviewed_at");
        r.setReviewedAt(rts != null ? rts.toLocalDateTime() : null);
        Long reviewedBy = rs.getLong("reviewed_by");
        r.setReviewedBy(rs.wasNull() ? null : reviewedBy);
        r.setAdminNotes(rs.getString("admin_notes"));
        return r;
    }
    @Override
    public void update(AidRequest request, Connection con) {
        try (PreparedStatement ps = con.prepareStatement(UPDATE)) {
            bind(ps, request);
            ps.setLong(12, request.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating aid request in transaction id=" + request.getId(), e);
        }
    }
}
