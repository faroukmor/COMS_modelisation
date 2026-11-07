package com.charity.service;

import static org.junit.jupiter.api.Assertions.*;
import com.charity.dao.AidRequestDAO;
import com.charity.dao.DistributionDAO;
import com.charity.dao.DonationDAO;
import com.charity.model.AidRequest;
import com.charity.model.PaymentMethod;
import com.charity.model.RequestStatus;
import com.charity.service.fakes.FakeAidRequestDAO;
import com.charity.service.fakes.FakeDistributionDAO;
import com.charity.service.fakes.FakeDonationDAO;
import java.math.BigDecimal;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Tests distribution business rules: positive amount, approved-only, overspending
 * prevention, available-balance cap, partial distribution, and fulfillment.
 */
class DistributionServiceTest {
    private AidRequestDAO requestDAO;
    private DistributionDAO distributionDAO;
    private DonationDAO donationDAO;

    @BeforeEach
    void setUp() {
        requestDAO = new FakeAidRequestDAO();
        distributionDAO = new FakeDistributionDAO();
        donationDAO = new FakeDonationDAO();
    }

    private DistributionService serviceWith(Supplier<java.sql.Connection> supplier) {
        return new DistributionService(requestDAO, distributionDAO, donationDAO, supplier);
    }

    private AidRequest approvedRequest(Long id, BigDecimal approved, BigDecimal distributed) {
        AidRequest r = new AidRequest();
        r.setId(id);
        r.setBeneficiaryId(1L);
        r.setRequestedAmount(approved);
        r.setApprovedAmount(approved);
        r.setDistributedAmount(distributed == null ? BigDecimal.ZERO : distributed);
        r.setStatus(RequestStatus.APPROVED);
        requestDAO.save(r);
        return r;
    }

    @Test
    @DisplayName("rejects non-positive distribution amount")
    void rejectNonPositiveAmount() {
        approvedRequest(1L, new BigDecimal("10000"), null);
        DistributionService s = serviceWith(DbFakes::connection);
        assertThrows(IllegalArgumentException.class,
                () -> s.disburse(1L, BigDecimal.ZERO, PaymentMethod.CASH, "ref", "desc"));
        assertThrows(IllegalArgumentException.class,
                () -> s.disburse(1L, new BigDecimal("-50"), PaymentMethod.CASH, "ref", "desc"));
        assertThrows(IllegalArgumentException.class,
                () -> s.disburse(1L, null, PaymentMethod.CASH, "ref", "desc"));
    }

    @Test
    @DisplayName("rejects null payment method")
    void rejectNullPaymentMethod() {
        approvedRequest(1L, new BigDecimal("10000"), null);
        DistributionService s = serviceWith(DbFakes::connection);
        assertThrows(IllegalArgumentException.class,
                () -> s.disburse(1L, new BigDecimal("1000"), null, "ref", "desc"));
    }

    @Test
    @DisplayName("rejects unknown request")
    void rejectUnknownRequest() {
        DistributionService s = serviceWith(DbFakes::connection);
        assertThrows(IllegalArgumentException.class,
                () -> s.disburse(99L, new BigDecimal("1000"), PaymentMethod.CASH, "ref", "desc"));
    }

    @Test
    @DisplayName("rejects non-approved request (pending)")
    void rejectNonApproved() {
        AidRequest r = new AidRequest();
        r.setId(2L); r.setBeneficiaryId(1L); r.setRequestedAmount(new BigDecimal("10000"));
        r.setApprovedAmount(new BigDecimal("10000")); r.setDistributedAmount(BigDecimal.ZERO);
        r.setStatus(RequestStatus.PENDING);
        requestDAO.save(r);
        DistributionService s = serviceWith(DbFakes::connection);
        assertThrows(IllegalStateException.class,
                () -> s.disburse(2L, new BigDecimal("1000"), PaymentMethod.CASH, "ref", "desc"));
    }

    @Test
    @DisplayName("prevents overspending beyond remaining approved amount")
    void preventOverspending() {
        approvedRequest(3L, new BigDecimal("10000"), new BigDecimal("8000")); // remaining 2000
        DistributionService s = serviceWith(DbFakes::connection);
        assertThrows(IllegalArgumentException.class,
                () -> s.disburse(3L, new BigDecimal("5000"), PaymentMethod.CASH, "ref", "desc"));
    }

    @Test
    @DisplayName("prevents distribution exceeding available fund balance")
    void preventExceedsBalance() {
        DonationHelper.record(donationDAO, 1L, new BigDecimal("5000.00"), true); // available 5000
        approvedRequest(4L, new BigDecimal("10000"), null);
        DistributionService s = serviceWith(DbFakes::connection);
        assertThrows(IllegalArgumentException.class,
                () -> s.disburse(4L, new BigDecimal("6000"), PaymentMethod.CASH, "ref", "desc"));
    }

    @Test
    @DisplayName("partial distribution keeps request APPROVED and updates distributed amount")
    void partialDistribution() {
        DonationHelper.record(donationDAO, 1L, new BigDecimal("50000.00"), true);
        approvedRequest(5L, new BigDecimal("10000"), null);
        DistributionService s = serviceWith(DbFakes::connection);
        Long distId = s.disburse(5L, new BigDecimal("4000"), PaymentMethod.CASH, "ref", "desc");
        assertNotNull(distId);
        AidRequest r = requestDAO.findById(5L).get();
        assertEquals(RequestStatus.APPROVED, r.getStatus());
        assertEquals(new BigDecimal("4000"), r.getDistributedAmount());
        assertEquals(new BigDecimal("6000"), r.getRemainingAmount());
    }

    @Test
    @DisplayName("full distribution marks request FULFILLED")
    void fullDistributionFulfills() {
        DonationHelper.record(donationDAO, 1L, new BigDecimal("50000.00"), true);
        approvedRequest(6L, new BigDecimal("7000"), null);
        DistributionService s = serviceWith(DbFakes::connection);
        s.disburse(6L, new BigDecimal("7000"), PaymentMethod.CASH, "ref", "desc");
        AidRequest r = requestDAO.findById(6L).get();
        assertEquals(RequestStatus.FULFILLED, r.getStatus());
        assertEquals(new BigDecimal("7000"), r.getDistributedAmount());
    }

    @Test
    @DisplayName("second partial distribution can complete fulfillment")
    void secondPartialCompletesFulfillment() {
        DonationHelper.record(donationDAO, 1L, new BigDecimal("50000.00"), true);
        approvedRequest(7L, new BigDecimal("10000"), null);
        DistributionService s = serviceWith(DbFakes::connection);
        s.disburse(7L, new BigDecimal("4000"), PaymentMethod.CASH, "ref", "a");
        s.disburse(7L, new BigDecimal("6000"), PaymentMethod.CASH, "ref", "b");
        AidRequest r = requestDAO.findById(7L).get();
        assertEquals(RequestStatus.FULFILLED, r.getStatus());
        assertEquals(new BigDecimal("10000"), r.getDistributedAmount());
    }
}
