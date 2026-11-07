package com.charity.util;

import java.security.MessageDigest;
import java.security.spec.InvalidKeySpecException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Secure password hashing using PBKDF2WithHmacSHA256.
 * Uses the standard Java JCA pattern for PBKDF2 password hashing.
 */
public final class PasswordUtil {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 655360;
    private static final int KEY_LENGTH = 256;

    private PasswordUtil() {
    }

    /**
     * Hashes a plain-text password with a random salt.
     * @return Encoded hash string in the format: pbkdf2:sha256:<iterations>$<salt>$<hash>
     */
    public static String hash(String password) {
        try {
            byte[] salt = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(salt);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            byte[] hash = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
            return "pbkdf2:sha256:" + ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("PBKDF2 algorithm not available", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid key specification", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored hashed value.
     * @return {@code true} if the password matches, {@code false} otherwise
     */
    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        try {
            String[] parts = stored.split("\\$");
            if (parts.length != 3) {
                return false;
            }
            int iterations = Integer.parseInt(parts[0].substring("pbkdf2:sha256:".length()));
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hashBytes = Base64.getDecoder().decode(parts[2]);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            byte[] derivedKey = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
            return MessageDigest.isEqual(derivedKey, hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("PBKDF2 algorithm not available", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid key specification", e);
        }
    }
}
