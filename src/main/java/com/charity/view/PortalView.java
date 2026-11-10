package com.charity.view;

import com.charity.controller.AppController;
import com.charity.model.AidRequest;
import com.charity.model.Beneficiary;
import com.charity.model.Donation;
import com.charity.model.DonationStatus;
import com.charity.model.Donor;
import com.charity.model.PaymentMethod;
import com.charity.model.Role;
import com.charity.model.User;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Home page for donor, beneficiary and association accounts: their own donations or aid requests,
 * and a form to make a donation / submit a request. Linked to the account by e-mail address.
 */
public class PortalView extends BaseView {
    private static final long serialVersionUID = 1L;
    private final JPanel body = new JPanel(new BorderLayout(0, 16));
    private final User user;

    public PortalView(AppController app) {
        super(app);
        this.user = app.getCurrentUser();
        String who = user.getFullName() == null ? user.getUsername() : user.getFullName();
        add(UI.pageHeader("My account", "Signed in as " + who + " (" + Format.label(user.getRole()) + ")"),
                BorderLayout.NORTH);
        body.setOpaque(false);
        add(body, BorderLayout.CENTER);
        load();
    }

    private record Loaded(Optional<Donor> donor, List<Donation> donations,
                          Optional<Beneficiary> beneficiary, List<AidRequest> requests) {
    }

    private void load() {
        async(() -> {
            if (user.getRole() == Role.DONOR) {
                Optional<Donor> d = app.userService.findDonorProfile(user);
                return new Loaded(d, d.map(x -> app.getDonationDAO().findByDonor(x.getId())).orElse(List.of()),
                        Optional.empty(), List.of());
            }
            Optional<Beneficiary> b = app.userService.findBeneficiaryProfile(user);
            return new Loaded(Optional.empty(), List.of(), b,
                    b.map(x -> app.getRequestDAO().findByBeneficiary(x.getId())).orElse(List.of()));
        }, this::show);
    }

    private void show(Loaded l) {
        body.removeAll();
        if (user.getRole() == Role.DONOR) {
            if (l.donor().isPresent()) {
                body.add(donorContent(l.donor().get(), l.donations()), BorderLayout.CENTER);
            } else {
                body.add(noProfile("donor"), BorderLayout.NORTH);
            }
        } else if (l.beneficiary().isPresent()) {
            body.add(beneficiaryContent(l.beneficiary().get(), l.requests()), BorderLayout.CENTER);
        } else {
            body.add(noProfile("beneficiary"), BorderLayout.NORTH);
        }
        body.revalidate();
        body.repaint();
    }

    private JComponent noProfile(String kind) {
        UI.Card c = new UI.Card();
        c.setLayout(new BorderLayout(0, 8));
        c.add(UI.label("Your account is not linked to a " + kind + " profile yet", Theme.H2_FONT, Theme.TEXT_DARK), BorderLayout.NORTH);
        c.add(UI.label("<html>Ask an administrator to add a " + kind + " record that uses the same email address as this account ("
                + user.getEmail() + ").</html>", Theme.BODY_FONT, Theme.TEXT_MUTED), BorderLayout.CENTER);
        return c;
    }

    // ------------------------------------------------------------------ donor

