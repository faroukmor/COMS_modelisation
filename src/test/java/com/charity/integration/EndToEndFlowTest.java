package com.charity.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.charity.controller.AppController;
import com.charity.dao.*;
import com.charity.model.*;
import com.charity.service.DistributionService;
import com.charity.service.UserService.NewUser;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Walks through the real application flows with real DAOs, real SQL and a real transaction.
 */
class EndToEndFlowTest {
    private AppController app;

    @BeforeEach
    void setUp() throws Exception {
        H2Database.reset(true);
        app = new AppController();
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, bd(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    @Test
    @DisplayName("sign up -> log in as the new user -> profile is linked")
    void signUpAndLogin() {
        NewUser data = new NewUser("New.Donor", "Passw0rdX", "New Donor", "new.donor@test.org",
                "+213 555 00 11 22", Role.DONOR, null, null, null, "Tlemcen");
        User created = app.userService.register(data);
        assertNotNull(created.getId());

        User loggedIn = app.authenticate("new.donor", "Passw0rdX");
        assertNotNull(loggedIn);
        assertEquals(Role.DONOR, loggedIn.getRole());
        assertEquals(created.getId(), loggedIn.getId());
        assertNull(app.authenticate("new.donor", "Passw0rdY"));

        Donor profile = app.userService.findDonorProfile(loggedIn).orElseThrow();
        assertEquals("New Donor", profile.getFullName());
        assertEquals(4, app.donorService.findAll().size());
    }

    @Test
    @DisplayName("sign-up refuses duplicates against the real unique constraints")
    void signUpDuplicates() {
        NewUser data = new NewUser("dup", "Passw0rdX", "Dup", "dup@test.org", null,
                Role.DONOR, null, null, null, null);
        app.userService.register(data);
        assertThrows(IllegalArgumentException.class, () -> app.userService.register(data));
        // a sample donor's e-mail cannot be claimed by a stranger
        assertThrows(IllegalArgumentException.class, () -> app.userService.register(new NewUser(
                "stranger", "Passw0rdX", "Stranger", "ahmed.benali@email.com", null,
                Role.DONOR, null, null, null, null)));
        assertTrue(app.authService.authenticate("stranger", "Passw0rdX") == null);
    }

    @Test
    @DisplayName("admin adds a second admin, who can log in")
    void adminAddsAdmin() {
        User admin = app.authenticate("admin", "Admin123");
        assertNotNull(admin);
        app.userService.createByAdmin(admin, new NewUser("deputy", "Deputy2026", "Deputy Admin", null, null,
                Role.ADMIN, null, null, null, null));
        User deputy = app.authenticate("deputy", "Deputy2026");
        assertNotNull(deputy);
        assertTrue(app.authService.isAdmin(deputy));
        assertEquals(2, app.userService.findAll().size());
    }

    @Test
    @DisplayName("full money flow: donate -> confirm -> request -> approve -> disburse -> fulfilled")
    void fullFinancialFlow() {
        User admin = app.authenticate("admin", "Admin123");
        // sample data: confirmed 100000, distributed 25000 => balance 75000
        assertMoney("75000", app.financialService.getAvailableBalance());

        Long donationId = app.donationService.recordDonation(1L, bd("5000"), PaymentMethod.CASH, "Test", "T-1");
        assertMoney("75000", app.financialService.getAvailableBalance()); // pending does not count
        app.donationService.confirmDonation(donationId);
        assertMoney("80000", app.financialService.getAvailableBalance());
        assertMoney("55000", app.donorService.findById(1L).getTotalDonated()); // 50000 sample + 5000

        Long requestId = app.requestService.submitRequest(1L, bd("12000"), "Winter clothes");
        AidRequest submitted = app.getRequestDAO().findById(requestId).orElseThrow();
        assertEquals(RequestStatus.PENDING, submitted.getStatus());
        assertNull(submitted.getReviewedBy());

        app.requestService.reviewRequest(requestId, RequestStatus.APPROVED, admin.getId(), "ok", bd("10000"));
        AidRequest approved = app.getRequestDAO().findById(requestId).orElseThrow();
        assertEquals(admin.getId(), approved.getReviewedBy());

        app.distributionService.disburse(requestId, bd("4000"), PaymentMethod.BANK_TRANSFER, "D-1", "part 1");
        AidRequest partial = app.getRequestDAO().findById(requestId).orElseThrow();
        assertEquals(RequestStatus.APPROVED, partial.getStatus());
        assertMoney("4000", partial.getDistributedAmount());
        assertMoney("76000", app.financialService.getAvailableBalance());

        app.distributionService.disburse(requestId, bd("6000"), PaymentMethod.CASH, "D-2", "part 2");
        AidRequest done = app.getRequestDAO().findById(requestId).orElseThrow();
        assertEquals(RequestStatus.FULFILLED, done.getStatus());
        assertMoney("70000", app.financialService.getAvailableBalance());

        assertThrows(IllegalStateException.class,
                () -> app.distributionService.disburse(requestId, bd("1"), PaymentMethod.CASH, null, null));
    }

    @Test
    @DisplayName("business rules hold on the real database: no over-spending, no distributing unapproved aid")
    void rulesOnRealDatabase() {
        // sample request #2: approved 20000, distributed 10000 => 10000 remaining
        assertThrows(IllegalArgumentException.class,
                () -> app.distributionService.disburse(2L, bd("10001"), PaymentMethod.CASH, null, null));
        // sample request #3 is still PENDING
        assertThrows(IllegalStateException.class,
                () -> app.distributionService.disburse(3L, bd("100"), PaymentMethod.CASH, null, null));
        // sample request #4 was REJECTED
        assertThrows(IllegalStateException.class,
                () -> app.distributionService.disburse(4L, bd("100"), PaymentMethod.CASH, null, null));
    }

    @Test
    @DisplayName("an empty fund (no donations at all) refuses to disburse and does not crash on NULL sums")
    void emptyFund() throws Exception {
        try (Connection con = H2Database.connection(); Statement st = con.createStatement()) {
            st.execute("DELETE FROM distributions");
            st.execute("DELETE FROM donations");
        }
        assertMoney("0", app.financialService.getAvailableBalance());
        assertThrows(IllegalArgumentException.class,
                () -> app.distributionService.disburse(2L, bd("100"), PaymentMethod.CASH, null, null));
    }

    @Test
    @DisplayName("a failure in the middle of a disbursement rolls everything back")
    void disbursementRollsBack() throws Exception {
        AidRequestDAO failingRequests = new OracleAidRequestDAO() {
            @Override
            public void update(AidRequest request, Connection con) {
                throw new RuntimeException("simulated failure while updating the request");
            }
        };
        Supplier<Connection> supplier = () -> {
            try {
                return H2Database.connection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        DistributionService service = new DistributionService(
                failingRequests, new OracleDistributionDAO(), new OracleDonationDAO(), supplier) {
        };
        assertThrows(RuntimeException.class,
                () -> service.disburse(2L, bd("500"), PaymentMethod.CASH, "X", "should not persist"));
        try (Connection con = H2Database.connection(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM distributions")) {
            rs.next();
            assertEquals(2, rs.getInt(1), "the distribution row must not survive a failed transaction");
        }
        assertMoney("10000", app.getRequestDAO().findById(2L).orElseThrow().getDistributedAmount());
    }

    @Test
    @DisplayName("deleting a donor that still has donations gives a readable error, not a SQL dump")
    void deleteDonorWithDonations() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> app.donorService.delete(1L));
        assertTrue(ex.getMessage().contains("cannot be deleted"), ex.getMessage());
        assertTrue(app.donorService.findAll().stream().anyMatch(d -> d.getId() == 1L));
    }
}
