package com.charity.service;

import static org.junit.jupiter.api.Assertions.*;

import com.charity.model.Beneficiary;
import com.charity.model.CharityAssociation;
import com.charity.model.Donor;
import com.charity.model.Individual;
import com.charity.model.Role;
import com.charity.model.User;
import com.charity.service.UserService.NewUser;
import com.charity.service.fakes.FakeBeneficiaryDAO;
import com.charity.service.fakes.FakeDonorDAO;
import com.charity.service.fakes.FakeUserDAO;
import com.charity.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserServiceTest {
    private FakeUserDAO userDAO;
    private FakeDonorDAO donorDAO;
    private FakeBeneficiaryDAO beneficiaryDAO;
    private UserService service;
    private AuthenticationService auth;

    @BeforeEach
    void setUp() {
        userDAO = new FakeUserDAO();
        donorDAO = new FakeDonorDAO();
        beneficiaryDAO = new FakeBeneficiaryDAO();
        service = new UserService(userDAO, donorDAO, beneficiaryDAO);
        auth = new AuthenticationService(userDAO);
    }

    private NewUser donor(String username, String email) {
        return new NewUser(username, "Passw0rdX", "Amine Donor", email, "0555 12 34 56",
                Role.DONOR, null, null, null, "Tlemcen");
    }

    @Test
    @DisplayName("sign-up creates a user that can immediately log in")
    void registerThenLogin() {
        User u = service.register(donor("Amine", "amine@test.org"));
        assertNotNull(u.getId());
        assertEquals("amine", u.getUsername(), "usernames are stored lower-case");
        assertTrue(PasswordUtil.verify("Passw0rdX", u.getPasswordHash()));
        assertNotEquals("Passw0rdX", u.getPasswordHash(), "the password must be hashed, never stored");

        assertNotNull(auth.authenticate("AMINE", "Passw0rdX"), "login is case-insensitive on username");
        assertNotNull(auth.authenticate("  amine  ", "Passw0rdX"), "surrounding spaces are ignored");
        assertNull(auth.authenticate("amine", "wrongpass1"));
    }

    @Test
    @DisplayName("a donor sign-up also creates the donor profile, linked by e-mail")
    void donorProfileCreated() {
        User u = service.register(donor("amine", "amine@test.org"));
        Donor profile = service.findDonorProfile(u).orElseThrow();
        assertEquals("Amine Donor", profile.getFullName());
        assertEquals("Tlemcen", profile.getAddress());
    }

    @Test
    @DisplayName("a beneficiary sign-up creates an Individual profile and needs a national ID")
    void beneficiaryProfile() {
        NewUser data = new NewUser("sara", "Passw0rdX", "Sara B", "sara@test.org", null,
                Role.BENEFICIARY, "NID-77", null, null, null);
        User u = service.register(data);
        Beneficiary b = service.findBeneficiaryProfile(u).orElseThrow();
        assertInstanceOf(Individual.class, b);
        assertEquals("NID-77", ((Individual) b).getNationalId());

        NewUser noId = new NewUser("sara2", "Passw0rdX", "Sara C", "sara2@test.org", null,
                Role.BENEFICIARY, " ", null, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.register(noId));
        assertTrue(userDAO.findByUsername("sara2").isEmpty(), "no half-created account");
    }

    @Test
    @DisplayName("an association sign-up creates a CharityAssociation profile and needs a registration number")
    void associationProfile() {
        NewUser data = new NewUser("helpers", "Passw0rdX", "Helping Hands", "hh@test.org", null,
                Role.ASSOCIATION, null, "REG-1", "Mr Rep", null);
        User u = service.register(data);
        CharityAssociation a = (CharityAssociation) service.findBeneficiaryProfile(u).orElseThrow();
        assertEquals("Helping Hands", a.getOrganizationName());
        assertEquals("Mr Rep", a.getRepresentativeName());

        NewUser noReg = new NewUser("helpers2", "Passw0rdX", "Other", "o@test.org", null,
                Role.ASSOCIATION, null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.register(noReg));
    }

    @Test
    @DisplayName("public sign-up can never create an administrator")
    void noSelfAdmin() {
        NewUser data = new NewUser("evil", "Passw0rdX", "Evil", "evil@test.org", null,
                Role.ADMIN, null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.register(data));
        assertThrows(IllegalArgumentException.class, () -> service.register(
                new NewUser("evil", "Passw0rdX", "Evil", "evil@test.org", null, null, null, null, null, null)));
        assertTrue(userDAO.findByUsername("evil").isEmpty());
    }

    @Test
    @DisplayName("an administrator can create another administrator; others cannot")
    void adminCreatesAdmin() {
        User admin = new User();
        admin.setRole(Role.ADMIN);
        NewUser data = new NewUser("boss2", "Passw0rdX", "Second Admin", null, null,
                Role.ADMIN, null, null, null, null);
        User created = service.createByAdmin(admin, data);
        assertEquals(Role.ADMIN, created.getRole());
        assertTrue(service.findDonorProfile(created).isEmpty(), "admins have no donor profile");

        User donor = new User();
        donor.setRole(Role.DONOR);
        assertThrows(SecurityException.class, () -> service.createByAdmin(donor, data));
        assertThrows(SecurityException.class, () -> service.createByAdmin(null, data));
    }

    @Test
    @DisplayName("duplicate username (any letter case) is rejected")
    void duplicateUsername() {
        service.register(donor("amine", "a1@test.org"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register(donor("AMINE", "a2@test.org")));
        assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    @Test
    @DisplayName("an e-mail that already belongs to another account or profile is rejected")
    void duplicateEmail() {
        service.register(donor("amine", "same@test.org"));
        assertThrows(IllegalArgumentException.class,
                () -> service.register(donor("other", "SAME@test.org")));

        // an existing profile created by an admin must not be claimable by a stranger
        donorDAO.save(new Donor(null, "Existing Donor", "existing@test.org", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.register(donor("stranger", "existing@test.org")));
        assertTrue(userDAO.findByUsername("stranger").isEmpty());
    }

    @Test
    @DisplayName("field validation: username, password, name, e-mail, phone")
    void fieldValidation() {
        assertThrows(IllegalArgumentException.class, () -> service.register(donor("ab", "a@test.org")));
        assertThrows(IllegalArgumentException.class, () -> service.register(donor("bad name", "a@test.org")));
        assertThrows(IllegalArgumentException.class, () -> service.register(donor("okname", "not-an-email")));
        assertThrows(IllegalArgumentException.class, () -> service.register(donor("okname", null)));
        assertThrows(IllegalArgumentException.class, () -> service.register(
                new NewUser("okname", "short1", "Name", "a@test.org", null, Role.DONOR, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.register(
                new NewUser("okname", "Passw0rdX", " ", "a@test.org", null, Role.DONOR, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.register(
                new NewUser("okname", "Passw0rdX", "Name", "a@test.org", "abc", Role.DONOR, null, null, null, null)));
        assertTrue(userDAO.all().isEmpty());
    }

    @Test
    @DisplayName("profile lookup without an e-mail returns nothing")
    void profileLookupWithoutEmail() {
        assertTrue(service.findDonorProfile(null).isEmpty());
        assertTrue(service.findDonorProfile(new User()).isEmpty());
        assertTrue(service.findBeneficiaryProfile(new User()).isEmpty());
    }
}
