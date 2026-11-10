package com.charity.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.charity.model.Role;
import com.charity.model.User;
import com.charity.dao.OracleUserDAO;
import com.charity.service.AuthenticationService;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the SQL scripts that ship with the project.
 */
class SchemaScriptsTest {

    @Test
    @DisplayName("schema.sql and sample_data.sql execute without errors")
    void scriptsRun() {
        assertDoesNotThrow(() -> H2Database.reset(true));
    }

    @Test
    @DisplayName("sample data creates admin, donors, beneficiaries, donations, requests, distributions")
    void sampleDataCounts() throws Exception {
        H2Database.reset(true);
        try (Connection con = H2Database.connection(); Statement st = con.createStatement()) {
            assertEquals(1, count(st, "users"));
            assertEquals(3, count(st, "donors"));
            assertEquals(4, count(st, "beneficiaries"));
            assertEquals(4, count(st, "donations"));
            assertEquals(4, count(st, "aid_requests"));
            assertEquals(2, count(st, "distributions"));
        }
    }

    @Test
    @DisplayName("the documented admin account (admin / Admin123) can really log in")
    void documentedAdminCanLogin() throws Exception {
        H2Database.reset(true);
        AuthenticationService auth = new AuthenticationService(new OracleUserDAO());
        User admin = auth.authenticate("admin", "Admin123");
        assertNotNull(admin, "admin/Admin123 from sample_data.sql must authenticate");
        assertEquals(Role.ADMIN, admin.getRole());
        assertNull(auth.authenticate("admin", "wrong-password"));
    }

    @Test
    @DisplayName("schema.sql uses Oracle-valid column syntax (DEFAULT must precede NOT NULL)")
    void oracleColumnClauseOrder() throws Exception {
        String schema = Files.readString(Paths.get("src/main/resources/database/schema.sql"));
        assertFalse(schema.matches("(?is).*NOT\\s+NULL\\s+DEFAULT.*"),
                "Oracle requires 'DEFAULT <expr> NOT NULL', not 'NOT NULL DEFAULT <expr>'");
    }

    @Test
    @DisplayName("schema.sql works on Oracle 11g: no IDENTITY columns, every table has a sequence and a trigger")
    void oracle11gCompatible() throws Exception {
        String schema = Files.readString(Paths.get("src/main/resources/database/schema.sql"));
        assertFalse(schema.matches("(?is).*GENERATED\\s+(BY\\s+DEFAULT|ALWAYS).*"),
                "IDENTITY columns do not exist before Oracle 12c");
        for (String table : new String[]{"users", "donors", "beneficiaries", "donations", "aid_requests", "distributions"}) {
            assertTrue(schema.contains("CREATE SEQUENCE seq_" + table), "missing sequence for " + table);
            assertTrue(schema.contains("CREATE OR REPLACE TRIGGER trg_" + table + "_bi"), "missing trigger for " + table);
        }
        assertEquals(6, schema.lines().filter(l -> l.trim().equals("/")).count(), "each trigger must end with a '/' line");
    }

    @Test
    @DisplayName("generated ids are sequential and start at 1 (admin is user #1, which sample data relies on)")
    void idsComeFromSequences() throws Exception {
        H2Database.reset(true);
        try (Connection con = H2Database.connection(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM users WHERE username = 'admin'")) {
            assertTrue(rs.next());
            assertEquals(1L, rs.getLong(1));
        }
    }

    private static int count(Statement st, String table) throws Exception {
        try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
