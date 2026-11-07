package com.charity.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JTable;
import com.charity.controller.AppController;
import com.charity.model.Donation;
import com.charity.model.Distribution;
import com.charity.model.RequestStatus;

/**
 * Administrator dashboard: key financial/operational cards and recent activity tables.
 */
public class DashboardView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final AppController app;
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("ar", "DZ"));

    private JLabel lblConfirmed, lblDistributed, lblBalance;
    private JLabel lblDonors, lblBeneficiaries, lblPending;

    public DashboardView(AppController app) {
        this.app = app;
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel title = new JLabel("Administrator Dashboard");
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.TEXT_DARK);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(title, BorderLayout.NORTH);

        add(buildCards(), BorderLayout.CENTER);
        add(buildRecentActivity(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildCards() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 12, 12));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(0, 0, 14, 0));

        lblConfirmed = valueLabel("0.00");
        lblDistributed = valueLabel("0.00");
        lblBalance = valueLabel("0.00");
        lblDonors = valueLabel("0");
        lblBeneficiaries = valueLabel("0");
        lblPending = valueLabel("0");

        panel.add(card("Total Confirmed Donations", lblConfirmed, Theme.GREEN));
        panel.add(card("Total Distributed Funds", lblDistributed, Theme.RED));
        panel.add(card("Available Balance", lblBalance, Theme.GREEN_DARK));
        panel.add(card("Total Donors", lblDonors, Theme.BLUE));
        panel.add(card("Total Beneficiaries", lblBeneficiaries, Theme.BLUE));
        panel.add(card("Pending Aid Requests", lblPending, Theme.AMBER));
        return panel;
    }

    private JLabel valueLabel(String text) {
        JLabel v = new JLabel(text);
        v.setFont(Theme.CARD_VALUE);
        v.setForeground(Theme.TEXT_DARK);
        return v;
    }

    private JPanel card(String label, JLabel value, Color accent) {
        JPanel c = new JPanel();
        c.setBackground(Theme.SURFACE);
        c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(14, 14, 14, 14));
        c.setPreferredSize(new Dimension(200, 130));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setBackground(accent);
        top.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel tag = new JLabel(label);
        tag.setFont(Theme.CARD_LABEL);
        tag.setForeground(Color.WHITE);
        top.add(tag);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottom.setBackground(Theme.SURFACE);
        bottom.setBorder(new EmptyBorder(14, 14, 14, 14));
        bottom.add(value);

        c.add(top, BorderLayout.NORTH);
        c.add(bottom, BorderLayout.CENTER);
        return c;
    }

    private JPanel buildRecentActivity() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(Theme.BG);

        JLabel title = new JLabel("Recent Activity");
        title.setFont(Theme.H2_FONT);
        title.setForeground(Theme.TEXT_DARK);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel tables = new JPanel(new GridLayout(1, 2, 12, 0));
        tables.setBackground(Theme.BG);
        List<Donation> donations = app.getDonationDAO().findAll();
        List<Distribution> distributions = app.getDistributionDAO().findAll();
        tables.add(recentTable("Recent Donations",
                new String[]{"Date", "Donor", "Amount", "Status"},
                donationRows(donations)));
        tables.add(recentTable("Recent Distributions",
                new String[]{"Date", "Request #", "Amount", "Method"},
                distributionRows(distributions)));
        panel.add(tables, BorderLayout.CENTER);
        return panel;
    }

    private JPanel recentTable(String title, String[] columns, Object[][] rows) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.SURFACE);
        panel.setBorder(Theme.CARD_BORDER);

        JLabel t = new JLabel(title);
        t.setFont(Theme.H3_FONT);
        t.setForeground(Theme.TEXT_DARK);
        t.setBorder(new EmptyBorder(12, 12, 8, 12));
        panel.add(t, BorderLayout.NORTH);

        JTable table = TableHelper.createTable(columns);
        TableHelper.setData(table, rows);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.SURFACE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private Object[][] donationRows(List<Donation> list) {
        int n = Math.min(list.size(), 5);
        Object[][] data = new Object[n][4];
        for (int i = 0; i < n; i++) {
            Donation d = list.get(i);
            data[i][0] = d.getDonationDate() != null ? d.getDonationDate().toString() : "";
            data[i][1] = "Donor #" + d.getDonorId();
            data[i][2] = formatMoney(d.getAmount());
            data[i][3] = d.getStatus();
        }
        return data;
    }

    private Object[][] distributionRows(List<Distribution> list) {
        int n = Math.min(list.size(), 5);
        Object[][] data = new Object[n][4];
        for (int i = 0; i < n; i++) {
            Distribution d = list.get(i);
            data[i][0] = d.getDistributionDate() != null ? d.getDistributionDate().toString() : "";
            data[i][1] = "Req #" + d.getAidRequestId();
            data[i][2] = formatMoney(d.getAmount());
            data[i][3] = d.getPaymentMethod();
        }
        return data;
    }

    public void refresh() {
        BigDecimal confirmed = app.financialService.getTotalConfirmedDonations();
        BigDecimal distributed = app.financialService.getTotalDistributed();
        BigDecimal balance = app.financialService.getAvailableBalance();

        lblConfirmed.setText(formatMoney(confirmed));
        lblDistributed.setText(formatMoney(distributed));
        lblBalance.setText(formatMoney(balance));
        lblBalance.setForeground(balance.signum() < 0 ? Theme.RED : Theme.GREEN_DARK);

        lblDonors.setText(String.valueOf(app.getDonorDAO().findAll().size()));
        lblBeneficiaries.setText(String.valueOf(app.getBeneficiaryDAO().findAll().size()));
        int pending = app.getRequestDAO().countByStatus(RequestStatus.PENDING);
        lblPending.setText(String.valueOf(pending));
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) amount = BigDecimal.ZERO;
        return money.format(amount);
    }
}
