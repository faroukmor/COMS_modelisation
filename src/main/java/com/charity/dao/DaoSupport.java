package com.charity.dao;

import java.sql.SQLException;

/**
 * Small helpers shared by the JDBC DAO implementations.
 */
final class DaoSupport {
    private DaoSupport() {
    }

    /**
     * Builds the exception for a failed DELETE. A foreign-key violation (Oracle ORA-02292,
     * SQLSTATE class 23) becomes a readable {@link IllegalStateException} instead of a raw SQL error.
     */
    static RuntimeException deleteFailure(String what, Long id, SQLException e) {
        if (isIntegrityViolation(e)) {
            return new IllegalStateException(
                    "This " + what + " cannot be deleted because other records still refer to it.", e);
        }
        return new RuntimeException("Error deleting " + what + " id=" + id, e);
    }

    static boolean isIntegrityViolation(SQLException e) {
        for (SQLException cur = e; cur != null; cur = cur.getNextException()) {
            String state = cur.getSQLState();
            if (cur.getErrorCode() == 2292 || (state != null && state.startsWith("23"))) {
                return true;
            }
        }
        return false;
    }
}
