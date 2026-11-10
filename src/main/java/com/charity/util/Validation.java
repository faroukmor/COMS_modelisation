package com.charity.util;

import java.util.regex.Pattern;

/**
 * Small input-validation helpers shared by the service layer and the UI.
 */
public final class Validation {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\-\\s]{6,20}$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,30}$");

    private Validation() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Trims; returns {@code null} for null or blank input. */
    public static String clean(String s) {
        return isBlank(s) ? null : s.trim();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME.matcher(username.trim()).matches();
    }

    /** Password policy: at least 8 characters, containing at least one letter and one digit. */
    public static String passwordProblem(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (password.length() > 128) {
            return "Password must be at most 128 characters";
        }
        boolean letter = false;
        boolean digit = false;
        for (char c : password.toCharArray()) {
            letter |= Character.isLetter(c);
            digit |= Character.isDigit(c);
        }
        if (!letter || !digit) {
            return "Password must contain at least one letter and one digit";
        }
        return null;
    }
}
