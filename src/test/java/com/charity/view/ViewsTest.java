package com.charity.view;

import static com.charity.view.UiTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import com.charity.controller.AppController;
import com.charity.integration.H2Database;
import com.charity.model.AidRequest;
import com.charity.model.RequestStatus;
import com.charity.model.Role;
import com.charity.model.User;
import com.charity.service.UserService.NewUser;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Drives the real screens (no window needed) against the real DAOs on H2: the data really loads,
 * the buttons really work, and every page can be painted.
 */
class ViewsTest {
    private AppController app;
    private User admin;

    @BeforeEach
    void setUp() throws Exception {
        H2Database.reset(true);
        app = new AppController();
        admin = app.authenticate("admin", "Admin123");
        app.setCurrentUser(admin);
    }

    private static JTable firstTable(JComponent view) {
        return all(view, JTable.class).get(0);
    }

    private static void paint(JComponent view) throws Exception {
        onEdt(() -> {
            view.setSize(new Dimension(1100, 700));
            view.doLayout();
            layoutDeep(view);
            BufferedImage img = new BufferedImage(1100, 700, BufferedImage.TYPE_INT_ARGB);
            view.paint(img.createGraphics());
        });
    }

    private static void layoutDeep(java.awt.Container c) {
        c.doLayout();
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof java.awt.Container cc) {
                layoutDeep(cc);
            }
        }
    }

    @Test
    @DisplayName("every administrator page builds, loads its rows and paints without errors")
    void adminPagesLoad() throws Exception {
        Object[][] expectations = {
                {"dashboard", 4},      // recent donations (4 rows in sample data)
                {"donors", 3}, {"donations", 4}, {"beneficiaries", 4},
                {"aidrequests", 4}, {"distributions", 2}, {"users", 1}};
        for (Object[] e : expectations) {
            JComponent[] holder = new JComponent[1];
            onEdt(() -> holder[0] = switch ((String) e[0]) {
                case "dashboard" -> new DashboardView(app);
                case "donors" -> new DonorView(app);
                case "donations" -> new DonationView(app);
                case "beneficiaries" -> new BeneficiaryView(app);
                case "aidrequests" -> new AidRequestView(app);
                case "distributions" -> new DistributionView(app);
                default -> new UsersView(app);
            });
            JTable t = firstTable(holder[0]);
            waitUntil(() -> t.getRowCount() == (int) e[1]);
            paint(holder[0]);
        }
        JComponent[] reports = new JComponent[1];
        onEdt(() -> reports[0] = new ReportView(app));
        waitUntil(() -> all(reports[0], JTable.class).get(0).getRowCount() == 4);
        paint(reports[0]);
    }

    @Test
    @DisplayName("dashboard shows the real balance")
    void dashboardNumbers() throws Exception {
        JComponent[] v = new JComponent[1];
        onEdt(() -> v[0] = new DashboardView(app));
        waitUntil(() -> all(v[0], StatCard.class).stream().anyMatch(c -> hasText(c, "75,000.00 DZD")));
        assertTrue(all(v[0], StatCard.class).stream().anyMatch(c -> hasText(c, "100,000.00 DZD")));
    }

    private static boolean hasText(JComponent c, String text) {
        return all(c, JLabel.class).stream().anyMatch(l -> text.equals(l.getText()));
    }

    @Test
    @DisplayName("donor screen: add a donor with the button, then it appears; invalid input shows no crash")
    void addDonorThroughUi() throws Exception {
        DonorView[] v = new DonorView[1];
        onEdt(() -> v[0] = new DonorView(app));
        JTable t = firstTable(v[0]);
        waitUntil(() -> t.getRowCount() == 3);

        List<JTextField> fields = all(v[0], JTextField.class); // [search, name, email, phone, address]
        onEdt(() -> {
            fields.get(1).setText("Nadia Test");
            fields.get(2).setText("nadia@test.org");
            button(v[0], "Add donor").doClick();
        });
        waitUntil(() -> t.getRowCount() == 4);

        onEdt(() -> { // blank name is refused by the service: row count must not change
            fields.get(1).setText("");
            button(v[0], "Add donor").doClick();
        });
        Thread.sleep(300);
        onEdt(() -> { });
        assertEquals(4, t.getRowCount());
    }

    @Test
    @DisplayName("aid requests: a new request is accepted, and REJECT works without typing an approved amount")
    void submitAndRejectRequest() throws Exception {
        AidRequestView[] v = new AidRequestView[1];
        onEdt(() -> v[0] = new AidRequestView(app));
        JTable t = firstTable(v[0]);
        waitUntil(() -> t.getRowCount() == 4);

        List<JTextField> fields = all(v[0], JTextField.class); // [search, requested, description, approved, notes]
        onEdt(() -> {
            fields.get(1).setText("1 500,50");
            fields.get(2).setText("Books, pens and notebooks");
            button(v[0], "Submit request").doClick();
        });
        waitUntil(() -> t.getRowCount() == 5);

        // select the pending sample request (#3) and reject it with an empty "approved amount"
        onEdt(() -> {
            for (int r = 0; r < t.getRowCount(); r++) {
                if (Long.valueOf(3).equals(t.getValueAt(r, 0))) {
                    t.setRowSelectionInterval(r, r);
                }
            }
            fields.get(3).setText("");
            fields.get(4).setText("not enough funds");
            button(v[0], "Reject").doClick();
        });
        waitUntil(() -> app.getRequestDAO().findById(3L).map(AidRequest::getStatus).orElse(null) == RequestStatus.REJECTED);
        assertEquals(admin.getId(), app.getRequestDAO().findById(3L).orElseThrow().getReviewedBy());
    }

    @Test
    @DisplayName("distribution screen disburses the typed amount against a chosen approved request")
    void disburseThroughUi() throws Exception {
        DistributionView[] v = new DistributionView[1];
        onEdt(() -> v[0] = new DistributionView(app));
        JTable t = firstTable(v[0]);
        waitUntil(() -> t.getRowCount() == 2 && !all(v[0], javax.swing.JComboBox.class).get(0).getModel().equals(null)
                && all(v[0], javax.swing.JComboBox.class).get(0).getItemCount() == 1);

        onEdt(() -> {
            List<JTextField> f = all(v[0], JTextField.class); // [search, amount, reference, description]
            f.get(1).setText("2500");
            button(v[0], "Disburse aid").doClick();
        });
        waitUntil(() -> t.getRowCount() == 3);
        assertEquals(0, new java.math.BigDecimal("12500")
                .compareTo(app.getRequestDAO().findById(2L).orElseThrow().getDistributedAmount()));
    }

    @Test
    @DisplayName("login screen: wrong password shows a message, correct password signs in")
    void loginScreen() throws Exception {
        app.setCurrentUser(null);
        LoginView[] v = new LoginView[1];
        onEdt(() -> v[0] = new LoginView(app));
        JTextField user = all(v[0], JTextField.class).stream()
                .filter(f -> !(f instanceof JPasswordField)).findFirst().orElseThrow();
        JPasswordField pass = all(v[0], JPasswordField.class).get(0);

        onEdt(() -> {
            user.setText("admin");
            pass.setText("wrong-password");
            button(v[0], "Sign in").doClick();
        });
        waitUntil(() -> all(v[0], JLabel.class).stream().anyMatch(l -> l.getText() != null && l.getText().contains("Invalid username or password.")));
        assertNull(app.getCurrentUser());

        onEdt(() -> {
            user.setText("  Admin ");
            pass.setText("Admin123");
            button(v[0], "Sign in").doClick();
        });
        waitUntil(() -> app.getCurrentUser() != null);
        assertEquals(Role.ADMIN, app.getCurrentUser().getRole());
    }

    @Test
    @DisplayName("empty login fields are refused before touching the database")
    void loginEmptyFields() throws Exception {
        app.setCurrentUser(null);
        LoginView[] v = new LoginView[1];
        onEdt(() -> v[0] = new LoginView(app));
        onEdt(() -> button(v[0], "Sign in").doClick());
        assertTrue(all(v[0], JLabel.class).stream().anyMatch(l -> l.getText() != null && l.getText().contains("Please enter both")));
        assertNull(app.getCurrentUser());
    }

    @Test
    @DisplayName("donor portal: a freshly registered donor sees an empty history and can donate")
    void donorPortal() throws Exception {
        User donor = app.userService.register(new NewUser("portal.donor", "Passw0rdX", "Portal Donor",
                "portal@test.org", null, Role.DONOR, null, null, null, null));
        app.setCurrentUser(donor);
        PortalView[] v = new PortalView[1];
        onEdt(() -> v[0] = new PortalView(app));
        waitUntil(() -> !all(v[0], JTable.class).isEmpty());
        JTable t = firstTable(v[0]);
        assertEquals(0, t.getRowCount());

        onEdt(() -> {
            List<JTextField> f = all(v[0], JTextField.class); // [amount, note]
            f.get(0).setText("750");
            button(v[0], "Donate").doClick();
        });
        waitUntil(() -> !all(v[0], JTable.class).isEmpty() && firstTable(v[0]).getRowCount() == 1);
        assertEquals(1, app.getDonationDAO().findByDonor(app.userService.findDonorProfile(donor).orElseThrow().getId()).size());
        paint(v[0]);
    }

    @Test
    @DisplayName("beneficiary portal: submit a request linked to the account's own profile")
    void beneficiaryPortal() throws Exception {
        User ben = app.userService.register(new NewUser("portal.ben", "Passw0rdX", "Portal Ben",
                "ben@test.org", null, Role.BENEFICIARY, "NID-555", null, null, null));
        app.setCurrentUser(ben);
        PortalView[] v = new PortalView[1];
        onEdt(() -> v[0] = new PortalView(app));
        waitUntil(() -> !all(v[0], JTable.class).isEmpty());
        onEdt(() -> {
            List<JTextField> f = all(v[0], JTextField.class); // [amount, description]
            f.get(0).setText("3000");
            f.get(1).setText("Winter heating");
            button(v[0], "Submit request").doClick();
        });
        waitUntil(() -> !all(v[0], JTable.class).isEmpty() && firstTable(v[0]).getRowCount() == 1);
        Long profileId = app.userService.findBeneficiaryProfile(ben).orElseThrow().getId();
        assertEquals(1, app.getRequestDAO().findByBeneficiary(profileId).size());
    }

    @Test
    @DisplayName("an account without a linked profile gets a clear explanation instead of a blank page")
    void portalWithoutProfile() throws Exception {
        User orphan = new User(null, "orphan", "x", Role.DONOR, "Orphan", "nobody@nowhere.org", null);
        app.setCurrentUser(orphan);
        PortalView[] v = new PortalView[1];
        onEdt(() -> v[0] = new PortalView(app));
        waitUntil(() -> all(v[0], JLabel.class).stream().anyMatch(l -> l.getText() != null && l.getText().contains("not linked")));
        paint(v[0]);
    }
}
