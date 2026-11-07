package com.charity.util;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Secure password hashing using PBKDF2WithHmacSHA256.
 * <p>
 * Produces an encoded string of the form:
 * {@code pbkdf2:sha256:<iterations>$<salt>$<hash>}
 * which can be stored directly in the database and verified later.
 */
public final class PasswordUtil {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 250_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * Hashes a plain-text password with a random salt.
     */
    public static String hash(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        return "pbkdf2:sha256:" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a plain-text password against a stored hashed value.
     *
     * @return {@code true} if the password matches, {@code false} otherwise
     */
    public static boolean verify(String password, String stored) {
        if (password == null || stored == null || !stored.startsWith("pbkdf2:sha256:")) {
            return false;
        }
        try {
            String[] parts = stored.split("\\$");
            if (parts.length != 3) {
                return false;
            }
            int iterations = Integer.parseInt(parts[0].substring("pbkdf2:sha256:".length()));
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
            byte[] actualHash = deriveKey(password.toCharArray(), salt, iterations, expectedHash.length);
            return constantTimeEquals(actualHash, expectedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] deriveKey(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("PBKDF2 key derivation failed", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
