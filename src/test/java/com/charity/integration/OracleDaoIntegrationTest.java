package com.charity.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.charity.dao.*;
import com.charity.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Runs the real JDBC DAOs against the real schema (H2 in Oracle mode).
 * These tests catch SQL/constraint bugs that the fake-based service tests cannot see.
 */
class OracleDaoIntegrationTest {
    private final OracleUserDAO users = new OracleUserDAO();
    private final OracleDonorDAO donors = new OracleDonorDAO();
    private final OracleBeneficiaryDAO beneficiaries = new OracleBeneficiaryDAO();
    private final OracleDonationDAO donations = new OracleDonationDAO();
    private final OracleAidRequestDAO requests = new OracleAidRequestDAO();
    private final OracleDistributionDAO distributions = new OracleDistributionDAO();

    @BeforeEach
    void freshDatabase() throws Exception {
        H2Database.reset(true);
    }

    // ---------------------------------------------------------------- users
    @Test
    @DisplayName("user: save, find (case-insensitive), exists, update, list")
    void userCrud() {
        User u = new User(null, "newuser", "hash", Role.DONOR, "New User", "n@x.org", "123");
        Long id = users.save(u);
        assertNotNull(id);

        assertTrue(users.exists("newuser"));
        assertTrue(users.exists("NewUser"), "username lookup must ignore case");
        assertFalse(users.exists("nobody"));

        User found = users.findByUsername("NEWUSER").orElseThrow();
        assertEquals("New User", found.getFullName());
        assertEquals(Role.DONOR, found.getRole());

        found.setFullName("Renamed");
        users.update(found);
        assertEquals("Renamed", users.findByUsername("newuser").orElseThrow().getFullName());

        assertEquals(2, users.findAll().size()); // admin + newuser
    }

    // --------------------------------------------------------------- donors
    @Test
    @DisplayName("donor: save, find, update, search, delete")
    void donorCrud() {
        Donor d = new Donor(null, "Layla Test", "layla@test.org", "555", "Tlemcen");
        Long id = donors.save(d);
        assertEquals("Layla Test", donors.findById(id).orElseThrow().getFullName());

        d.setId(id);
        d.setPhone("999");
        donors.update(d);
        assertEquals("999", donors.findById(id).orElseThrow().getPhone());

        assertEquals(1, donors.search("layla").size());
        assertEquals(1, donors.search("TEST.ORG").size());
        assertTrue(donors.search("zzz-nothing").isEmpty());

        donors.delete(id);
        assertTrue(donors.findById(id).isEmpty());
    }

    @Test
    @DisplayName("donor: searching with a null/blank keyword does not blow up")
    void donorSearchNull() {
        assertEquals(3, donors.search(null).size());
        assertEquals(3, donors.search("  ").size());
    }

    // -------------------------------------------------------- beneficiaries
    @Test
    @DisplayName("beneficiary: insert + update individual and association")
    void beneficiaryCrud() {
        Individual ind = new Individual(null, "Yacine Test", "NID-1", "y@test.org", "1", "Oran");
        Long indId = beneficiaries.save(ind);
        ind.setId(indId);
        ind.setFullName("Yacine Renamed");
        beneficiaries.update(ind);
        Individual back = (Individual) beneficiaries.findById(indId).orElseThrow();
        assertEquals("Yacine Renamed", back.getFullName());
        assertEquals("NID-1", back.getNationalId());

        CharityAssociation a = new CharityAssociation(null, "Helping Hands", "REG-9", "Rep", "a@test.org", "2", "Algiers");
        Long aId = beneficiaries.save(a);
        a.setId(aId);
        a.setRepresentativeName("New Rep");
        beneficiaries.update(a);
        CharityAssociation backA = (CharityAssociation) beneficiaries.findById(aId).orElseThrow();
        assertEquals("New Rep", backA.getRepresentativeName());

        assertEquals(3, beneficiaries.findByType(BeneficiaryType.INDIVIDUAL).size());
        assertEquals(3, beneficiaries.findByType(BeneficiaryType.ASSOCIATION).size());
        assertFalse(beneficiaries.search("helping").isEmpty());
    }

