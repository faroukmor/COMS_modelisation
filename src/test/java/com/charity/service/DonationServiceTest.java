package com.charity.service;

import static org.junit.jupiter.api.Assertions.*;
import com.charity.dao.DonationDAO;
import com.charity.dao.DonorDAO;
import com.charity.model.DonationStatus;
import com.charity.model.PaymentMethod;
import com.charity.service.fakes.FakeDonationDAO;
import com.charity.service.fakes.FakeDonorDAO;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class DonationServiceTest {
    private DonationDAO donationDAO;
    private DonorDAO donorDAO;
    private DonationService service;

    @BeforeEach
    void setUp() {
        donationDAO = new FakeDonationDAO();
        donorDAO = new FakeDonorDAO();
        service = new DonationService(donationDAO, donorDAO);
    }

    @Test
    @DisplayName("records a pending donation with positive amount")
    void recordPositiveDonation() {
        Long id = service.recordDonation(1L, new BigDecimal("100.00"), PaymentMethod.CASH, "Zakat", null);
        assertNotNull(id);
        assertEquals(DonationStatus.PENDING, donationDAO.findById(id).get().getStatus());
    }

    @Test
    @DisplayName("rejects zero amount")
    void rejectZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordDonation(1L, BigDecimal.ZERO, PaymentMethod.CASH, null, null));
    }

    @Test
    @DisplayName("rejects negative amount")
    void rejectNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordDonation(1L, new BigDecimal("-50"), PaymentMethod.CASH, null, null));
    }

    @Test
    @DisplayName("rejects null amount")
    void rejectNullAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordDonation(1L, null, PaymentMethod.CASH, null, null));
    }

    @Test
    @DisplayName("confirm only pending donations")
    void confirmOnlyPending() {
        Long id = service.recordDonation(1L, new BigDecimal("100.00"), PaymentMethod.CASH, null, null);
        service.confirmDonation(id);
        assertEquals(DonationStatus.CONFIRMED, donationDAO.findById(id).get().getStatus());
    }

    @Test
    @DisplayName("confirming already confirmed donation throws")
    void confirmAlreadyConfirmed() {
        Long id = service.recordDonation(1L, new BigDecimal("100.00"), PaymentMethod.CASH, null, null);
        service.confirmDonation(id);
        assertThrows(IllegalStateException.class, () -> service.confirmDonation(id));
    }

    @Test
    @DisplayName("cancelling a confirmed donation throws")
    void cancelConfirmedThrows() {
        Long id = service.recordDonation(1L, new BigDecimal("100.00"), PaymentMethod.CASH, null, null);
        service.confirmDonation(id);
        assertThrows(IllegalStateException.class, () -> service.cancelDonation(id));
    }

    @Test
    @DisplayName("cancelling a pending donation works")
    void cancelPending() {
        Long id = service.recordDonation(1L, new BigDecimal("100.00"), PaymentMethod.CASH, null, null);
        service.cancelDonation(id);
        assertEquals(DonationStatus.CANCELLED, donationDAO.findById(id).get().getStatus());
    }
}
