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

    static {
        try (InputStream is = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (is != null) {
                PROPS.load(is);
            } else {
                // Fall back to the example file for development guidance only.
                try (InputStream ex = DatabaseConfig.class.getClassLoader()
                        .getResourceAsStream("db.properties.example")) {
                    if (ex != null) {
                        PROPS.load(ex);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load database configuration", e);
        }
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
        return PROPS.getProperty(key, defaultValue);
    }

    private static String getRequired(String key) {
        String value = PROPS.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required database property: " + key);
        }
        return value.trim();
    }
}
