package com.charity.service;

import com.charity.dao.AidRequestDAO;
import com.charity.dao.DistributionDAO;
import com.charity.dao.DonationDAO;
import com.charity.database.DatabaseConnection;
import com.charity.model.AidRequest;
import com.charity.model.Distribution;
import com.charity.model.PaymentMethod;
import com.charity.model.RequestStatus;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Business logic for disbursing financial aid against approved requests.
 * <p>
 * All financial rules are enforced here using injected DAOs. The distribution is
 * performed inside a database transaction so that either all changes commit or
 * none do (rollback on failure). The {@link Connection} supplier is injectable
 * so the business rules can be tested without a live database.
 */
public class DistributionService {
    private final AidRequestDAO requestDAO;
    private final DistributionDAO distributionDAO;
    private final DonationDAO donationDAO;
    private final Supplier<Connection> connectionSupplier;

    public DistributionService(AidRequestDAO requestDAO,
                               DistributionDAO distributionDAO,
                               DonationDAO donationDAO) {
        this(requestDAO, distributionDAO, donationDAO, () -> {
            try {
                return DatabaseConnection.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    DistributionService(AidRequestDAO requestDAO,
                        DistributionDAO distributionDAO,
                        DonationDAO donationDAO,
                        Supplier<Connection> connectionSupplier) {
        this.requestDAO = requestDAO;
        this.distributionDAO = distributionDAO;
        this.donationDAO = donationDAO;
        this.connectionSupplier = connectionSupplier;
    }

    /**
     * Disburses financial aid against an approved request.
     *
     * @throws IllegalArgumentException if any business rule is violated
     * @throws IllegalStateException    if the request is not approved
     */
    public Long disburse(Long requestId, BigDecimal amount, PaymentMethod paymentMethod,
                         String reference, String description) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Distribution amount must be positive");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        // Load and validate via injected DAO (no DB connection needed for validation)
        AidRequest request = requestDAO.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Aid request not found: " + requestId));
        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new IllegalStateException("Only approved requests can receive a distribution");
        }

        // Rule: amount cannot exceed remaining approved amount
        BigDecimal remaining = request.getApprovedAmount().subtract(request.getDistributedAmount());
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

        // Write phase: single transaction
        Connection con = null;
        try {
            con = connectionSupplier.get();
            con.setAutoCommit(false);

            Distribution distribution = new Distribution();
            distribution.setAidRequestId(requestId);
            distribution.setBeneficiaryId(request.getBeneficiaryId());
            distribution.setAmount(amount);
            distribution.setPaymentMethod(paymentMethod);
            distribution.setReference(reference);
            distribution.setDescription(description);
            Long distributionId = distributionDAO.save(distribution, con);

            BigDecimal newDistributed = request.getDistributedAmount().add(amount);
            request.setDistributedAmount(newDistributed);
            if (newDistributed.compareTo(request.getApprovedAmount()) >= 0) {
                request.setStatus(RequestStatus.FULFILLED);
            }
            requestDAO.update(request, con);

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
}
