package com.charity.service;

import static org.junit.jupiter.api.Assertions.*;

import com.charity.util.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidationTest {

    @Test
    @DisplayName("e-mail validation")
    void email() {
        assertTrue(Validation.isValidEmail("user@example.com"));
        assertTrue(Validation.isValidEmail(" first.last@sub.example.dz "));
        assertFalse(Validation.isValidEmail("no-at-sign"));
        assertFalse(Validation.isValidEmail("a@b"));
        assertFalse(Validation.isValidEmail("a b@c.com"));
        assertFalse(Validation.isValidEmail(null));
    }

    @Test
    @DisplayName("username validation")
    void username() {
        assertTrue(Validation.isValidUsername("farouk"));
        assertTrue(Validation.isValidUsername("a.b-c_9"));
        assertFalse(Validation.isValidUsername("ab"));
        assertFalse(Validation.isValidUsername("has space"));
        assertFalse(Validation.isValidUsername("x".repeat(31)));
        assertFalse(Validation.isValidUsername("semi;colon"));
        assertFalse(Validation.isValidUsername(null));
    }

    @Test
    @DisplayName("phone validation")
    void phone() {
        assertTrue(Validation.isValidPhone("+213 550 123 456"));
        assertTrue(Validation.isValidPhone("(021) 12-34-56"));
        assertFalse(Validation.isValidPhone("12"));
        assertFalse(Validation.isValidPhone("call me maybe"));
    }

    @Test
    @DisplayName("password policy: 8+ characters with a letter and a digit")
    void password() {
        assertNull(Validation.passwordProblem("Admin123"));
        assertNotNull(Validation.passwordProblem("short1"));
        assertNotNull(Validation.passwordProblem("onlyletters"));
        assertNotNull(Validation.passwordProblem("12345678"));
        assertNotNull(Validation.passwordProblem(""));
        assertNotNull(Validation.passwordProblem(null));
        assertNotNull(Validation.passwordProblem("a1".repeat(70)));
    }

    @Test
    @DisplayName("clean() trims and turns blank into null")
    void clean() {
        assertEquals("x", Validation.clean("  x "));
        assertNull(Validation.clean("   "));
        assertNull(Validation.clean(null));
    }
}
