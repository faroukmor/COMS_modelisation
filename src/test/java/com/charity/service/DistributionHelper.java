package com.charity.service;

import com.charity.dao.DistributionDAO;
import com.charity.model.Distribution;
import com.charity.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

final class DistributionHelper {
    private DistributionHelper() {}
    static void record(DistributionDAO dao, Long requestId, BigDecimal amount) {
        Distribution d = new Distribution();
        d.setAidRequestId(requestId);
        d.setBeneficiaryId(1L);
        d.setAmount(amount);
        d.setDistributionDate(LocalDateTime.now());
        d.setPaymentMethod(PaymentMethod.CASH);
        dao.save(d);
    }
}
