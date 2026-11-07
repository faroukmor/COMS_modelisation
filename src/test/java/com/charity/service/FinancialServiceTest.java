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
        // Donation 1: 100000 confirmed
        DonationHelper.record(donationDAO, 1L, new BigDecimal("100000.00"), true);
        // Donation 2: 50000 pending (not counted toward balance)
        DonationHelper.record(donationDAO, 2L, new BigDecimal("50000.00"), false);
        // Donation 3: 20000 pending (not counted toward balance)
        DonationHelper.record(donationDAO, 3L, new BigDecimal("20000.00"), false);
        // Distribution of 30000
        DistributionHelper.record(distributionDAO, 1L, new BigDecimal("30000.00"));

        // Confirmed = 100000 only (donations 2&3 are pending)
        // Distributed = 30000
        // Available = 100000 - 30000 = 70000
        assertEquals(new BigDecimal("100000.00"), service.getTotalConfirmedDonations());
        assertEquals(new BigDecimal("30000.00"), service.getTotalDistributed());
        assertEquals(new BigDecimal("70000.00"), service.getAvailableBalance());
    }

    @Test
    @DisplayName("available balance is zero when no transactions")
    void emptyBalance() {
        assertEquals(BigDecimal.ZERO, service.getAvailableBalance());
    }
}
