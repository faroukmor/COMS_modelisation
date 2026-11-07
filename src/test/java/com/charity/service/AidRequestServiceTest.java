package com.charity.service;

import static org.junit.jupiter.api.Assertions.*;
import com.charity.dao.AidRequestDAO;
import com.charity.dao.BeneficiaryDAO;
import com.charity.model.AidRequest;
import com.charity.model.Beneficiary;
import com.charity.model.BeneficiaryType;
import com.charity.model.RequestStatus;
import com.charity.service.fakes.FakeAidRequestDAO;
import com.charity.service.fakes.FakeBeneficiaryDAO;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class AidRequestServiceTest {
    private AidRequestDAO requestDAO;
    private BeneficiaryDAO beneficiaryDAO;
    private AidRequestService service;

    @BeforeEach
    void setUp() {
        requestDAO = new FakeAidRequestDAO();
        beneficiaryDAO = new FakeBeneficiaryDAO();
        service = new AidRequestService(requestDAO, beneficiaryDAO);
        Beneficiary b = new Beneficiary() {
            @Override public String getDisplayName() { return "Test Beneficiary"; }
        };
        b.setId(1L); b.setType(BeneficiaryType.INDIVIDUAL);
        beneficiaryDAO.save(b);
    }

    @Test
    @DisplayName("submit request with positive amount creates PENDING request")
    void submitValid() {
        Long id = service.submitRequest(1L, new BigDecimal("10000.00"), "Medical aid");
        assertNotNull(id);
        AidRequest r = requestDAO.findById(id).get();
        assertEquals(RequestStatus.PENDING, r.getStatus());
        assertEquals(new BigDecimal("10000.00"), r.getRequestedAmount());
    }

    @Test
    @DisplayName("rejects non-positive requested amount")
    void rejectNonPositiveAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.submitRequest(1L, BigDecimal.ZERO, "x"));
        assertThrows(IllegalArgumentException.class,
                () -> service.submitRequest(1L, new BigDecimal("-10"), "x"));
    }

    @Test
    @DisplayName("rejects empty description")
    void rejectEmptyDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> service.submitRequest(1L, new BigDecimal("10"), ""));
    }

    @Test
    @DisplayName("rejects unknown beneficiary")
    void rejectUnknownBeneficiary() {
        assertThrows(IllegalArgumentException.class,
                () -> service.submitRequest(99L, new BigDecimal("10"), "x"));
    }

    @Test
    @DisplayName("approve request sets approved amount and status")
    void approve() {
        Long id = service.submitRequest(1L, new BigDecimal("10000.00"), "Medical aid");
        service.reviewRequest(id, RequestStatus.APPROVED, 1L, "Approved", new BigDecimal("8000.00"));
        AidRequest r = requestDAO.findById(id).get();
        assertEquals(RequestStatus.APPROVED, r.getStatus());
        assertEquals(new BigDecimal("8000.00"), r.getApprovedAmount());
        assertEquals(BigDecimal.ZERO, r.getDistributedAmount());
    }

    @Test
    @DisplayName("reject request sets approved and distributed to zero")
    void reject() {
        Long id = service.submitRequest(1L, new BigDecimal("10000.00"), "Medical aid");
        service.reviewRequest(id, RequestStatus.REJECTED, 1L, "No funds", BigDecimal.ZERO);
        AidRequest r = requestDAO.findById(id).get();
        assertEquals(RequestStatus.REJECTED, r.getStatus());
        assertEquals(BigDecimal.ZERO, r.getApprovedAmount());
        assertEquals(BigDecimal.ZERO, r.getDistributedAmount());
    }

    @Test
    @DisplayName("approving with amount exceeding requested throws")
    void approveExceedsRequested() {
        Long id = service.submitRequest(1L, new BigDecimal("10000.00"), "Medical aid");
        assertThrows(IllegalArgumentException.class,
                () -> service.reviewRequest(id, RequestStatus.APPROVED, 1L, "x", new BigDecimal("20000.00")));
    }

    @Test
    @DisplayName("approving with non-positive approved amount throws")
    void approveNonPositiveAmount() {
        Long id = service.submitRequest(1L, new BigDecimal("10000.00"), "Medical aid");
        assertThrows(IllegalArgumentException.class,
                () -> service.reviewRequest(id, RequestStatus.APPROVED, 1L, "x", BigDecimal.ZERO));
    }

    @Test
    @DisplayName("reviewing already approved request throws")
    void reviewAlreadyDecided() {
        Long id = service.submitRequest(1L, new BigDecimal("10000.00"), "Medical aid");
        service.reviewRequest(id, RequestStatus.APPROVED, 1L, "x", new BigDecimal("5000.00"));
        assertThrows(IllegalStateException.class,
                () -> service.reviewRequest(id, RequestStatus.REJECTED, 1L, "x", BigDecimal.ZERO));
    }
}
