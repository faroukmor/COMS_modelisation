package com.charity.service;

import static org.junit.jupiter.api.Assertions.*;
import com.charity.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Tests for PBKDF2 password hashing and verification.
 */
class PasswordUtilTest {

    @Test
    @DisplayName("verify returns true for correct password")
    void verifyCorrectPassword() {
        String hashed = PasswordUtil.hash("Secret123");
        assertTrue(PasswordUtil.verify("Secret123", hashed));
    }

    @Test
    @DisplayName("verify returns false for wrong password")
    void verifyWrongPassword() {
        String hashed = PasswordUtil.hash("Secret123");
        assertFalse(PasswordUtil.verify("WrongPass", hashed));
    }

    @Test
    @DisplayName("same password produces different hashes (random salt)")
    void randomSalt() {
        String h1 = PasswordUtil.hash("SamePass");
        String h2 = PasswordUtil.hash("SamePass");
        assertNotEquals(h1, h2);
        assertTrue(PasswordUtil.verify("SamePass", h1));
        assertTrue(PasswordUtil.verify("SamePass", h2));
    }

    @Test
    @DisplayName("verify handles null inputs safely")
    void nullInputsSafe() {
        assertFalse(PasswordUtil.verify(null, null));
        assertFalse(PasswordUtil.verify("pass", null));
        assertFalse(PasswordUtil.verify(null, "pbkdf2:sha256:100$salt$hash"));
    }

    @Test
    @DisplayName("verify returns false for malformed stored hash")
    void malformedHash() {
        assertFalse(PasswordUtil.verify("pass", "not-a-hash"));
    }

    @Test
    @DisplayName("verify honours the iteration count stored in the hash (not a hard-coded one)")
    void honoursStoredIterations() throws Exception {
        // hash produced elsewhere with only 1000 iterations must still verify
        byte[] salt = "0123456789abcdef".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec("Legacy123".toCharArray(), salt, 1000, 256);
        byte[] dk = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        String stored = "pbkdf2:sha256:1000$" + java.util.Base64.getEncoder().encodeToString(salt)
                + "$" + java.util.Base64.getEncoder().encodeToString(dk);
        assertTrue(PasswordUtil.verify("Legacy123", stored));
        assertFalse(PasswordUtil.verify("Legacy124", stored));
    }

    @Test
    @DisplayName("verify never throws on garbage (bad number, bad base64, wrong prefix, absurd iterations)")
    void garbageNeverThrows() {
        assertFalse(PasswordUtil.verify("x", "pbkdf2:sha256:abc$AAAA$AAAA"));
        assertFalse(PasswordUtil.verify("x", "pbkdf2:sha256:1000$!!!$???"));
        assertFalse(PasswordUtil.verify("x", "md5:1000$AAAA$AAAA"));
        assertFalse(PasswordUtil.verify("x", "pbkdf2:sha256:2000000000$AAAA$AAAA"));
        assertFalse(PasswordUtil.verify("x", "pbkdf2:sha256:0$AAAA$AAAA"));
        assertFalse(PasswordUtil.verify("x", ""));
    }

    @Test
    @DisplayName("hash rejects empty passwords")
    void hashRejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(null));
    }
}