    private JComponent donorContent(Donor donor, List<Donation> donations) {
        BigDecimal confirmed = donations.stream().filter(d -> d.getStatus() == DonationStatus.CONFIRMED)
                .map(Donation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        StatCard total = new StatCard("Confirmed donations", Icons.Kind.HEART, Theme.GREEN, Theme.GREEN_LIGHT);
        total.setValue(Format.money(confirmed));
        StatCard count = new StatCard("Donations made", Icons.Kind.COIN, Theme.BLUE, Theme.BLUE_LIGHT);
        count.setValue(String.valueOf(donations.size()));
        JPanel stats = new JPanel(new GridLayout(1, 2, 16, 0));
        stats.setOpaque(false);
        stats.add(total);
        stats.add(count);

        JTable table = TableHelper.createTable(new String[]{"ID", "Amount", "Date", "Method", "Status"});
        Object[][] rows = new Object[donations.size()][];
        for (int i = 0; i < rows.length; i++) {
            Donation d = donations.get(i);
            rows[i] = new Object[]{d.getId(), d.getAmount(), d.getDonationDate(), d.getPaymentMethod(), d.getStatus()};
        }
        TableHelper.setData(table, rows);

        JTextField amount = UI.styleField(new JTextField(), "0.00");
        JComboBox<PaymentMethod> method = UI.enumCombo(PaymentMethod.values());
        JTextField note = UI.styleField(new JTextField());
        UI.FormGrid g = new UI.FormGrid(1);
        g.add("Amount (DZD)", amount);
        g.add("Payment method", method);
        g.add("Note (optional)", note);
        JLabel info = UI.muted("<html>A donation stays pending until an administrator confirms it.</html>");
        JPanel formBody = new JPanel(new BorderLayout(0, 8));
        formBody.setOpaque(false);
        formBody.add(g, BorderLayout.NORTH);
        formBody.add(info, BorderLayout.CENTER);
        JButton give = UI.button("Donate", UI.Style.PRIMARY, Icons.Kind.HEART);
        give.addActionListener(e -> {
            final BigDecimal value;
            try {
                value = Format.parseAmount(amount.getText());
            } catch (IllegalArgumentException ex) {
                toastError(ex.getMessage());
                return;
            }
            async(() -> app.donationService.recordDonation(donor.getId(), value,
                    (PaymentMethod) method.getSelectedItem(), DonationStatus.PENDING, note.getText(), null), id -> {
                toast("Thank you! Your donation was recorded and awaits confirmation.");
                load();
            });
        });
        return layout(stats, TableHelper.tableCard("My donations", table, null),
                formCard(cardTitle("Make a donation"), formBody, UI.buttonRow(give)));
    }

    // ------------------------------------------------------------ beneficiary

    private JComponent beneficiaryContent(Beneficiary profile, List<AidRequest> requests) {
        BigDecimal approved = BigDecimal.ZERO;
        BigDecimal received = BigDecimal.ZERO;
        for (AidRequest r : requests) {
            approved = approved.add(r.getApprovedAmount() == null ? BigDecimal.ZERO : r.getApprovedAmount());
            received = received.add(r.getDistributedAmount() == null ? BigDecimal.ZERO : r.getDistributedAmount());
        }
        StatCard c1 = new StatCard("Requests sent", Icons.Kind.FILE, Theme.BLUE, Theme.BLUE_LIGHT);
        c1.setValue(String.valueOf(requests.size()));
        StatCard c2 = new StatCard("Approved", Icons.Kind.CHECK, Theme.GREEN, Theme.GREEN_LIGHT);
        c2.setValue(Format.money(approved));
        StatCard c3 = new StatCard("Received", Icons.Kind.WALLET, Theme.GREEN_DARK, Theme.GREEN_LIGHT);
        c3.setValue(Format.money(received));
        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.add(c1);
        stats.add(c2);
        stats.add(c3);

        JTable table = TableHelper.createTable(new String[]{"ID", "Requested", "Approved", "Received", "Status", "Description"});
        Object[][] rows = new Object[requests.size()][];
        for (int i = 0; i < rows.length; i++) {
            AidRequest r = requests.get(i);
            rows[i] = new Object[]{r.getId(), r.getRequestedAmount(), r.getApprovedAmount(),
                    r.getDistributedAmount(), r.getStatus(), r.getDescription()};
        }
        TableHelper.setData(table, rows);

        JTextField amount = UI.styleField(new JTextField(), "0.00");
        JTextField description = UI.styleField(new JTextField(), "What do you need help with?");
        UI.FormGrid g = new UI.FormGrid(1);
        g.add("Requested amount (DZD)", amount);
        g.add("Description", description);
        JButton send = UI.button("Submit request", UI.Style.PRIMARY, Icons.Kind.SEND);
        send.addActionListener(e -> {
            final BigDecimal value;
            try {
                value = Format.parseAmount(amount.getText());
            } catch (IllegalArgumentException ex) {
                toastError(ex.getMessage());
                return;
            }
            async(() -> app.requestService.submitRequest(profile.getId(), value, description.getText()), id -> {
                toast("Request #" + id + " submitted. We will review it soon.");
                load();
            });
        });
        return layout(stats, TableHelper.tableCard("My aid requests", table, null),
                formCard(cardTitle("New aid request"), g, UI.buttonRow(send)));
    }

    private JComponent layout(JComponent stats, JComponent list, JComponent form) {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.add(stats, BorderLayout.NORTH);
        p.add(masterDetail(list, form, 390), BorderLayout.CENTER);
        return p;
    }
}
