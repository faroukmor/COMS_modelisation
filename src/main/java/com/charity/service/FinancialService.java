package com.charity.service;

import com.charity.dao.DistributionDAO;
import com.charity.dao.DonationDAO;
import java.math.BigDecimal;

/**
 * Financial calculations for the organization's single general fund.
 * <p>
 * Available Balance = Total Confirmed Donations - Total Distributed Funds.
 * The balance is always derived from recorded transactions, never edited manually.
 */
public class FinancialService {
    private final DonationDAO donationDAO;
    private final DistributionDAO distributionDAO;

    public FinancialService(DonationDAO donationDAO, DistributionDAO distributionDAO) {
        this.donationDAO = donationDAO;
        this.distributionDAO = distributionDAO;
    }

    public BigDecimal getTotalConfirmedDonations() {
        return donationDAO.totalConfirmedAmount() == null ? BigDecimal.ZERO : donationDAO.totalConfirmedAmount();
    }

    public BigDecimal getTotalDistributed() {
        return distributionDAO.totalAmount() == null ? BigDecimal.ZERO : distributionDAO.totalAmount();
    }

    /**
     * Available balance = confirmed donations - distributed funds.
     */
    public BigDecimal getAvailableBalance() {
        return getTotalConfirmedDonations().subtract(getTotalDistributed());
    }
}

