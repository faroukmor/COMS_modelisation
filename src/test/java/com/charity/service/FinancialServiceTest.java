package com.charity.service;

import static org.junit.jupiter.api.Assertions.*;
import com.charity.dao.DistributionDAO;
import com.charity.dao.DonationDAO;
import com.charity.service.fakes.FakeDonationDAO;
import com.charity.service.fakes.FakeDistributionDAO;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class FinancialServiceTest {
    private DonationDAO donationDAO;
    private DistributionDAO distributionDAO;
    private FinancialService service;

    @BeforeEach
    void setUp() {
        donationDAO = new FakeDonationDAO();
        distributionDAO = new FakeDistributionDAO();
        service = new FinancialService(donationDAO, distributionDAO);
    }

    @Test
    @DisplayName("available balance = confirmed donations - distributed funds")
    void availableBalance() {
        DonationHelper.record(donationDAO, 1L, new BigDecimal("100000.00"), true);
        DonationHelper.record(donationDAO, 2L, new BigDecimal("50000.00"), true);
        DonationHelper.record(donationDAO, 3L, new BigDecimal("20000.00"), false); // pending, not counted
        DistributionHelper.record(distributionDAO, 1L, new BigDecimal("30000.00"));

        assertEquals(new BigDecimal("100000.00"), service.getTotalConfirmedDonations());
        assertEquals(new BigDecimal("30000.00"), service.getTotalDistributed());
        assertEquals(new BigDecimal("70000.00"), service.getAvailableBalance());
    }

    @Test
    @DisplayName("available balance is zero when no transactions")
    void emptyBalance() {
        assertEquals(BigDecimal.ZERO, service.getAvailableBalance());
    }

    @Test
    @DisplayName("balance can go negative if distributions exceed donations (not prevented at calc level)")
    void negativeBalance() {
        DistributionHelper.record(distributionDAO, 1L, new BigDecimal("50000.00"));
        assertEquals(new BigDecimal("-50000.00"), service.getAvailableBalance());
    }
}
