package com.charity.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads database configuration from {@code db.properties}.
 * <p>
 * The {@code db.properties} file is git-ignored and must be created by copying
 * {@code db.properties.example} and filling in the real Oracle credentials.
 * Passwords are never hardcoded in source code.
 */
public final class DatabaseConfig {
    private static final Properties PROPS = new Properties();

    private static final boolean FILE_FOUND;

    static {
        boolean found = false;
        try (InputStream is = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (is != null) {
                PROPS.load(is);
                found = true;
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load database configuration", e);
        }
        FILE_FOUND = found;
    }

    private DatabaseConfig() {
    }

    public static String getUrl() {
        return getRequired("db.url");
    }

    public static String getUsername() {
        return getRequired("db.username");
    }

    public static String getPassword() {
        return getRequired("db.password");
    }

    public static String get(String key, String defaultValue) {
        return lookup(key, defaultValue);
    }

    /**
     * Resolves a property. JVM system properties ({@code -Ddb.url=...}) take precedence over
     * {@code db.properties}, which lets tests and CI point the application at another database.
     */
    private static String lookup(String key, String defaultValue) {
        String override = System.getProperty(key);
        if (override != null) {
            return override;
        }
        return PROPS.getProperty(key, defaultValue);
    }

    private static String getRequired(String key) {
        String value = lookup(key, null);
        if (value == null && !FILE_FOUND) {
            // Do NOT silently fall back to the example file: its values are placeholders and the
            // resulting "connection refused" error would point at the wrong problem.
            throw new IllegalStateException("db.properties was not found. Copy "
                    + "src/main/resources/db.properties.example to src/main/resources/db.properties, "
                    + "enter your Oracle URL, username and password, then rebuild and run again.");
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required database property: " + key);
        }
        return value.trim();
    }
}
