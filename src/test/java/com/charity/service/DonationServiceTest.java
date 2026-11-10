package com.charity.service;

import static org.junit.jupiter.api.Assertions.*;
import com.charity.dao.DonationDAO;
import com.charity.dao.DonorDAO;
import com.charity.model.DonationStatus;
import com.charity.model.Donor;
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
        donorDAO.save(new Donor(null, "Test Donor", "donor@test.org", "0555", "Algiers")); // id 1
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

    @Test
    @DisplayName("rejects an unknown donor instead of failing later on a foreign key")
    void rejectUnknownDonor() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordDonation(99L, new BigDecimal("10"), PaymentMethod.CASH, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.recordDonation(null, new BigDecimal("10"), PaymentMethod.CASH, null, null));
    }

    @Test
    @DisplayName("rejects a missing payment method")
    void rejectMissingPaymentMethod() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordDonation(1L, new BigDecimal("10"), null, null, null));
    }

    @Test
    @DisplayName("donor total only grows when a donation is CONFIRMED (not when pending/cancelled)")
    void donorTotalUpdatesOnConfirmOnly() {
        Long pending = service.recordDonation(1L, new BigDecimal("100"), PaymentMethod.CASH, null, null);
        Long cancelled = service.recordDonation(1L, new BigDecimal("40"), PaymentMethod.CASH, null, null);
        assertEquals(0, BigDecimal.ZERO.compareTo(donorDAO.findById(1L).get().getTotalDonated()));

        service.cancelDonation(cancelled);
        assertEquals(0, BigDecimal.ZERO.compareTo(donorDAO.findById(1L).get().getTotalDonated()));

        service.confirmDonation(pending);
        assertEquals(0, new BigDecimal("100").compareTo(donorDAO.findById(1L).get().getTotalDonated()));
    }

    @Test
    @DisplayName("can record and confirm in one step")
    void recordAsConfirmed() {
        Long id = service.recordDonation(1L, new BigDecimal("250"), PaymentMethod.BANK_TRANSFER,
                DonationStatus.CONFIRMED, "Zakat", "REF-1");
        assertEquals(DonationStatus.CONFIRMED, donationDAO.findById(id).get().getStatus());
        assertEquals(0, new BigDecimal("250").compareTo(donorDAO.findById(1L).get().getTotalDonated()));
    }

    @Test
    @DisplayName("a donation cannot be recorded as cancelled, nor cancelled twice")
    void cancelRules() {
        assertThrows(IllegalArgumentException.class, () -> service.recordDonation(
                1L, new BigDecimal("5"), PaymentMethod.CASH, DonationStatus.CANCELLED, null, null));
        Long id = service.recordDonation(1L, new BigDecimal("5"), PaymentMethod.CASH, null, null);
        service.cancelDonation(id);
        assertThrows(IllegalStateException.class, () -> service.cancelDonation(id));
        assertThrows(IllegalStateException.class, () -> service.confirmDonation(id));
    }
}
