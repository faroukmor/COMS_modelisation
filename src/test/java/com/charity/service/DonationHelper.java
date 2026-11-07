package com.charity.service;

import com.charity.dao.DonationDAO;
import com.charity.model.Donation;
import com.charity.model.DonationStatus;
import com.charity.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

final class DonationHelper {
    private DonationHelper() {}
    static void record(DonationDAO dao, Long donorId, BigDecimal amount, boolean confirmed) {
        Donation d = new Donation();
        d.setDonorId(donorId);
        d.setAmount(amount);
        d.setDonationDate(LocalDateTime.now());
        d.setPaymentMethod(PaymentMethod.CASH);
        d.setStatus(confirmed ? DonationStatus.CONFIRMED : DonationStatus.PENDING);
        dao.save(d);
    }
}
