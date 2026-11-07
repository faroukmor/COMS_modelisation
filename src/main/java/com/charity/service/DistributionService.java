package com.charity.service;

import com.charity.dao.AidRequestDAO;
import com.charity.dao.DistributionDAO;
import com.charity.dao.DonationDAO;
import com.charity.database.DatabaseConnection;
import com.charity.model.Distribution;
import com.charity.model.PaymentMethod;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Business logic for disbursing financial aid against approved requests.
 * <p>
 * All financial rules are enforced here and the distribution is performed inside a
 * database transaction so that either all changes commit or none do.
 */
public class DistributionService {
    private final AidRequestDAO requestDAO;
    private final DistributionDAO distributionDAO;
    private final DonationDAO donationDAO;

    public DistributionService(AidRequestDAO requestDAO,
                               DistributionDAO distributionDAO,
                               DonationDAO donationDAO) {
        this.requestDAO = requestDAO;
        this.distributionDAO = distributionDAO;
        this.donationDAO = donationDAO;
    }

    /**
     * Disburses financial aid against an approved request.
     *
     * @throws IllegalArgumentException if any business rule is violated
     */
    public Long disburse(Long requestId, BigDecimal amount, PaymentMethod paymentMethod,
                         String reference, String description) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Distribution amount must be positive");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        Connection con = null;
        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            // Load the request within the transaction
            AidRequestHolder holder = new AidRequestHolder();
            loadRequest(con, requestId, holder);
            if (holder.request.getStatus() != com.charity.model.RequestStatus.APPROVED) {
                throw new IllegalStateException("Only approved requests can receive a distribution");
            }

            // Rule: amount cannot exceed remaining approved amount
            BigDecimal remaining = holder.request.getApprovedAmount()
                    .subtract(holder.request.getDistributedAmount());
            if (amount.compareTo(remaining) > 0) {
                throw new IllegalArgumentException(
                        "Distribution amount exceeds remaining approved amount (" + remaining + ")");
            }

            // Rule: amount cannot exceed available fund balance
            BigDecimal available = donationDAO.totalConfirmedAmount()
                    .subtract(distributionDAO.totalAmount());
            if (available == null) {
                available = BigDecimal.ZERO;
            }
            if (amount.compareTo(available) > 0) {
                throw new IllegalArgumentException(
                        "Distribution amount exceeds available fund balance (" + available + ")");
            }

            // Create the distribution
            Distribution distribution = new Distribution();
            distribution.setAidRequestId(requestId);
            distribution.setBeneficiaryId(holder.request.getBeneficiaryId());
            distribution.setAmount(amount);
            distribution.setPaymentMethod(paymentMethod);
            distribution.setReference(reference);
            distribution.setDescription(description);
            Long distributionId = distributionDAO.save(distribution, con);

            // Update the request's distributed amount
            BigDecimal newDistributed = holder.request.getDistributedAmount().add(amount);
            holder.request.setDistributedAmount(newDistributed);
            if (newDistributed.compareTo(holder.request.getApprovedAmount()) >= 0) {
                holder.request.setStatus(com.charity.model.RequestStatus.FULFILLED);
            }
            requestDAO.update(holder.request, con);

            con.commit();
            return distributionId;
        } catch (IllegalArgumentException | IllegalStateException e) {
            rollback(con);
            throw e;
        } catch (SQLException e) {
            rollback(con);
            throw new RuntimeException("Database error during distribution", e);
        } finally {
            close(con);
        }
    }

    private void loadRequest(Connection con, Long requestId, AidRequestHolder holder) throws SQLException {
        try (java.sql.PreparedStatement ps = con.prepareStatement(
                "SELECT id, beneficiary_id, beneficiary_type, requested_amount, approved_amount, "
                + "distributed_amount, description, submission_date, status, reviewed_at, reviewed_by, admin_notes "
                + "FROM aid_requests WHERE id = ?")) {
            ps.setLong(1, requestId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Aid request not found: " + requestId);
                }
                com.charity.model.AidRequest r = new com.charity.model.AidRequest();
                r.setId(rs.getLong("id"));
                r.setBeneficiaryId(rs.getLong("beneficiary_id"));
                r.setBeneficiaryType(com.charity.model.BeneficiaryType.valueOf(rs.getString("beneficiary_type")));
                r.setRequestedAmount(rs.getBigDecimal("requested_amount"));
                r.setApprovedAmount(rs.getBigDecimal("approved_amount"));
                r.setDistributedAmount(rs.getBigDecimal("distributed_amount"));
                r.setStatus(com.charity.model.RequestStatus.valueOf(rs.getString("status")));
                holder.request = r;
            }
        }
    }

    private void rollback(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private static final class AidRequestHolder {
        com.charity.model.AidRequest request;
    }
}
