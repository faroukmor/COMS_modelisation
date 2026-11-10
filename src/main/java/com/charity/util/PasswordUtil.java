package com.charity.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Secure password hashing using PBKDF2WithHmacSHA256.
 * <p>
 * Stored format: {@code pbkdf2:sha256:<iterations>$<base64 salt>$<base64 hash>}.
 * The iteration count and hash length are read back from the stored value when verifying, so the
 * work factor can be raised later without invalidating existing accounts.
 */
public final class PasswordUtil {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2:sha256:";
    private static final int ITERATIONS = 655360;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final int MAX_ITERATIONS = 10_000_000; // guards against absurd/malicious stored values
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /**
     * Hashes a plain-text password with a random salt.
     *
     * @return encoded hash in the format {@code pbkdf2:sha256:<iterations>$<salt>$<hash>}
     */
    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        return PREFIX + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a plain-text password against a stored hashed value.
     * Never throws for malformed input: an unreadable stored hash simply does not match.
     *
     * @return {@code true} if the password matches, {@code false} otherwise
     */
    public static boolean verify(String password, String stored) {
        if (password == null || stored == null || !stored.startsWith(PREFIX)) {
            return false;
        }
        try {
            String[] parts = stored.split("\\$");
            if (parts.length != 3) {
                return false;
            }
            int iterations = Integer.parseInt(parts[0].substring(PREFIX.length()));
            if (iterations < 1 || iterations > MAX_ITERATIONS) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            if (salt.length == 0 || expected.length == 0) {
                return false;
            }
            byte[] actual = derive(password, salt, iterations, expected.length * 8);
            return MessageDigest.isEqual(actual, expected);
        } catch (IllegalArgumentException e) {
            // NumberFormatException and invalid Base64 both end up here
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations, int keyBits) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyBits);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("PBKDF2 algorithm not available", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Invalid key specification", e);
        } finally {
            spec.clearPassword();
        }
    }
}
