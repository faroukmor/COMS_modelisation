package com.charity.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages Oracle JDBC database connections.
 * <p>
 * Each call to {@link #getConnection()} opens a new physical connection.
 * Callers are responsible for closing the connection (use try-with-resources).
 */
public final class DatabaseConnection {
    static {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Oracle JDBC driver not found on classpath", e);
        }
    }

    private DatabaseConnection() {
    }

    /**
     * Opens a new connection to the Oracle database using configured credentials.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword());
    }
}
