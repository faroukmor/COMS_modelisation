package com.charity.service;

import static org.junit.jupiter.api.Assertions.*;
import com.charity.dao.UserDAO;
import com.charity.model.Role;
import com.charity.model.User;
import com.charity.service.fakes.FakeUserDAO;
import com.charity.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class AuthenticationServiceTest {
    private UserDAO userDAO;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        userDAO = new FakeUserDAO();
        service = new AuthenticationService(userDAO);
        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(PasswordUtil.hash("Admin123"));
        admin.setRole(Role.ADMIN);
        admin.setFullName("Admin User");
        userDAO.save(admin);
    }

    @Test
    @DisplayName("authenticates valid admin credentials")
    void validAdmin() {
        User u = service.authenticate("admin", "Admin123");
        assertNotNull(u);
        assertEquals(Role.ADMIN, u.getRole());
    }

    @Test
    @DisplayName("rejects wrong password")
    void wrongPassword() {
        assertNull(service.authenticate("admin", "WrongPass"));
    }

    @Test
    @DisplayName("rejects unknown user")
    void unknownUser() {
        assertNull(service.authenticate("ghost", "x"));
    }

    @Test
    @DisplayName("requireAdmin passes for admin and throws otherwise")
    void requireAdmin() {
        User admin = service.authenticate("admin", "Admin123");
        assertDoesNotThrow(() -> service.requireAdmin(admin));

        User donor = new User();
        donor.setRole(Role.DONOR);
        assertThrows(SecurityException.class, () -> service.requireAdmin(donor));
        assertThrows(SecurityException.class, () -> service.requireAdmin(null));
    }

    @Test
    @DisplayName("isAdmin/isDonor/isBeneficiary role checks")
    void roleChecks() {
        User admin = service.authenticate("admin", "Admin123");
        assertTrue(service.isAdmin(admin));
        assertFalse(service.isDonor(admin));
        assertFalse(service.isBeneficiary(admin));
    }
}
