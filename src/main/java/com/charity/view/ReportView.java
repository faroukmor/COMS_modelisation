package com.charity.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import com.charity.controller.AppController;
import com.charity.model.AidRequest;
import com.charity.model.Distribution;
import com.charity.model.Donation;
import com.charity.model.RequestStatus;

/**
 * Financial and operational reports with CSV export.
 */
public class ReportView extends JPanel {
    private static final long serialVersionUID = 1L;
    private final AppController app;

    private JLabel lblConfirmed, lblDistributed, lblBalance;
    private final JTable donationsTable = TableHelper.createTable(
            new String[]{"ID", "Donor #", "Amount", "Date", "Method", "Status"});
    private final JTable distributionsTable = TableHelper.createTable(
            new String[]{"ID", "Request #", "Amount", "Date", "Method"});
    private final JTable requestsTable = TableHelper.createTable(
            new String[]{"ID", "Beneficiary #", "Requested", "Approved", "Status"});

    public ReportView(AppController app) {
        this.app = app;
        setBackground(Theme.BG);
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        add(buildSummary(), BorderLayout.NORTH);
        add(buildTables(), BorderLayout.CENTER);
        add(buildExport(), BorderLayout.SOUTH);
        refresh();
    }

    private JPanel buildSummary() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 12, 12));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));
        lblConfirmed = valueLabel("0.00");
        lblDistributed = valueLabel("0.00");
        lblBalance = valueLabel("0.00");
        panel.add(card("Total Confirmed Donations", lblConfirmed, Theme.GREEN));
        panel.add(card("Total Distributed Funds", lblDistributed, Theme.RED));
        panel.add(card("Available Balance", lblBalance, Theme.GREEN_DARK));
        return panel;
    }

    private JLabel valueLabel(String text) {
        JLabel v = new JLabel(text);
        v.setFont(Theme.CARD_VALUE);
        v.setForeground(Theme.TEXT_DARK);
        return v;
    }

    private JPanel card(String label, JLabel value, Color accent) {
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(Theme.SURFACE);
        c.setBorder(new EmptyBorder(14, 14, 14, 14));
        c.setPreferredSize(new Dimension(240, 110));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setBackground(accent);
        top.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel tag = new JLabel(label);
        tag.setFont(Theme.CARD_LABEL);
        tag.setForeground(Color.WHITE);
        top.add(tag);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottom.setBackground(Theme.SURFACE);
        bottom.setBorder(new EmptyBorder(10, 12, 10, 12));
        bottom.add(value);
        c.add(top, BorderLayout.NORTH);
        c.add(bottom, BorderLayout.CENTER);
        return c;
    }

    private JPanel buildTables() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBackground(Theme.BG);
        panel.add(reportPanel("Donation History", donationsTable));
        panel.add(reportPanel("Distribution History", distributionsTable));
        panel.add(reportPanel("Aid Requests", requestsTable));
        return panel;
    }

    private JPanel reportPanel(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.SURFACE);
        panel.setBorder(Theme.CARD_BORDER);
        JLabel t = new JLabel(title);
        t.setFont(Theme.H3_FONT);
        t.setForeground(Theme.TEXT_DARK);
        t.setBorder(new EmptyBorder(10, 10, 6, 10));
        panel.add(t, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.SURFACE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildExport() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Theme.BG);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel filter = new JLabel("Filter (leave blank for all): ");
        filter.setFont(Theme.BODY_SMALL);
        filter.setForeground(Theme.TEXT_MUTED);
        JTextField txtFilter = new JTextField(12);
        JButton btnExport = button("Export to CSV", Theme.GREEN);
        btnExport.addActionListener(e -> exportCSV(txtFilter.getText().trim()));
        panel.add(filter); panel.add(txtFilter); panel.add(btnExport);
        return panel;
    }

    private JButton button(String label, Color color) {
        JButton b = new JButton(label);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(Theme.BUTTON_FONT);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        return b;
    }

    public void refresh() {
        BigDecimal confirmed = app.financialService.getTotalConfirmedDonations();
        BigDecimal distributed = app.financialService.getTotalDistributed();
        BigDecimal balance = app.financialService.getAvailableBalance();
        lblConfirmed.setText(money(confirmed));
        lblDistributed.setText(money(distributed));
        lblBalance.setText(money(balance));
        lblBalance.setForeground(balance.signum() < 0 ? Theme.RED : Theme.GREEN_DARK);

        List<Donation> donations = app.getDonationDAO().findAll();
        Object[][] d = new Object[donations.size()][6];
        for (int i = 0; i < donations.size(); i++) {
            Donation x = donations.get(i);
            d[i][0] = x.getId(); d[i][1] = x.getDonorId(); d[i][2] = money(x.getAmount());
            d[i][3] = x.getDonationDate(); d[i][4] = x.getPaymentMethod(); d[i][5] = x.getStatus();
        }
        TableHelper.setData(donationsTable, d);

        List<Distribution> dists = app.getDistributionDAO().findAll();
        Object[][] ds = new Object[dists.size()][5];
        for (int i = 0; i < dists.size(); i++) {
            Distribution x = dists.get(i);
            ds[i][0] = x.getId(); ds[i][1] = x.getAidRequestId(); ds[i][2] = money(x.getAmount());
            ds[i][3] = x.getDistributionDate(); ds[i][4] = x.getPaymentMethod();
        }
        TableHelper.setData(distributionsTable, ds);

        List<AidRequest> reqs = app.getRequestDAO().findAll();
        Object[][] r = new Object[reqs.size()][5];
        for (int i = 0; i < reqs.size(); i++) {
            AidRequest x = reqs.get(i);
            r[i][0] = x.getId(); r[i][1] = x.getBeneficiaryId(); r[i][2] = money(x.getRequestedAmount());
            r[i][3] = money(x.getApprovedAmount()); r[i][4] = x.getStatus();
        }
        TableHelper.setData(requestsTable, r);
    }

    private String money(BigDecimal v) { return v == null ? "0.00" : v.stripTrailingZeros().toString(); }

    private void exportCSV(String filter) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter w = new FileWriter(chooser.getSelectedFile())) {
                w.write("Report - Charity Fund Management System\n");
                w.write("Generated filter: " + (filter.isEmpty() ? "none" : filter) + "\n\n");
                w.write("Total Confirmed Donations," + money(app.financialService.getTotalConfirmedDonations()) + "\n");
                w.write("Total Distributed," + money(app.financialService.getTotalDistributed()) + "\n");
                w.write("Available Balance," + money(app.financialService.getAvailableBalance()) + "\n");
                w.write("\nDonations:\n");
                for (Object[] row : toRows(donationsTable)) {
                    w.write(join(row) + "\n");
                }
                w.write("\nDistributions:\n");
                for (Object[] row : toRows(distributionsTable)) {
                    w.write(join(row) + "\n");
                }
                w.write("\nAid Requests:\n");
                for (Object[] row : toRows(requestsTable)) {
                    w.write(join(row) + "\n");
                }
                JOptionPane.showMessageDialog(this, "Report exported to CSV.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Object[][] toRows(JTable table) {
        int rows = table.getRowCount(), cols = table.getColumnCount();
        Object[][] data = new Object[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                data[i][j] = table.getValueAt(i, j);
        return data;
    }

    private String join(Object[] row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(row[i] == null ? "" : row[i]);
        }
        return sb.toString();
    }
}
