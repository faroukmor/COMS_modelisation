package com.charity.service;

import static org.junit.jupiter.api.Assertions.*;

import com.charity.model.CharityAssociation;
import com.charity.model.Donor;
import com.charity.model.Individual;
import com.charity.service.fakes.FakeBeneficiaryDAO;
import com.charity.service.fakes.FakeDonorDAO;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BeneficiaryAndDonorServiceTest {
    private BeneficiaryService beneficiaries;
    private DonorService donors;

    @BeforeEach
    void setUp() {
        beneficiaries = new BeneficiaryService(new FakeBeneficiaryDAO());
        donors = new DonorService(new FakeDonorDAO());
    }

    @Test
    @DisplayName("individual needs full name, national ID and a valid e-mail")
    void individualValidation() {
        assertThrows(IllegalArgumentException.class, () -> beneficiaries.create(
                new Individual(null, " ", "N1", "a@test.org", null, null)));
        assertThrows(IllegalArgumentException.class, () -> beneficiaries.create(
                new Individual(null, "Name", "", "a@test.org", null, null)));
        assertThrows(IllegalArgumentException.class, () -> beneficiaries.create(
                new Individual(null, "Name", "N1", "", null, null)));
        assertThrows(IllegalArgumentException.class, () -> beneficiaries.create(
                new Individual(null, "Name", "N1", "bad-email", null, null)));
        assertNotNull(beneficiaries.create(new Individual(null, "Name", "N1", "a@test.org", null, null)));
    }

    @Test
    @DisplayName("association needs organization name and registration number")
    void associationValidation() {
        assertThrows(IllegalArgumentException.class, () -> beneficiaries.create(
                new CharityAssociation(null, "", "R1", "Rep", "a@test.org", null, null)));
        assertThrows(IllegalArgumentException.class, () -> beneficiaries.create(
                new CharityAssociation(null, "Org", " ", "Rep", "a@test.org", null, null)));
        assertNotNull(beneficiaries.create(
                new CharityAssociation(null, "Org", "R1", "Rep", "a@test.org", null, null)));
    }

    @Test
    @DisplayName("update validates too, and requires an existing beneficiary")
    void updateRules() {
        Individual ind = new Individual(null, "Name", "N1", "a@test.org", null, null);
        Long id = beneficiaries.create(ind);
        ind.setId(id);
        ind.setFullName(" ");
        assertThrows(IllegalArgumentException.class, () -> beneficiaries.update(ind));
        Individual ghost = new Individual(999L, "Ghost", "N9", "g@test.org", null, null);
        assertThrows(IllegalArgumentException.class, () -> beneficiaries.update(ghost));
        assertThrows(IllegalArgumentException.class, () -> beneficiaries.update(
                new Individual(null, "No Id", "N0", "n@test.org", null, null)));
    }

    @Test
    @DisplayName("donor: name required, e-mail/phone format checked, input trimmed")
    void donorValidation() {
        assertThrows(IllegalArgumentException.class, () -> donors.create(new Donor(null, " ", null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> donors.create(new Donor(null, "N", "bad", null, null)));
        assertThrows(IllegalArgumentException.class, () -> donors.create(new Donor(null, "N", null, "xx", null)));
        Donor d = new Donor(null, "  Spaced Name  ", " a@test.org ", "", "  ");
        Long id = donors.create(d);
        Donor back = donors.findById(id);
        assertEquals("Spaced Name", back.getFullName());
        assertEquals("a@test.org", back.getEmail());
        assertNull(back.getPhone());
        assertNull(back.getAddress());
    }

    @Test
    @DisplayName("editing a donor never overwrites the accumulated total donated")
    void updateKeepsTotal() {
        Donor d = new Donor(null, "Total Keeper", null, null, null);
        d.setTotalDonated(new BigDecimal("5000"));
        Long id = donors.create(d);

        Donor edit = new Donor(id, "Total Keeper Renamed", null, null, null); // form object: total = 0
        donors.update(edit);
        Donor back = donors.findById(id);
        assertEquals("Total Keeper Renamed", back.getFullName());
        assertEquals(0, new BigDecimal("5000").compareTo(back.getTotalDonated()));
    }

    @Test
    @DisplayName("search with null keyword returns everything instead of failing")
    void searchNull() {
        donors.create(new Donor(null, "Someone", null, null, null));
        assertEquals(1, donors.search(null).size());
        assertEquals(1, donors.search("some").size());
    }
}
