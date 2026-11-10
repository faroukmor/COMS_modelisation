package com.charity.integration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Test helper: runs the project's real {@code schema.sql} and {@code sample_data.sql} on an in-memory
 * H2 database running in Oracle compatibility mode, and points the application's
 * {@link com.charity.database.DatabaseConfig} at it via system properties.
 * <p>
 * This lets the real {@code Oracle*DAO} classes and the SQL scripts be exercised by the test suite
 * without an Oracle server.
 */
public final class H2Database {
    public static final String URL =
            "jdbc:h2:mem:charity;MODE=Oracle;DATABASE_TO_LOWER=FALSE;DB_CLOSE_DELAY=-1";
    public static final String USER = "sa";
    public static final String PASSWORD = "test";

    private static final Path RESOURCES = Paths.get("src", "main", "resources", "database");

    private H2Database() {
    }

    /** Points the application at the in-memory database and (re)creates schema + sample data. */
    public static void reset(boolean withSampleData) throws Exception {
        System.setProperty("db.url", URL);
        System.setProperty("db.username", USER);
        System.setProperty("db.password", PASSWORD);
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement st = con.createStatement()) {
            st.execute("DROP ALL OBJECTS");
            runScript(con, RESOURCES.resolve("schema.sql"));
            if (withSampleData) {
                runScript(con, RESOURCES.resolve("sample_data.sql"));
            }
        }
    }

    public static Connection connection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Executes a SQL script the way SQL*Plus would: statements end with {@code ;}, PL/SQL blocks
     * (triggers) end with a line containing only {@code /}, {@code --} comments are ignored and
     * COMMIT is skipped.
     * <p>
     * H2 cannot run PL/SQL triggers, so each "assign the key from a sequence" trigger is emulated by
     * giving that table's id column the sequence as its default value. The trigger source itself is
     * still validated by {@code SchemaScriptsTest}.
     */
    public static void runScript(Connection con, Path file) throws IOException, SQLException {
        java.util.regex.Pattern onTable = java.util.regex.Pattern.compile("(?is)BEFORE\\s+INSERT\\s+ON\\s+(\\w+)");
        java.util.regex.Pattern seq = java.util.regex.Pattern.compile("(?is):NEW\\.id\\s*:=\\s*(\\w+)\\.NEXTVAL");
        List<String> after = new ArrayList<>();
        for (String sql : splitStatements(Files.readString(file, StandardCharsets.UTF_8))) {
            if (sql.toUpperCase().startsWith("CREATE OR REPLACE TRIGGER")) {
                var t = onTable.matcher(sql);
                var q = seq.matcher(sql);
                if (!t.find() || !q.find()) {
                    throw new SQLException("Unrecognised trigger in " + file.getFileName() + ":\n" + sql);
                }
                after.add("ALTER TABLE " + t.group(1) + " ALTER COLUMN id SET DEFAULT NEXT VALUE FOR " + q.group(1));
                continue;
            }
            execute(con, file, sql);
        }
        for (String sql : after) {
            execute(con, file, sql);
        }
    }

    private static void execute(Connection con, Path file, String sql) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new SQLException("Failed statement in " + file.getFileName() + ":\n" + sql
                    + "\n-> " + e.getMessage(), e);
        }
    }

    static List<String> splitStatements(String script) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean plsql = false;
        for (String line : script.split("\\R")) {
            String t = line.trim();
            if (t.startsWith("--")) {
                continue;
            }
            if (cur.length() == 0) {
                if (t.isEmpty()) {
                    continue;
                }
                String up = t.toUpperCase();
                plsql = up.startsWith("CREATE OR REPLACE TRIGGER") || up.startsWith("BEGIN") || up.startsWith("DECLARE");
            }
            if (plsql) {
                if (t.equals("/")) {
                    out.add(cur.toString().trim());
                    cur.setLength(0);
                } else {
                    cur.append(line).append('\n');
                }
            } else {
                cur.append(line).append('\n');
                if (t.endsWith(";")) {
                    String sql = cur.toString().trim();
                    sql = sql.substring(0, sql.length() - 1).trim();
                    if (!sql.equalsIgnoreCase("COMMIT")) {
                        out.add(sql);
                    }
                    cur.setLength(0);
                }
            }
        }
        if (cur.toString().trim().length() > 0) {
            throw new IllegalStateException("Script ends in the middle of a statement:\n" + cur);
        }
        return out;
    }
}
