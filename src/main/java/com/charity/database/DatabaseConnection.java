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
    private DatabaseConnection() {
    }

    /**
     * Opens a new connection to the Oracle database using configured credentials.
     */
    public static Connection getConnection() throws SQLException {
        String url = DatabaseConfig.getUrl();
        if (url.startsWith("jdbc:oracle:")) {
            // JDBC 4 drivers self-register; this only produces a clearer error if ojdbc is missing.
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Oracle JDBC driver (ojdbc) not found on the classpath", e);
            }
        }
        return DriverManager.getConnection(
                url,
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword());
    }
}
