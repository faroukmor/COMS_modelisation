package com.charity.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal JDK-proxy based fakes for java.sql.Connection, PreparedStatement and
 * ResultSet. Only methods exercised by DistributionService.disburse are implemented;
 * everything else throws UnsupportedOperationException.
 */
public final class DbFakes {
    private DbFakes() {}

    /** Fake connection whose prepared statements succeed and return a generated key of 1. */
    public static Connection connection() {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "setAutoCommit": return null;
                case "commit": return null;
                case "rollback": return null;
                case "close": return null;
                case "isClosed": return false;
                case "prepareStatement":
                    return preparedStatement();
                default:
                    throw new UnsupportedOperationException("Connection." + name);
            }
        };
        return (Connection) Proxy.newProxyInstance(
                DbFakes.class.getClassLoader(), new Class[]{Connection.class}, handler);
    }

    private static PreparedStatement preparedStatement() {
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "setLong": case "setString": case "setBigDecimal": case "setTimestamp":
                case "setInt": case "clearParameters": return null;
                case "executeUpdate": return 1;
                case "getGeneratedKeys": return resultSet();
                case "close": return null;
                default:
                    throw new UnsupportedOperationException("PreparedStatement." + name);
            }
        };
        return (PreparedStatement) Proxy.newProxyInstance(
                DbFakes.class.getClassLoader(), new Class[]{PreparedStatement.class}, handler);
    }

    private static ResultSet resultSet() {
        final boolean[] called = {false};
        InvocationHandler handler = (proxy, method, args) -> {
            String name = method.getName();
            switch (name) {
                case "next":
                    if (!called[0]) { called[0] = true; return true; }
                    return false;
                case "getLong": return 1L;
                case "close": return null;
                case "wasNull": return false;
                default:
                    throw new UnsupportedOperationException("ResultSet." + name);
            }
        };
        return (ResultSet) Proxy.newProxyInstance(
                DbFakes.class.getClassLoader(), new Class[]{ResultSet.class}, handler);
    }
}