    // ------------------------------------------------------------ donations
    @Test
    @DisplayName("donation: save, status change, totals only count CONFIRMED")
    void donationFlow() {
        BigDecimal before = donations.totalConfirmedAmount();
        assertEquals(0, new BigDecimal("100000").compareTo(before));

        Donation d = new Donation();
        d.setDonorId(1L);
        d.setAmount(new BigDecimal("2500.50"));
        d.setPaymentMethod(PaymentMethod.CASH);
        Long id = donations.save(d);

        assertEquals(0, before.compareTo(donations.totalConfirmedAmount()), "pending must not count");
        donations.updateStatus(id, DonationStatus.CONFIRMED);
        assertEquals(0, before.add(new BigDecimal("2500.50")).compareTo(donations.totalConfirmedAmount()));
        assertEquals(DonationStatus.CONFIRMED, donations.findById(id).orElseThrow().getStatus());
        assertFalse(donations.findByDonor(1L).isEmpty());
        assertFalse(donations.findByStatus(DonationStatus.PENDING).isEmpty());
    }

    // ----------------------------------------------------------- aid requests
    @Test
    @DisplayName("aid request: a brand-new request (no reviewer yet) can be inserted")
    void insertRequestWithoutReviewer() {
        AidRequest r = new AidRequest();
        r.setBeneficiaryId(1L);
        r.setBeneficiaryType(BeneficiaryType.INDIVIDUAL);
        r.setRequestedAmount(new BigDecimal("5000"));
        r.setDescription("School supplies");
        Long id = requests.save(r);

        AidRequest back = requests.findById(id).orElseThrow();
        assertNull(back.getReviewedBy(), "reviewed_by must stay NULL until an admin reviews it");
        assertEquals(RequestStatus.PENDING, back.getStatus());
    }

    @Test
    @DisplayName("aid request: review stores the reviewer and approved amount")
    void reviewRequest() {
        AidRequest r = new AidRequest();
        r.setBeneficiaryId(2L);
        r.setBeneficiaryType(BeneficiaryType.INDIVIDUAL);
        r.setRequestedAmount(new BigDecimal("8000"));
        r.setDescription("Rent");
        Long id = requests.save(r);

        AidRequest loaded = requests.findById(id).orElseThrow();
        loaded.setStatus(RequestStatus.APPROVED);
        loaded.setApprovedAmount(new BigDecimal("6000"));
        loaded.setReviewedBy(1L);
        loaded.setReviewedAt(LocalDateTime.now());
        requests.update(loaded);

        AidRequest back = requests.findById(id).orElseThrow();
        assertEquals(1L, back.getReviewedBy());
        assertEquals(0, new BigDecimal("6000").compareTo(back.getApprovedAmount()));
        assertEquals(1, requests.countByStatus(RequestStatus.PENDING) , "only the sample PENDING request remains pending");
    }

    // ---------------------------------------------------------- distributions
    @Test
    @DisplayName("distribution: save and total")
    void distributionTotals() {
        BigDecimal before = distributions.totalAmount();
        Distribution d = new Distribution();
        d.setAidRequestId(2L);
        d.setBeneficiaryId(2L);
        d.setAmount(new BigDecimal("1000"));
        d.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        Long id = distributions.save(d);
        assertNotNull(id);
        assertEquals(0, before.add(new BigDecimal("1000")).compareTo(distributions.totalAmount()));
        assertEquals(1, distributions.findByRequest(1L).size());
        Optional<Distribution> back = distributions.findById(id);
        assertTrue(back.isPresent());
    }

    @Test
    @DisplayName("sample data: findAll methods return the seeded rows")
    void seededRows() {
        assertEquals(3, donors.findAll().size());
        assertEquals(4, beneficiaries.findAll().size());
        assertEquals(4, donations.findAll().size());
        assertEquals(4, requests.findAll().size());
        assertEquals(2, distributions.findAll().size());
        List<AidRequest> rejected = requests.findByStatus(RequestStatus.REJECTED);
        assertEquals(1, rejected.size());
        assertEquals(1L, rejected.get(0).getReviewedBy());
    }
}
